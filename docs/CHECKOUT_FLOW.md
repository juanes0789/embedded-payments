# Embedded Checkout & Payment Flow Documentation

## Overview

This document describes the public embedded checkout flow and the protected transaction management endpoints for the Embedded Payments Platform.

## Endpoints Implemented

### 1. GET /checkout/intents/{id}
**Visibility**: Public (no authentication required)

Retrieves public information about a payment intent for display in the checkout form.

#### Request
```
GET /checkout/intents/{id}
```

#### Path Parameters
- `id` (UUID): Payment intent ID

#### Response (200 OK)
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440030",
  "amount": 100.00,
  "currency": "USD",
  "status": "CREATED",
  "description": "Test payment for development",
  "createdAt": "2026-05-30T16:10:00Z",
  "updatedAt": "2026-05-30T16:10:00Z"
}
```

#### Response (404 Not Found)
```json
{
  "error": "PAYMENT_INTENT_NOT_FOUND",
  "message": "Payment intent not found",
  "details": ["intentId: 550e8400-e29b-41d4-a716-446655440030"]
}
```

#### Response (410 Gone)
```json
{
  "error": "PAYMENT_INTENT_INVALID",
  "message": "This payment intent is no longer available",
  "details": ["status: CANCELED"]
}
```

#### Notes
- Public endpoint; no X-API-Key or JWT token required
- Returns only non-sensitive information (no merchant bank details)
- Returns 410 Gone if the intent is CANCELED or FAILED
- Merchant information is not included in the response

---

### 2. POST /checkout/submit
**Visibility**: Public (no authentication required)

Submits customer payment information and processes the transaction against a payment intent.

#### Request
```
POST /checkout/submit
Content-Type: application/json
```

#### Request Body
```json
{
  "checkoutId": "550e8400-e29b-41d4-a716-446655440030",
  "customerEmail": "customer@example.com",
  "customerName": "John Customer"
}
```

#### Response (201 Created)
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440040",
  "status": "SUCCEEDED",
  "processorReference": "mock_abc12345"
}
```

#### Response (409 Conflict)
```json
{
  "error": "TRANSACTION_ALREADY_EXISTS",
  "message": "A transaction is already being processed for this payment intent",
  "details": ["transactionId: 550e8400-e29b-41d4-a716-446655440041"]
}
```

#### Response (422 Unprocessable Entity)
```json
{
  "error": "PAYMENT_INTENT_NOT_PROCESSABLE",
  "message": "Payment intent cannot be processed in its current state",
  "details": ["paymentIntentId: 550e8400-e29b-41d4-a716-446655440030", "status: SUCCEEDED"]
}
```

#### Flow
1. Customer provides email, name, and payment data
2. System verifies the payment intent exists and is in CREATED/PROCESSING state
3. System creates or retrieves the customer record
4. System calls the payment processor (mock for MVP, real gateway later)
5. Based on processor result, transaction created with SUCCEEDED or FAILED status
6. Response includes transaction ID and status for UI confirmation

#### Notes
- Public endpoint; no authentication required
- Customer record is created automatically if it doesn't exist (associated with merchant)
- Payment processing uses mock processor in MVP (simulates ~90% success rate)
- Idempotent on transaction level: re-submitting creates new transaction if previous is terminal

---

### 3. GET /api/v1/transactions
**Visibility**: Protected (requires X-API-Key header)

Lists all transactions for the authenticated merchant. Requires X-API-Key authentication.

#### Request
```
GET /api/v1/transactions?page=1&pageSize=10
Headers:
  X-API-Key: epk_merchantapikey
```

#### Query Parameters
- `page` (int, default=1): Page number (1-indexed)
- `pageSize` (int, default=10): Number of results per page (1-100)

#### Response (200 OK)
```json
{
  "items": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440040",
      "merchantId": "550e8400-e29b-41d4-a716-446655440000",
      "paymentIntentId": "550e8400-e29b-41d4-a716-446655440030",
      "amount": 100.00,
      "currency": "USD",
      "status": "SUCCEEDED",
      "createdAt": "2026-05-30T16:10:00Z",
      "updatedAt": "2026-05-30T16:10:05Z"
    }
  ],
  "total": 1,
  "page": 1,
  "pageSize": 10
}
```

#### Response (401 Unauthorized)
```json
{
  "error": "UNAUTHORIZED",
  "message": "Missing or invalid API key",
  "details": []
}
```

#### Notes
- Requires valid X-API-Key header
- Returns only transactions belonging to the authenticated merchant (authorized via API key)
- Supports pagination for large result sets
- Default sort order: most recent transactions first (can be extended)

---

## Database Schema

### payment_intents table
```sql
CREATE TABLE payment_intents (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    customer_id UUID REFERENCES customers(id),
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(255) DEFAULT 'CREATED',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
```

### transactions table
```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    payment_intent_id UUID NOT NULL REFERENCES payment_intents(id),
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3),
    status VARCHAR(255) DEFAULT 'PENDING',
    payment_method VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
```

