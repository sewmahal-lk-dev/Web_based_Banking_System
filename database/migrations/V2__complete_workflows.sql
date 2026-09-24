-- Apply once after taking a schema + data backup. All existing enum values are retained.
ALTER TABLE loan
MODIFY status ENUM(
    'PENDING',
    'APPROVED',
    'REJECTED',
    'ACTIVE',
    'CLOSED',
    'CANCELLED'
) DEFAULT 'PENDING',
ADD COLUMN account_number BIGINT NULL,
ADD CONSTRAINT fk_loan_account FOREIGN KEY (account_number) REFERENCES account (account_number);

ALTER TABLE investment
MODIFY status ENUM(
    'ACTIVE',
    'MATURED',
    'CLOSED',
    'PENDING',
    'CANCELLED',
    'REJECTED'
) DEFAULT 'PENDING',
ADD COLUMN account_number BIGINT NULL,
ADD COLUMN term_months INT NULL,
ADD COLUMN payout_amount DECIMAL(15, 2) NULL,
ADD CONSTRAINT fk_investment_account FOREIGN KEY (account_number) REFERENCES account (account_number);

ALTER TABLE payment
MODIFY payment_type ENUM(
    'BILL_PAYMENT',
    'TRANSFER',
    'LOAN_PAYMENT',
    'CARD_PAYMENT',
    'LOAN_DISBURSEMENT',
    'INVESTMENT_FUNDING',
    'INVESTMENT_PAYOUT'
) NOT NULL;

ALTER TABLE card ADD COLUMN decision_note VARCHAR(500) NULL;

ALTER TABLE service_request
MODIFY status ENUM(
    'PENDING',
    'PROCESSING',
    'APPROVED',
    'REJECTED',
    'COMPLETED',
    'CANCELLED'
) DEFAULT 'PENDING',
ADD COLUMN account_number BIGINT NULL,
ADD COLUMN requested_account_type ENUM('SAVINGS', 'CURRENT') NULL,
ADD COLUMN requested_email VARCHAR(150) NULL,
ADD COLUMN response VARCHAR(500) NULL,
ADD CONSTRAINT fk_request_account FOREIGN KEY (account_number) REFERENCES account (account_number);