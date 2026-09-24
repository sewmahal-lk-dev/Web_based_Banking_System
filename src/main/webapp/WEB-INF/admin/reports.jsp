<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*,com.banking.util.Input,com.banking.dao.ReportDAO,com.banking.dao.AdminDAO,com.banking.model.AdminReport" %>
<%
AdminReport report=(AdminReport)request.getAttribute("report");
ReportDAO.Filter filter=(ReportDAO.Filter)session.getAttribute("adminReportFilter");
String type=filter==null?"CUSTOMER":filter.type();
Map<String,Long> totals=(Map<String,Long>)request.getAttribute("totals");
List<String> productTypes=(List<String>)request.getAttribute("productTypes");
int pageNumber=(Integer)request.getAttribute("pageNumber"),pageCount=(Integer)request.getAttribute("pageCount");
String ctx=request.getContextPath();
%>
<!DOCTYPE html><html lang="en"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="csrf-token" content="<%= Input.html(session.getAttribute("csrf")) %>">
<title>Reports | LankaTrust Bank</title>
<script src="<%= ctx %>/assets/js/theme-init.js"></script>
<link rel="stylesheet" href="<%= ctx %>/assets/css/dashboard.css">
<link rel="stylesheet" href="<%= ctx %>/assets/css/services.css">
<link rel="stylesheet" href="<%= ctx %>/assets/css/lankatrust.css">
<link rel="stylesheet" href="<%= ctx %>/assets/css/banking-system.css">
<link rel="stylesheet" href="<%= ctx %>/assets/css/notifications.css">
<link rel="stylesheet" href="<%= ctx %>/assets/css/design-system.css">
<link rel="stylesheet" href="<%= ctx %>/assets/css/workspaces.css">
<link rel="stylesheet" href="<%= ctx %>/assets/css/admin-reports.css">
<script defer src="<%= ctx %>/assets/js/banking-ui.js"></script>
<script defer src="<%= ctx %>/assets/js/notifications.js"></script>
<script defer src="<%= ctx %>/assets/js/admin-reports.js"></script>
</head><body class="staff-portal workspace-app admin-reports" data-role="SYSTEM_ADMIN">
<div class="staff-brandbar"><a href="<%= ctx %>/">LankaTrust</a><span>Banking Operations</span></div>
<header class="topbar"><div><span class="eyebrow">ADMINISTRATION MANAGEMENT</span><h1>Reports</h1></div>
<span><%= Input.html(session.getAttribute("userName")) %> | <a href="<%= ctx %>/logout">Sign Out</a></span>
<jsp:include page="/WEB-INF/fragments/notification-bell.jsp" /></header>
<main class="dashboard-content services-content">
<nav class="staff-navigation" hidden aria-label="Staff sections">
<a href="<%= ctx %>/employee/dashboard">Dashboard</a>
<a href="<%= ctx %>/employee/dashboard#Customers">Customers</a>
<a href="<%= ctx %>/employee/dashboard#Employees">Employees</a>
<a href="<%= ctx %>/employee/dashboard#Products">Products</a>
<a href="<%= ctx %>/employee/dashboard#Audit-log">Audit log</a>
<a href="<%= ctx %>/admin/reports" aria-current="page">Reports</a>
<a href="<%= ctx %>/employee/notifications">Notifications</a>
</nav>
<div class="customer-workspace-header"><span class="eyebrow">LANKATRUST / ADMINISTRATION</span><h2>Reports</h2><p>Generate and download administrative reports.</p></div>
<div class="workspace-metrics" aria-label="Current database totals">
<% for(var entry:totals.entrySet()) { %><div class="workspace-metric"><span><%= Input.html(entry.getKey().replace('_',' ')) %></span><strong><%= entry.getValue() %></strong><small>Current database total</small></div><% } %>
</div>
<% if(request.getAttribute("reportError")!=null) { %><p class="notice error" role="alert"><%= Input.html(request.getAttribute("reportError")) %></p><% } %>
<section class="panel report-controls" aria-labelledby="filters-heading"><h3 id="filters-heading">Report filters</h3>
<form method="post" action="<%= ctx %>/admin/reports" id="report-filters">
<input type="hidden" name="csrf" value="<%= Input.html(session.getAttribute("csrf")) %>">
<label>Report type<select name="type" id="report-type"><% for(var entry:ReportDAO.TYPES.entrySet()) { %><option value="<%= entry.getKey() %>" <%= type.equals(entry.getKey())?"selected":"" %>><%= entry.getValue() %></option><% } %></select></label>
<label data-report-types="CUSTOMER EMPLOYEE PRODUCT">Status<select name="status"><option value="">All statuses</option><% for(String status:List.of("ACTIVE","INACTIVE","BLOCKED")) { %><option value="<%= status %>" <%= filter!=null&&status.equals(filter.status())?"selected":"" %>><%= status %></option><% } %></select></label>
<label data-report-types="EMPLOYEE">Role<select name="role"><option value="">All roles</option><% for(String role:AdminDAO.ROLES) { %><option value="<%= role %>" <%= filter!=null&&role.equals(filter.role())?"selected":"" %>><%= role.replace('_',' ') %></option><% } %></select></label>
<label data-report-types="PRODUCT">Product type<select name="productType"><option value="">All product types</option><% for(String kind:productTypes) { %><option value="<%= Input.html(kind) %>" <%= filter!=null&&kind.equals(filter.productType())?"selected":"" %>><%= Input.html(kind) %></option><% } %></select></label>
<label data-report-types="AUDIT">Action (exact match)<input name="action" maxlength="100" value="<%= Input.html(filter==null?"":filter.action()) %>" placeholder="All actions"></label>
<label>From date<input type="date" name="from" min="1000-01-01" max="9998-12-31" value="<%= filter==null||filter.from()==null?"":filter.from() %>"></label>
<label>To date (inclusive)<input type="date" name="to" min="1000-01-01" max="9998-12-31" value="<%= filter==null||filter.to()==null?"":filter.to() %>"></label>
<button class="ui-primary" type="submit">Generate Report</button>
</form><p class="report-help">Dates filter creation time, or action time for audit logs. Reports support up to 5,000 records; narrow the filters for larger results.</p></section>
<% if(report!=null) { %>
<section class="panel report-preview" aria-labelledby="preview-heading">
<div class="report-preview-heading"><div><h3 id="preview-heading"><%= Input.html(report.title()) %></h3>
<p>Generated: <time><%= Input.html(report.generatedLabel()) %></time> · Records: <strong><%= report.rows().size() %></strong></p></div>
<a class="workspace-button" href="<%= ctx %>/admin/reports/pdf?report=<%= report.token() %>">Download PDF</a></div>
<p class="report-help">Snapshot filters: <%= Input.html(report.filters().isEmpty()?"All records":report.filters()) %>. Preview and PDF use this same snapshot.</p>
<div class="report-table-scroll ui-table-scroll" tabindex="0" role="region" aria-label="Report records">
<table><caption class="sr-only"><%= Input.html(report.title()) %></caption><thead><tr><% for(String header:report.headers()) { %><th scope="col"><%= Input.html(header) %></th><% } %></tr></thead><tbody>
<% if(report.rows().isEmpty()) { %><tr><td colspan="<%= report.headers().size() %>">No records match the selected filters.</td></tr><% }
for(List<String> row:report.rows().subList(Math.min((pageNumber-1)*25,report.rows().size()),Math.min(pageNumber*25,report.rows().size()))) { %>
<tr><% for(String value:row) { %><td><%= Input.html(value) %></td><% } %></tr><% } %>
</tbody></table></div>
<nav class="report-pagination" aria-label="Report pages"><% if(pageNumber>1) { %><a href="<%= ctx %>/admin/reports?page=<%= pageNumber-1 %>">Previous</a><% } %><span>Page <%= pageNumber %> of <%= pageCount %> · 25 records per page</span><% if(pageNumber<pageCount) { %><a href="<%= ctx %>/admin/reports?page=<%= pageNumber+1 %>">Next</a><% } %></nav>
</section><% } else { %><div class="empty-state"><h3>Your report preview will appear here</h3><p>Select a report and generate it to preview records and download a PDF.</p></div><% } %>
</main></body></html>
