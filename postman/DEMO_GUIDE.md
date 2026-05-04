# Embedded Payments Demo Guide - Postman Collection

## Overview
This guide explains how to use the **embedded-payments-demo.postman_collection.json** to demonstrate Sprint 1 deliverables (HU 1.2-1.6) for the Embedded Payments Platform.

---

## Setup Instructions

### 1. Import the Collection into Postman

1. Open **Postman** (Download from [postman.com](https://www.postman.com/downloads/) if needed)
2. Click **Import** (top-left)
3. Select **Upload Files** tab
4. Choose `embedded-payments-demo.postman_collection.json`
5. Click **Import**

### 2. Create/Configure Environment

The collection uses environment variables. Postman will auto-create them, but configure these:

**Variables to set:**
- `baseURL`: `http://localhost:8085` (local) or `https://embedded-payments-1.onrender.com` (production)
- `jwt_token`: Auto-populated after running "Generate Admin JWT Token"
- `merchant_jwt_token`: Auto-populated after running "Generate Merchant JWT Token"
- `merchant_id`: Auto-populated from login response

**To set manually:**
1. Click **Environments** (top-left sidebar)
2. Create new or edit existing environment
3. Set `baseURL` to your backend address
4. Save

---

## Demo Execution Order

Follow this sequence for a smooth demo video:

### Phase 1: Authentication Setup (1 min)

1. **Run:** `Setup & Authentication` → `Generate Admin JWT Token`
   - ✅ Validates admin can log in
   - Stores JWT token for admin operations
   - Response: 200 OK with admin token

2. **Run:** `Setup & Authentication` → `Generate Merchant JWT Token`
   - ✅ Validates merchant can log in
   - Stores JWT token for merchant operations
   - Response: 200 OK with merchant token

**What to show in demo:**
- Both authentication requests return 200 OK
- JWT tokens are generated successfully
- Tokens are stored in environment variables (click eye icon)

---

### Phase 2: Merchant Profile Setup (2 min)

3. **Run:** `HU 1.2 - Update Merchant Contact`
   - ✅ **HU 1.2 Requirement**: Update contact information
   - Request: PUT `/api/v1/merchants/{id}/contact`
   - Body: New contact name and email
   - Response: 200 OK with updated merchant details

**What to show in demo:**
```
"contact_name": "Juan Esteban Mosquera",
"contact_email": "juan.mosquera@embedded-payments.com"
```
- Show request headers include JWT token
- Show response confirms update
- Highlight security: Only authenticated users can update

---

4. **Run:** `HU 1.3 - Register Bank Account`
   - ✅ **HU 1.3 Requirement**: Register encrypted bank account data
   - Request: PUT `/api/v1/merchants/{id}/bank-account`
   - Body: Valid IBAN + Routing Number
   - Response: 200 OK with merchant details

**IBAN Reference (Spanish format):**
```
"iban": "ES9121000418450200051332"
"routing_number": "021000021"
"account_holder_name": "Empresa Payments S.L."
```

**What to show in demo:**
- IBAN validation passes (ISO 7064 checksum)
- Routing number validation passes (9-digit USA format)
- Response indicates successful registration
- Highlight security: Bank data is encrypted with AES-256-GCM
- Show merchant status is ACTIVE

---

### Phase 3: Admin Operations (2 min)

5. **Run:** `HU 1.4 - Activate Merchant`
   - ✅ **HU 1.4 Requirement**: Admin activates merchant
   - Request: PATCH `/api/v1/merchants/{id}/activate`
   - Authorization: Admin JWT token only
   - Response: 200 OK with status ACTIVE

**What to show in demo:**
- Only admin (ROLE_ADMIN) can activate merchants
- State transition logged: INACTIVE → ACTIVE
- Response: `message: "Merchant activated successfully"`
- Highlight: Non-admin requests receive 403 Forbidden

---

6. **Run:** `HU 1.5 - Deactivate Merchant`
   - ✅ **HU 1.5 Requirement**: Admin deactivates merchant
   - Request: PATCH `/api/v1/merchants/{id}/deactivate`
   - Authorization: Admin JWT token only
   - Response: 200 OK with status INACTIVE

**What to show in demo:**
- Admin performs state transition: ACTIVE → INACTIVE
- Blocks new payment transactions automatically
- Response: `message: "Merchant deactivated successfully"`
- Highlight: Audit trail records reason and timestamp

---

### Phase 4: Data Protection & Security (1 min)

7. **Run:** `HU 1.6 - Get Merchant Details`
   - ✅ **HU 1.6 Requirement**: Query merchant with data masking
   - Request: GET `/api/v1/merchants/{id}`
   - Response: 200 OK with protected sensitive data

**What to show in demo:**
- Admin sees complete unmasked data:
  ```
  "contact_email": "juan@example.com",
  "bank_account_data": "[ENCRYPTED - Access granted]"
  ```
- Merchant (non-owner) sees masked data:
  ```
  "contact_email": "***@example.com",
  "bank_account_data": "[RESTRICTED]"
  ```
- Highlight OWASP compliance: Data masking protects PII
- Show response includes all required fields

---

## Optional: Error Scenarios Testing

For comprehensive demo (add 1-2 min):

8. **Run:** `Error Scenarios - Optional Testing` → `Invalid IBAN Format`
   - Shows validation error handling
   - Response: 400/422 Bad Request

9. **Run:** `Error Scenarios - Optional Testing` → `Missing JWT Token`
   - Shows authentication requirement
   - Response: 401 Unauthorized

10. **Run:** `Error Scenarios - Optional Testing` → `Unauthorized Admin Operation`
    - Shows authorization enforcement
    - Response: 403 Forbidden (merchant cannot activate)

---

## Expected Results Summary

| HU | Endpoint | Method | Status | Feature |
|---|---|---|---|---|
| 1.2 | `/merchants/{id}/contact` | PUT | ✅ 200 | Update merchant contact info |
| 1.3 | `/merchants/{id}/bank-account` | PUT | ✅ 200 | Register encrypted bank data |
| 1.4 | `/merchants/{id}/activate` | PATCH | ✅ 200 | Admin activate merchant |
| 1.5 | `/merchants/{id}/deactivate` | PATCH | ✅ 200 | Admin deactivate merchant |
| 1.6 | `/merchants/{id}` | GET | ✅ 200 | Query with data masking |

---

## Security Features Demonstrated

✅ **JWT Authentication** - All endpoints require valid JWT token  
✅ **Role-Based Authorization** - Admin operations restricted to ROLE_ADMIN  
✅ **Data Encryption** - Bank account data encrypted with AES-256-GCM  
✅ **Data Masking** - PII masked for non-owners (OWASP compliance)  
✅ **State Management** - Valid state transitions enforced  
✅ **Audit Trail** - All operations logged with reason, user, timestamp  

---

## Backend Requirements

**Ensure backend is running with:**
```bash
cd /Users/juanestebanmosquera/embedded-payments
./mvnw spring-boot:run
```

Or use Docker:
```bash
docker build -t embedded-payments .
docker run -p 8085:8085 embedded-payments
```

**Production deployment:**
```
https://embedded-payments-1.onrender.com
```

---

## Test Data Reference

### Admin Account
```
email: admin@embedded-payments.com
password: admin@123
role: ROLE_ADMIN
```

### Merchant Account
```
email: merchant@example.com
password: merchant@123
role: ROLE_MERCHANT
```

### Valid Bank Data (HU 1.3)
```
IBAN: ES9121000418450200051332 (Spanish IBAN with valid checksum)
Routing: 021000021 (USA routing number with valid checksum)
Holder: Empresa Payments S.L.
```

---

## Video Recording Tips

1. **Set resolution**: 1920x1080 or 1280x720
2. **Record Postman**: Show request → Response → Test results
3. **Pace**: ~5-7 minutes total for all 5 endpoints
4. **Focus on**:
   - Request headers (highlight Authorization: Bearer token)
   - Response status codes (200 OK, 403 Forbidden, etc.)
   - Response body highlighting HU completion
   - Test results passing (green checkmarks)
5. **Audio narration** (optional):
   - Explain each HU requirement
   - Show how JWT authentication works
   - Demonstrate data masking for security
   - Highlight audit trail capabilities

---

## Troubleshooting

### Issue: 401 Unauthorized
**Solution**: Run authentication requests first to generate tokens

### Issue: 400 Bad Request on Bank Account
**Solution**: Verify IBAN and Routing Number format:
- IBAN: ES + 2 digits + alphanumeric (24 total)
- Routing: Exactly 9 digits

### Issue: 403 Forbidden on Activate/Deactivate
**Solution**: Use admin JWT token, not merchant token

### Issue: Connection refused
**Solution**: Start backend on port 8085:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8085"
```

---

## Collection Statistics

- **Total Requests**: 10
  - Setup: 2
  - HU Implementations: 5
  - Error Scenarios: 3
- **Built-in Tests**: 15 test cases validating responses
- **Authentication**: JWT Bearer tokens for all endpoints
- **Coverage**: 100% of HU 1.2-1.6 requirements

---

## Next Steps

After demo video:
1. Upload video to team repository
2. Include collection JSON in sprint deliverables
3. Share guide with stakeholders
4. Update production baseURL when deploying to Render
5. Add additional error scenarios for comprehensive regression testing

---

**Created**: May 2024  
**Version**: 1.0  
**Scope**: Embedded Payments Platform - Sprint 1  
**HU Coverage**: 1.2, 1.3, 1.4, 1.5, 1.6  
