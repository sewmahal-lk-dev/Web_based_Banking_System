# LankaTrust UI checkpoint

## Scope

Presentation-only changes. All 40 Java source file hashes match the verified backend baseline. Existing form tags, request parameter names, actions, CSRF controls and workflow buttons match the pre-UI baseline. No migration applied to the original database and no deployment to installed Tomcat.

## UI files

- `src/main/webapp/index.jsp`: public root entry, replacing login redirect.
- `src/main/webapp/WEB-INF/public/home.jsp`: full public homepage.
- `src/main/webapp/assets/css/public.css`, `assets/js/public.js`: responsive public layout, menu, subtle reveals and reduced-motion support.
- `src/main/webapp/assets/css/lankatrust.css`: shared portal/authentication polish.
- `src/main/webapp/login.jsp`, `register.jsp`: branding, shared styles, home navigation and supported security copy.
- `src/main/webapp/customer/dashboard.jsp`, `accounts.jsp`, `transfer.jsp`, `payments.jsp`: branding and shared styling.
- `src/main/webapp/WEB-INF/customer/services.jsp`: shared styling for history, cards, loans, investments, requests, products and settings.
- `src/main/webapp/WEB-INF/employee/dashboard.jsp`: all six roles, internal brand header and status badges.
- `src/main/webapp/WEB-INF/fragments/customer-sidebar.jsp`: LankaTrust wordmark.
- `src/main/webapp/WEB-INF/web.xml`: display name only.
- `src/main/webapp/assets/images/`: three local photographs and source credits.

## Homepage

Sticky responsive navigation and hero; Personal Banking, Accounts, Cards, Loans, Investments, Digital Banking, Why LankaTrust, Security & Trust, About, Platform Achievements, Contact/Support and footer. Contact routes use existing support workflows; no invented phone numbers or awards. Local photos are illustrative, not presented as Sri Lankan locations or customer endorsements.

## Verification

- 34 isolated financial/security checks, 270 end-to-end workflow checks and 75 read-only rendering/access checks.
- 84 source checks for backend preservation, form contracts and asset paths.
- 81 Chrome checks: 20 routes/roles at 1440, 1280, 768 and 390 pixels, plus mobile navigation. No page overflow, failed images, server-error pages or old branding. Lazy images were explicitly decoded before checking.
- Root URL returns HTTP 200 with the public homepage.
- Desktop/mobile screenshots reviewed and saved under `verification/ui/`.
- No user-visible Aureus branding remains. No empty href or missing local asset references found.

## Boundaries

Scheduled payments still require manual execution when due. Financial terms, card issuance and settlement retain the existing demo scope. No backend logic was redesigned. Migration and deployment remain pending by instruction.

## Reproduce

`tools/Run-LowMemory.cmd` runs the clean Maven build and 304 safety/end-to-end checks. `tools/SmokeTest.java` runs the 75 read-only checks against the isolated test database. `python tools/ui-check.py` checks source preservation. `tools/UiPreview.java` and `tools/ui-browser.mjs` provide isolated loopback browser verification; test sessions are generated only from test database fixtures. Stop the preview and its dedicated headless browser before Maven clean to release temporary files.
