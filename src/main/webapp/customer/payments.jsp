<%@ page import="com.banking.util.Input" %>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
        <%@ page import="com.banking.model.Account" %>

            <% String userName=(String) session.getAttribute("userName"); String userEmail=(String)
                session.getAttribute("userEmail"); if (userName==null || userName.trim().isEmpty()) {
                userName="Customer" ; } if (userEmail==null) { userEmail="" ; } String firstLetter=userName.substring(0,
                1) .toUpperCase(); Account account=(Account) request.getAttribute("account"); String success=(String)
                request.getAttribute("success"); String error=(String) request.getAttribute("error"); %>

                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <script src="<%= request.getContextPath() %>/assets/js/theme-init.js"></script>

                    <meta charset="UTF-8">

                    <meta name="viewport" content="width=device-width, initial-scale=1.0">

                    <title>Payments | LankaTrust Bank</title>







                    <style>
                        * {
                            box-sizing: border-box;
                            margin: 0;
                            padding: 0;
                        }

                        body {
                            font-family: "Inter", Arial, sans-serif;
                            background: #f4f5f7;
                            color: #18202a;
                        }

                        .app-layout {
                            min-height: 100vh;
                            display: flex;
                        }

                        .sidebar {
                            width: 255px;
                            background: #111820;
                            color: white;
                            padding: 28px 20px;
                            display: flex;
                            flex-direction: column;
                            min-height: 100vh;
                        }

                        .brand {
                            display: flex;
                            align-items: center;
                            gap: 12px;
                            margin-bottom: 38px;
                        }

                        .logo {
                            width: 42px;
                            height: 42px;
                            border-radius: 8px;
                            background: #c8a15a;
                            color: #111820;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-size: 22px;
                            font-weight: 700;
                        }

                        .brand strong {
                            display: block;
                            letter-spacing: 2px;
                        }

                        .brand span {
                            display: block;
                            font-size: 10px;
                            color: #89939f;
                            margin-top: 3px;
                            letter-spacing: 1px;
                        }

                        .nav-menu {
                            display: flex;
                            flex-direction: column;
                            gap: 6px;
                        }

                        .nav-item {
                            color: #aeb7c1;
                            text-decoration: none;
                            padding: 13px 14px;
                            border-radius: 7px;
                            display: flex;
                            align-items: center;
                            gap: 13px;
                            font-size: 14px;
                        }

                        .nav-item:hover {
                            background: #1d2731;
                            color: white;
                        }

                        .nav-item.active {
                            background: #c8a15a;
                            color: #111820;
                            font-weight: 600;
                        }

                        .nav-icon {
                            width: 20px;
                            text-align: center;
                            font-size: 17px;
                        }

                        .sidebar-bottom {
                            margin-top: auto;
                        }

                        .logout {
                            color: #d1d5da;
                        }

                        .main-content {
                            flex: 1;
                            min-width: 0;
                        }

                        .topbar {
                            background: white;
                            border-bottom: 1px solid #e4e6e8;
                            padding: 22px 34px;
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                        }

                        .topbar-label {
                            color: #b28c49;
                            font-size: 11px;
                            letter-spacing: 1.5px;
                            font-weight: 700;
                        }

                        .topbar h1 {
                            margin-top: 5px;
                            font-size: 23px;
                        }

                        .profile {
                            display: flex;
                            align-items: center;
                            gap: 11px;
                        }

                        .avatar {
                            width: 40px;
                            height: 40px;
                            border-radius: 50%;
                            background: #111820;
                            color: white;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            font-weight: 700;
                        }

                        .profile strong {
                            display: block;
                            font-size: 13px;
                        }

                        .profile span {
                            display: block;
                            color: #7c858f;
                            font-size: 11px;
                            margin-top: 3px;
                        }

                        .content {
                            padding: 38px;
                            max-width: 1150px;
                            margin: 0 auto;
                        }

                        .page-heading span {
                            color: #b28c49;
                            font-size: 11px;
                            letter-spacing: 1.5px;
                            font-weight: 700;
                        }

                        .page-heading h2 {
                            font-size: 29px;
                            margin-top: 7px;
                        }

                        .page-heading p {
                            color: #717982;
                            margin-top: 8px;
                            line-height: 1.6;
                        }

                        .payment-layout {
                            margin-top: 28px;
                            display: grid;
                            grid-template-columns: 1fr 340px;
                            gap: 24px;
                            align-items: start;
                        }

                        .panel {
                            background: white;
                            border: 1px solid #e2e5e8;
                            border-radius: 12px;
                            padding: 28px;
                            box-shadow: 0 3px 15px rgba(0, 0, 0, 0.03);
                        }

                        .panel-label {
                            color: #b28c49;
                            font-size: 11px;
                            font-weight: 700;
                            letter-spacing: 1.3px;
                        }

                        .panel h3 {
                            font-size: 20px;
                            margin-top: 6px;
                            margin-bottom: 24px;
                        }

                        .form-group {
                            margin-bottom: 20px;
                        }

                        .form-group label {
                            display: block;
                            font-size: 13px;
                            font-weight: 600;
                            margin-bottom: 8px;
                        }

                        .form-group input {
                            width: 100%;
                            height: 48px;
                            border: 1px solid #d8dde2;
                            border-radius: 7px;
                            padding: 0 14px;
                            font-family: inherit;
                            font-size: 14px;
                            outline: none;
                        }

                        .form-group input:focus {
                            border-color: #b28c49;
                            box-shadow: 0 0 0 3px rgba(178, 140, 73, 0.1);
                        }

                        .pay-button {
                            width: 100%;
                            border: none;
                            height: 50px;
                            border-radius: 7px;
                            background: #111820;
                            color: white;
                            font-family: inherit;
                            font-size: 14px;
                            font-weight: 600;
                            cursor: pointer;
                        }

                        .pay-button:hover {
                            background: #202c37;
                        }

                        .account-box {
                            background: #111820;
                            color: white;
                            border-radius: 10px;
                            padding: 24px;
                        }

                        .account-box .label {
                            color: #b8c0c8;
                            font-size: 10px;
                            letter-spacing: 1.3px;
                        }

                        .account-box h3 {
                            margin: 7px 0 20px;
                            font-size: 19px;
                        }

                        .account-row {
                            padding: 13px 0;
                            border-top: 1px solid #2c3640;
                        }

                        .account-row span {
                            display: block;
                            color: #8f9aa5;
                            font-size: 11px;
                        }

                        .account-row strong {
                            display: block;
                            margin-top: 5px;
                            font-size: 14px;
                        }

                        .balance {
                            color: #d3b06b;
                            font-size: 23px !important;
                        }

                        .message {
                            margin-bottom: 20px;
                            border-radius: 7px;
                            padding: 14px 16px;
                            font-size: 13px;
                            line-height: 1.5;
                        }

                        .success {
                            background: #edf8f1;
                            border: 1px solid #b9dfc7;
                            color: #216b3b;
                        }

                        .error {
                            background: #fff1f1;
                            border: 1px solid #efc0c0;
                            color: #a02d2d;
                        }

                        .security {
                            margin-top: 20px;
                            color: #7b838c;
                            font-size: 12px;
                            line-height: 1.6;
                        }

                        @media (max-width: 900px) {

                            .sidebar {
                                width: 210px;
                            }

                            .payment-layout {
                                grid-template-columns: 1fr;
                            }
                        }
                    </style>

                    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/customer-navigation.css">
                    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/services.css">
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

                        <jsp:include page="/WEB-INF/fragments/customer-sidebar.jsp">
                            <jsp:param name="active" value="payments" />
                        </jsp:include>


                        <main class="main-content">

                            <header class="topbar">

                                <div>

                                    <span class="topbar-label">
                                        PERSONAL BANKING
                                    </span>

                                    <h1>
                                        Payments
                                    </h1>

                                </div>


                                <div class="profile">

                                    <div class="avatar">
                                        <%= Input.html(firstLetter) %>
                                    </div>

                                    <div>

                                        <strong>
                                            <%= Input.html(userName) %>
                                        </strong>

                                        <span>
                                            <%= Input.html(userEmail) %>
                                        </span>

                                    </div>

                                </div>

                            <jsp:include page="/WEB-INF/fragments/notification-bell.jsp" /></header>


                            <section class="content">

                                <div class="page-heading">

                                    <span>
                                        BILL PAYMENTS
                                    </span>

                                    <h2>
                                        Pay your bills securely
                                    </h2>

                                    <p>
                                        Make payments directly from your
                                        LankaTrust banking account.
                                    </p>

                                </div>


                                <div class="payment-layout">

                                    <div class="panel">

                                        <span class="panel-label">
                                            NEW PAYMENT
                                        </span>

                                        <h3>
                                            Payment details
                                        </h3>


                                        <% if (success !=null) { %>

                                            <div class="message success">
                                                <%= Input.html(success) %>
                                            </div>

                                            <% } %>


                                                <% if (error !=null) { %>

                                                    <div class="message error">
                                                        <%= Input.html(error) %>
                                                    </div>

                                                    <% } %>


                                                        <form method="post"
                                                            action="<%= request.getContextPath() %>/customer/payments">
                                                            <input type="hidden" name="csrf"
                                                                value="<%= session.getAttribute("csrf") %>">

                                                            <div class="form-group">

                                                                <label for="recipient">
                                                                    Service Provider
                                                                </label>

                                                                <input type="text" id="recipient" name="recipient"
                                                                    placeholder="Example: Dialog, CEB, Water Board"
                                                                    maxlength="150" required>

                                                            </div>


                                                            <div class="form-group">

                                                                <label for="billReference">Bill / Consumer Account Number</label>
                                                                <input type="text" id="billReference" name="billReference" maxlength="100"
                                                                    placeholder="Enter consumer or bill account number" required
                                                                    value="<%= Input.html(request.getParameter("billReference")) %>">

                                                                <label for="amount">
                                                                    Payment Amount (LKR)
                                                                </label>

                                                                <input type="number" id="amount" name="amount"
                                                                    placeholder="0.00" min="0.01" step="0.01" required>

                                                            </div>


                                                            <button type="submit" class="pay-button">

                                                                Pay Bill

                                                            </button>

                                                        </form>


                                                        <div class="security">

                                                            Your payment is processed securely.
                                                            The payment amount will be deducted
                                                            from your active banking account only
                                                            after all validations are completed.

                                                        </div>

                                    </div>


                                    <div class="account-box">

                                        <span class="label">
                                            PAYMENT ACCOUNT
                                        </span>

                                        <h3>
                                            Your Account
                                        </h3>


                                        <% if (account !=null) { %>

                                            <div class="account-row">

                                                <span>
                                                    Account Type
                                                </span>

                                                <strong>
                                                    <%= account.getAccountType() %>
                                                </strong>

                                            </div>


                                            <div class="account-row">

                                                <span>
                                                    Account Number
                                                </span>

                                                <strong>
                                                    <%= account.getAccountNumber() %>
                                                </strong>

                                            </div>


                                            <div class="account-row">

                                                <span>
                                                    Available Balance
                                                </span>

                                                <strong class="balance">

                                                    LKR

                                                    <%= account.getBalance() !=null ? String.format( "%,.2f" ,
                                                        account.getBalance()) : "0.00" %>

                                                </strong>

                                            </div>


                                            <div class="account-row">

                                                <span>
                                                    Account Status
                                                </span>

                                                <strong>
                                                    <%= account.getStatus() %>
                                                </strong>

                                            </div>

                                            <% } else { %>

                                                <div class="account-row">

                                                    <strong>
                                                        No active banking account found.
                                                    </strong>

                                                </div>

                                                <% } %>

                                    </div>

                                </div>

                                <jsp:include page="/WEB-INF/fragments/scheduled-payments.jsp" />
                            </section>

                        </main>

                    </div>

                </body>

                </html>