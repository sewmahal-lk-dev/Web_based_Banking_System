<%@ page import="java.util.*,com.banking.util.Input" %>
<% if ("SYSTEM_ADMIN".equals(session.getAttribute("role"))) {
List<Map<String,Object>> adminNotes=(List<Map<String,Object>>)request.getAttribute("notificationItems");
List<Map<String,Object>> adminActivity=(List<Map<String,Object>>)request.getAttribute("adminRecentActivity");
%>
<div class="admin-overview">
  <section id="admin-recent-notifications" class="panel admin-recent-notifications" aria-labelledby="admin-notifications-heading">
    <div class="panel-heading"><h2 id="admin-notifications-heading">Recent Notifications</h2><a href="<%= request.getContextPath() %>/employee/notifications">View All<span class="sr-only"> notifications</span></a></div>
    <% if(Boolean.TRUE.equals(request.getAttribute("notificationUnavailable"))){ %>
    <p class="notify-empty">Notifications are temporarily unavailable.</p>
    <% } else {
       Object previousItems=request.getAttribute("notificationDisplayItems");
       request.setAttribute("notificationDisplayItems",adminNotes==null?Collections.emptyList():adminNotes.subList(0,Math.min(5,adminNotes.size()))); %>
    <jsp:include page="/WEB-INF/fragments/notification-items.jsp" />
    <% if(previousItems==null)request.removeAttribute("notificationDisplayItems");else request.setAttribute("notificationDisplayItems",previousItems);
       } %>
  </section>
  <section class="panel admin-recent-activity" aria-labelledby="admin-activity-heading">
    <div class="panel-heading"><h2 id="admin-activity-heading">Recent Administrative Activity</h2><a href="<%= request.getContextPath() %>/employee/dashboard#Audit-log">View All<span class="sr-only"> audit records</span></a></div>
    <% if(Boolean.TRUE.equals(request.getAttribute("adminActivityUnavailable"))){ %>
    <p class="notify-empty">Administrative activity is temporarily unavailable.</p>
    <% } else if(adminActivity==null||adminActivity.isEmpty()){ %>
    <p class="notify-empty">No administrative activity recorded yet.</p>
    <% } else { %><ol class="admin-activity-list">
    <% for(Map<String,Object> activity:adminActivity){
       Object actor=activity.get("actor_name");
       if(actor==null)actor=activity.get("employee_id")==null?"Actor not recorded":"Employee #"+activity.get("employee_id"); %>
      <li><strong><%= Input.html(String.valueOf(activity.get("action")).replace('_',' ').replace('-',' ')) %></strong>
        <p><%= Input.html(activity.get("details")) %></p>
        <div class="admin-activity-meta"><span><%= Input.html(actor) %></span><time><%= Input.html(activity.get("action_time")) %></time></div>
      </li>
    <% } %></ol><% } %>
  </section>
</div>
<% } %>
