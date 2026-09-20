<%@ page import="java.util.*,com.banking.util.Input" %>
<%
List<Map<String,Object>> tickets=(List<Map<String,Object>>)request.getAttribute("tickets");
long activeTickets=tickets==null?0:tickets.stream().filter(t->!"CLOSED".equals(t.get("status"))).count();
Map<String,String> departments=Map.of("CUSTOMER_SERVICE_OFFICER","Customer Support","CARD_SERVICES_OFFICER","Card Services","LOAN_OFFICER","Loans","INVESTMENT_OFFICER","Investments","COMPLIANCE_RISK_OFFICER","Compliance and Risk","SYSTEM_ADMIN","Administration");
%>
<section class="panel support-center">
<header class="support-heading"><h2>Support Center</h2><span class="support-count" title="Active tickets"><%= activeTickets %><span> active tickets</span></span></header>
<% if(tickets!=null && tickets.isEmpty()){ %><p class="support-empty">No support tickets yet. Create a ticket below to get help.</p><% } %>
<% if(tickets!=null)for(Map<String,Object> ticket:tickets){
String state=String.valueOf(ticket.get("status"));
String displayState="OPEN".equals(state)&&ticket.get("assigned_employee_id")!=null?"ASSIGNED":state;
List<String> steps=List.of("OPEN","ASSIGNED","IN_PROGRESS","RESOLVED","CLOSED");
int current=steps.indexOf("ESCALATED".equals(state)?"IN_PROGRESS":displayState);
String role=String.valueOf(ticket.get("assigned_role"));
List<Map<String,Object>> messages=(List<Map<String,Object>>)ticket.get("messages");
%>
<article id="ticket-<%= ticket.get("ticket_id") %>" tabindex="-1" class="support-ticket" aria-labelledby="ticket-title-<%= ticket.get("ticket_id") %>">
<p class="support-reference">Ticket #TKT-<%= String.format("%04d",((Number)ticket.get("ticket_id")).intValue()) %></p>
<h3 id="ticket-title-<%= ticket.get("ticket_id") %>"><%= Input.html(ticket.get("subject")) %></h3>
<ol class="ticket-progress" aria-label="Ticket progress">
<% for(int i=0;i<steps.size();i++){ %><li class="<%= i<current?"complete":i==current?"current":"" %>" <%= i==current?"aria-current=\"step\"":"" %>><span><%= steps.get(i).replace('_',' ') %></span></li><% } %>
</ol>
<% if("ESCALATED".equals(state)){ %><p class="ticket-escalated">Escalated for further review</p><% } %>
<dl class="ticket-assignment"><div><dt>Category:</dt><dd><%= Input.html(ticket.get("ticket_type")) %></dd></div><div><dt>Current status:</dt><dd><span class="status-badge"><%= Input.html(state) %></span></dd></div><div><dt>Created:</dt><dd><%= Input.html(ticket.get("date_created")) %></dd></div><div><dt>Assigned Department:</dt><dd><%= Input.html(departments.getOrDefault(role,"Awaiting assignment")) %></dd></div><div><dt>Assigned Officer:</dt><dd><%= Input.html(ticket.get("assigned_officer")==null?"Awaiting assignment":ticket.get("assigned_officer")) %></dd></div><div><dt>Priority:</dt><dd><span class="ticket-priority"><%= Input.html(ticket.get("priority")) %></span></dd></div></dl>
<h4 class="conversation-title">Conversation &amp; timeline</h4>
<div class="ticket-conversation" aria-label="Conversation">
<div class="ticket-message" data-sender="customer"><header><strong>Customer</strong><time><%= Input.html(ticket.get("date_created")) %></time></header><p><%= Input.html(ticket.get("description")) %></p></div>
<% if(messages!=null)for(Map<String,Object> message:messages){ %>
<div class="ticket-message" data-sender="<%= "Customer".equals(message.get("sender_label"))?"customer":"Support update".equals(message.get("sender_label"))?"system":"staff" %>"><header><strong><%= Input.html(message.get("sender_label")) %></strong><time><%= Input.html(message.get("created_at")) %></time></header><p><%= Input.html(message.get("body")) %></p></div>
<% } %>
<% if((messages==null||messages.isEmpty())&&ticket.get("response")!=null){ %><div class="ticket-message" data-sender="staff"><header><strong>Support Officer</strong><time><%= Input.html(ticket.get("date_updated")) %></time></header><p><%= Input.html(ticket.get("response")) %></p></div><% } %>
</div>
<% if(!"CLOSED".equals(state)){ %>
<form method="post" class="ticket-reply"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="action" value="ticket-reply"><input type="hidden" name="id" value="<%= ticket.get("ticket_id") %>"><label for="reply-<%= ticket.get("ticket_id") %>">Write a reply</label><textarea id="reply-<%= ticket.get("ticket_id") %>" name="reply" maxlength="5000" rows="3" placeholder="Write a reply..." required></textarea><button type="submit">Send Reply</button></form>
<% }else{ %><p class="support-empty">This ticket is closed. Your conversation is saved in your history.</p><% } %>
<details class="ticket-options"><summary>Ticket options</summary><% request.setAttribute("ticketRecord",ticket); %><jsp:include page="/WEB-INF/fragments/ticket-actions.jsp" /></details>
</article><% } %>
</section>
<section class="panel"><h2>New support ticket / complaint</h2><form method="post"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="action" value="ticket">
<label>Type<select name="type"><% for(String type:List.of("INQUIRY","COMPLAINT","TECHNICAL","OTHER")){ %><option><%= type %></option><% } %></select></label>
<label>Subject<input name="subject" maxlength="150" required></label><label>Description<textarea name="description" maxlength="5000" required></textarea></label><button>Submit ticket</button></form></section>
