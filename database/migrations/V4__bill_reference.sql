-- Apply once through Apply-Migration.ps1 -Version 4. Existing payments retain NULL.
ALTER TABLE payment ADD COLUMN bill_reference VARCHAR(100) NULL;
