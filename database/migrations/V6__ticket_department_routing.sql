-- V5 has messages and employee assignment, but no department queue or ASSIGNED state.
ALTER TABLE ticket
  ADD COLUMN assigned_role ENUM('CUSTOMER_SERVICE_OFFICER','LOAN_OFFICER','CARD_SERVICES_OFFICER','INVESTMENT_OFFICER','COMPLIANCE_RISK_OFFICER') NULL,
  MODIFY COLUMN status ENUM('OPEN','IN_PROGRESS','ESCALATED','RESOLVED','CLOSED','ASSIGNED') DEFAULT 'OPEN',
  ADD INDEX ticket_department_status (assigned_role,status);

-- Retain existing officer ownership and ticket timestamps.
UPDATE ticket t JOIN employee e ON e.employee_id=t.assigned_employee_id
SET t.assigned_role=e.role,t.date_updated=t.date_updated
WHERE t.assigned_role IS NULL AND e.role IN ('CUSTOMER_SERVICE_OFFICER','LOAN_OFFICER','CARD_SERVICES_OFFICER','INVESTMENT_OFFICER','COMPLIANCE_RISK_OFFICER');

UPDATE ticket SET status='ASSIGNED',date_updated=date_updated
WHERE status='OPEN' AND assigned_role IS NOT NULL;
