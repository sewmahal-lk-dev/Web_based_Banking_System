Task 2: database-backed notifications ? implemented and verified

V7 was required because the live database had no notification table. Only V7 was applied, successfully, after creating database/backups/migration-20260917-133353. V1?V6 were not modified or reapplied. No production records were reset, reseeded, or dropped. V7 creates a new empty table; historical ticket events are not backfilled.

Modified files (relative to project root):

- src/main/java/com/banking/controller/AccessFilter.java
- src/main/java/com/banking/dao/TicketDAO.java
- src/main/java/com/banking/dao/CustomerServicesDAO.java
- src/main/webapp/customer/dashboard.jsp
- src/main/webapp/customer/accounts.jsp
- src/main/webapp/customer/transfer.jsp
- src/main/webapp/customer/payments.jsp
- src/main/webapp/WEB-INF/customer/services.jsp
- src/main/webapp/WEB-INF/employee/dashboard.jsp
- src/main/webapp/WEB-INF/fragments/support-tickets.jsp
- tools/Apply-Migration.ps1
- tools/ApplyMigration.java
- tools/SupportWorkflowTest.java
- tools/EndToEndTest.java

New implementation and test files:

- database/migrations/V7__notifications.sql
- src/main/java/com/banking/dao/NotificationDAO.java
- src/main/java/com/banking/controller/NotificationServlet.java
- src/main/webapp/WEB-INF/notifications.jsp
- src/main/webapp/WEB-INF/fragments/notification-bell.jsp
- src/main/webapp/WEB-INF/fragments/notification-items.jsp
- src/main/webapp/assets/css/notifications.css
- src/main/webapp/assets/js/notifications.js
- tools/NotificationWorkflowTest.java
- tools/notification-browser.mjs

Table: notification

| Column | Type / purpose |
| --- | --- |
| notification_id | BIGINT auto-increment primary key |
| customer_id | Nullable INT foreign key to customer |
| employee_id | Nullable INT foreign key to employee |
| notification_type | VARCHAR(40) |
| title | VARCHAR(150) |
| message | VARCHAR(500), generic ticket event text |
| related_ticket_id | Nullable INT foreign key to ticket; SET NULL on deletion |
| is_read | BOOLEAN, default false |
| created_at | TIMESTAMP, current timestamp by default |
| read_at | Nullable TIMESTAMP |

A database CHECK requires exactly one customer/employee recipient. Indexes cover recipient, read state, and notification ID. Customer/employee foreign keys use DELETE RESTRICT.

Customer notifications: assignment/reassignment, staff reply, actual status change including escalation, and resolution. No notification is generated for the customer's own ticket creation/reply. An unchanged status does not generate another notification.

Staff notifications: new ticket to active Customer Service officers; assignment/reassignment to active staff in the receiving department; customer replies to the assigned department (or Customer Service if unassigned); escalation to other active colleagues in the ticket's department. Acting employees are excluded. V6's assigned_employee_id is a last-handler field; department ownership remains role-wide. NotificationDAO also supports a specific employee recipient for a future explicit individual assignment.

Ticket changes, messages, notifications, and audit records share the existing database transaction. Injected notification failures rolled back assignment and reply writes. Notification text excludes customer names, ticket descriptions, reply bodies, account information, and passwords.

The SVG bell appears in customer headers (dashboard, accounts, transfer, payments, and the shared customer services page) and the shared employee dashboard header used by all six staff roles. Its badge counts unread notifications only and disappears at zero. The previous Support Center active-ticket indicator is still labeled as active tickets but no longer uses a bell icon.

The dropdown shows eight recent notifications, timestamps, distinct read/unread states, Mark all as read, and View all notifications. Native details/summary supports keyboard activation; Escape closes and returns focus, and outside clicks close it. The paginated full list shows 25 items per page. A scoped white popup, subtle gold/burgundy details, reduced-motion support, and mobile layout do not change the rest of the site theme.

Read/open and mark-all operations use POST with existing CSRF enforcement. Customer/employee identity comes from the authenticated, active session. Every list, count, and write is scoped to that identity. Invalid or foreign notification IDs return 404; CSRF errors return 403. Output is HTML-escaped and SQL uses prepared parameters.

Links are constructed by the server, never taken from request/database URLs. Customer links go to /customer/tickets?ticket=ID#ticket-ID. Staff links go to /employee/dashboard?ticket=ID#support-ticket-ID, opening the existing ticket details. Opening marks the notification read; repeated opening is idempotent. Current ticket ownership/department access is rechecked. Deleted or reassigned-away tickets fall back to the recipient's notification list. Closed tickets remain readable through the existing authorized history.

Verification completed:

- mvn clean package: BUILD SUCCESS.
- 284 support/notification checks passed. This suite includes password login, V5/V6 routing, all customer routes, all six staff dashboards, administration, bill reference, audit search, notification ownership, CSRF, forged recipients/redirects, invalid IDs, inactive employees, correct unread counts, mark-one/all, unchanged-status deduplication, atomic rollback, escaped content, recipient CHECK constraints, and bell count after a rejected form.
- 196 additional banking CRUD checks passed using the existing EndToEndTest workflows: transfers/payments, scheduling, card operations, loans and repayment, investments, service requests, account closure, administration, and profiles. The older support test now follows V6's required IN_PROGRESS step before resolution.
- 34 isolated financial/security, 34 cash deposit/withdrawal, and 26 investment safety checks passed (94 total).
- 14 real headless Chrome checks passed. The browser created an ATM ticket through the customer form, assigned it through Customer Service, opened the Card officer bell, clicked the assignment notification, processed/replied from the ticket, opened the customer bell, clicked the reply, verified the decreased unread count, marked all read, resolved the ticket, and saw the resolution notification. Outside click, Enter, Escape/focus restoration, and a 390px mobile popup were checked.
- Actual Tomcat/Jasper rendering passed for customer pages, Customer Service, Loan, Card, Investment, Compliance, System Admin, and notification pages. Both customer and staff bells/dropdowns were rendered and exercised.

The main support-suite count includes two aggregate assertions for the separate CRUD/browser suites; the suite counts above should not be treated as a count of unique assertions by simply adding them.

All workflow/browser fixtures used connection-local MySQL TEMPORARY tables shadowing the real tables. Financial/cash/investment safety tests used isolated JDBC doubles. No test reset/reseeded the production database. Temporary table twins only work around MySQL's restriction on repeated references to a temporary table. Expected injected-error logs are part of rollback testing, not unresolved failures.

Evidence:

- verification/notifications-build.log
- verification/notifications-tests.log
- verification/notifications-financial.log
- verification/notifications-cash.log
- verification/notifications-investment.log
- verification/notifications-migration.log
- verification/notification-desktop.png
- verification/notification-mobile.png

The rebuilt artifact is target/WebBasedBankingSystem.war. It has NOT been redeployed to the running server. V7 is applied; deployment of this WAR is the remaining operational step before the existing running application shows the feature. No known failing checks remain. Updates appear on reload; WebSockets/push and the Task 3 redesign were not introduced.
