-- Embedded Payments Platform - Migration for Transaction Audit and Rejections (HU 4.1 & HU 4.2)
-- Script idempotente para agregar soporte de motivos de rechazo e historial de estados.

BEGIN;

-- 1. Agregar columna reason_code a la tabla transactions si no existe
ALTER TABLE transactions 
ADD COLUMN IF NOT EXISTS reason_code VARCHAR(255);

-- 2. Crear tabla de historial de estados de transacciones para la bitácora inmutable (Timeline)
CREATE TABLE IF NOT EXISTS transaction_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    previous_status transaction_status,
    new_status transaction_status NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    reason_code VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Índices para optimizar la consulta del historial ordenado
CREATE INDEX IF NOT EXISTS idx_transaction_status_history_transaction ON transaction_status_history(transaction_id);
CREATE INDEX IF NOT EXISTS idx_transaction_status_history_created ON transaction_status_history(created_at);

COMMIT;
