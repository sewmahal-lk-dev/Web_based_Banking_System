CREATE TABLE notification (
  notification_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NULL,
  employee_id INT NULL,
  notification_type VARCHAR(40) NOT NULL,
  title VARCHAR(150) NOT NULL,
  message VARCHAR(500) NOT NULL,
  related_ticket_id INT NULL,
  is_read BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at TIMESTAMP NULL,
  CONSTRAINT notification_recipient CHECK ((customer_id IS NOT NULL AND employee_id IS NULL) OR (customer_id IS NULL AND employee_id IS NOT NULL)),
  CONSTRAINT notification_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE RESTRICT,
  CONSTRAINT notification_employee FOREIGN KEY (employee_id) REFERENCES employee(employee_id) ON DELETE RESTRICT,
  CONSTRAINT notification_ticket FOREIGN KEY (related_ticket_id) REFERENCES ticket(ticket_id) ON DELETE SET NULL,
  INDEX notification_customer_unread (customer_id,is_read,notification_id),
  INDEX notification_employee_unread (employee_id,is_read,notification_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
