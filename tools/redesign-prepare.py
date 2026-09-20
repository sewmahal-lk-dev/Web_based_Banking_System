from pathlib import Path
import json,hashlib,re
r=Path('src/main/webapp')
baseline={str(p):re.findall(r'<(?:form|input|select|textarea|button)\b[^>]*',p.read_text(encoding='utf-8-sig'),re.I) for p in r.rglob('*.jsp')}
Path('verification/redesign-forms.json').write_text(json.dumps(baseline,indent=2),encoding='utf-8')
Path('verification/redesign-backend.json').write_text(json.dumps({str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in Path('src/main/java').rglob('*.java')},indent=2))
for p in r.rglob('*.jsp'):
 s=p.read_text(encoding='utf-8-sig')
 if '</head>' not in s:continue
 s=s.replace('<head>','<head>\n<script src="<%= request.getContextPath() %>/assets/js/theme-init.js"></script>')
 s=s.replace('</head>','<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/banking-system.css">\n<script defer src="<%= request.getContextPath() %>/assets/js/banking-ui.js"></script>\n</head>')
 # System typography removes remote font requests entirely.
 s=re.sub(r'<link\b[^>]*https://fonts\.(?:googleapis|gstatic)\.com[^>]*>','',s,flags=re.S)
 if p.name=='home.jsp':
  s=s.replace('<body>','<body class="public-site">').replace('A little more<br>confidence.<br><em>Every day.</em>','Trust in every<br><em>transaction.</em>')
  s=s.replace('Residential architecture in leafy surroundings','Modern home with a pool and outdoor living space')
 if p.parent.name=='customer':s=s.replace('<body>','<body class="customer-portal">')
 if p.name=='services.jsp':s=s.replace('<body>','<body class="customer-portal service-page" data-section="<%= section %>">')
 p.write_text(s,encoding='utf-8')
