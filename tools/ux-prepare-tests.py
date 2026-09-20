from pathlib import Path
import runpy
runpy.run_path('tools/task3-prepare-tests.py')
p=Path('target/task3-tools/SupportWorkflowTest.java')
s=p.read_text(encoding='utf-8').replace('tools/task3-browser.mjs','tools/ux-browser.mjs')
s=s.replace('    Files.writeString(Path.of("target/notification-test-url.txt"),base);', '''    execute("INSERT INTO customer(customer_id,name,email,password,status) VALUES(700,'Card Applicant','card-applicant@example.invalid','"+com.banking.util.PasswordUtil.hashPassword("Fixture-Login-Only-2026!")+"','ACTIVE'),(701,'Other Card Customer','card-other@example.invalid','unused','ACTIVE')");
    execute("INSERT INTO account(account_number,customer_id,account_type,balance,status,open_date) VALUES(910000,700,'SAVINGS',0,'CLOSED',CURDATE()),(910001,700,'SAVINGS',1000,'ACTIVE',CURDATE()),(910002,700,'CURRENT',500,'ACTIVE',CURDATE()),(920001,701,'SAVINGS',1000,'ACTIVE',CURDATE())");fixture.commit();
    Files.writeString(Path.of("target/notification-test-url.txt"),base);''')
s=s.replace('    check(browser.exitValue()==0,"real browser notification interactions");', '''    check(browser.exitValue()==0,"real browser notification interactions");
    check(((Number)scalar("SELECT COUNT(*) FROM card WHERE account_number=910001")).intValue()==2,"card application and tampered request use authenticated first active account");
    check(((Number)scalar("SELECT COUNT(*) FROM card WHERE account_number IN (910000,910002,920001)")).intValue()==0,"card request cannot select closed alternate or foreign accounts");
    check("ACTIVE".equals(scalar("SELECT status FROM card WHERE account_number=910001 AND card_type='DEBIT'")),"browser officer approval activates debit application");
    check("PENDING".equals(scalar("SELECT status FROM card WHERE account_number=910001 AND card_type='CREDIT'")),"tampered credit request does not fake approval");
    check(((java.math.BigDecimal)scalar("SELECT daily_limit FROM card WHERE account_number=910001 AND card_type='CREDIT'")).compareTo(com.banking.util.DemoRules.number("card.dailyLimit"))==0,"tampered limits cannot override configured card limits");''')
p.write_text(s,encoding='utf-8')
p=Path('target/task3-tools/notification-browser.mjs')
s=p.read_text(encoding='utf-8').replace('verification/task3-notification-','verification/ux-notification-')
s=s.replace("document.querySelector('#support-ticket-${id} details').open", "!document.querySelector('#support-ticket-${id}').hidden")
p.write_text(s,encoding='utf-8')
print('UX verification uses only temporary fixtures; existing ticket-link assertion updated for selected details pane.')
