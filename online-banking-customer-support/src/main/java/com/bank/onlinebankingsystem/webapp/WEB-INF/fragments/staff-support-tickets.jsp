<%@ page contentType="text/html;charset=UTF-8" import="java.util.*,com.banking.util.Input,com.banking.dao.TicketDAO" %>
<%
List<Map<String,Object>> supportTickets=(List<Map<String,Object>>)request.getAttribute("supportTickets");
String supportRole=(String)session.getAttribute("role");
boolean canAssign="CUSTOMER_SERVICE_OFFICER".equals(supportRole);
if(supportTickets!=null){
%>
<section class="panel support-center" id="assigned-support-tickets">
<h2><%= canAssign?"Support Tickets — Assign / Reassign Department":"Assigned Support Tickets" %></h2>
<% if(supportTickets.isEmpty()){ %><p class="support-empty">No support tickets assigned to your department.</p><% } %>
<% for(Map<String,Object> ticket:supportTickets){
String ticketState=(String)ticket.get("status");
String assignedRole=(String)ticket.get("assigned_role");
boolean active=List.of("OPEN","ASSIGNED","IN_PROGRESS","ESCALATED").contains(ticketState);
boolean canRespond=active&&(supportRole.equals(assignedRole)||(canAssign&&assignedRole==null));
Object ticketId=ticket.get("ticket_id");
List<String> supportSteps=List.of("OPEN","ASSIGNED","IN_PROGRESS","RESOLVED","CLOSED");
int supportCurrent=supportSteps.indexOf("ESCALATED".equals(ticketState)?"IN_PROGRESS":ticketState);
%>
<article class="support-ticket" id="support-ticket-<%= ticketId %>">
<h3><%= "COMPLAINT".equals(ticket.get("ticket_type"))?"COMPLAINT":"Ticket" %> #<%= ticketId %> — <%= Input.html(ticket.get("subject")) %></h3>
<ol class="ticket-progress" aria-label="Ticket progress">
<% for(int step=0;step<supportSteps.size();step++){ %><li class="<%= step<supportCurrent?"complete":step==supportCurrent?"current":"" %>" <%= step==supportCurrent?"aria-current=\"step\"":"" %>><span><%= supportSteps.get(step).replace('_',' ') %></span></li><% } %>
</ol>
<dl class="ticket-assignment">
<div><dt>Customer:</dt><dd><%= Input.html(ticket.get("customer_name")) %> (#<%= Input.html(ticket.get("customer_id")) %>)</dd></div>
<div><dt>Customer email:</dt><dd><%= Input.html(ticket.get("customer_email")) %></dd></div>
<div><dt>Type/category:</dt><dd><%= Input.html(ticket.get("ticket_type")) %></dd></div>
<div><dt>Current Status:</dt><dd><span class="status-badge"><%= Input.html(ticketState) %></span></dd></div>
<div><dt>Assigned Department:</dt><dd><%= Input.html(TicketDAO.department(assignedRole)) %></dd></div>
<div><dt>Assigned Officer:</dt><dd><%= Input.html(ticket.get("assigned_officer")==null?"Awaiting officer":ticket.get("assigned_officer")) %></dd></div>
<div><dt>Priority:</dt><dd><%= Input.html(ticket.get("priority")) %></dd></div>
<div><dt>Created:</dt><dd><%= Input.html(ticket.get("date_created")) %></dd></div>
</dl>
<p><strong>Description:</strong> <%= Input.html(ticket.get("description")) %></p>
<% if(canAssign&&active){ %>
<form method="post" class="ticket-assign">
<input type="hidden" name="csrf" value="<%= Input.html(session.getAttribute("csrf")) %>">
<input type="hidden" name="action" value="ticket-assign"><input type="hidden" name="id" value="<%= ticketId %>">
<label for="assign-role-<%= ticketId %>">Assign / Reassign Department</label>
<select id="assign-role-<%= ticketId %>" name="assignedRole" required>
<option value="">Select department</option>
<% for(String destination:TicketDAO.ROLES){ %><option value="<%= destination %>" <%= destination.equals(assignedRole)?"selected":"" %>><%= Input.html(TicketDAO.department(destination)) %></option><% } %>
</select><button type="submit">Assign Ticket</button>
</form>
<% } %>
<details <%= String.valueOf(ticketId).equals(request.getParameter("ticket"))?"open":"" %>>
<summary>Open ticket — Conversation and updates</summary>
<h4 class="conversation-title">Conversation &amp; timeline</h4><div class="ticket-conversation" aria-label="Conversation">
<div class="ticket-message" data-sender="customer"><header><strong>Customer</strong><time><%= Input.html(ticket.get("date_created")) %></time></header><p><%= Input.html(ticket.get("description")) %></p></div>
<% for(Map<String,Object> message:(List<Map<String,Object>>)ticket.get("messages")){ %>
<div class="ticket-message" data-sender="<%= "Customer".equals(message.get("sender_label"))?"customer":"Support update".equals(message.get("sender_label"))?"system":"staff" %>"><header><strong><%= Input.html(message.get("sender_label")) %></strong><time><%= Input.html(message.get("created_at")) %></time></header><p><%= Input.html(message.get("body")) %></p></div>
<% } %>
</div>
<% if("RESOLVED".equals(ticketState)&&supportRole.equals(assignedRole)){ %>
<form method="post"><input type="hidden" name="csrf" value="<%= Input.html(session.getAttribute("csrf")) %>"><input type="hidden" name="id" value="<%= ticketId %>"><input type="hidden" name="action" value="ticket-status"><input type="hidden" name="status" value="CLOSED"><button type="submit">Close ticket</button></form>
<% } %>
<% if(canRespond){ %>
<form method="post">
<input type="hidden" name="csrf" value="<%= Input.html(session.getAttribute("csrf")) %>"><input type="hidden" name="id" value="<%= ticketId %>"><input type="hidden" name="action" value="ticket-status">
<label for="ticket-status-<%= ticketId %>">Update status</label><select id="ticket-status-<%= ticketId %>" name="status" required>
<option value="IN_PROGRESS">In progress</option><option value="ESCALATED">Escalated</option>
<% if(List.of("IN_PROGRESS","ESCALATED").contains(ticketState)){ %><option value="RESOLVED">Resolved</option><% } %>
</select><button type="submit">Update Status</button>
</form>
<form method="post" class="ticket-reply">
<input type="hidden" name="csrf" value="<%= Input.html(session.getAttribute("csrf")) %>"><input type="hidden" name="id" value="<%= ticketId %>"><input type="hidden" name="action" value="ticket-reply">
<label for="staff-reply-<%= ticketId %>">Write a reply</label><textarea id="staff-reply-<%= ticketId %>" name="reason" maxlength="5000" rows="3" required></textarea><button type="submit">Send Reply</button>
</form>
<% } %>
</details>
</article>
<% } %>
</section>
<% } %>
