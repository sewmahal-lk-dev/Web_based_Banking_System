<%@ page import="java.util.*,com.banking.util.Input" %>
<% List<Map<String,Object>> notificationRows=(List<Map<String,Object>>)request.getAttribute("notificationDisplayItems");
String notificationEndpoint=request.getContextPath()+("CUSTOMER".equals(session.getAttribute("role"))?"/customer/notifications":"/employee/notifications");
if(notificationRows==null||notificationRows.isEmpty()){ %><p class="notify-empty">You're all caught up. No notifications yet.</p><% }else for(Map<String,Object> note:notificationRows){
boolean noteRead=Boolean.TRUE.equals(note.get("is_read"))||"1".equals(String.valueOf(note.get("is_read")));
%>
<form class="notify-item <%= noteRead?"is-read":"is-unread" %>" method="post" action="<%= notificationEndpoint %>">
<input type="hidden" name="csrf" value="<%= Input.html(session.getAttribute("csrf")) %>"><input type="hidden" name="action" value="open"><input type="hidden" name="id" value="<%= note.get("notification_id") %>">
<button type="submit"><span class="notify-title"><span class="notify-dot" aria-hidden="true"></span><%= Input.html(note.get("title")) %><span class="notify-state"><%= noteRead?"Read":"Unread" %></span></span><span class="notify-message"><%= Input.html(note.get("message")) %></span><time><%= Input.html(note.get("created_at")) %></time></button>
</form><% } %>
