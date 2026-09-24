<%@ page contentType="text/html;charset=UTF-8" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <script src="<%= request.getContextPath() %>/assets/js/theme-init.js"></script>

        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">

        <title>LankaTrust | Trust in every transaction.</title>

        <meta name="description" content="A thoughtful approach to personal banking with LankaTrust.">

        <!-- Shared styles first -->
        <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/banking-system.css">

        <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/design-system.css">

        <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/workspaces.css">

        <!-- Public homepage overrides LAST -->
        <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/public.css">

        <script defer src="<%= request.getContextPath() %>/assets/js/banking-ui.js"></script>

        <script defer src="<%= request.getContextPath() %>/assets/js/public.js"></script>
    </head>

    <body class="public-site">

        <a class="skip" href="#main">Skip to content</a>


        <!-- ======================================================
     TOP BAR
     ====================================================== -->

        <div class="utility">
            <div class="wrap utility-inner">
                <span class="utility-main">Personal Banking</span>
                <span class="utility-message">
                    A considered approach to your finances.
                </span>
            </div>
        </div>


        <!-- ======================================================
     NAVIGATION
     ====================================================== -->

        <header class="site-header">

            <nav class="wrap nav" aria-label="Main navigation">

                <a class="wordmark" href="<%= request.getContextPath() %>/">

                    <span class="brand-symbol" aria-hidden="true">
                        <span>LT</span>
                    </span>

                    <span class="brand-copy">
                        <strong>LankaTrust</strong>
                        <small>PRIVATE BANKING</small>
                    </span>

                </a>


                <button class="menu-toggle" type="button" aria-expanded="false" aria-controls="nav-links">
                    Menu ☰
                </button>


                <div id="nav-links" class="nav-links">

                    <a href="#home">Home</a>

                    <a href="#personal">Personal Banking</a>

                    <a href="#accounts">Accounts</a>

                    <a href="#cards">Cards</a>

                    <a href="#loans">Loans</a>

                    <a href="#investments">Investments</a>

                    <a href="#about">About</a>

                    <a href="#contact">Contact</a>

                    <a class="nav-login" href="<%= request.getContextPath() %>/login.jsp">
                        Login
                    </a>

                    <a class="button nav-cta" href="<%= request.getContextPath() %>/register.jsp">
                        Open an Account
                    </a>

                </div>

            </nav>

        </header>


        <!-- ======================================================
     MAIN
     ====================================================== -->

        <main id="main">


            <!-- ==================================================
         HERO
         ================================================== -->

            <section id="home" class="hero">

                <!-- Add assets/images/colombo-lotus.jpg: evening Colombo panorama, Lotus Tower in the right third. -->
                <div class="hero-skyline" aria-hidden="true"></div>
                <div class="hero-light hero-light-one"></div>
                <div class="hero-light hero-light-two"></div>

                <div class="hero-line hero-line-one"></div>
                <div class="hero-line hero-line-two"></div>

                <div class="wrap hero-grid">


                    <!-- LEFT -->

                    <div class="hero-content">

                        <div class="hero-kicker">
                            <span></span>
                            PERSONAL BANKING, REIMAGINED
                        </div>

                        <h1>
                            Trust in every<br>
                            <em>transaction.</em>
                        </h1>

                        <p class="hero-description">
                            From everyday banking to tomorrow's ambitions,
                            LankaTrust brings your financial world together
                            with clarity, confidence and care.
                        </p>


                        <div class="hero-actions">

                            <a class="button primary-cta" href="<%= request.getContextPath() %>/login.jsp">

                                Login to Banking

                                <span aria-hidden="true">↗</span>

                            </a>


                            <a class="secondary-cta" href="<%= request.getContextPath() %>/register.jsp">

                                Open an Account

                                <span aria-hidden="true">→</span>

                            </a>

                        </div>


                        <div class="hero-trust">

                            <div class="trust-mark">
                                LT
                            </div>

                            <div>
                                <strong>Trust in every transaction.</strong>

                                <span>
                                    Secure. Simple. Considered.
                                </span>
                            </div>

                        </div>

                    </div>


                    <!-- RIGHT VISUAL -->

                    <div class="hero-visual" aria-label="LankaTrust premium banking card">

                        <div class="visual-orbit orbit-one"></div>
                        <div class="visual-orbit orbit-two"></div>

                        <div class="glass-panel glass-panel-back"></div>
                        <div class="glass-panel glass-panel-front"></div>


                        <div class="premium-card">

                            <div class="card-top">

                                <div class="card-bank">
                                    LankaTrust
                                </div>

                                <div class="card-symbol">
                                    LT
                                </div>

                            </div>


                            <div class="premium-chip" aria-hidden="true">

                                <span></span>
                                <span></span>
                                <span></span>

                            </div>


                            <div class="card-number">
                                5412&nbsp;&nbsp;8846&nbsp;&nbsp;2391&nbsp;&nbsp;7084
                            </div>


                            <div class="card-bottom">

                                <div>

                                    <small>CARD HOLDER</small>

                                    <strong>
                                        LANKATRUST CLIENT
                                    </strong>

                                </div>


                                <div>

                                    <small>VALID THRU</small>

                                    <strong>
                                        09 / 30
                                    </strong>

                                </div>


                                <div class="card-type">
                                    LT
                                    <span>PREMIER</span>
                                </div>

                            </div>

                        </div>


                        <div class="floating-badge">

                            <span class="badge-icon">✓</span>

                            <div>
                                <small>SECURE BANKING</small>
                                <strong>Protected access</strong>
                            </div>

                        </div>


                        <div class="visual-caption">

                            <span></span>

                            Your finances.
                            <br>
                            Beautifully in view.

                        </div>

                    </div>

                </div>


                <div class="hero-bottom">

                    <div class="wrap hero-bottom-inner">

                        <div>
                            <span>01</span>
                            Everyday banking
                        </div>

                        <div>
                            <span>02</span>
                            Cards &amp; payments
                        </div>

                        <div>
                            <span>03</span>
                            Loans &amp; investments
                        </div>

                        <a href="#personal">
                            Explore LankaTrust ↓
                        </a>

                    </div>

                </div>

            </section>


            <!-- ==================================================
         PERSONAL BANKING
         ================================================== -->

            <section id="personal" class="section intro-section">

                <div class="wrap">

                    <div class="section-heading">

                        <div>

                            <p class="eyebrow">
                                PERSONAL BANKING
                            </p>

                            <h2>
                                Made for the way
                                <br>
                                you live today.
                            </h2>

                        </div>


                        <div class="heading-copy">

                            <p>
                                One considered banking experience for your
                                everyday money, future plans and everything
                                in between.
                            </p>

                            <a class="text-link" href="<%= request.getContextPath() %>/register.jsp">
                                Become a LankaTrust customer →
                            </a>

                        </div>

                    </div>


                    <div class="service-row">

                        <a href="#accounts">

                            <span class="service-number">
                                01
                            </span>

                            <div class="service-icon">
                                LT
                            </div>

                            <h3>
                                Accounts
                            </h3>

                            <p>
                                Keep your everyday finances organised
                                with a clear view of your accounts.
                            </p>

                            <strong>
                                Explore accounts →
                            </strong>

                        </a>


                        <a href="#cards">

                            <span class="service-number">
                                02
                            </span>

                            <div class="service-icon">
                                ◇
                            </div>

                            <h3>
                                Cards
                            </h3>

                            <p>
                                Request and manage your cards with
                                greater control and visibility.
                            </p>

                            <strong>
                                Explore cards →
                            </strong>

                        </a>


                        <a href="#loans">

                            <span class="service-number">
                                03
                            </span>

                            <div class="service-icon">
                                ↗
                            </div>

                            <h3>
                                Loans
                            </h3>

                            <p>
                                Take the next step with loan services
                                designed around your plans.
                            </p>

                            <strong>
                                Explore loans →
                            </strong>

                        </a>

                    </div>

                </div>

            </section>


            <!-- ==================================================
         ACCOUNTS
         ================================================== -->

            <section id="accounts" class="section cream-section">

                <div class="wrap split">

                    <div class="content-block">

                        <p class="eyebrow">
                            ACCOUNTS
                        </p>

                        <h2>
                            A strong start
                            <br>
                            begins here.
                        </h2>

                        <p>
                            Bring your everyday banking together.
                            View savings and current accounts,
                            review balances and follow transactions
                            through your personal banking portal.
                        </p>


                        <ul class="feature-list">

                            <li>
                                Savings and current account services
                            </li>

                            <li>
                                Clear balances and transaction history
                            </li>

                            <li>
                                Account requests you can follow online
                            </li>

                        </ul>


                        <a class="button" href="<%= request.getContextPath() %>/register.jsp">
                            Open an Account →
                        </a>

                    </div>


                    <div class="account-showcase">

                        <div class="showcase-decoration">
                            LT
                        </div>

                        <div class="showcase-content">

                            <p class="eyebrow">
                                LANKATRUST PERSONAL BANKING
                            </p>

                            <h3>
                                Small steps.
                                <br>
                                Meaningful progress.
                            </h3>

                            <p>
                                From your first account to your next
                                financial milestone, begin with a clear
                                view of where you stand.
                            </p>

                            <a class="text-link light-link" href="<%= request.getContextPath() %>/customer/accounts">
                                View your accounts →
                            </a>

                        </div>

                    </div>

                </div>

            </section>


            <!-- ==================================================
         CARDS
         ================================================== -->

            <section id="cards" class="section cards-section">

                <div class="wrap split">


                    <div class="card-stage">

                        <div class="mini-glow"></div>

                        <div class="secondary-card">

                            <span>
                                LankaTrust
                            </span>

                        </div>


                        <div class="display-card">

                            <div class="display-card-top">

                                <span>
                                    LankaTrust
                                </span>

                                <strong>
                                    LT
                                </strong>

                            </div>


                            <div class="display-chip"></div>


                            <div class="display-number">
                                •••• &nbsp; •••• &nbsp; •••• &nbsp; 1568
                            </div>


                            <div class="display-card-bottom">

                                <span>
                                    LANKATRUST CLIENT
                                </span>

                                <span>
                                    DEBIT
                                </span>

                            </div>

                        </div>

                    </div>


                    <div class="content-block">

                        <p class="eyebrow">
                            CARD SERVICES
                        </p>

                        <h2>
                            More control.
                            <br>
                            Less complication.
                        </h2>

                        <p>
                            Request a debit or credit card, track
                            approval and manage card settings through
                            your banking portal.
                        </p>


                        <a class="text-link" href="<%= request.getContextPath() %>/customer/cards">
                            Explore card services →
                        </a>


                        <p class="fine">
                            Card issuance in this platform is for
                            demonstration purposes.
                        </p>

                    </div>

                </div>

            </section>


            <!-- ==================================================
         LOANS
         ================================================== -->

            <section id="loans" class="section cream-section">

                <div class="wrap split">

                    <div class="content-block">

                        <p class="eyebrow">
                            LOANS
                        </p>

                        <h2>
                            For plans worth
                            <br>
                            moving towards.
                        </h2>

                        <p>
                            A fresh beginning. A personal milestone.
                            Apply online, follow the review process
                            and see approved terms before accepting
                            your loan.
                        </p>


                        <ul class="feature-list">

                            <li>
                                Application progress in one place
                            </li>

                            <li>
                                Terms available before acceptance
                            </li>

                            <li>
                                Installment and repayment tracking
                            </li>

                        </ul>


                        <a class="button" href="<%= request.getContextPath() %>/customer/loans">
                            View loan options →
                        </a>

                    </div>


                    <div class="image-frame">

                        <img src="<%= request.getContextPath() %>/assets/images/home.jpg" width="1200" height="800"
                            loading="lazy" alt="Modern home with a pool and outdoor living space">

                        <div class="image-label">

                            <small>
                                LANKATRUST LENDING
                            </small>

                            <strong>
                                Your next chapter,
                                thoughtfully financed.
                            </strong>

                        </div>

                    </div>

                </div>

            </section>


            <!-- ==================================================
         INVESTMENTS
         ================================================== -->

            <section id="investments" class="section investment-section">

                <div class="wrap split">


                    <div class="image-frame investment-image">

                        <img src="<%= request.getContextPath() %>/assets/images/city.jpg" width="1600" height="1067"
                            loading="lazy" alt="Modern office buildings viewed from below">

                        <div class="image-number">
                            04
                        </div>

                    </div>


                    <div class="content-block">

                        <p class="eyebrow">
                            INVESTMENTS
                        </p>

                        <h2>
                            Give tomorrow
                            <br>
                            some thought today.
                        </h2>

                        <p>
                            Explore investment services, review your
                            application terms and keep track of active
                            holdings, maturity dates and withdrawals.
                        </p>


                        <a class="text-link" href="<%= request.getContextPath() %>/customer/investments">
                            Explore investments →
                        </a>


                        <p class="fine">
                            Rates and calculations use configurable
                            demonstration terms. Review the terms shown
                            in your portal.
                        </p>

                    </div>

                </div>

            </section>


            <!-- ==================================================
         DIGITAL BANKING
         ================================================== -->

            <section id="digital" class="section digital-section">

                <div class="digital-glow"></div>

                <div class="wrap">

                    <div class="section-heading digital-heading">

                        <div>

                            <p class="eyebrow">
                                DIGITAL BANKING
                            </p>

                            <h2>
                                Your banking.
                                <br>
                                Within easy reach.
                            </h2>

                        </div>


                        <a class="button gold-button" href="<%= request.getContextPath() %>/login.jsp">
                            Access your banking →
                        </a>

                    </div>


                    <div class="feature-grid">

                        <div>

                            <span>
                                01 / TRANSFER
                            </span>

                            <h3>
                                Move money simply
                            </h3>

                            <p>
                                Transfer between active LankaTrust
                                accounts and review the result in
                                your history.
                            </p>

                        </div>


                        <div>

                            <span>
                                02 / PAY
                            </span>

                            <h3>
                                Keep payments organised
                            </h3>

                            <p>
                                Record bill payments and prepare
                                scheduled payments to execute when due.
                            </p>

                        </div>


                        <div>

                            <span>
                                03 / MANAGE
                            </span>

                            <h3>
                                Stay informed
                            </h3>

                            <p>
                                Review requests, manage your profile
                                and contact customer service.
                            </p>

                        </div>

                    </div>

                </div>

            </section>


            <!-- ==================================================
         WHY LANKATRUST
         ================================================== -->

            <section id="why" class="section why-section">

                <div class="wrap">

                    <div class="section-heading">

                        <div>

                            <p class="eyebrow">
                                WHY LANKATRUST
                            </p>

                            <h2>
                                Clarity comes first.
                            </h2>

                        </div>

                        <p>
                            A banking experience focused on useful
                            details, straightforward actions and a
                            clear view of your money.
                        </p>

                    </div>


                    <div class="feature-grid light-features">

                        <div>

                            <span>01</span>

                            <h3>
                                Everything in perspective
                            </h3>

                            <p>
                                Balances, transactions and service
                                requests together in your personal portal.
                            </p>

                        </div>


                        <div>

                            <span>02</span>

                            <h3>
                                You stay involved
                            </h3>

                            <p>
                                Follow applications and review approved
                                loan terms before accepting.
                            </p>

                        </div>


                        <div>

                            <span>03</span>

                            <h3>
                                A clear path to support
                            </h3>

                            <p>
                                Send a request, read responses and
                                follow conversations in one place.
                            </p>

                        </div>

                    </div>

                </div>

            </section>


            <!-- ==================================================
         SECURITY
         ================================================== -->

            <section id="security" class="section cream-section">

                <div class="wrap split">

                    <div class="content-block">

                        <p class="eyebrow">
                            SECURITY &amp; TRUST
                        </p>

                        <h2>
                            Care at
                            <br>
                            every step.
                        </h2>

                        <p>
                            Protection is part of how LankaTrust works,
                            from signing in to completing an account
                            operation.
                        </p>


                        <p class="security-tip">
                            Keep your password private. Sign out when
                            you finish, especially on a shared device.
                        </p>

                    </div>


                    <div class="security-rows">

                        <div>

                            <span>
                                01
                            </span>

                            <h3>
                                Protected sign-in
                            </h3>

                            <p>
                                Password hashing and session checks
                                support account access.
                            </p>

                        </div>


                        <div>

                            <span>
                                02
                            </span>

                            <h3>
                                Access with purpose
                            </h3>

                            <p>
                                Customer ownership checks and staff
                                role permissions protect operations.
                            </p>

                        </div>


                        <div>

                            <span>
                                03
                            </span>

                            <h3>
                                Consistent transactions
                            </h3>

                            <p>
                                Financial changes commit together or
                                roll back on failure.
                            </p>

                        </div>

                    </div>

                </div>

            </section>


            <!-- ==================================================
         ABOUT
         ================================================== -->

            <section id="about" class="section about-section">

                <div class="wrap split">

                    <div>

                        <p class="eyebrow">
                            ABOUT LANKATRUST
                        </p>

                        <h2>
                            A local perspective.
                            <br>
                            A considered experience.
                        </h2>

                    </div>


                    <div>

                        <p class="lead">
                            Created with Sri Lankan banking needs in mind,
                            LankaTrust brings everyday financial services
                            into a connected digital experience.
                        </p>

                        <p>
                            LankaTrust is a university web-based banking
                            project demonstrating customer services and
                            banking operations. It is not a licensed
                            commercial bank and does not provide
                            real-world banking services.
                        </p>

                    </div>

                </div>

            </section>


            <!-- ==================================================
         ACHIEVEMENTS
         ================================================== -->

            <section id="achievements" class="section achievements-section">

                <div class="wrap">

                    <p class="eyebrow">
                        PLATFORM ACHIEVEMENTS
                    </p>

                    <h2>
                        Thoughtfully built,
                        <br>
                        from the inside out.
                    </h2>


                    <div class="achievement-grid">

                        <div>
                            <span>01</span>
                            <h3>
                                Secure Banking Architecture
                            </h3>
                        </div>

                        <div>
                            <span>02</span>
                            <h3>
                                Modern Digital Banking Experience
                            </h3>
                        </div>

                        <div>
                            <span>03</span>
                            <h3>
                                Customer-Centred Design
                            </h3>
                        </div>

                        <div>
                            <span>04</span>
                            <h3>
                                Role-Based Banking Operations
                            </h3>
                        </div>

                    </div>


                    <p class="fine">
                        Project capabilities and design milestones.
                        These are not external awards or certifications.
                    </p>

                </div>

            </section>


            <!-- ==================================================
         CONTACT
         ================================================== -->

            <section id="contact" class="section contact-section">

                <div class="wrap split">

                    <div class="content-block">

                        <p class="eyebrow">
                            CUSTOMER SUPPORT
                        </p>

                        <h2>
                            Here when
                            <br>
                            you need us.
                        </h2>

                        <p>
                            Sign in to send a service request or open
                            a support ticket. Your messages and responses
                            stay together in your portal.
                        </p>


                        <a class="button" href="<%= request.getContextPath() %>/customer/requests">
                            Contact customer service →
                        </a>

                    </div>


                    <div class="contact-options">

                        <p class="eyebrow">
                            HELP CENTRE
                        </p>

                        <h3>
                            How can we help?
                        </h3>


                        <details>

                            <summary>
                                How do I get started?
                            </summary>

                            <p>
                                Select Open an Account, complete
                                registration, then sign in to your
                                customer portal.
                            </p>

                        </details>


                        <details>

                            <summary>
                                Where can I follow an application?
                            </summary>

                            <p>
                                Your Cards, Loans and Investments pages
                                show the status of your requests.
                            </p>

                        </details>


                        <details>

                            <summary>
                                How do scheduled payments work?
                            </summary>

                            <p>
                                Create a payment with a future date.
                                Return to Payments on or after that date
                                to execute it. Payments do not run
                                automatically.
                            </p>

                        </details>

                    </div>

                </div>

            </section>

        </main>


        <!-- ======================================================
     FOOTER
     ====================================================== -->

        <footer>

            <div class="wrap footer-grid">


                <div class="footer-brand">

                    <a class="wordmark" href="#home">

                        <span class="brand-symbol" aria-hidden="true">
                            <span>LT</span>
                        </span>

                        <span class="brand-copy">
                            <strong>LankaTrust</strong>
                            <small>PRIVATE BANKING</small>
                        </span>

                    </a>

                    <p>
                        Trust in every transaction.
                    </p>

                </div>


                <div>

                    <h3>
                        PERSONAL BANKING
                    </h3>

                    <a href="#accounts">Accounts</a>
                    <a href="#cards">Cards</a>
                    <a href="#loans">Loans</a>
                    <a href="#investments">Investments</a>

                </div>


                <div>

                    <h3>
                        HERE TO HELP
                    </h3>

                    <a href="#security">
                        Security &amp; trust
                    </a>

                    <a href="#contact">
                        Customer support
                    </a>

                    <a href="#about">
                        About LankaTrust
                    </a>

                </div>


                <div>

                    <h3>
                        YOUR BANKING
                    </h3>

                    <a href="<%= request.getContextPath() %>/login.jsp">
                        Login
                    </a>

                    <a href="<%= request.getContextPath() %>/register.jsp">
                        Open an Account
                    </a>

                </div>

            </div>


            <div class="wrap footer-bottom">

                <span>
                    &copy; <%= java.time.Year.now() %>
                        LankaTrust. University banking project.
                </span>

                <span>
                    Demonstration platform · No real-world settlement
                </span>

            </div>

        </footer>

    </body>

    </html>