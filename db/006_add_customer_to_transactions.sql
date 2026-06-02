-- Add customer_id field to transactions table
-- This creates a direct relationship between transactions and customers
-- instead of relying only on the indirect path via payment_intents

ALTER TABLE "transactions"
ADD COLUMN "customer_id" uuid,
ADD CONSTRAINT "transactions_customer_id_fkey"
  FOREIGN KEY ("customer_id") REFERENCES "customers"("id") ON DELETE SET NULL;

-- Create index for efficient lookups
CREATE INDEX "idx_transactions_customer" ON "transactions" ("customer_id");

-- Optional: update existing transactions to link customer via payment_intent
-- This assumes all payment_intents with customer_id should have transactions linked
UPDATE "transactions" t
SET "customer_id" = pi."customer_id"
FROM "payment_intents" pi
WHERE t."payment_intent_id" = pi."id"
  AND pi."customer_id" IS NOT NULL
  AND t."customer_id" IS NULL;

