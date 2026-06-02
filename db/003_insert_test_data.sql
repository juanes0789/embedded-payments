-- Test data for development
-- This file inserts test merchants and sample data for local development

BEGIN;

-- Insert test merchant #1 - Test Company
-- Contact: John Doe, test@example.com
-- Bank account: IBAN-style (encrypted at application level)
INSERT INTO merchants (
    id,
    name,
    email,
    contact_name,
    contact_email,
    status,
    bank_account_encrypted,
    bank_account_hash,
    created_at,
    updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    'Test Company',
    'test@example.com',
    'John Doe',
    'john@example.com',
    'ACTIVE',
    'ENCRYPTED_BANK_DATA_HERE',
    'BANK_HASH_HERE',
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Insert test merchant #2 - Another Test Store
INSERT INTO merchants (
    id,
    name,
    email,
    contact_name,
    contact_email,
    status,
    created_at,
    updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440100'::uuid,
    'Another Test Store',
    'store@example.com',
    'Jane Smith',
    'jane@example.com',
    'ACTIVE',
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Insert test customer for merchant 1
INSERT INTO customers (
    id,
    merchant_id,
    email,
    name,
    created_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440010'::uuid,
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    'customer@example.com',
    'Test Customer',
    NOW()
) ON CONFLICT DO NOTHING;

-- Insert test payment intent
INSERT INTO payment_intents (
    id,
    merchant_id,
    customer_id,
    amount,
    currency,
    status,
    description,
    created_at,
    updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440030'::uuid,
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    NULL,
    100.00,
    'USD',
    'CREATED',
    'Test payment for development',
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

COMMIT;
