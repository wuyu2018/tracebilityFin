-- ============================================
-- Migration: Remove company_id columns
-- Date: 2026-05-24
-- Reason: Company-based data isolation replaced
--         by agent-type-based access control
-- ============================================

-- WARNING: This operation drops columns and is IRREVERSIBLE.
-- Ensure you have a backup before running.

-- ALTER TABLE product DROP COLUMN IF EXISTS company_id;
-- ALTER TABLE material DROP COLUMN IF EXISTS company_id;
-- ALTER TABLE production_batch DROP COLUMN IF EXISTS company_id;
-- ALTER TABLE storage DROP COLUMN IF EXISTS company_id;
-- ALTER TABLE transport_sale DROP COLUMN IF EXISTS company_id;
-- ALTER TABLE inspection DROP COLUMN IF EXISTS company_id;
-- ALTER TABLE material_purchase DROP COLUMN IF EXISTS company_id;
-- ALTER TABLE security_code DROP COLUMN IF EXISTS company_id;
-- ALTER TABLE admin DROP COLUMN IF EXISTS company_id;

ALTER TABLE product DROP COLUMN company_id;
ALTER TABLE material DROP COLUMN company_id;
ALTER TABLE production_batch DROP COLUMN company_id;
ALTER TABLE storage DROP COLUMN company_id;
ALTER TABLE transport_sale DROP COLUMN company_id;
ALTER TABLE inspection DROP COLUMN company_id;
ALTER TABLE material_purchase DROP COLUMN company_id;
ALTER TABLE security_code DROP COLUMN company_id;
ALTER TABLE admin DROP COLUMN company_id;
-- Note: admin.agent_type column is preserved and now serves
-- as the primary means of module-level access control.
-- Role values: SUPER_ADMIN (all access) or ADMIN (limited by agent_type)
-- Agent types: PRODUCTION, CIRCULATION, SALES
