<%@ page import="com.banking.util.Input" %>
<% long unreadNotifications=request.getAttribute("notificationUnread")==null?0:((Number)request.getAttribute("notificationUnread")).longValue();
String notificationPath=request.getContextPath()+("CUSTOMER".equals(session.getAttribute("role"))?"/customer/notifications":"/employee/notifications"); %>
<details class="notification-center">
<summary class="notify-bell" aria-label="Notifications<%= unreadNotifications>0?", "+unreadNotifications+" unread":"" %>">
<svg viewBox="0 0 24 24" width="23" height="23" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9Z"/><path d="M10 21h4"/></svg>
<% if(unreadNotifications>0){ %><span class="notify-badge"><%= unreadNotifications %></span><% } %>
</summary>
<div class="notify-popup" role="region" aria-label="Notifications">
<div class="notify-heading"><strong>Notifications</strong><% if(unreadNotifications>0){ %><form method="post" action="<%= notificationPath %>"><input type="hidden" name="csrf" value="<%= Input.html(session.getAttribute("csrf")) %>"><input type="hidden" name="action" value="read-all"><button type="submit">Mark all as read</button></form><% } %></div>
<% if(Boolean.TRUE.equals(request.getAttribute("notificationUnavailable"))){ %><p class="notify-empty">Notifications are temporarily unavailable.</p><% }else{request.setAttribute("notificationDisplayItems",request.getAttribute("notificationItems")); %><div class="notify-list"><jsp:include page="/WEB-INF/fragments/notification-items.jsp" /></div><% } %>
<a class="notify-footer" href="<%= notificationPath %>">View all notifications</a>
</div>
</details>
