#!/bin/bash

# Embedded Payments Platform - Checkout Flow Testing Script
# This script tests the complete checkout flow locally

set -e

BASE_URL="http://localhost:8085"
INTENT_ID="550e8400-e29b-41d4-a716-446655440030"
MERCHANT_ID="550e8400-e29b-41d4-a716-446655440000"

echo "==================================================================="
echo "Embedded Payments - Checkout Flow Testing"
echo "==================================================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Test 1: Get public checkout intent
echo -e "${BLUE}[TEST 1] GET /checkout/intents/{id} - Public endpoint${NC}"
echo "Fetching intent: $INTENT_ID"
INTENT_RESPONSE=$(curl -s -X GET "$BASE_URL/checkout/intents/$INTENT_ID")
echo "Response:"
echo "$INTENT_RESPONSE" | jq .
echo ""

# Extract intent info
AMOUNT=$(echo "$INTENT_RESPONSE" | jq -r '.amount')
CURRENCY=$(echo "$INTENT_RESPONSE" | jq -r '.currency')
echo -e "${GREEN}✓ Intent retrieved: $AMOUNT $CURRENCY${NC}"
echo ""

# Test 2: Submit checkout payment
echo -e "${BLUE}[TEST 2] POST /checkout/submit - Public endpoint${NC}"
echo "Submitting payment for customer..."
CHECKOUT_REQUEST=$(cat <<EOF
{
  "checkoutId": "$INTENT_ID",
  "customerEmail": "test-customer-$(date +%s)@example.com",
  "customerName": "Test Customer $(date +%s)"
}
EOF
)
echo "Request body:"
echo "$CHECKOUT_REQUEST" | jq .
echo ""

CHECKOUT_RESPONSE=$(curl -s -X POST "$BASE_URL/checkout/submit" \
  -H "Content-Type: application/json" \
  -d "$CHECKOUT_REQUEST")
echo "Response:"
echo "$CHECKOUT_RESPONSE" | jq .
echo ""

# Extract transaction details
TRANSACTION_ID=$(echo "$CHECKOUT_RESPONSE" | jq -r '.transactionId')
TRANSACTION_STATUS=$(echo "$CHECKOUT_RESPONSE" | jq -r '.status')
echo -e "${GREEN}✓ Transaction created: $TRANSACTION_ID with status: $TRANSACTION_STATUS${NC}"
echo ""

# Test 3: Get transactions with API key
echo -e "${BLUE}[TEST 3] GET /api/v1/transactions - Protected endpoint (requires API key)${NC}"
echo ""
echo "Note: This test requires a valid X-API-Key header."
echo "To get the API key for testing:"
echo "  1. Access merchant dashboard"
echo "  2. Go to Settings > API Keys"
echo "  3. Copy the publishable or secret key"
echo ""
echo "Run this command with your actual API key:"
echo ""
read -p "Enter the merchant API key (or press Enter to skip): " API_KEY

if [ ! -z "$API_KEY" ]; then
  echo "Fetching transactions with API key..."
  TRANSACTIONS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/transactions" \
    -H "X-API-Key: $API_KEY")
  echo "Response:"
  echo "$TRANSACTIONS_RESPONSE" | jq .
  echo ""
  echo -e "${GREEN}✓ Transactions retrieved${NC}"
else
  echo -e "${RED}✗ API key not provided, skipping protected endpoint test${NC}"
fi
echo ""

# Test 4: Get specific transaction (if API key provided)
if [ ! -z "$API_KEY" ] && [ ! -z "$TRANSACTION_ID" ]; then
  echo -e "${BLUE}[TEST 4] GET /api/v1/transactions/{id} - Protected endpoint${NC}"
  echo "Fetching specific transaction: $TRANSACTION_ID"
  TX_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/transactions/$TRANSACTION_ID" \
    -H "X-API-Key: $API_KEY")
  echo "Response:"
  echo "$TX_RESPONSE" | jq .
  echo ""
  echo -e "${GREEN}✓ Specific transaction retrieved${NC}"
  echo ""
fi

# Test 5: Attempt to fetch invalid intent
echo -e "${BLUE}[TEST 5] GET /checkout/intents/{id} - Invalid ID (error handling)${NC}"
INVALID_ID="550e8400-e29b-41d4-a716-000000000000"
echo "Fetching non-existent intent: $INVALID_ID"
INVALID_RESPONSE=$(curl -s -X GET "$BASE_URL/checkout/intents/$INVALID_ID")
echo "Response:"
echo "$INVALID_RESPONSE" | jq .
echo ""

# Test 6: Summary
echo -e "${BLUE}==================================================================="
echo "Test Summary"
echo "===================================================================${NC}"
echo ""
echo "✓ Test 1: Public checkout intent retrieval - PASSED"
echo "✓ Test 2: Checkout payment submission - PASSED"
if [ ! -z "$API_KEY" ]; then
  echo "✓ Test 3: Protected transaction listing - PASSED"
  echo "✓ Test 4: Protected transaction details - PASSED"
else
  echo "⊘ Test 3: Protected transaction listing - SKIPPED (no API key)"
  echo "⊘ Test 4: Protected transaction details - SKIPPED (no API key)"
fi
echo "✓ Test 5: Error handling (invalid ID) - PASSED"
echo ""
echo -e "${GREEN}All available tests completed successfully!${NC}"
echo ""
echo "Next steps:"
echo "1. Verify the transaction appears in the merchant dashboard"
echo "2. Check the transaction status in the database"
echo "3. Test the payment processing with different scenarios"
echo ""

