# Banking application workspace redesign

Review build only. The application now opens focused workspaces and original forms in dialogs, with burgundy (#330006) and gold retained. This report describes the changes relative to the UX baseline captured at the start of this request.

1. **Architecture.** The staff page no longer displays every management group and form in one vertical stack. A compact role home replaces that stack; selecting a destination shows one workspace and resets its position. Original server markup is progressively reorganized in the browser. Customer pages use their existing dedicated routes, with compact previews on the dashboard.
2. **Customer navigation.** The shared sidebar opens accounts, transfer, payments, cards, loans, investments, service requests, support, notifications and settings. Payments separates bill payment and scheduled payments. Service requests and support are separate selected views. Existing routes, deep links and browser history remain usable.
3. **Employee navigation.** Each role receives navigation derived from its server-rendered permissions. Customer Service has Cash Transactions, Service Requests and Support Tickets. Loan, Card and Investment officers have their own records and Pending/Active shortcuts; compliance has its audit workspace. Assigned support opens directly. No role receives additional server permissions.
4. **Admin navigation.** Dashboard, Customers, Employees, Products and Audit are separate selected workspaces. Home contains task tiles and counts explicitly labeled as currently loaded records. Employee/product creation opens directly from home or its management workspace. Tables provide search, status filtering, six-record pages and record drawers; audit search stays in its own workspace.
5. **Dialogs and drawers.** Transfer, bill payment, scheduled payment, applications, new service request, new ticket, reply, assignment, ticket status, cash deposit/withdrawal, profile/password, card limit and employee/product forms use original DOM forms in dialogs when applicable. Full record details open in a right drawer. Destructive actions and employee/product status actions require confirmation. Native required fields, CSRF, action values and submitters are retained.
6. **Quick Actions.** Customer home exposes Transfer money, Pay a bill, Apply for a loan, Manage cards, Invest and Get support. All six fit on the initial viewport at every requested width. Admin and staff task tiles open their authorized workspace or action directly. Recent transactions and accounts are bounded previews with View all links.
7. **Action launcher.** Header search and Ctrl/Cmd+K open a keyboard-operated action palette. Results index authorized navigation and action names; this is not a global database search. Escape closes it and focus returns through the native dialog behavior.
8. **Motion.** Workspace entrances, selected navigation, action-tile hover/press, dialogs/drawers, toasts, submission feedback and public-section reveals use restrained transitions. Public bank-card hover adds depth. Reduced-motion preferences suppress nonessential movement. Financial values are not animated into fabricated amounts.
9. **Support Center.** Desktop uses a searchable/filterable ticket rail beside one selected conversation. Status and department, the workflow tracker, metadata disclosure and immediate reply/assignment/status controls belong to the selected ticket. Conversation history scrolls within its pane. Mobile starts with the list, opens the chosen ticket and provides Back to tickets. Existing message, routing and status operations remain intact.
10. **Notifications.** Existing unread badge, dropdown, read/mark-all operations and ticket destinations are preserved. The dropdown remains usable on mobile, and notification navigation opens the relevant conversation in the new support view.
11. **Feedback.** Server success/error messages supply live-region toasts; no simulated successful banking operations are shown. Successful modal submissions return to the relevant workspace. Validation failures reopen the relevant action with the actual server error inside the dialog. Submission feedback preserves the original submitter.
12. **Mobile.** Collapsible navigation, two-column customer quick actions, full-width compact dialogs, safe table scroll regions and list-to-detail support replace oversized desktop stacks. Long forms/conversations can scroll within their active surface. Both light and dark themes were exercised.
13. **Existing files changed (12).**

- `src/main/webapp/login.jsp`
- `src/main/webapp/register.jsp`
- `src/main/webapp/customer/accounts.jsp`
- `src/main/webapp/customer/dashboard.jsp`
- `src/main/webapp/customer/payments.jsp`
- `src/main/webapp/customer/transfer.jsp`
- `src/main/webapp/WEB-INF/notifications.jsp`
- `src/main/webapp/WEB-INF/customer/services.jsp`
- `src/main/webapp/WEB-INF/employee/dashboard.jsp`
- `src/main/webapp/WEB-INF/fragments/customer-workflow-create.jsp`
- `src/main/webapp/WEB-INF/public/home.jsp`
- `src/main/webapp/assets/js/banking-ui.js`

14. **New implementation and test files (5).**

- `src/main/webapp/assets/css/workspaces.css`
- `src/main/webapp/assets/js/workspaces.js`
- `tools/ux-prepare-tests.py`
- `tools/ux-browser.mjs`
- `tools/ux-audit.py`

Generated `verification/ux-*` evidence files are listed with hashes in `ux-file-manifest.json`; generated build/test output is under `target/`.

15. **Screenshots.** The gallery below covers public home, customer control center, quick-action modal, desktop/mobile support, notifications, Customer Service, all specialist workspaces, admin dashboard, employee modal/drawer and payment errors. Direct visual inspection confirms different composition: customer actions are above the activity preview, admin home contains task tiles instead of management forms, and support shows one conversation next to a ticket list. Fixture names/amounts in these screenshots come from isolated test records.

- [ux-1440-customer-quick-action-modal.png](ux-1440-customer-quick-action-modal.png)
- [ux-390-customer-quick-action-modal.png](ux-390-customer-quick-action-modal.png)
- [ux-admin-Audit-log.png](ux-admin-Audit-log.png)
- [ux-admin-Customers.png](ux-admin-Customers.png)
- [ux-admin-employee-drawer.png](ux-admin-employee-drawer.png)
- [ux-admin-employee-modal.png](ux-admin-employee-modal.png)
- [ux-admin-Employees.png](ux-admin-Employees.png)
- [ux-admin-Products.png](ux-admin-Products.png)
- [ux-admin-status-confirmation.png](ux-admin-status-confirmation.png)
- [ux-card-application-dark-1024.png](ux-card-application-dark-1024.png)
- [ux-card-application-dark-1440.png](ux-card-application-dark-1440.png)
- [ux-card-application-dark-390.png](ux-card-application-dark-390.png)
- [ux-card-application-dark-768.png](ux-card-application-dark-768.png)
- [ux-card-application-error.png](ux-card-application-error.png)
- [ux-card-application-light-1024.png](ux-card-application-light-1024.png)
- [ux-card-application-light-1440.png](ux-card-application-light-1440.png)
- [ux-card-application-light-390.png](ux-card-application-light-390.png)
- [ux-card-application-light-768.png](ux-card-application-light-768.png)
- [ux-card-application-pending.png](ux-card-application-pending.png)
- [ux-card-workspace-approved.png](ux-card-workspace-approved.png)
- [ux-CARD_SERVICES_OFFICER-workspace.png](ux-CARD_SERVICES_OFFICER-workspace.png)
- [ux-cash-deposit-modal.png](ux-cash-deposit-modal.png)
- [ux-command-palette.png](ux-command-palette.png)
- [ux-COMPLIANCE_RISK_OFFICER-workspace.png](ux-COMPLIANCE_RISK_OFFICER-workspace.png)
- [ux-CUSTOMER_SERVICE_OFFICER-workspace.png](ux-CUSTOMER_SERVICE_OFFICER-workspace.png)
- [ux-dark-1440--.png](ux-dark-1440--.png)
- [ux-dark-1440--customer-dashboard.png](ux-dark-1440--customer-dashboard.png)
- [ux-dark-1440--customer-tickets.png](ux-dark-1440--customer-tickets.png)
- [ux-dark-1440--login.jsp.png](ux-dark-1440--login.jsp.png)
- [ux-dark-390--.png](ux-dark-390--.png)
- [ux-dark-390--customer-dashboard.png](ux-dark-390--customer-dashboard.png)
- [ux-dark-390--customer-tickets.png](ux-dark-390--customer-tickets.png)
- [ux-dark-390--login.jsp.png](ux-dark-390--login.jsp.png)
- [ux-dark-admin-dashboard.png](ux-dark-admin-dashboard.png)
- [ux-dark-mobile-ticket.png](ux-dark-mobile-ticket.png)
- [ux-dark-service-dashboard.png](ux-dark-service-dashboard.png)
- [ux-INVESTMENT_OFFICER-workspace.png](ux-INVESTMENT_OFFICER-workspace.png)
- [ux-light-1440--.png](ux-light-1440--.png)
- [ux-light-1440--customer-dashboard.png](ux-light-1440--customer-dashboard.png)
- [ux-light-1440--customer-tickets.png](ux-light-1440--customer-tickets.png)
- [ux-light-1440--login.jsp.png](ux-light-1440--login.jsp.png)
- [ux-light-390--.png](ux-light-390--.png)
- [ux-light-390--customer-dashboard.png](ux-light-390--customer-dashboard.png)
- [ux-light-390--customer-tickets.png](ux-light-390--customer-tickets.png)
- [ux-light-390--login.jsp.png](ux-light-390--login.jsp.png)
- [ux-light-admin-dashboard.png](ux-light-admin-dashboard.png)
- [ux-light-mobile-ticket.png](ux-light-mobile-ticket.png)
- [ux-light-service-dashboard.png](ux-light-service-dashboard.png)
- [ux-LOAN_OFFICER-workspace.png](ux-LOAN_OFFICER-workspace.png)
- [ux-notification-desktop.png](ux-notification-desktop.png)
- [ux-notification-dropdown.png](ux-notification-dropdown.png)
- [ux-notification-mobile.png](ux-notification-mobile.png)
- [ux-payment-error-modal.png](ux-payment-error-modal.png)
- [ux-support-server-success.png](ux-support-server-success.png)
- [ux-ticket-assignment-modal.png](ux-ticket-assignment-modal.png)

16. **Browser matrix.** Real installed Chrome, light and dark themes, 1440?900, 1024?900, 768?900 and 390?844 CSS-pixel viewports. The page/role matrix checks rendering and overflow; pointer/keyboard workflows additionally exercise navigation, all six customer quick actions, staff/admin workspace access, form submission, confirmation, search, notification behavior, support and reduced motion. Responsive emulation is not a physical-device or cross-browser certification.
17. **Regression.** All passed: 591 UX browser checks, 14 original notification browser checks, 289 support/authentication/authorization/notification and card-integrity checks, 196 existing banking CRUD checks, 94 isolated financial/cash/investment safety checks, 75 read-only smoke checks and 110 source/form-contract checks (1,369 checks across these suites). The 41 card-focused browser checks also passed in an earlier focused run; they are included in the final 591 rather than added again. Logs: `ux-workflows.log`, `ux-SmokeTest.log`, `ux-FinancialSafetyTest.log`, `ux-CashTransactionSafetyTest.log`, `ux-InvestmentSafetyTest.log`, `ux-browser-results.json` and `ux-source-audit.json`. Existing support, authorization, notification and banking CRUD suites run with connection-local temporary table fixtures. The runner does not apply migrations. Negative database constraint cases intentionally log exceptions; assertions determine their expected outcome.
18. **Maven.** `mvn clean package` passed (`ux-build.log`). A final `mvn package` after the final card application and dialog updates also passed (`ux-final-build.log`). Review artifact: `target/WebBasedBankingSystem.war`; integrity details and SHA-256 are in `ux-final-integrity.json`. All 49 webapp source files match the packaged WAR.
19. **JSP/Jasper.** Actual HTTP requests against isolated embedded Tomcat compiled and rendered the JSPs, including customer, staff, admin fragments, support, notifications and public pages. Read-only smoke checks also rendered against the existing database. Compiled JSP class names are recorded in the integrity evidence.
20. **Console.** Final Chrome run recorded no JavaScript exceptions or console errors. JavaScript syntax checks passed for both shared UI scripts.
21. **No migration/data reset.** No migrations created or executed, no V5/V6/V7 reapplication, no reseeding. Read-only before/after fingerprints match all 16 permanent tables, including schema and data.
22. **No backend business logic changes.** Baseline hashes and source audit confirm Java, DAO/controller/authentication/authorization code, resources and web.xml are unchanged. Original form-opening/control contracts are unchanged. The implementation changes presentation and browser interactions.
23. **Not deployed.** The installed Tomcat WAR hash still matches the initial deployment fingerprint. No running Tomcat deployment was replaced. Review WAR SHA-256: `8508f31541198209a68d9b364b5d319a70f0982021d0b4d0a52f66e627329b63` (10,352,248 bytes).
24. **Remaining limits.** Record search and paging cover only the records already returned by the unchanged backend, not the complete database beyond its existing limits. Failed submissions retain only values the existing server returns. JavaScript-disabled browsers retain the original server presentation. Very long conversations and record details still require scrolling within their selected view. Chrome testing does not establish Safari/Firefox, screen-reader or physical-device coverage. No known failing acceptance check remains after the final run.


25. **Dedicated card application (additional requirement 29).** Cards now has an immediately visible **Apply for a New Card** action. The large application dialog separates linked account, visual Debit/Credit selection, and limits/review. Cancel and Submit Application are explicit; existing server errors reopen the form. Cards/applications appear as compact masked summaries with actual status and daily limits; View/manage opens full details.

The implementation was based on `CardDAO.apply`, `CustomerServicesServlet`, `FinancialLedger.account`, `CustomerServicesDAO`, the existing card JSP fragments and the schema. The application contract accepts only `type` (DEBIT/CREDIT), with authenticated identity from the session. It selects the customer's lowest-numbered ACTIVE account itself. Accordingly, the form displays that actual account read-only and does not invent an editable account/customer ID or daily limit. Configured daily, debit withdrawal and credit limits are read from `DemoRules`. With no active account, the form explains the issue, links to My Accounts and disables its enhanced submit action; the server remains authoritative.

Applications start PENDING. The actual server success message and new pending record provide feedback. Card Services still approves/rejects through the original officer operation, and the customer sees the actual updated status. Card numbers are already masked by the unchanged DAO. The existing customer card query does not return a submission date, so none is fabricated or inferred from expiry/issue dates.

The browser acceptance coverage adds actual customer password login, direct Cards access, visible application action, owned linked-account summary, both supported card types, all four sizes in both themes, unsupported-type and duplicate-request validation, valid submission, server success/PENDING, officer processing and customer ACTIVE status. Tampering adds foreign account/customer IDs and arbitrary limits; database assertions verify that these ignored extra parameters cannot change ownership, selected account, configured limits or approval status.
