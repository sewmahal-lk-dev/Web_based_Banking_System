"""Compare production contracts and protected files with this UX task's starting state."""
from pathlib import Path
import hashlib,json,re
baseline=json.loads(Path('verification/ux-baseline.json').read_text(encoding='utf-8'))
contracts=json.loads(Path('verification/ux-contracts.json').read_text(encoding='utf-8'))
checks=[]
def check(ok,label):checks.append({'check':label,'passed':bool(ok)})
changed=[]
for name,expected in baseline.items():
    p=Path(name);actual=hashlib.sha256(p.read_bytes()).hexdigest() if p.is_file() else None
    if actual!=expected:changed.append(name)
    if name.startswith(('src/main/java/','src/main/resources/','database/')) or name.endswith('web.xml'):
        check(actual==expected,'Protected file unchanged: '+name)
for name,expected in contracts.items():
    actual=re.findall(r'<(?:form|input|select|textarea|button)\b[^>]*(?:%>[^>]*)?>',Path(name).read_text(encoding='utf-8'),re.S)
    check(actual==expected,'Form contracts unchanged: '+name)
for p in Path('src/main/webapp').rglob('*.jsp'):
    source=p.read_text(encoding='utf-8')
    if 'design-system.css' in source and '</head>' in source:
        check('workspaces.css' in source and 'workspaces.js' in source,'Shared workspaces included: '+str(p))
check(all(str(p).replace('\\','/') in baseline for p in Path('database/migrations').glob('*')),'No migration created')
result={'checks':len(checks),'passed':sum(c['passed'] for c in checks),'failures':[c for c in checks if not c['passed']],'changedExistingFiles':changed}
Path('verification/ux-source-audit.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
if result['failures']:raise SystemExit(1)
