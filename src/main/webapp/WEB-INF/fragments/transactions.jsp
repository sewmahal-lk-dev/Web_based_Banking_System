<%@ page import="java.util.*,com.banking.util.Input" %>
<% List<Map<String,Object>> activity=(List<Map<String,Object>>)request.getAttribute("transactions"); %>
<% if(request.getAttribute("transactionError")!=null) { %><p role="alert">Transaction history is temporarily unavailable. Please try again.</p>
<% } else if(activity==null || activity.isEmpty()) { %><p>No transactions yet.</p>
<% } else if("transactions".equals(request.getAttribute("section"))) { %>
<h2>Transaction history</h2>
<p>Review your account activity. Scroll across to see every transaction detail.</p>
<table class="transaction-table"><caption class="sr-only">Account transactions, most recent first</caption>
<thead><tr><th scope="col">Date</th><th scope="col">Type</th><th scope="col">Status</th><th scope="col">Reference</th><th scope="col">Recipient</th><th scope="col">Bill / consumer no.</th><th scope="col">Amount (LKR)</th></tr></thead><tbody>
<% for(Map<String,Object> transaction:activity){ %>
<tr><td><%= Input.html(transaction.get("payment_date")) %></td><td><%= Input.html(transaction.get("payment_type")) %></td><td><span class="status-badge"><%= Input.html(transaction.get("status")) %></span></td><td><%= Input.html(transaction.get("reference_number")) %></td><td><%= Input.html(transaction.get("recipient")) %></td><td><%= "BILL_PAYMENT".equals(transaction.get("payment_type")) && transaction.get("bill_reference")!=null?Input.html(transaction.get("bill_reference")):"Not applicable" %></td><td class="transaction-value"><%= "IN".equals(transaction.get("direction")) ? "+" : "-" %> LKR <%= Input.html(transaction.get("amount")) %></td></tr>
<% } %></tbody></table>
<% } else { for(Map<String,Object> transaction:activity) { %>
<div class="transaction <%= "IN".equals(transaction.get("direction")) ? "credit" : "debit" %>">
 <span class="transaction-direction" aria-hidden="true"><%= "IN".equals(transaction.get("direction")) ? "&#8601;" : "&#8599;" %></span>
 <div class="transaction-info"><strong><%= Input.html(transaction.get("payment_type")) %></strong><span><%= Input.html(transaction.get("payment_date")) %> | <%= Input.html(transaction.get("status")) %></span><span>Reference: <%= Input.html(transaction.get("reference_number")) %> | Recipient: <%= Input.html(transaction.get("recipient")) %></span><% if("BILL_PAYMENT".equals(transaction.get("payment_type")) && transaction.get("bill_reference")!=null){ %><span>Consumer / Bill No: <%= Input.html(transaction.get("bill_reference")) %></span><% } %></div>
 <div class="transaction-amount"><%= "IN".equals(transaction.get("direction")) ? "+" : "-" %> LKR <%= Input.html(transaction.get("amount")) %></div>
</div>
<% } } %>
