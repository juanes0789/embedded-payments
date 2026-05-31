# 🚀 Local Testing & Deployment Guide

## Complete Test Setup Instructions

### Prerequisites ✅
- Java 21+
- Node.js 18+
- PostgreSQL 12+ (or local setup with Docker)
- Maven (included via mvnw)
- npm (included with Node.js)

---

## Step 1: Setup PostgreSQL Database

### Option A: Local PostgreSQL
```bash
# macOS with Homebrew
brew install postgresql@15
brew services start postgresql@15

# Create database
createdb embeddedpayments

# Create user (if needed)
psql -d embeddedpayments -c "CREATE USER dev WITH PASSWORD 'dev';"
```

### Option B: Using Docker
```bash
docker run --name postgres-ep -e POSTGRES_DB=embeddedpayments \
  -e POSTGRES_PASSWORD=dev -p 5432:5432 -d postgres:15
```

### Option C: Use Neon Cloud (Already configured)
- Frontend/Backend can connect directly to Neon
- Connection string in environment variables

---

## Step 2: Start Backend

```bash
cd /Users/juanestebanmosquera/embedded-payments

# Set environment variables
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/embeddedpayments"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="postgres"
export JWT_SECRET="your-secret-key-change-this-change-this"
export PAYMENT_PROCESSOR="mock"

# Run backend
./mvnw spring-boot:run

# Expected output:
# [main] ... Embedded Payments Application started in X seconds
# [main] Started EmbeddedPaymentsApplication in X seconds
# Tomcat started on port(s): 8085 (http) with context path ''
```

**Verify Backend is Running:**
```bash
curl http://localhost:8085/swagger-ui.html
# Should return HTML page (Swagger UI)
```

---

## Step 3: Start Frontend

```bash
cd frontend/payment-gateway-ui

# Install dependencies (first time only)
npm install

# Start dev server
npm run dev

# Expected output:
# VITE v4.5.14  ready in 222 ms
# ➜  Local:   http://localhost:5173/
# ➜  Network: use --host to expose
# ➜  press h to show help
```

**Verify Frontend is Running:**
```bash
open http://localhost:5173
# Should show Embedded Payments UI
```

---

## Step 4: Create Test Merchant

### Option A: Via Backend API

```bash
# Register merchant
curl -X POST http://localhost:8085/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "merchant@test.com",
    "password": "password123"
  }'

# Login to get JWT token
RESPONSE=$(curl -X POST http://localhost:8085/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "merchant@test.com",
    "password": "password123"
  }')

TOKEN=$(echo $RESPONSE | jq -r '.token')
MERCHANT_ID=$(echo $RESPONSE | jq -r '.merchantId')

echo "Token: $TOKEN"
echo "Merchant ID: $MERCHANT_ID"
```

### Option B: Use Test Data (Pre-loaded)
- Email: `test@example.com`
- Password: (check db/003_insert_test_data.sql)
- Merchant ID: `550e8400-e29b-41d4-a716-446655440000`

---

## Step 5: Get Merchant API Key

### Option A: Get from Test Data
```bash
# Already created in test data

# Test with:
MERCHANT_ID="550e8400-e29b-41d4-a716-446655440000"

# Get API keys
sqlite3 embeddedpayments.db "SELECT * FROM api_keys WHERE merchant_id = '$MERCHANT_ID';"
```

### Option B: Generate New API Key
```bash
# Via dashboard (future feature) or backend admin endpoint
# For now, use test fixture

API_KEY="test-api-key-123"
```

---

## Step 6: Create Payment Intent

```bash
# Create payment order for $99.99 USD
INTENT_RESPONSE=$(curl -X POST http://localhost:8085/api/v1/payments/intents \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 99.99,
    "currency": "USD"
  }')

INTENT_ID=$(echo $INTENT_RESPONSE | jq -r '.id')
echo "Payment Intent Created: $INTENT_ID"
```

---

## Step 7: Test Checkout Flow

### Open Frontend with Payment Link
```bash
# Open in browser:
open "http://localhost:5173/pay/$INTENT_ID"

# Or manually:
# 1. Visit http://localhost:5173
# 2. Append: /pay/{INTENT_ID}
```

### Fill and Submit Payment Form
```
1. Form auto-loads with:
   - Amount: $99.99
   - Currency: USD
   - Status: CREATED

2. Enter customer information:
   - Cardholder name: John Doe
   - Card number: 4242 4242 4242 4242
   - Expiry: 12/25 (any future date)
   - CVC: 123
   - Email: customer@test.com

3. Click "Pay $99.99"

4. Expected result:
   - Loading state for 1-2 seconds
   - Success page with receipt
   - Transaction ID: (shown on receipt)
   - Processor Reference: mock_xxxxx
   - Status: SUCCEEDED (90% chance)
```

