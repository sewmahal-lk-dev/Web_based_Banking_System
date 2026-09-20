<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,com.banking.util.Input" %>
<% String section=(String)request.getAttribute("section");
String title=Map.of("cards","Cards","loans","Loans","investments","Investments","requests","Service Requests","tickets","Support Center","settings","Settings","transactions","Transaction History","products","Banking Products").get(section);
List<Map<String,Object>> records=(List<Map<String,Object>>)request.getAttribute("records");
%>
<!DOCTYPE html><html lang="en"><head>
<script src="<%= request.getContextPath() %>/assets/js/theme-init.js"></script><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title><%= title %> | LankaTrust Bank</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/dashboard.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/customer-navigation.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/services.css"><link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/lankatrust.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/banking-system.css">
<script defer src="<%= request.getContextPath() %>/assets/js/banking-ui.js"></script>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/notifications.css">
<script defer src="<%= request.getContextPath() %>/assets/js/notifications.js"></script>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/design-system.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/workspaces.css">
<script defer src="<%= request.getContextPath() %>/assets/js/workspaces.js"></script>
</head><body class="customer-portal" data-workspace-page="<%= Input.html(section) %>"><div class="app-layout">
<jsp:include page="/WEB-INF/fragments/customer-sidebar.jsp"><jsp:param name="active" value="<%= section %>" /></jsp:include>
<main class="main-content"><header class="topbar"><h1><%= title %></h1><span><%= Input.html(session.getAttribute("userName")) %></span><jsp:include page="/WEB-INF/fragments/notification-bell.jsp" /></header><section class="dashboard-content services-content">
<% if(request.getAttribute("error")!=null) { %><p class="notice error" role="alert"><%= Input.html(request.getAttribute("error")) %></p><% } %>
<% if(request.getAttribute("success")!=null) { %><p class="notice" role="status"><%= Input.html(request.getAttribute("success")) %></p><% } %>
<jsp:include page="/WEB-INF/fragments/customer-workflow-create.jsp" />
<% if("transactions".equals(section)) { %>
<section class="panel"><jsp:include page="/WEB-INF/fragments/transactions.jsp" /></section>
<nav aria-label="Transaction pages"><% int current=(Integer)request.getAttribute("pageNumber"); if(current>1){ %><a href="?page=<%= current-1 %>">Previous</a><% } %> Page <%= current %> <% List<?> tx=(List<?>)request.getAttribute("transactions"); if(tx!=null && tx.size()==25){ %><a href="?page=<%= current+1 %>">Next</a><% } %></nav>
<% } else if("settings".equals(section)) { if(records!=null && !records.isEmpty()){Map<String,Object> profile=records.get(0); %>
<section class="panel"><h2>Your profile</h2><p>Email: <%= Input.html(profile.get("email")) %></p><p>Date of birth: <%= Input.html(profile.get("date_of_birth")) %></p>
<form method="post"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="action" value="profile">
<label>Name<input name="name" maxlength="100" required value="<%= Input.html(profile.get("name")) %>"></label>
<label>Phone<input name="phone" maxlength="20" required value="<%= Input.html(profile.get("phone")) %>"></label>
<label>Address<input name="address" maxlength="255" value="<%= Input.html(profile.get("address")) %>"></label>
<label>City<input name="city" maxlength="100" value="<%= Input.html(profile.get("city")) %>"></label>
<label>Postal code<input name="postalCode" maxlength="20" value="<%= Input.html(profile.get("postal_code")) %>"></label><button>Save profile</button></form>
<p>For email or date-of-birth corrections, submit a profile update service request.</p></section>
<section class="panel"><h2>Change password</h2><p>You will be signed out after changing your password.</p><form method="post">
<input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="action" value="password">
<label>Current password<input type="password" name="currentPassword" autocomplete="current-password" required></label>
<label>New password<input type="password" name="newPassword" autocomplete="new-password" minlength="8" required></label>
<label>Confirm new password<input type="password" name="confirmPassword" autocomplete="new-password" minlength="8" required></label><button>Change password</button></form></section>
<section class="panel"><h2>Deactivate profile</h2><p>All accounts must be closed and financial obligations settled first. Your banking history is retained.</p><form method="post"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="action" value="deactivate"><label>Current password<input type="password" name="currentPassword" autocomplete="current-password" required></label><button>Deactivate my profile</button></form></section>
<% } } else if("tickets".equals(section)) { %>
<jsp:include page="/WEB-INF/fragments/support-tickets.jsp" />
<% } else { %>



<section class="panel"><h2>Your <%= title.toLowerCase() %></h2>
<% if(records!=null && records.isEmpty()){ %><p>No records yet.</p><% } if(records!=null)for(Map<String,Object> record:records){ %>
<article class="record"><dl><% for(Map.Entry<String,Object> field:record.entrySet()){ %><div><dt><%= Input.html(field.getKey().replace('_',' ')) %></dt><dd><%= Input.html(field.getValue()) %></dd></div><% } %></dl>
<% if(!"products".equals(section)){ request.setAttribute("workflowRecord",record); %><jsp:include page="/WEB-INF/fragments/customer-workflow-actions.jsp" /><% } %>
<% if("cards".equals(section) && "ACTIVE".equals(record.get("status"))){ %><form method="post"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="action" value="block"><input type="hidden" name="id" value="<%= record.get("card_id") %>"><button>Block this card</button></form><% } %></article><% } %></section>
<% if("loans".equals(section)){ List<Map<String,Object>> repayments=(List<Map<String,Object>>)request.getAttribute("repayments"); %><section class="panel"><h2>Repayment schedule</h2><p>Payments use the exact installment amount already recorded by the bank.</p>
<% if(repayments!=null && repayments.isEmpty()){ %><p>No installments recorded.</p><% } if(repayments!=null)for(Map<String,Object> record:repayments){ %><article class="record"><dl><% for(Map.Entry<String,Object> field:record.entrySet()){ %><div><dt><%= Input.html(field.getKey().replace('_',' ')) %></dt><dd><%= Input.html(field.getValue()) %></dd></div><% } %></dl>
<% if(!"PAID".equals(record.get("status"))){ %><form method="post"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="action" value="repay"><input type="hidden" name="id" value="<%= record.get("repayment_id") %>"><button>Pay LKR <%= Input.html(record.get("amount")) %></button></form><% } %></article><% } %></section><% } %>
<% if("requests".equals(section)){ %>
<section class="panel"><h2>New service request</h2><form method="post"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="action" value="request">
<label>Request type<select name="type" required><% for(String type:List.of("ACCOUNT_OPENING","ACCOUNT_CLOSURE","PROFILE_UPDATE","CHEQUE_BOOK","ACCOUNT_STATEMENT")){ %><option value="<%= type %>"><%= type.replace('_',' ') %></option><% } %></select></label>
<label>Account (closure, statement or cheque book)<select name="account"><option value="">Select account when needed</option><% for(com.banking.model.Account owned:(List<com.banking.model.Account>)request.getAttribute("accounts")){ %><option value="<%= owned.getAccountNumber() %>"><%= owned.getAccountNumber() %> - <%= Input.html(owned.getAccountType()) %></option><% } %></select></label>
<label>New account type (account opening)<select name="accountType"><option>SAVINGS</option><option>CURRENT</option></select></label>
<label>New email (profile update)<input type="email" name="email" maxlength="150"></label>
<label>Description<textarea name="description" maxlength="500" required></textarea></label><button>Submit request</button></form></section>
<jsp:include page="/WEB-INF/fragments/support-tickets.jsp" />
<% } } %></section></main></div></body></html>