### customers table
```sql
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    email VARCHAR(255),
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Payment Processing Flow

### State Transitions

**Payment Intent States**:
- CREATED → PROCESSING → SUCCEEDED (on successful transaction)
- CREATED → PROCESSING → FAILED (on failed transaction)
- CREATED → CANCELED (by merchant admin)

**Transaction States**:
- PENDING → SUCCEEDED (payment processor approved)
- PENDING → FAILED (payment processor rejected)
- SUCCEEDED → REFUNDED (via refund API)
- FAILED/CANCELED → (terminal, no further transitions)

### Mock Payment Processor (MVP)

The mock processor simulates payment processing with:
- **Success Rate**: ~90% successful payments
- **Response Time**: Immediate (no delay simulation)
- **Processor Reference**: Generated as `mock_<random8chars>`

To enable real payment processor integration:
1. Implement `PaymentProcessor` interface
2. Create new implementation (e.g., StripePaymentProcessor)
3. Update Spring `@ConditionalOnProperty` configuration
4. Configure secrets (API keys) in application properties

---

## Integration Guide

### Creating a Payment Intent (Merchant API)

The merchant creates a payment intent using their API key. This endpoint is protected and implementation-specific. Example:

```bash
curl -X POST http://localhost:8085/api/v1/payments/intents \
  -H "X-API-Key: epk_merchantkey" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "USD",
    "description": "Order #12345"
  }'
```

### Customer Checkout Flow

1. **Merchant** creates a payment intent → receives intent ID
2. **Merchant** generates checkout link: `https://checkout.example.com/pay/550e8400-e29b-41d4-a716-446655440030`
3. **Customer** visits checkout link
4. **Frontend** calls GET `/checkout/intents/{id}` to display payment form
5. **Customer** enters email, name, payment details
6. **Frontend** calls POST `/checkout/submit` with customer info
7. **Backend** processes payment via mock/real processor
8. **Frontend** receives transaction ID and status
9. **Frontend** shows success/failure message
10. **Merchant Dashboard** polls GET `/api/v1/transactions` to see transaction updates

### Example Frontend Implementation

```typescript
// services/checkout.ts
export async function getCheckoutIntent(intentId: string) {
  const response = await fetch(`/checkout/intents/${intentId}`);
  if (!response.ok) throw new Error('Intent not found');
  return response.json();
}

export async function submitCheckout(
  intentId: string,
  email: string,
  name: string
) {
  const response = await fetch('/checkout/submit', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      checkoutId: intentId,
      customerEmail: email,
      customerName: name
    })
  });
  if (!response.ok) throw new Error('Payment failed');
  return response.json(); // { transactionId, status, processorReference }
}
```

---

## Error Handling

### Common Errors

| Error Code | HTTP Status | Description |
|-----------|------------|-------------|
| `PAYMENT_INTENT_NOT_FOUND` | 404 | Intent ID does not exist |
| `PAYMENT_INTENT_INVALID` | 410 | Intent is CANCELED or FAILED |
| `PAYMENT_INTENT_NOT_PROCESSABLE` | 422 | Intent status does not allow processing |
| `TRANSACTION_ALREADY_EXISTS` | 409 | Transaction exists and is not terminal |
| `CUSTOMER_NOT_FOUND` | 404 | Customer lookup failed |
| `UNAUTHORIZED` | 401 | Missing or invalid X-API-Key |
| `PAYMENT_PROCESSING_ERROR` | 500 | Error during payment processing |

### Retry Strategy

- **Transient Errors** (5xx, timeouts): Retry with exponential backoff (max 3 attempts)
- **Client Errors** (4xx except 429): Do not retry; fix request
- **Rate Limiting** (429): Retry after delay specified in `Retry-After` header

---

## Testing Locally

### 1. Start Backend
```bash
cd embedded-payments
./mvnw spring-boot:run
```

### 2. Create Test Data
Already in `db/003_insert_test_data.sql`:
- Merchant: test@example.com (ID: 550e8400-e29b-41d4-a716-446655440000)
- Payment intent: USD 100 (ID: 550e8400-e29b-41d4-a716-446655440030)

### 3. Test Endpoints

**Get checkout intent:**
```bash
curl http://localhost:8085/checkout/intents/550e8400-e29b-41d4-a716-446655440030
```

**Submit payment:**
```bash
curl -X POST http://localhost:8085/checkout/submit \
  -H "Content-Type: application/json" \
  -d '{
    "checkoutId": "550e8400-e29b-41d4-a716-446655440030",
    "customerEmail": "customer@test.com",
    "customerName": "Test Customer"
  }'
```

**List transactions (with API key):**
```bash
# First, get merchant API key from DB:
# SELECT key_hash FROM api_keys WHERE merchant_id = '550e8400-e29b-41d4-a716-446655440000';

curl -H "X-API-Key: <ACTUAL_API_KEY>" \
  http://localhost:8085/api/v1/transactions
```

---

## Future Enhancements

- [ ] Add idempotency key support for `/checkout/submit`
- [ ] Integrate real payment gateways (Stripe, Adyen, etc.)
- [ ] Add webhook support for async payment updates
- [ ] Add rate limiting to public endpoints
- [ ] Add transaction filtering by date/status
- [ ] Add customer portal for payment history
- [ ] Add reconciliation reports for merchants