### Observe Mock Payment Outcomes
```
✅ 90% Success:
   - Status: SUCCEEDED
   - Transaction created
   - Green confirmation page

❌ 10% Failure:
   - Status: FAILED
   - Error message shown
   - Allow retry

Expected Behavior:
   - Multiple attempts: ~9 succeed, ~1 fails
```

---

## Step 8: View Transaction in Dashboard

### Login to Merchant Dashboard
```bash
# URL
http://localhost:5173/login

# Credentials (from Step 4):
- Email: merchant@test.com
- Password: password123

# Or use test account:
- Email: test@example.com
- Password: (see test data)
```

### Navigate to Transactions
```
1. Dashboard → Transactions
2. Should see table with:
   - Transaction ID (UUID)
   - Payment Intent (UUID)
   - Amount: $99.99 USD
   - Status: SUCCEEDED or FAILED
   - Date: Today's date

3. Click "View" to see details
4. Details page shows:
   - Amount, currency, status
   - Payment intent ID
   - Created/updated dates
   - Option to refund if SUCCEEDED
```

---

## Step 9: Test Error Cases

### Test Error 404 - Intent Not Found
```bash
# Open with invalid intent ID
open "http://localhost:5173/pay/invalid-uuid-1234-5678"

# Expected: "Payment intent not found" error
```

### Test Error 409 - Duplicate Transaction
```bash
# Load same intent, submit twice quickly
# Expected: "Transaction already in progress" error
```

### Test Form Validation
```bash
1. Empty form and click Pay
   → "Cardholder name is required"

2. Wrong card format
   → "Card number must have 16 digits"

3. Invalid email
   → "Enter a valid email address"

4. Wrong CVC format
   → "CVC must contain 3 or 4 digits"
```

---

## Step 10: Verify Database Records

### Check Transactions Table
```bash
# Using psql
psql -d embeddedpayments -c "
SELECT id, payment_intent_id, amount, currency, status, created_at 
FROM transactions 
ORDER BY created_at DESC 
LIMIT 10;
"

# Expected output:
# id | payment_intent_id | amount | currency | status | created_at
# ---|---|---|---|---|---
# xxx | yyy | 99.99 | USD | SUCCEEDED | 2026-05-30 16:10:05
```

### Check Customers Table
```bash
psql -d embeddedpayments -c "
SELECT id, merchant_id, email, name, created_at 
FROM customers 
WHERE email = 'customer@test.com';
"

# Expected: Customer was auto-created on first payment
```

### Check Payment Intents
```bash
psql -d embeddedpayments -c "
SELECT id, merchant_id, customer_id, amount, currency, status 
FROM payment_intents 
ORDER BY created_at DESC 
LIMIT 5;
"

# Expected: Status changed from CREATED → PROCESSING → SUCCEEDED/FAILED
```

---

## Step 11: Run Automated Test Script

```bash
# From project root
./scripts/test_checkout_flow.sh

# Expected output:
# [TEST 1] GET /checkout/intents/{id} ✓
# [TEST 2] POST /checkout/submit ✓
# [TEST 3] GET /api/v1/transactions ✓
# [TEST 4] GET /checkout/intents/{id} - invalid ✓
# [TEST 5] Error handling ✓
# All tests PASSED ✓
```

---

## Troubleshooting

### Issue: Backend won't start
```
❌ Error: "Connection refused" on port 8085

Solution:
1. Check if port 8085 is in use: lsof -i :8085
2. Kill existing process: kill -9 <PID>
3. Try again: ./mvnw spring-boot:run
```

### Issue: "Cannot connect to database"
```
❌ Error: "FATAL: role 'postgres' does not exist"

Solution:
1. Check PostgreSQL is running: pg_isready
2. Verify credentials in connection string
3. Ensure user exists: createuser postgres
4. Run migrations: psql -d embeddedpayments -f db/001_create_schema.sql
```

### Issue: Frontend shows blank page
```
❌ Error: "Cannot GET /pay/..."

Solution:
1. Clear browser cache: Cmd+Shift+Delete
2. Check frontend is running: http://localhost:5173
3. Verify intent ID format (should be UUID)
4. Check browser console for errors
```

### Issue: Payment fails with network error
```
❌ Error: "Failed to load payment intent"

Solution:
1. Check backend is running on :8085
2. Verify CORS configuration
3. Check browser console for error details
4. Check backend logs for exceptions
5. Verify intent ID exists in database
```

