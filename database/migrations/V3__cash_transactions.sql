-- LankaTrust Banking System
-- V3: Cash Deposit and Cash Withdrawal support
-- Apply only after V2__complete_workflows.sql.
-- Existing records are preserved.

ALTER TABLE payment
MODIFY payment_type ENUM(
    'BILL_PAYMENT',
    'TRANSFER',
    'LOAN_PAYMENT',
    'CARD_PAYMENT',
    'LOAN_DISBURSEMENT',
    'INVESTMENT_FUNDING',
    'INVESTMENT_PAYOUT',
    'CASH_DEPOSIT',
    'CASH_WITHDRAWAL'
) NOT NULL;