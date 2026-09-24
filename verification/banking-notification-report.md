# Banking notification extension — 24 September 2026

Implemented and deployed using the existing `notification` table, `NotificationDAO`, notification servlet, JSPs, bell, badge, and read/unread controls. No UI, route, authentication, schema, or existing support-ticket behavior was redesigned.

## Changed application files

All paths below are under `src/main/java/com/banking/dao/`.

| File | Change |
| --- | --- |
| `NotificationDAO.java` | Central banking event helpers; database-derived ownership; active-role queues; payment-event duplicate protection; safe navigation to existing sections. Existing support-ticket helpers remain intact. |
| `LoanDAO.java` | Officer application notices; customer approval, rejection, cancellation, and activation notices. |
| `LoanRepaymentDAO.java` | Successful installment payment notice and loan-closed notice after the final installment. |
| `InvestmentDAO.java` | Officer submission notices; customer rejection, funding/activation, cancellation, and payout/closure notices. |
| `CardDAO.java` | Debit/credit request queues and customer approval, rejection, block, unblock, and cancellation/closure notices. Ordinary limit edits remain silent. |
| `PaymentDAO.java` | Successful bill-payment notice inside the payment transaction. |
| `TransferDAO.java` | Sender completion and receiving-account owner's receipt notices inside the transfer transaction. |
| `ScheduledPaymentDAO.java` | Successful execution and cancellation notices. |
| `CashTransactionDAO.java` | Cash deposit/withdrawal notices for the account owner. |
| `FinancialLedger.java` | Notices for recorded loan disbursement and investment funding/payout transactions. |
| `ServiceRequestDAO.java` | Customer Service submission queue; processing, rejection, completion, and cancellation notices. Identical repeated processing updates remain silent. |
| `AccountWorkflowDAO.java` | Account opening/activation, closure, and customer-profile deactivation notices. |
| `CustomerDAO.java` | Account activation notice following successful registration/account creation. |
| `AdminDAO.java` | Administrator notices for actual employee role/status changes; customer reactivation notices. Ordinary employee/product CRUD remains silent. |
| `EmployeeDAO.java` | Compliance freeze/reactivation notices; account-freeze administrator alerts; employee access changes; legacy decision entry points. |
| `CustomerServicesDAO.java` | Atomic notifications for legacy card-block and service-request entry points. Existing support-ticket methods are unchanged. |

Additional authored files:

- `tools/BankingNotificationTest.java`: isolated MySQL integration and real-session HTTP tests.
- `verification/banking-notification-report.md`: this implementation and manual verification guide.

Generated evidence:

- `verification/banking-notification-build.log`: Maven output.
- `verification/banking-notification-tests.log`: 180 passing assertions.
- `verification/banking-notification-tests.json`: machine-readable pass result.
- `verification/banking-notification-test-errors.log`: embedded Tomcat lifecycle output.
- `verification/banking-notification-changes.diff`: source review diff.
- `verification/banking-notification-deployment.json`: exact deployed class entries, archive hash, and smoke-check result.
- `verification/banking-notification-originals/`: pre-edit DAO snapshots.
- `verification/notification-tools/`: compiled integration test.
- `target/WebBasedBankingSystem.war`: standard Maven package.
- `target/WebBasedBankingSystem-notifications.war`: local deployment package preserving all previously deployed non-task entries.
- `deployment-backups/before-banking-notifications-20260924-015626.war`: previous deployed WAR.

## SQL / database changes

**None to the application schema or existing banking data.** No migration is required. New banking notifications have a null `related_ticket_id`; support-ticket relationships retain their existing semantics.

Integration tests copied table definitions only into a uniquely named disposable `banking_test_notifications_<timestamp>` database, used synthetic records, and dropped that database in cleanup. No production banking records were used as fixtures.

## Delivery and duplicate behavior

- Notifications are inserted after successful business writes on the same connection and commit atomically with those writes. Any later error rolls back both.
- New applications/requests notify all active employees with the matching existing role. No arbitrary employee or customer IDs are used in application code.
- Customers are resolved from the saved loan/investment/request or through card/payment/account ownership.
- GET requests never create notifications. Existing POST/redirect/GET behavior is retained.
- Existing status guards prevent repeated decisions, disbursements, payouts, cancellations, closures, and scheduled executions from producing additional notices.
- Repeated identical service-processing responses and unchanged employee access edits are silent. A genuine later block/unblock cycle still notifies.
- Payment notification delivery locks the saved payment and checks the recipient/type/message containing its payment ID and reference. Replaying delivery for that same record, including concurrently, is idempotent even after it has been read.
- This does not change banking-operation idempotency: a fresh successful transfer/payment submission creates a new financial record under the existing banking logic and therefore receives its own notice. No financial transaction is suppressed by comparing amount or recipient.
- Loan/investment state changes and their separate ledger postings can each produce a notice: one describes product status, the other the recorded money movement.

