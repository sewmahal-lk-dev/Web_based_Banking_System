<%@ page pageEncoding="UTF-8" %>
<% String navPage = request.getParameter("active");
String[][] navItems = {
    {"dashboard", "Overview", "&#8962;"}, {"accounts", "My Accounts", "&#9635;"},
    {"transfer", "Transfers", "&#8644;"}, {"payments", "Payments", "&#9636;"},
    {"transactions", "Transactions", "&#8645;"},
    {"cards", "Cards", "&#9671;"}, {"loans", "Loans", "&#9651;"},
    {"investments", "Investments", "&#8599;"}, {"requests", "Service Requests", "?"},
    {"tickets", "Support Tickets", "?"},
    {"notifications", "Notifications", "&#9830;"},
    {"settings", "Settings", "&#9881;"}
}; %>
<aside class="customer-sidebar" id="customerSidebar">
    <div class="customer-brand"><div class="customer-logo">LT</div><div><strong>LankaTrust</strong><small>PERSONAL BANKING</small></div></div>
    <nav class="customer-navigation" aria-label="Customer banking">
    <% for (String[] item : navItems) { boolean selected = item[0].equals(navPage); %>
        <a class="customer-nav-item<%= selected ? " active" : "" %>" href="<%= request.getContextPath() %>/customer/<%= item[0] %>" <%= selected ? "aria-current=\"page\"" : "" %>>
            <span class="customer-nav-icon" aria-hidden="true"><%= item[2] %></span><span><%= item[1] %></span>
        </a>
    <% } %>
    </nav>
    <a class="customer-nav-item customer-signout" href="<%= request.getContextPath() %>/logout"><span class="customer-nav-icon" aria-hidden="true">&#8618;</span>Sign Out</a>
</aside>
