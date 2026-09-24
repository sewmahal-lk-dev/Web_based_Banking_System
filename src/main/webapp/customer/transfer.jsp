<%@ page import="com.banking.util.Input" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page import="com.banking.model.Account" %>

        <% String userName=(String) session.getAttribute("userName"); String userEmail=(String)
            session.getAttribute("userEmail"); if (userName==null || userName.trim().isEmpty()) { userName="Customer" ;
            } if (userEmail==null) { userEmail="" ; } String firstLetter=userName.substring(0, 1).toUpperCase(); Account
            account=(Account) request.getAttribute("account"); String error=(String) request.getAttribute("error");
            String success=(String) request.getAttribute("success"); %>

            <!DOCTYPE html>
            <html lang="en">

            <head>
<script src="<%= request.getContextPath() %>/assets/js/theme-init.js"></script>

                <meta charset="UTF-8">

                <meta name="viewport" content="width=device-width, initial-scale=1.0">

                <title>Transfer Money | LankaTrust Bank</title>

                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }

                    body {
                        font-family: Arial, Helvetica, sans-serif;
                        background: #f4f6f8;
                        color: #102133;
                    }

                    .layout {
                        min-height: 100vh;
                        display: flex;
                    }

                    .sidebar {
                        width: 260px;
                        min-height: 100vh;
                        background: #061b2d;
                        color: white;
                        padding: 30px 22px;
                        position: fixed;
                        left: 0;
                        top: 0;
                        bottom: 0;
                    }

                    .brand {
                        display: flex;
                        align-items: center;
                        gap: 15px;
                        margin: 0 10px 35px;
                    }

                    .logo {
                        width: 43px;
                        height: 43px;
                        border: 1px solid #d7aa55;
                        border-radius: 11px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: #f0c76c;
                        font-family: Georgia, serif;
                        font-size: 26px;
                    }

                    .brand h2 {
                        font-size: 15px;
                        letter-spacing: 4px;
                    }

                    .brand span {
                        display: block;
                        color: #6f9ab8;
                        font-size: 7px;
                        letter-spacing: 2px;
                        margin-top: 4px;
                    }

                    .nav {
                        display: flex;
                        flex-direction: column;
                        gap: 7px;
                    }

                    .nav a {
                        color: #91bedc;
                        text-decoration: none;
                        padding: 15px 17px;
                        border-radius: 8px;
                        font-size: 13px;
                    }

                    .nav a:hover {
                        background: rgba(255, 255, 255, 0.05);
                        color: white;
                    }

                    .nav .active {
                        background: rgba(255, 255, 255, 0.07);
                        color: white;
                        border-left: 2px solid #d7aa55;
                    }

                    .main {
                        margin-left: 260px;
                        width: calc(100% - 260px);
                    }

                    .topbar {
                        min-height: 100px;
                        background: white;
                        border-bottom: 1px solid #dfe5ea;
                        padding: 25px 44px;
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                    }

                    .label {
                        color: #d69b38;
                        font-size: 9px;
                        font-weight: bold;
                        letter-spacing: 3px;
                        margin-bottom: 8px;
                    }

                    h1,
                    h2 {
                        font-family: Georgia, serif;
                        font-weight: normal;
                    }

                    .profile {
                        display: flex;
                        align-items: center;
                        gap: 12px;
                    }

                    .avatar {
                        width: 40px;
                        height: 40px;
                        border-radius: 50%;
                        background: #061b2d;
                        color: #f0c76c;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-family: Georgia, serif;
                    }

                    .profile strong {
                        display: block;
                        font-size: 12px;
                    }

                    .profile span {
                        display: block;
                        font-size: 9px;
                        color: #8b99a7;
                        margin-top: 4px;
                    }

                    .content {
                        padding: 40px 44px;
                        max-width: 1050px;
                    }

                    .heading {
                        margin-bottom: 28px;
                    }

                    .heading h2 {
                        font-size: 30px;
                        margin-bottom: 8px;
                    }

                    .heading p {
                        color: #748091;
                        font-size: 14px;
                    }

                    .transfer-grid {
                        display: grid;
                        grid-template-columns: 1fr 1.3fr;
                        gap: 24px;
                    }

                    .account-card {
                        background: #073b57;
                        color: white;
                        border-radius: 16px;
                        padding: 30px;
                        min-height: 260px;
                    }

                    .account-card .small {
                        color: #78b4d3;
                        font-size: 8px;
                        letter-spacing: 2px;
                    }

                    .account-card h3 {
                        font-family: Georgia, serif;
                        font-size: 23px;
                        margin-top: 12px;
                    }

                    .balance-label {
                        margin-top: 45px;
                        color: #78b4d3;
                        font-size: 8px;
                        letter-spacing: 2px;
                    }

                    .balance {
                        margin-top: 10px;
                        font-family: Georgia, serif;
                        font-size: 38px;
                    }

                    .balance span {
                        font-family: Arial, sans-serif;
                        color: #f0c76c;
                        font-size: 13px;
                        margin-right: 10px;
                    }

                    .form-card {
                        background: white;
                        border: 1px solid #dfe5ea;
                        border-radius: 16px;
                        padding: 32px;
                    }

                    .form-card h2 {
                        font-size: 24px;
                        margin-bottom: 7px;
                    }

                    .form-card>p {
                        color: #748091;
                        font-size: 12px;
                        margin-bottom: 25px;
                    }

                    .field {
                        margin-bottom: 20px;
                    }

                    .field label {
                        display: block;
                        font-size: 10px;
                        font-weight: bold;
                        margin-bottom: 8px;
                        letter-spacing: 1px;
                    }

                    .field input {
                        width: 100%;
                        padding: 14px 15px;
                        border: 1px solid #d5dde3;
                        border-radius: 8px;
                        outline: none;
                        font-size: 14px;
                    }

                    .field input:focus {
                        border-color: #d7aa55;
                    }

                    .transfer-button {
                        width: 100%;
                        padding: 15px;
                        border: 0;
                        border-radius: 8px;
                        background: #061b2d;
                        color: #f0c76c;
                        font-weight: bold;
                        cursor: pointer;
                    }

                    .transfer-button:hover {
                        background: #0b2942;
                    }

                    .message {
                        padding: 13px 15px;
                        border-radius: 8px;
                        margin-bottom: 20px;
                        font-size: 12px;
                    }

                    .error {
                        background: #fff0f0;
                        border: 1px solid #efc1c1;
                        color: #a52a2a;
                    }

                    .success {
                        background: #edf9f4;
                        border: 1px solid #b9e5d2;
                        color: #16724e;
                    }

                    .warning {
                        margin-top: 20px;
                        font-size: 10px;
                        color: #8a97a3;
                        line-height: 1.6;
                    }
                </style>

            <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/customer-navigation.css">
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

                <div class="layout">

                    <jsp:include page="/WEB-INF/fragments/customer-sidebar.jsp"><jsp:param name="active" value="transfer" /></jsp:include>


                    <main class="main">

                        <header class="topbar">

                            <div>

                                <div class="label">
                                    PERSONAL BANKING
                                </div>

                                <h1>
                                    Transfer Money
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

                            <div class="heading">

                                <div class="label">
                                    MONEY TRANSFER
                                </div>

                                <h2>
                                    Send money securely
                                </h2>

                                <p>
                                    Transfer funds to another active LankaTrust bank account.
                                </p>

                            </div>


                            <div class="transfer-grid">

                                <div class="account-card">

                                    <div class="small">
                                        FROM ACCOUNT
                                    </div>


                                    <% if (account !=null) { %>

                                        <h3>
                                            <%= account.getAccountType() %>
                                        </h3>

                                        <p style="margin-top:8px;color:#8fc0d8;font-size:11px;">
                                            Account <%= account.getAccountNumber() %>
                                        </p>


                                        <div class="balance-label">
                                            AVAILABLE BALANCE
                                        </div>

                                        <div class="balance">

                                            <span>
                                                LKR
                                            </span>

                                            <%= account.getBalance() !=null ? String.format( "%,.2f" ,
                                                account.getBalance()) : "0.00" %>

                                        </div>

                                        <% } else { %>

                                            <h3>
                                                No Active Account
                                            </h3>

                                            <p style="margin-top:15px;color:#8fc0d8;">
                                                You need an active bank account
                                                before making a transfer.
                                            </p>

                                            <% } %>

                                </div>


                                <div class="form-card">

                                    <h2>
                                        Transfer details
                                    </h2>

                                    <p>
                                        Enter the receiver's LankaTrust account number
                                        and the amount you want to send.
                                    </p>


                                    <% if (error !=null) { %>

                                        <div class="message error">
                                            <%= Input.html(error) %>
                                        </div>

                                        <% } %>


                                            <% if (success !=null) { %>

                                                <div class="message success" role="status">
                                                    <%= Input.html(success) %>
                                                    <% com.banking.model.TransferReceipt receipt = (com.banking.model.TransferReceipt) request.getAttribute("transferReceipt");
                                                       if (receipt != null) { %>
                                                    <p><a class="workspace-button" href="<%= request.getContextPath() %>/customer/transfer/receipt?reference=<%= java.net.URLEncoder.encode(receipt.reference(), java.nio.charset.StandardCharsets.UTF_8) %>">Download Receipt</a></p>
                                                    <% } else if (Boolean.TRUE.equals(request.getAttribute("receiptUnavailable"))) { %>
                                                    <p>Your receipt is temporarily unavailable. Download it later from transaction history.</p>
                                                    <% } %>
                                                </div>

                                                <% } %>


                                                    <form method="post"
                                                        action="<%= request.getContextPath() %>/customer/transfer">
<input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>">


                                                        <div class="field">

                                                            <label>
                                                                RECEIVER ACCOUNT NUMBER
                                                            </label>

                                                            <input type="number" name="receiverAccount"
                                                                placeholder="Example: 1000000001" required>

                                                        </div>


                                                        <div class="field">

                                                            <label>
                                                                AMOUNT (LKR)
                                                            </label>

                                                            <input type="number" name="amount" min="0.01" step="0.01"
                                                                placeholder="0.00" required>

                                                        </div>


                                                        <button class="transfer-button" type="submit" <%=account==null
                                                            ? "disabled" : "" %>>

                                                            Transfer Money

                                                        </button>

                                                    </form>


                                                    <div class="warning">
                                                        Please verify the receiver account number before
                                                        confirming your transfer. Completed transfers
                                                        immediately update both account balances.
                                                    </div>

                                </div>

                            </div>

                        </section>

                    </main>

                </div>

            </body>

            </html>