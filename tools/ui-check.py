import json,re,hashlib
from pathlib import Path
baseline=json.loads(Path('tools/ui-form-baseline.json').read_text())
checks=0
for name,tags in baseline.items():
    current=re.findall(r'<(?:form|input|select|textarea|button)\b[^>]*',Path(name).read_text(encoding='utf-8-sig'),re.I)
    assert current==tags, 'Form contract changed: '+name
    checks+=1
for name,digest in json.loads(Path('tools/ui-backend-baseline.json').read_text()).items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==digest, 'Backend changed: '+name
    checks+=1
root=Path('src/main/webapp')
for p in root.rglob('*.jsp'):
    s=p.read_text(encoding='utf-8-sig')
    assert 'aureus' not in s.lower(),str(p)
    assert not re.search(r'href=[\"\']#?[\"\']',s),str(p)
    for asset in re.findall(r'/assets/[\w./-]+\.(?:css|js|jpg|png)',s):
        assert (root/asset.lstrip('/')).is_file(),asset
        checks+=1
print(f'PASS {checks} form/backend/asset checks; branding and empty links clean')
