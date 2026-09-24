# Admin dashboard and notifications

Implemented against existing registration, employee management, audit, notification, and workspace routes. No new banking workflow or permission was introduced.

## Exact Admin notification events

1. **Successful new customer registration** — new `CUSTOMER_REGISTERED` notice to all active `SYSTEM_ADMIN` employees. Title: “New Customer Registration”; message uses the saved customer name. Written in the customer/account transaction and rolled back if any part fails.
2. **Existing employee role or active-status change** — existing `EMPLOYEE_ACCESS` notice now excludes the acting administrator. Other active administrators receive it. Unchanged access and ordinary name/contact edits do not notify.
3. **Compliance account freeze** — existing `ACCOUNT_FROZEN` administrator alert is retained. The actual workflow is performed by Compliance, not Admin.

No historical notices are deleted or backfilled. Other roles retain their existing loan, investment, card, payment, service-request, and support-ticket notices.

## Dashboard additions

- Recent Notifications: latest five notifications belonging to the signed-in Admin, with title, message, timestamp, read/unread state and existing View All destination.
- Reuses the same `notification-items.jsp` component and notification POST endpoint. Opening a registration notice marks it read and navigates to existing Customers management. The existing bell reads the recipient's unread count after redirect.
- Recent Administrative Activity: latest five matching rows from `audit_log`, joined to employee names where available. Shows action, details, actor and timestamp; View All opens the existing `#Audit-log` workspace.
- Administrative actions included: `EMPLOYEE_CREATE`, `EMPLOYEE_UPDATE`, `employee-status`, `PRODUCT_CREATE`, `PRODUCT_UPDATE`, `CUSTOMER_STATUS`, and `REPORT_GENERATED`. These are existing emitted action names; historical role changes do not hide relevant audit rows.
- Existing welcome, Start a Task, management sections, Reports, Notifications, and four count cards remain unchanged.
- New styles are scoped to the Admin overview and inherit the current dark/Light Mode palette. No existing CSS file was changed.

## Authored files changed or added

| Path | Change |
| --- | --- |
| `src/main/java/com/banking/dao/NotificationDAO.java` | Registration helper, actor exclusion for access notices, navigation to existing Customers/Employees sections. |
| `src/main/java/com/banking/dao/CustomerDAO.java` | Calls registration helper after customer/account writes, before atomic commit. |
| `src/main/java/com/banking/dao/AdminDAO.java` | Supplies acting Admin to access notices; retrieves five relevant audit rows with actor information and Admin authorization. |
| `src/main/java/com/banking/dao/EmployeeDAO.java` | Supplies acting Admin for the existing employee-status action. |
| `src/main/java/com/banking/controller/EmployeeServlet.java` | Loads recent activity only for System Administrator; handles unavailable activity without replacing existing management data. |
| `src/main/webapp/WEB-INF/employee/dashboard.jsp` | Includes Admin-only overview and scoped stylesheet. |
| `src/main/webapp/WEB-INF/fragments/admin-overview.jsp` | New compact recent-notification and recent-activity sections; escapes database content and includes empty/error states. |
| `src/main/webapp/assets/js/workspaces.js` | Places the overview below existing dashboard metrics; keeps notification forms out of banking-action dialogs. |
| `src/main/webapp/assets/css/admin-overview.css` | New responsive Admin-only styling; overrides inherited form/button rules within the notification section only. |
| `tools/BankingNotificationTest.java` | Updates previous regression expectations to exclude the acting Admin. |
| `tools/AdminDashboardTest.java` | New Admin-specific integration/HTTP tests plus complete banking notification regression suite. |
| `verification/admin-dashboard-browser.mjs` | Browser checks for integration, navigation, reading notifications, responsive layouts and themes. |
| `verification/admin-dashboard-report.md` | This change and validation record. |

## Database changes

None. Existing `notification` and `audit_log` tables are reused. No schema migration, new table, or production fixture changes.

Tests copied table definitions into a uniquely named disposable `banking_test_admin_dashboard_<timestamp>` database, used synthetic data, then dropped it during cleanup. No live customer or employee data was changed for testing.

## Validation

- Final `mvn clean package`: **BUILD SUCCESS**.
- **223 integration/HTTP assertions passed**, including the complete existing banking notification regression suite.
- **51 browser assertions passed** at 1440, 768, 390 and 320 pixels, in dark and Light Mode. No JavaScript exceptions or horizontal overflow.
- Verified active-role registration recipients, inactive Admin exclusion, actor exclusion, failed-registration rollback, unchanged counts on refresh, notification ownership, read-all isolation, correct bell counts and escaped customer names.
- Verified actual database rows in both recent sections, five-row limits, actor/timestamp display, existing Customers/Employees/Products/Audit Log workspaces, audit search, report generation and correct recipient isolation for other banking roles.
- Visually inspected desktop dark and mobile Light Mode screenshots after fixing inherited form styles.
- Existing app assets unrelated to this task are preserved in the staged deployment WAR.

Evidence: `admin-dashboard-build.log`, `admin-dashboard-tests.log`, `admin-dashboard-tests.json`, `admin-dashboard-test-errors.log`, `admin-dashboard-browser.json`, `admin-dashboard-{dark,light}-{1440,390}.png`, `admin-dashboard-changes.diff`, and `admin-dashboard-deployment.json` in this directory. Pre-edit snapshots are in `admin-dashboard-originals/`.

## Intentionally omitted

- No Admin ticket escalation, loan approval, investment approval or card approval workflow exists; none was added and those operational notices are not copied to Admin.
- Ordinary customer/employee/product CRUD and report generation do not produce notification spam. Their already-recorded administrative actions remain visible through the audit section.
- No notifications for viewing reports, reading audit logs, refreshing the dashboard, or opening notification lists.
- No separate activity feed table, notification store, polling service or invented audit records.

## Quick manual check

1. Register a new test customer, then sign in as Admin. The new registration appears in the bell and Recent Notifications.
2. Open it: Customers management opens, the notice becomes read, and unread count decreases once. Refresh: no duplicate is added.
3. With two Admin sessions, change an employee's role/status as one Admin. Only the other active Admin receives the access-change notice.
4. Create/edit a product or generate a report. Recent Administrative Activity shows the real audit action; the bell receives no new notice.
5. Use both View All links and all existing management/sidebar links. Verify another role's dashboard does not contain the Admin overview.
