# Latest: LankaTrust UI pass complete

See [UI_CHECKPOINT.md](verification/UI_CHECKPOINT.md) for the presentation changes and checks. Final UI WAR SHA256: `6a563136aeee00e23276a413605a120bb18972f8d79db7d1065a034d704a1237`. The backend checkpoint below is historical; migration and deployment remain pending.

# Final verification checkpoint — 2026-09-16

## Result

BUILD SUCCESS: low-memory Microsoft JDK 17 `mvn clean package` completed at 00:08:11 Asia/Colombo using `tools/Run-LowMemory.cmd`.

379 automated checks passed: 34 isolated financial/validation checks, 270 end-to-end checks against a separate MySQL test database and isolated loopback Tomcat, and 75 read-only rendering/access checks. These are standalone test harness checks, not Maven Surefire test counts. JSPs were exercised through Tomcat.

WAR: `target/WebBasedBankingSystem.war`. Verified 41 compiled classes, required workflow resources, and absence of compiler-error bytecode.
SHA256: `102FA1F37CF7CA6ACB0ABE41A0941FCDF49CE78FF05CE09423F6C3AB1E6B4A30`.

## Completed workflows

- Customer registration/login, profile updates, password change with session invalidation, product browsing and transaction history.
- Account opening, guarded closure, customer deactivation and administrator reactivation.
- Transactional transfers and bill payments, scheduled payment create/edit/cancel and due-date execution with duplicate prevention.
- Card request, approval/rejection, limits, blocking/unblocking and guarded closure; debit/credit subtype records.
- Loan application/edit/cancel, officer approval/rejection, customer acceptance, transactional disbursement and installment repayment through closure.
- Investment application/edit/cancel, approval/funding, early withdrawal and maturity payout with duplicate prevention.
- Service requests, account/profile actions, owned CSV statement downloads, demo cheque-book fulfillment, and support ticket responses/closure.
- Employee and product administration using status changes; role actions, compliance account freeze/unfreeze and searchable audit history.
- CUSTOMER and all six employee roles: login redirects, cross-role denial, active-status checks, CSRF and ownership checks.

Financial changes use transaction commit/rollback and bound parameters; completed financial history is retained. Tests include rollback after a debit, replay prevention, invalid inputs and unauthorized access.

## Demo scope and limits

Financial configuration is in `src/main/resources/demo-rules.properties`. Loans use configured flat simple interest and monthly installments with final rounding adjustment; investments use configured ACT/365 simple interest and principal-only early withdrawal. Cards use DEMO identifiers. Existing loans/investments retain their recorded terms.

Scheduled payments require explicit execution on/after their due date; no unattended scheduler exists. External bill settlement, card-network issuance and physical cheque fulfillment are outside this demo. No unresolved compilation or tested workflow failures remain. Source scan found no TODO/FIXME, dead href="#", Not implemented or UnsupportedOperationException markers. Normal input placeholder attributes remain.

## Database migration — REQUIRED before deployment

`database/migrations/V2__complete_workflows.sql` is applied and verified in `banking_test_20260915`. Java workflows ran against that schema; new columns, enum values and three account foreign keys were verified in its schema snapshot.

The original `banking_system` has NOT been migrated or modified. Before/after schema and data snapshot hashes match exactly. No destructive SQL was executed.

From the project root, after approving the migration, run:

```powershell
$env:JAVA_HOME = 'C:\Users\User\.jdks\ms-17.0.20'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\tools\Apply-Migration.ps1
```

The script backs up the configured database before applying the additive V2 migration. Ensure BANK_DB_URL / bank.db.url overrides do not point to the test database. MySQL DDL commits per statement; retain the backup if execution is interrupted. The runner skips completed table steps.

## Deployment checkpoint

Not deployed. Installed Tomcat WAR remains unchanged (SHA256 `15AE10E7DF0CAE37DE84F4C7D255F56750440FC4CA0AD0BD10EA2C72E667DD2A`). Next: approve/apply the backed-up V2 migration, then authorize deployment of the verified WAR. No additional credentials were required for local verification.

The low-memory build isolates Maven output in `target/maven-classes`; Java 17 editor settings prevent the earlier compiler-error classes. Maven reports a WAR-plugin read-only parameter warning, but the generated artifact was inspected and exercised successfully. The isolated smoke server logs JDBC cleanup warnings at shutdown; its 75 checks passed.
