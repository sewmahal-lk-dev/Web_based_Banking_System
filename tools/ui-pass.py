from pathlib import Path
import re,json,hashlib
root=Path('src/main/webapp')
snapshot={}
for p in root.rglob('*.jsp'):
    s=p.read_text(encoding='utf-8-sig')
    snapshot[str(p)]=re.findall(r'<(?:form|input|select|textarea|button)\b[^>]*',s,re.I)
Path('tools/ui-form-baseline.json').write_text(json.dumps(snapshot,indent=2),encoding='utf-8')
Path('tools/ui-backend-baseline.json').write_text(json.dumps({str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in Path('src/main/java').rglob('*.java')},indent=2))
for p in root.rglob('*'):
    if p.suffix not in ('.jsp','.css','.js','.xml'): continue
    s=p.read_text(encoding='utf-8-sig'); old=s
    s=re.sub('aureus','LankaTrust',s,flags=re.I)
    if p.suffix=='.jsp' and '</head>' in s:
        s=s.replace('</head>','<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/lankatrust.css">\n</head>')
    if p.name in ('login.jsp','register.jsp'):
        s=s.replace('<body>','<body class="auth-page">\n<a class="auth-home" href="<%= request.getContextPath() %>/">&larr; LankaTrust home</a>')
        s=re.sub(r'(<div class="logo-box">)\s*A\s*(</div>)',r'\1LT\2',s)
        for a,b in [('256-bit','Protected'),('Secure Encryption','Customer operations'),('24/7','Personal'),('100%','Role-based'),('Secure Access','Access control')]: s=s.replace(a,b)
    if p.name=='customer-sidebar.jsp': s=s.replace('class="customer-logo">A<','class="customer-logo">LT<')
    if p==root/'WEB-INF/employee/dashboard.jsp':
        s=s.replace('<body>','<body class="staff-portal">').replace('<header class="topbar">','<div class="staff-brandbar"><a href="<%= request.getContextPath() %>/">LankaTrust</a><span>Banking operations</span></div><header class="topbar">')
    if s!=old:p.write_text(s,encoding='utf-8')
