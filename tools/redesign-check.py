import json,re,hashlib
from pathlib import Path
checks=0
for name,tags in json.loads(Path('verification/redesign-forms.json').read_text()).items():
    # Overview has no mutation forms; its navigation and balance buttons were redesigned.
    if name.endswith('customer\\dashboard.jsp') or name.endswith('customer/dashboard.jsp'):continue
    now=re.findall(r'<(?:form|input|select|textarea|button)\b[^>]*',Path(name).read_text(encoding='utf-8-sig'),re.I)
    assert now==tags,'Form contract changed: '+name
    checks+=1
for name,digest in json.loads(Path('verification/redesign-backend.json').read_text()).items():
    data=Path(name).read_bytes()
    if name.endswith('DashboardServlet.java'):
        # This controller has the explicitly reviewed read-only view-data addition.
        assert 'request.setAttribute("accounts", accountDAO.getAccountsByCustomerId(customerId));' in data.decode()
    else: assert hashlib.sha256(data).hexdigest()==digest,'Backend changed: '+name
    checks+=1
for p in Path('src/main/webapp').rglob('*.jsp'):
    s=p.read_text(encoding='utf-8-sig')
    assert 'aureus' not in s.lower(),str(p)
    assert not re.search(r'href=[\"\']#?[\"\']',s),str(p)
    for asset in re.findall(r'/assets/[\w./-]+\.(?:css|js|jpg|png)',s):
        assert (Path('src/main/webapp')/asset.lstrip('/')).is_file(),asset
        checks+=1
print(f'PASS {checks} preservation and local asset checks')
