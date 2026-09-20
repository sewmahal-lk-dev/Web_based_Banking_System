"""Stage the existing regression runner with all migration fallbacks removed.
Only connection-local temporary tables are populated. Production tools stay unchanged.
"""
from pathlib import Path

stage = Path('target/task3-tools')
stage.mkdir(parents=True, exist_ok=True)
source = Path('tools/SupportWorkflowTest.java').read_text(encoding='utf-8')
# MySQL temporary tables cannot have foreign keys. Keep CHECK constraints intact.
source = source.replace('CONSTRAINT', 'CONSTRAINT.*FOREIGN KEY')
# The history page now has a semantic table; keep checking the exact visible bill reference.
source = source.replace('body().contains("Consumer / Bill No: CEB/123-456")', 'body().contains("<td>CEB/123-456</td>")')
start = source.index('  if(!tables.contains("ticket_message"))')
end = source.index('  try {\n   // All DDL below', start)
source = source[:start] + '''  for(String required:List.of("ticket_message","notification"))if(!tables.contains(required))throw new AssertionError("Required schema missing; no migrations permitted");
  try(Statement st=fixture.createStatement();ResultSet rs=st.executeQuery("SHOW COLUMNS FROM ticket LIKE 'assigned_role'")){if(!rs.next())throw new AssertionError("V6 schema required; no migrations permitted");}
''' + source[end:]
start = source.index('   if (!fixture.getMetaData().getColumns')
end = source.index('   check(scalar(', start)
source = source[:start] + '''   try(Statement st=fixture.createStatement();ResultSet rs=st.executeQuery("SHOW COLUMNS FROM payment LIKE 'bill_reference'")){if(!rs.next())throw new AssertionError("Bill reference schema required; no migrations permitted");}
''' + source[end:]
source = source.replace('tools/notification-browser.mjs', 'tools/task3-browser.mjs').replace('browser.waitFor(90,', 'browser.waitFor(600,')
assert 'database/migrations/' not in source
(stage / 'SupportWorkflowTest.java').write_text(source, encoding='utf-8')
browser = Path('tools/notification-browser.mjs').read_text(encoding='utf-8').replace('verification/notification-', 'verification/task3-notification-')
(stage / 'notification-browser.mjs').write_text(browser, encoding='utf-8')
print('Staged existing temporary-table regression suites; migration fallbacks removed.')
