"""Read-only CSS parsing and JSP/form preservation checks against saved originals."""
from pathlib import Path
import re
import tinycss2

root = Path(__file__).resolve().parents[1]
web = root / 'src/main/webapp'
errors = []

def validate(rules):
    for rule in rules:
        if rule.type == 'error':
            errors.append(str(rule))
        elif rule.type == 'at-rule' and rule.content:
            validate(tinycss2.parse_rule_list(rule.content, skip_whitespace=True, skip_comments=True))
        elif rule.type == 'qualified-rule':
            declarations = tinycss2.parse_declaration_list(rule.content, skip_whitespace=True, skip_comments=True)
            errors.extend(str(d) for d in declarations if d.type == 'error')

validate(tinycss2.parse_stylesheet((web / 'assets/css/public.css').read_text(), skip_whitespace=True, skip_comments=True))
assert not errors, errors
for name in ['WEB-INF/public/home.jsp', 'login.jsp', 'register.jsp']:
    old = (root / 'verification/premium-originals' / name).read_text(encoding='utf-8')
    new = (web / name).read_text(encoding='utf-8')
    # Presentation includes add/remove contextPath expressions only.
    scripts = lambda text: [s for s in re.findall(r'<%[\s\S]*?%>', text) if 'request.getContextPath()' not in s]
    assert scripts(old) == scripts(new), name
    if not name.endswith('home.jsp'):
        forms = lambda text: re.findall(r'<form\b[\s\S]*?</form>', text)
        assert forms(old) == forms(new), name
print('CSS parsed; JSP scriptlets and complete authentication forms unchanged.')
