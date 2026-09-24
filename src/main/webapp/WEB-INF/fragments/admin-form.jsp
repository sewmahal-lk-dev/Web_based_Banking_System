<%@ page import="java.util.*,com.banking.util.*,com.banking.dao.AdminDAO" %>
<% boolean employeeForm="employee".equals(request.getAttribute("adminFormType"));Map<String,Object> edit=(Map<String,Object>)request.getAttribute("adminRecord");if(edit==null)edit=Collections.emptyMap();Object id=edit.get(employeeForm?"employee_id":"product_id"); %>
<form method="post"><input type="hidden" name="csrf" value="<%= session.getAttribute("csrf") %>"><input type="hidden" name="action" value="<%= employeeForm?"employee-save":"product-save" %>">
<% if(id!=null){ %><input type="hidden" name="id" value="<%= id %>"><% } %>
<label>Name<input name="name" maxlength="100" value="<%= Input.html(edit.get(employeeForm?"name":"product_name")) %>" required></label>
<% if(employeeForm){ %>
<label>Email<input type="email" name="email" maxlength="150" value="<%= Input.html(edit.get("email")) %>" required></label>
<label>Phone<input name="phone" maxlength="20" value="<%= Input.html(edit.get("phone")) %>" required></label>
<label>Role<select name="role"><% for(String roleOption:AdminDAO.ROLES){ %><option <%= roleOption.equals(edit.get("role"))?"selected":"" %>><%= roleOption %></option><% } %></select></label>
<% if(id==null){ %><label>Initial password<input type="password" name="password" minlength="8" autocomplete="new-password" required></label><% } %>
<% }else{ %><label>Product type<input name="type" maxlength="50" value="<%= Input.html(edit.get("product_type")) %>" required></label><label>Description<textarea name="description" maxlength="500" required><%= Input.html(edit.get("description")) %></textarea></label><% } %>
<label>Status<select name="status"><option>ACTIVE</option><option <%= "INACTIVE".equals(edit.get("status"))?"selected":"" %>>INACTIVE</option></select></label><button><%= id==null?"Create":"Save changes" %></button></form>
