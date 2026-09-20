CREATE TABLE IF NOT EXISTS ticket_message (
  message_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  ticket_id INT NOT NULL,
  sender_label VARCHAR(100) NOT NULL,
  body TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ticket_message_order (ticket_id, created_at, message_id),
  CONSTRAINT ticket_message_ticket FOREIGN KEY (ticket_id) REFERENCES ticket(ticket_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Preserve the latest legacy response. Previously overwritten responses cannot be recovered.
INSERT INTO ticket_message(ticket_id,sender_label,body,created_at)
SELECT t.ticket_id,'Support Officer',t.response,COALESCE(t.date_updated,CURRENT_TIMESTAMP)
FROM ticket t
WHERE t.response IS NOT NULL AND TRIM(t.response)<>''
AND NOT EXISTS (SELECT 1 FROM ticket_message m WHERE m.ticket_id=t.ticket_id);
