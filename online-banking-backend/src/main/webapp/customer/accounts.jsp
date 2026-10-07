<%@ page import="com.banking.util.Input" %>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
        <%@ page import="java.util.List" %>
            <%@ page import="com.banking.model.Account" %>

                <% String userName=(String) session.getAttribute("userName"); String userEmail=(String)
                    session.getAttribute("userEmail"); if (userName==null || userName.trim().isEmpty()) {
                    userName="Customer" ; } if (userEmail==null) { userEmail="" ; } String
                    firstLetter=userName.substring(0, 1).toUpperCase(); List<Account> accounts =
                    (List<Account>) request.getAttribute("accounts");
                        %>

                        <!DOCTYPE html>
                        <html lang="en">

                        <head>
                            <script src="<%= request.getContextPath() %>/assets/js/theme-init.js"></script>

                            <meta charset="UTF-8">

                            <meta name="viewport" content="width=device-width, initial-scale=1.0">

                            <title>My Accounts | LankaTrust Bank</title>

                            <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/accounts.css?v=4">

                            <link rel="stylesheet"
                                href="<%= request.getContextPath() %>/assets/css/customer-navigation.css">
                            <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/lankatrust.css">
                            <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/banking-system.css">
                            <script defer src="<%= request.getContextPath() %>/assets/js/banking-ui.js"></script>
                        <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/notifications.css">
<script defer src="<%= request.getContextPath() %>/assets/js/notifications.js"></script>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/design-system.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/workspaces.css">
<script defer src="<%= request.getContextPath() %>/assets/js/workspaces.js"></script>
</head>

                        <body class="customer-portal">

                            <div class="app-layout">

                                <!-- ===================================== -->
                                <!-- SIDEBAR -->
                                <!-- ===================================== -->

                                <jsp:include page="/WEB-INF/fragments/customer-sidebar.jsp">
                                    <jsp:param name="active" value="accounts" />
                                </jsp:include>


                                <!-- ===================================== -->
                                <!-- MAIN AREA -->
                                <!-- ===================================== -->

                                <main class="main-area">


                                    <!-- TOP BAR -->

                                    <header class="topbar">

                                        <div>

                                            <span class="section-label">
                                                PERSONAL BANKING
                                            </span>

                                            <h1>
                                                My Accounts
                                            </h1>

                                        </div>


                                        <div class="user-area">

                                            


                                            <div class="avatar">
                                                <%= Input.html(firstLetter) %>
                                            </div>


                                            <div class="user-info">

                                                <strong>
                                                    <%= Input.html(userName) %>
                                                </strong>

                                                <span>
                                                    <%= Input.html(userEmail) %>
                                                </span>

                                            </div>

                                        </div>

                                    <jsp:include page="/WEB-INF/fragments/notification-bell.jsp" /></header>


                                    <!-- ===================================== -->
                                    <!-- CONTENT -->
                                    <!-- ===================================== -->

                                    <section class="content accounts-page" aria-labelledby="accounts-heading">
                                        <div class="accounts-heading">
                                            <div>
                                                <span class="accounts-eyebrow">YOUR BANKING ACCOUNTS</span>
                                                <h2 id="accounts-heading">My Accounts</h2>
                                                <p>View your account balances, account types and current status.</p>
                                            </div>
                                            <a class="accounts-service-button" href="<%= request.getContextPath() %>/customer/requests">
                                                <svg class="ui-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 4h16v13H9l-5 4V4m4 5h8m-8 4h5" /></svg>
                                                Request Account Service
                                            </a>
                                        </div>

                                        <% String[] nameParts = userName.trim().split("\\s+");
                                           String initials = new String(Character.toChars(nameParts[0].codePointAt(0)));
                                           if (nameParts.length > 1) {
                                               initials += new String(Character.toChars(nameParts[nameParts.length - 1].codePointAt(0)));
                                           }
                                           initials = initials.toUpperCase(java.util.Locale.ROOT);
                                           Object customerId = session.getAttribute("userId"); %>
                                        <div class="accounts-layout">
                                            <aside class="accounts-profile" aria-labelledby="holder-heading">
                                                <span class="accounts-eyebrow">CUSTOMER PROFILE</span>
                                                <div class="accounts-initials" aria-hidden="true"><%= Input.html(initials) %></div>
                                                <h3 id="holder-heading"><%= Input.html(userName) %></h3>
                                                <p class="accounts-holder-label">Account holder</p>
                                                <dl class="accounts-profile-details">
                                                    <% if (!userEmail.trim().isEmpty()) { %>
                                                    <div><dt>Email</dt><dd><%= Input.html(userEmail) %></dd></div>
                                                    <% } if (customerId != null) { %>
                                                    <div><dt>Customer ID</dt><dd><%= Input.html(String.valueOf(customerId)) %></dd></div>
                                                    <% } %>
                                                </dl>
                                            </aside>
                                            <div class="accounts-overviews" aria-label="Account overview">
                                                <% if (accounts == null || accounts.isEmpty()) { %>
                                                <div class="empty-state">
                                                    <h3>No accounts found</h3>
                                                    <p>There are currently no banking accounts linked to your customer profile.</p>
                                                </div>
                                                <% } else { for (Account account : accounts) {
                                                    String accountNumber = String.valueOf(account.getAccountNumber());
                                                    String accountType = account.getAccountType() != null ? account.getAccountType() : "ACCOUNT";
                                                    String status = account.getStatus() != null ? account.getStatus() : "UNKNOWN";
                                                %>
                                                <article class="accounts-overview">
                                                    <div class="accounts-card-heading">
                                                        <div>
                                                            <span class="accounts-eyebrow">ACCOUNT OVERVIEW</span>
                                                            <h3><%= Input.html(accountType) %> ACCOUNT</h3>
                                                        </div>
                                                        <span class="accounts-status <%= "ACTIVE".equalsIgnoreCase(status) ? "is-active" : "" %>">
                                                            <span aria-hidden="true"></span><%= Input.html(status) %>
                                                        </span>
                                                    </div>
                                                    <div class="accounts-balance">
                                                        <span class="accounts-eyebrow">AVAILABLE BALANCE</span>
                                                        <div><span class="accounts-currency">LKR</span>
                                                            <strong><%= account.getBalance() != null ? String.format("%,.2f", account.getBalance()) : "0.00" %></strong>
                                                        </div>
                                                    </div>
                                                    <h4 class="accounts-details-heading">Account information</h4>
                                                    <dl class="accounts-details">
                                                        <div><dt>Account type</dt><dd><%= Input.html(accountType) %></dd></div>
                                                        <div><dt>Account number</dt><dd><%= Input.html(accountNumber) %></dd></div>
                                                        <div><dt>Open date</dt><dd><%= account.getOpenDate() != null ? Input.html(account.getOpenDate().toString()) : "-" %></dd></div>
                                                        <div><dt>Status</dt><dd><%= Input.html(status) %></dd></div>
                                                    </dl>
                                                </article>
                                                <% } } %>
                                            </div>
                                        </div>
                                        <p class="accounts-security">Your account information is securely retrieved from LankaTrust Digital Banking.</p>
                                    </section>

                                </main>

                            </div>

                        </body>

                        </html>