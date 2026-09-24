<%@ page import="com.banking.util.Input" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page import="com.banking.model.Account" %>

        <% String userName=(String) session.getAttribute("userName"); String userEmail=(String)
            session.getAttribute("userEmail"); if (userName==null || userName.trim().isEmpty()) { userName="Customer" ;
            } if (userEmail==null) { userEmail="" ; } String firstName=userName; if (userName.contains(" ")) {
        firstName = userName.substring(0, userName.indexOf(" "));
    }

    String firstLetter = firstName.substring(0, 1).toUpperCase();

    Account account = (Account) request.getAttribute("account"); /* * If dashboard.jsp is opened directly, * send the
            request through DashboardServlet * so the real account information is loaded. */ if (account==null &&
            request.getAttribute("dashboardLoaded")==null) { response.sendRedirect( request.getContextPath()
            + "/customer/dashboard" ); return; } %>

            <!DOCTYPE html><html lang="en"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Overview | LankaTrust</title>
<script src="<%= request.getContextPath() %>/assets/js/theme-init.js"></script>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/banking-system.css">
<script defer src="<%= request.getContextPath() %>/assets/js/banking-ui.js"></script>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/notifications.css">
<script defer src="<%= request.getContextPath() %>/assets/js/notifications.js"></script>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/design-system.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/workspaces.css">
<script defer src="<%= request.getContextPath() %>/assets/js/workspaces.js"></script>
</head><body class="customer-portal overview-page"><div class="app-layout">
<jsp:include page="/WEB-INF/fragments/customer-sidebar.jsp"><jsp:param name="active" value="dashboard" /></jsp:include>
<main class="main-content"><header class="topbar"><div><span class="eyebrow">PERSONAL BANKING</span><h1>Overview</h1></div><div class="profile"><span class="avatar"><%= Input.html(firstLetter) %></span><div><strong><%= Input.html(userName) %></strong><small><%= Input.html(userEmail) %></small></div></div><jsp:include page="/WEB-INF/fragments/notification-bell.jsp" /></header>
<div class="dashboard-content"><div class="overview-intro"><div><p class="eyebrow">YOUR MONEY, AT A GLANCE</p><h2>Good day, <%= Input.html(firstName) %>.</h2><p>Your everyday banking, one action away.</p></div><a class="quiet-link" href="<%= request.getContextPath() %>/customer/tickets">Customer support &nearr;</a></div>
<% java.util.List<Account> overviewAccounts=(java.util.List<Account>)request.getAttribute("accounts");
java.math.BigDecimal available=java.math.BigDecimal.ZERO;
if(overviewAccounts!=null)for(Account a:overviewAccounts)if("ACTIVE".equals(a.getStatus()) && a.getBalance()!=null)available=available.add(a.getBalance()); %>
<div class="overview-grid"><section class="wealth-panel"><div class="wealth-label"><span>Total available balance</span><span class="currency-pill">LKR</span></div><div class="wealth-number"><span id="balanceAmount"><%= String.format("%,.2f",available) %></span><button class="eye-button" id="balanceToggle" type="button" aria-label="Hide total balance" aria-pressed="false">Hide</button></div><p>Across your active accounts</p><div class="wealth-bottom"><span><%= overviewAccounts==null?0:overviewAccounts.size() %> linked accounts</span><a href="<%= request.getContextPath() %>/customer/accounts">Manage accounts &rarr;</a></div></section>
<section class="primary-panel"><div class="panel-heading"><h3>Primary account</h3><span class="account-symbol" aria-hidden="true">LT</span></div>
<% if(account!=null){String number=String.valueOf(account.getAccountNumber()); %><p class="primary-type"><%= Input.html(account.getAccountType()) %></p><strong class="masked-number">&bull;&bull;&bull;&bull; <%= number.substring(Math.max(0,number.length()-4)) %></strong><div class="primary-footer"><span class="status-badge"><%= Input.html(account.getStatus()) %></span><a href="<%= request.getContextPath() %>/customer/accounts">Account details &nearr;</a></div><% }else{ %><p>No banking account is linked yet.</p><a href="<%= request.getContextPath() %>/customer/requests">Request an account &rarr;</a><% } %></section></div>
<section class="control-actions" aria-label="Quick banking actions"><div class="control-actions-heading"><h3>What would you like to do?</h3><span>Quick actions</span></div><div class="quick-action-grid">
<% String[][] shortcuts={{"transfer#action-transfer","Transfer money","transfer"},{"payments#action-pay","Pay a bill","payments"},{"loans#action-apply","Apply for a loan","loans"},{"cards","Manage cards","cards"},{"investments#action-apply","Invest","investments"},{"tickets#action-ticket","Get support","tickets"}};for(String[] item:shortcuts){ %><a class="quick-action-tile" href="<%= request.getContextPath() %>/customer/<%= item[0] %>"><span class="shortcut-icon" data-icon="<%= item[2] %>" aria-hidden="true"></span><span><%= item[1] %></span><span class="tile-arrow" aria-hidden="true">&nearr;</span></a><% } %></div></section>
<div class="activity-grid"><section class="panel activity-panel"><div class="panel-heading"><div><p class="eyebrow">RECENT ACTIVITY</p><h3>Your latest transactions</h3></div><a href="<%= request.getContextPath() %>/customer/transactions">View all &rarr;</a></div><jsp:include page="/WEB-INF/fragments/transactions.jsp" /></section>
<aside class="panel account-directory"><div class="panel-heading"><h3>Your accounts</h3><a href="<%= request.getContextPath() %>/customer/accounts">View all</a></div>
<% if(overviewAccounts==null||overviewAccounts.isEmpty()){ %><div class="empty-state"><h3>A fresh start</h3><p>Your linked accounts will appear here.</p><a href="<%= request.getContextPath() %>/customer/requests">Request an account</a></div><% }else{for(Account a:overviewAccounts){String n=String.valueOf(a.getAccountNumber()); %><div class="account-directory-row"><span class="account-mini-icon" data-icon="accounts" aria-hidden="true"></span><div><strong><%= Input.html(a.getAccountType()) %></strong><small>&bull;&bull;&bull;&bull; <%= n.substring(Math.max(0,n.length()-4)) %> <span class="status-badge"><%= Input.html(a.getStatus()) %></span></small></div><b>LKR <%= a.getBalance()==null?"0.00":String.format("%,.2f",a.getBalance()) %></b></div><% }} %></aside></div>
<section class="next-step"><div><p class="eyebrow">LOOKING AHEAD</p><h3>A little planning goes a long way.</h3><p>Explore banking products or keep track of an existing application.</p></div><div><a href="<%= request.getContextPath() %>/customer/products">Browse products &nearr;</a><a href="<%= request.getContextPath() %>/customer/requests">New service request &nearr;</a></div></section>
<footer class="dashboard-footer"><span>LankaTrust Banking System</span><span>Trust in every transaction.</span></footer></div></main></div>
<script src="<%= request.getContextPath() %>/assets/js/dashboard.js"></script></body></html>