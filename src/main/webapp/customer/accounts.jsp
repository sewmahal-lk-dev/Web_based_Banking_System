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

                            <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/accounts.css?v=3">

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

                                    <section class="content">
                                        <p><a href="<%= request.getContextPath() %>/customer/requests">Request an
                                                additional account or account closure</a></p>

                                        <div class="page-heading">

                                            <div>

                                                <span class="small-gold">
                                                    ACCOUNTS
                                                </span>

                                                <h2>
                                                    Your banking accounts
                                                </h2>

                                                <p>
                                                    View your account balances,
                                                    account types and current status.
                                                </p>

                                            </div>

                                        </div>


                                        <% if (accounts==null || accounts.isEmpty()) { %>


                                            <!-- ===================================== -->
                                            <!-- NO ACCOUNTS -->
                                            <!-- ===================================== -->

                                            <div class="empty-state">

                                                <div class="empty-icon">
                                                    A
                                                </div>

                                                <h2>
                                                    No accounts found
                                                </h2>

                                                <p>
                                                    There are currently no banking accounts
                                                    linked to your customer profile.
                                                </p>

                                            </div>


                                            <% } else { for (Account account : accounts) { String
                                                accountNumber=String.valueOf( account.getAccountNumber() ); String
                                                maskedNumber=accountNumber; if (accountNumber.length()> 4) {

                                                maskedNumber =
                                                "•••• "
                                                + accountNumber.substring(
                                                accountNumber.length() - 4
                                                );
                                                }

                                                String accountType =
                                                account.getAccountType();

                                                if (accountType == null) {
                                                accountType = "ACCOUNT";
                                                }

                                                String status =
                                                account.getStatus();

                                                if (status == null) {
                                                status = "UNKNOWN";
                                                }
                                                %>


                                                <!-- ===================================== -->
                                                <!-- ACCOUNT CARD -->
                                                <!-- ===================================== -->

                                                <div class="account-card">

                                                    <div class="account-card-top">

                                                        <div>

                                                            <span class="account-label">
                                                                <%= accountType %> ACCOUNT
                                                            </span>

                                                            <h3>
                                                                <%= accountType %>
                                                            </h3>

                                                            <div class="account-number">

                                                                Account •
                                                                <%= maskedNumber %>

                                                            </div>

                                                        </div>


                                                        <div class="account-status
                        <%= " ACTIVE".equalsIgnoreCase(status) ? "status-active" : "status-other" %>">

                                                            <span class="status-dot"></span>

                                                            <%= status %>

                                                        </div>

                                                    </div>


                                                    <!-- BALANCE -->

                                                    <div class="balance-section">

                                                        <span class="balance-title">
                                                            AVAILABLE BALANCE
                                                        </span>


                                                        <div class="balance">

                                                            <span class="currency">
                                                                LKR
                                                            </span>

                                                            <span class="amount">

                                                                <%= account.getBalance() !=null ? String.format( "%,.2f"
                                                                    , account.getBalance() ) : "0.00" %>

                                                            </span>

                                                        </div>

                                                    </div>


                                                    <!-- ACCOUNT DETAILS -->

                                                    <div class="account-card-bottom">

                                                        <div class="account-detail">

                                                            <span>
                                                                Account type
                                                            </span>

                                                            <strong>
                                                                <%= accountType %>
                                                            </strong>

                                                        </div>


                                                        <div class="account-detail">

                                                            <span>
                                                                Open date
                                                            </span>

                                                            <strong>

                                                                <%= account.getOpenDate() !=null ?
                                                                    account.getOpenDate().toString() : "-" %>

                                                            </strong>

                                                        </div>


                                                        <div class="account-detail">

                                                            <span>
                                                                Account number
                                                            </span>

                                                            <strong>
                                                                <%= account.getAccountNumber() %>
                                                            </strong>

                                                        </div>

                                                    </div>

                                                </div>


                                                <% } } %>


                                                    <!-- ===================================== -->
                                                    <!-- SECURITY MESSAGE -->
                                                    <!-- ===================================== -->

                                                    <div class="security-message">

                                                        <span>
                                                            ◆
                                                        </span>

                                                        Your account information is securely retrieved
                                                        from LankaTrust Digital Banking.

                                                    </div>

                                    </section>

                                </main>

                            </div>

                        </body>

                        </html>