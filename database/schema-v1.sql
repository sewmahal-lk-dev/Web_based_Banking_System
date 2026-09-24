-- Restore into an empty database. No DROP/TRUNCATE statements.
SET FOREIGN_KEY_CHECKS=0;
CREATE TABLE `account` (
  `account_number` bigint NOT NULL,
  `customer_id` int NOT NULL,
  `account_type` enum('SAVINGS','CURRENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `balance` decimal(15,2) NOT NULL DEFAULT '0.00',
  `status` enum('ACTIVE','INACTIVE','CLOSED','FROZEN') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
  `open_date` date NOT NULL,
  PRIMARY KEY (`account_number`),
  KEY `customer_id` (`customer_id`),
  CONSTRAINT `account_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `audit_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` int DEFAULT NULL,
  `action` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `details` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `action_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `employee_id` (`employee_id`),
  CONSTRAINT `audit_log_ibfk_1` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `banking_product` (
  `product_id` int NOT NULL AUTO_INCREMENT,
  `product_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `card` (
  `card_id` int NOT NULL AUTO_INCREMENT,
  `account_number` bigint NOT NULL,
  `card_number` varchar(19) COLLATE utf8mb4_unicode_ci NOT NULL,
  `card_type` enum('CREDIT','DEBIT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `issue_date` date NOT NULL,
  `expiry_date` date NOT NULL,
  `status` enum('PENDING','ACTIVE','BLOCKED','EXPIRED','CANCELLED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `daily_limit` decimal(12,2) DEFAULT '100000.00',
  PRIMARY KEY (`card_id`),
  UNIQUE KEY `card_number` (`card_number`),
  KEY `account_number` (`account_number`),
  CONSTRAINT `card_ibfk_1` FOREIGN KEY (`account_number`) REFERENCES `account` (`account_number`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `credit_card` (
  `card_id` int NOT NULL,
  `credit_limit` decimal(12,2) NOT NULL DEFAULT '100000.00',
  `available_credit` decimal(12,2) NOT NULL DEFAULT '100000.00',
  PRIMARY KEY (`card_id`),
  CONSTRAINT `credit_card_ibfk_1` FOREIGN KEY (`card_id`) REFERENCES `card` (`card_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `customer` (
  `customer_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `postal_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','INACTIVE','BLOCKED') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `debit_card` (
  `card_id` int NOT NULL,
  `daily_withdrawal_limit` decimal(12,2) DEFAULT '50000.00',
  PRIMARY KEY (`card_id`),
  CONSTRAINT `debit_card_ibfk_1` FOREIGN KEY (`card_id`) REFERENCES `card` (`card_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `employee` (
  `employee_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('CUSTOMER_SERVICE_OFFICER','LOAN_OFFICER','CARD_SERVICES_OFFICER','INVESTMENT_OFFICER','COMPLIANCE_RISK_OFFICER','SYSTEM_ADMIN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `investment` (
  `investment_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `employee_id` int DEFAULT NULL,
  `investment_type` enum('FIXED_DEPOSIT','SAVINGS_PLAN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `interest_rate` decimal(5,2) DEFAULT NULL,
  `start_date` date NOT NULL,
  `maturity_date` date DEFAULT NULL,
  `status` enum('ACTIVE','MATURED','CLOSED','PENDING') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  PRIMARY KEY (`investment_id`),
  KEY `customer_id` (`customer_id`),
  KEY `employee_id` (`employee_id`),
  CONSTRAINT `investment_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `investment_ibfk_2` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `loan` (
  `loan_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `employee_id` int DEFAULT NULL,
  `loan_type` enum('STUDENT','PERSONAL','VEHICLE','HOME','BUSINESS') COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `interest_rate` decimal(5,2) NOT NULL,
  `term_months` int NOT NULL,
  `application_date` date NOT NULL,
  `status` enum('PENDING','APPROVED','REJECTED','ACTIVE','CLOSED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `rejection_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`loan_id`),
  KEY `customer_id` (`customer_id`),
  KEY `employee_id` (`employee_id`),
  CONSTRAINT `loan_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `loan_ibfk_2` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `loan_repayment` (
  `repayment_id` int NOT NULL AUTO_INCREMENT,
  `loan_id` int NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `due_date` date NOT NULL,
  `paid_date` date DEFAULT NULL,
  `status` enum('PENDING','PAID','OVERDUE') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  PRIMARY KEY (`repayment_id`),
  KEY `loan_id` (`loan_id`),
  CONSTRAINT `loan_repayment_ibfk_1` FOREIGN KEY (`loan_id`) REFERENCES `loan` (`loan_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `payment` (
  `payment_id` bigint NOT NULL AUTO_INCREMENT,
  `account_number` bigint NOT NULL,
  `payment_type` enum('BILL_PAYMENT','TRANSFER','LOAN_PAYMENT','CARD_PAYMENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `recipient` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `amount` decimal(15,2) NOT NULL,
  `payment_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `scheduled_date` datetime DEFAULT NULL,
  `status` enum('PENDING','COMPLETED','FAILED','CANCELLED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `reference_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `reference_number` (`reference_number`),
  KEY `account_number` (`account_number`),
  CONSTRAINT `payment_ibfk_1` FOREIGN KEY (`account_number`) REFERENCES `account` (`account_number`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `service_request` (
  `request_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `request_type` enum('ACCOUNT_OPENING','ACCOUNT_CLOSURE','PROFILE_UPDATE','CHEQUE_BOOK','ACCOUNT_STATEMENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('PENDING','PROCESSING','APPROVED','REJECTED','COMPLETED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `rejection_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_submitted` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`request_id`),
  KEY `customer_id` (`customer_id`),
  CONSTRAINT `service_request_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `ticket` (
  `ticket_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `assigned_employee_id` int DEFAULT NULL,
  `ticket_type` enum('INQUIRY','COMPLAINT','TECHNICAL','OTHER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `subject` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `priority` enum('LOW','MEDIUM','HIGH','URGENT') COLLATE utf8mb4_unicode_ci DEFAULT 'MEDIUM',
  `status` enum('OPEN','IN_PROGRESS','ESCALATED','RESOLVED','CLOSED') COLLATE utf8mb4_unicode_ci DEFAULT 'OPEN',
  `response` text COLLATE utf8mb4_unicode_ci,
  `date_created` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `date_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ticket_id`),
  KEY `customer_id` (`customer_id`),
  KEY `assigned_employee_id` (`assigned_employee_id`),
  CONSTRAINT `ticket_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `ticket_ibfk_2` FOREIGN KEY (`assigned_employee_id`) REFERENCES `employee` (`employee_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS ticket_message (
  message_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  ticket_id INT NOT NULL,
  sender_label VARCHAR(100) NOT NULL,
  body TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ticket_message_order (ticket_id, created_at, message_id),
  CONSTRAINT ticket_message_ticket FOREIGN KEY (ticket_id) REFERENCES ticket(ticket_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS=1;