### Issue: "X-API-Key not found" on transactions endpoint
```
❌ Error: "UNAUTHORIZED - Missing or invalid API key"

Solution:
1. Check API key is set in browser settings → profile → API Key
2. Verify it's in localStorage: Open DevTools → Application → localStorage
3. Check it's sent in request headers: Network tab → request header tabs
4. Ensure URL contains /api/v1/transactions for auto-injection
```

---

## Monitoring & Debugging

### View Backend Logs
```bash
# Real-time logs while running
./mvnw spring-boot:run

# Key log lines to look for:
# "Received request: GET /checkout/intents/{id}"
# "Processing payment for intent: {uuid}"
# "MockPaymentProcessor returned: SUCCEEDED"
# "Transaction created: {uuid}"
```

### View Frontend Console
```javascript
// Open DevTools: Cmd+Option+I
// Console tab shows:
// ✓ getPaymentIntent() response
// ✓ submitCheckoutPayment() response
// ✓ Any errors or warnings

// Network tab shows:
// ✓ GET /checkout/intents/{id} → 200
// ✓ POST /checkout/submit → 201
// ✓ GET /api/v1/transactions → 200
// ✓ X-API-Key header presence
```

### Check Database State
```bash
# Quick health check:
psql -d embeddedpayments -c "
SELECT 
  (SELECT COUNT(*) FROM merchants) as merchants,
  (SELECT COUNT(*) FROM customers) as customers,
  (SELECT COUNT(*) FROM payment_intents) as intents,
  (SELECT COUNT(*) FROM transactions) as transactions
;"

# Expected growth:
# merchants | customers | intents | transactions
# ---|---|---|---
# 1 | 1 | 1 | 1 (after 1 payment)
# 1 | 2 | 2 | 2 (after 2 payments from different customers)
```

---

## Performance Testing

### Measure Checkout Load Time
```javascript
// In browser console while on /pay/{intentId}
performance.mark('start');
// (page loads)
performance.mark('end');
performance.measure('load', 'start', 'end');
console.log(performance.getEntriesByType('measure')[0].duration, 'ms');

// Expected: <500ms for full page load
```

### Measure Payment Processing Time
```javascript
// In browser console before clicking pay
console.time('payment');
// (click pay, see success)
console.timeEnd('payment');

// Expected: 1-2 seconds
```

### Monitor Database Connections
```bash
# Check active connections
psql -d embeddedpayments -c "
SELECT datname, usename, count(*) FROM pg_stat_activity 
GROUP BY datname, usename;
"

# Expected: 1-2 connections from Spring Boot
```

---

## Load Testing (Optional)

### Simple Load Test with Apache Bench
```bash
# Install ab (macOS)
brew install httpd

# Test public endpoint (100 requests, 10 concurrent)
ab -n 100 -c 10 http://localhost:8085/checkout/intents/550e8400-e29b-41d4-a716-446655440030

# Expected:
# Requests per second: >100 (for mock processor)
# Failed requests: 0
```

---

## Deployment Checklist

Before going to production:

- [ ] Backend compiles without errors
- [ ] Frontend builds successfully  
- [ ] All tests pass locally
- [ ] Database migrations applied
- [ ] API keys configured
- [ ] Environment variables set
- [ ] CORS configured for production domain
- [ ] Security headers configured
- [ ] Rate limiting enabled
- [ ] Logging configured
- [ ] Monitoring alerts set
- [ ] Backup strategy in place
- [ ] Rollback plan documented
- [ ] Team trained on deployment
- [ ] Runbook documented

---

## Post-Deployment Verification

### Production Smoke Test
```bash
# 1. Verify API is accessible
curl https://api.youromain.com/checkout/intents/test-id

# 2. Verify frontend is accessible
curl https://checkout.yourdomain.com

# 3. Verify database connection
# (check application logs)

# 4. Verify payment flow works
# Open checkout, complete payment, verify in database

# 5. Check transaction appears in merchant dashboard
```

---

## Support & Help

### Quick Reference
- **Backend URL**: http://localhost:8085
- **Frontend URL**: http://localhost:5173
- **Database**: embeddedpayments (PostgreSQL)
- **API Docs**: http://localhost:8085/swagger-ui.html

### Documentation
- Full API Spec: `docs/CHECKOUT_FLOW.md`
- Frontend Guide: `docs/FRONTEND_INTEGRATION.md`
- Architecture: `docs/IMPLEMENTATION_SUMMARY.md`
- Quick Start: `docs/QUICK_START.md`

### Teams / Support
- Backend Issues: See logs in `/src`
- Frontend Issues: Check browser DevTools
- Database Issues: Check PostgreSQL connection
- General Questions: Review `docs/SUMMARY.md`

---

**Ready to test! 🧪 Let me know if you hit any issues.** 🚀