## Existing workflow limits

- `payment.status` supports `FAILED`, but no existing payment DAO persists failed attempts or a rejected-payment state. Those attempts roll back and generate no notice. The shared helper supports a saved `FAILED` state if an actual workflow is added later; no new failure workflow was invented.
- Accounts currently open directly as `ACTIVE`. There is no account approval/rejection operation. Rejected account-opening service requests use the request rejection notification.
- Card rejection uses the existing `CANCELLED` database state, with a message explicitly explaining rejection. There is no automated expiration transition in the existing application to hook.
- Investment approval funds the investment and sets `ACTIVE`; maturity/withdrawal pays out and sets `CLOSED`. No artificial `APPROVED` or `COMPLETED` investment state was introduced.
- Ordinary record edits, product CRUD, report views, and page views are intentionally silent.

## Validation and deployment

- `mvn clean package`: **BUILD SUCCESS**. Maven has no in-tree tests; the separate integration suite was compiled and run explicitly.
- `BankingNotificationTest`: **180 checks passed** against real MySQL and embedded Tomcat using actual password-authenticated sessions.
- Covered applications, decisions, both card types, cash/bill/transfer/scheduled payments, loan repayment, investment maturity, every service-request category, account changes, staff access changes, registration, and legacy entry points.
- Covered unrelated-customer denial, inactive-officer exclusion, cross-role route denial, forged recipient fields, read/unread, badge counts, escaped text, repeated GETs/decisions, concurrent payment delivery, and rollback after synthetic notification insertion failure.
- Existing ticket creation, department assignment, customer/staff replies, status changes, resolution, ownership, and ticket navigation passed regression checks.
- Local Tomcat deployed 18 changed/new DAO class entries. Every other archive entry, including all CSS/JSP/JS, was preserved exactly from the previous running deployment.
- Expanded deployed class hashes matched the staged package. Login returned HTTP 200; anonymous customer/employee notification access redirected to login. Tomcat logged successful deployment.
- Existing Maven `classesDirectory` configuration and Tomcat/JDBC shutdown warnings are unrelated to this change; neither prevented build, tests, or deployment.

## Manual testing guide

Use test users and small demonstration amounts. Sign in to each role in separate browser profiles to observe the correct bell and notification list.

| Flow | Steps and expected result |
| --- | --- |
| Loan | Customer: submit at `/customer/loans`. Loan Officer: see the new application notice, approve/reject on the existing dashboard. Customer: see decision. Accept an approved loan, then repay its installments to see activation/payment/closure notices. |
| Investment | Submit at `/customer/investments`. Investment Officer: see request, approve with sufficient funds or reject. Customer: see decision/funding. Withdraw an active investment, or process a due maturity as officer; customer sees payout/closure. |
| Cards | Request debit and credit cards at `/customer/cards`. Card Services: approve/reject, block/unblock, or close. Customer: see each meaningful change; repeat an already-completed action and confirm no extra notice. |
| Payments | Complete a bill payment and a transfer to another test customer's active account. Sender sees completion; receiver sees receipt. Refresh confirmation/history repeatedly; notice counts do not increase. Insufficient-funds attempts create no notice. |
| Cash | Customer Service: deposit/withdraw using the existing teller form. Only the account owner receives the completed-payment notice. |
| Scheduled payments | Create a scheduled bill through the existing page. Execute when due, or cancel while pending. Customer sees completion/cancellation once. |
| Service requests | Submit at `/customer/requests`. Customer Service sees the request; process, reject, or complete it. Customer sees updates. Re-submit the identical PROCESSING response and confirm no duplicate. Try opening, closure, profile update, cheque book, and statement requests with eligible accounts. |
| Accounts | Register a test customer or complete an account-opening request: customer sees activation. Compliance: freeze/reactivate an account; customer sees each change and administrators see the freeze. Complete an eligible zero-balance closure request: customer sees closure. |
| Administration | Change an employee's role or active status. Active System Administrators receive access-change notices. Save unchanged access details or edit only a name: no notice. Deactivate/reactivate an eligible customer profile: that customer has the respective access notices. |
| Support tickets | Create a ticket, assign a department, exchange replies, move to IN_PROGRESS, then RESOLVED. Existing staff/customer notices and ticket links continue to work. |
| Ownership/read state | Open a notice and verify its unread count decreases once; open again and confirm it stays unchanged. Mark all read and verify another user's count is unaffected. Another customer's notification ID must return 404. |

Notifications apply to new successful operations after deployment. Existing historical records are not backfilled.
