from pathlib import Path
import hashlib, json, re

baseline=json.loads(Path('verification/task3-baseline.json').read_text())
contracts=json.loads(Path('verification/task3-contracts.json').read_text())
checks=[]
def check(ok, label):
    checks.append({'check':label,'passed':bool(ok)})
    if not ok: print('FAIL:',label)

changed=[]
for name, expected in baseline.items():
    file=Path(name)
    actual=hashlib.sha256(file.read_bytes()).hexdigest() if file.exists() else None
    if actual != expected: changed.append(name)
    if name.startswith(('src/main/java/','src/main/resources/','database/')) or name.endswith('web.xml'):
        check(actual==expected, 'Unchanged protected file: '+name)
for name, expected in contracts.items():
    actual=re.findall(r'<(?:form|input|select|textarea|button)\b[^>]*(?:%>[^>]*)?>',Path(name).read_text(encoding='utf-8'),re.S)
    check(actual==expected,'Unchanged form contracts: '+name)
for file in Path('src/main/webapp').rglob('*.jsp'):
    source=file.read_text(encoding='utf-8')
    if '</head>' in source and 'banking-system.css' in source:
        check('design-system.css' in source, 'Shared design included: '+str(file))

def luminance(hexcolor):
    rgb=[int(hexcolor[i:i+2],16)/255 for i in (1,3,5)]
    rgb=[v/12.92 if v<=.04045 else ((v+.055)/1.055)**2.4 for v in rgb]
    return sum(a*b for a,b in zip(rgb,[.2126,.7152,.0722]))
palette={
    'Primary CTA':('#330006','#FFC107'), 'Navigation text':('#de cbd0'.replace(' ',''),'#330006'),
    'White on burgundy':('#ffffff','#330006'), 'Light text':('#291f22','#ffffff'),
    'Light muted text':('#6c6065','#ffffff'), 'Light links':('#795300','#ffffff'),
    'Light success badge':('#226044','#eaf5ef'), 'Light warning badge':('#785400','#fff4cc'),
    'Light error badge':('#a32137','#fcecf0'), 'Dark text':('#f7edf1','#241b20'),
    'Dark muted text':('#c4b3bb','#241b20'), 'Dark links':('#ffd35c','#241b20'),
    'Dark success badge':('#92ddb1','#20382b'), 'Dark warning badge':('#ffda74','#403519'),
    'Dark error badge':('#ffadbe','#48212c')}
ratios={}
for name,(fg,bg) in palette.items():
    light,dark=sorted([luminance(fg),luminance(bg)],reverse=True)
    ratio=(light+.05)/(dark+.05);ratios[name]={'foreground':fg,'background':bg,'ratio':round(ratio,2)}
    check(ratio>=4.5,'Text contrast >= 4.5: '+name)
new_sources=[str(p).replace('\\','/') for p in Path('src').rglob('*') if p.is_file() and str(p).replace('\\','/') not in baseline]
check(not any(str(p).replace('\\','/') not in baseline for p in Path('database/migrations').glob('*')), 'No migration created')
result={'checks':len(checks),'passed':sum(c['passed'] for c in checks),'failures':[c for c in checks if not c['passed']],'changedFiles':changed,'createdApplicationFiles':new_sources,'contrast':ratios}
Path('verification/task3-source-audit.json').write_text(json.dumps(result,indent=2))
print(f"Source and contrast audit: {result['passed']}/{result['checks']} passed")
if result['failures']: raise SystemExit(1)
