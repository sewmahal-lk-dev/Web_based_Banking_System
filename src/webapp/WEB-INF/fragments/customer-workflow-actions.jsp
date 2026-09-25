<%@ page import="com.banking.util.*,java.util.*" %>
<% String workflow=(String)request.getAttribute("section");Map<String,Object> item=(Map<String,Object>)request.getAttribute("workflowRecord");String state=String.valueOf(item.get("status"));
Object key=item.get("cards".equals(workflow)?"card_id":"loans".equals(workflow)?"loan_id":"investments".equals(workflow)?"investment_id":"request_id"); %>
<% if(List.of("loans","investments").contains(workflow) && "PENDING".equals(state)){ %>
<form method="post"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="id" value="<%= key %>"><input type="hidden" name="action" value="update">
<label>Amount<input type="number" name="amount" min="<%= DemoRules.number(("loans".equals(workflow)?"loan":"investment")+".minAmount") %>" step="0.01" value="<%= Input.html(item.get("amount")) %>" required></label>
<label>Term in months<input type="number" name="months" min="1" value="<%= Input.html(item.get("term_months")) %>" required></label><button>Update pending application</button></form><% } %>
<% if("cards".equals(workflow) && "ACTIVE".equals(state)){ %>
<form method="post"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="id" value="<%= key %>"><input type="hidden" name="action" value="limit">
<label>Daily limit (maximum LKR <%= DemoRules.number("card.dailyLimit") %>)<input type="number" name="limit" min="0.01" max="<%= DemoRules.number("card.dailyLimit") %>" step="0.01" value="<%= Input.html(item.get("daily_limit")) %>" required></label><button>Update daily limit</button></form><% } %>
<% if("requests".equals(workflow) && "PENDING".equals(state)){ %><form method="post"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="id" value="<%= key %>"><input type="hidden" name="action" value="request-update"><label>Description<textarea name="description" maxlength="500" required><%= Input.html(item.get("description")) %></textarea></label><button>Update pending request</button></form><% } %>
<% List<String[]> actions=new ArrayList<>();
if("PENDING".equals(state))actions.add(new String[]{"requests".equals(workflow)?"request-cancel":"cancel","Cancel pending request"});
if("cards".equals(workflow) && List.of("ACTIVE","BLOCKED","EXPIRED").contains(state))actions.add(new String[]{"close","Close card"});
if("loans".equals(workflow) && "APPROVED".equals(state)){actions.add(new String[]{"accept","Accept terms and receive loan"});actions.add(new String[]{"cancel","Cancel approved application"});}
if("investments".equals(workflow) && List.of("ACTIVE","MATURED").contains(state))actions.add(new String[]{"withdraw","Withdraw and close investment"});
if("loans".equals(workflow) && List.of("PENDING","APPROVED").contains(state)){ %><p>Total repayable: LKR <%= DemoRules.totalLoan((java.math.BigDecimal)item.get("amount"),(java.math.BigDecimal)item.get("interest_rate"),((Number)item.get("term_months")).intValue()) %>. Monthly installments begin one month after acceptance.</p><% }
for(String[] action:actions){ %><form method="post"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="id" value="<%= key %>"><input type="hidden" name="action" value="<%= action[0] %>"><button><%= action[1] %></button></form><% } %>
<% if("requests".equals(workflow) && "ACCOUNT_STATEMENT".equals(item.get("request_type")) && "COMPLETED".equals(state)){ %><a href="<%= request.getContextPath() %>/customer/statement?id=<%= key %>">Download account statement (CSV)</a><% } %>
