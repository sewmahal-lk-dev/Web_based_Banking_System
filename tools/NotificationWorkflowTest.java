import com.banking.dao.*;
import java.net.http.*;
import java.sql.*;
import java.nio.file.*;
import java.util.*;

/** Runs inside SupportWorkflowTest's connection-local temporary tables only. */
public class NotificationWorkflowTest extends SupportWorkflowTest {
 static int total(String where)throws Exception{return ((Number)scalar("SELECT COUNT(*) FROM notification WHERE "+where)).intValue();}
 static void verify()throws Exception {
  HttpClient customer=client(1,"CUSTOMER"),other=client(2,"CUSTOMER"),card=client(3,"CARD_SERVICES_OFFICER"),loan=client(2,"LOAN_OFFICER");
  // Restore the second temporary customer disabled by the earlier admin fixture.
  execute("UPDATE customer SET status='ACTIVE' WHERE customer_id=2");fixture.commit();
  // Reset this recipient in the connection-local fixture after admin status notifications.
  execute("DELETE FROM notification WHERE customer_id=2");fixture.commit();
  String empty=get(other,"/customer/dashboard").body();
  check(empty.contains("notification-center")&&empty.contains("No notifications yet")&&!empty.contains("class=\"notify-badge\""),"empty bell renders safely with no badge");
  execute("INSERT INTO employee(employee_id,name,email,password,role,status) VALUES(90,'Inactive Card','inactive@example.invalid','unused','CARD_SERVICES_OFFICER','INACTIVE')");fixture.commit();
  new CustomerServicesDAO().ticket(1,"COMPLAINT","ATM Withdrawal Issue notifications","ATM withdrawal failed but money was deducted.");
  int id=((Number)scalar("SELECT MAX(ticket_id) FROM ticket")).intValue();
  check(total("employee_id=1 AND related_ticket_id="+id+" AND notification_type='TICKET_CREATED'")==1,"new ticket notifies Customer Service");
  check(total("customer_id=1 AND related_ticket_id="+id)==0,"customer gets no notification for own ticket creation");
  failNotification=true;try{new TicketDAO().assign(1,id,"CARD_SERVICES_OFFICER");throw new AssertionError("Expected insert failure");}catch(SQLException expected){}finally{failNotification=false;}
  check(scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("OPEN")&&total("related_ticket_id="+id)==1,"notification failure rolls back assignment atomically");
  new TicketDAO().assign(1,id,"CARD_SERVICES_OFFICER");
  check(total("employee_id=3 AND related_ticket_id="+id)==1&&total("employee_id=90 AND related_ticket_id="+id)==0,"assignment notifies active receiving staff only");
  check(total("employee_id=2 AND related_ticket_id="+id)==0&&total("customer_id=1 AND related_ticket_id="+id)==1,"unrelated department excluded and owner notified");
  var dao=new NotificationDAO();long count=dao.unread(3,false);
  String page=get(card,"/card/dashboard.jsp").body();
  check(page.contains("class=\"notify-badge\">"+count+"</span>")&&page.contains("New Assigned Support Ticket"),"staff unread badge and assignment dropdown rendered");
  long note=((Number)scalar("SELECT notification_id FROM notification WHERE employee_id=3 AND related_ticket_id="+id)).longValue();
  check(post(loan,"/employee/notifications",form("csrf","fixture-token","action","open","id",""+note)).statusCode()==404,"employee cannot open another employee notification");
  check(post(card,"/employee/notifications",form("csrf","wrong","action","open","id",""+note)).statusCode()==403,"notification CSRF failure rejected");
  var opened=post(card,"/employee/notifications",form("csrf","fixture-token","action","open","id",""+note,"redirect","https://example.invalid/","employeeId","2"));
  check(opened.statusCode()==302&&opened.headers().firstValue("location").orElse("").equals("/employee/dashboard?ticket="+id+"#support-ticket-"+id),"staff open marks own notification and uses safe ticket link ignoring forged fields");
  check(dao.unread(3,false)==count-1,"staff unread decreases exactly once");
  post(card,"/employee/notifications",form("csrf","fixture-token","action","open","id",""+note));check(dao.unread(3,false)==count-1,"opening read notification is idempotent");
  new TicketDAO().update(3,id,"ticket-status","IN_PROGRESS",null);
  int before=total("customer_id=1 AND related_ticket_id="+id);
  new TicketDAO().update(3,id,"ticket-status","IN_PROGRESS",null);
  check(total("customer_id=1 AND related_ticket_id="+id)==before,"unchanged status produces no duplicate notification");
  failNotification=true;try{new TicketDAO().update(3,id,"ticket-reply",null,"Should rollback");throw new AssertionError("Expected insert failure");}catch(SQLException expected){}finally{failNotification=false;}
  check(((Number)scalar("SELECT COUNT(*) FROM ticket_message WHERE body='Should rollback'")).intValue()==0,"notification failure rolls back staff reply");
  new TicketDAO().update(3,id,"ticket-reply",null,"Card Services is investigating your ATM transaction.");
  check(total("customer_id=1 AND related_ticket_id="+id)==3,"customer gets assignment status and reply notifications");
  page=get(customer,"/customer/dashboard").body();check(page.contains("notification-center")&&page.contains("New Support Reply")&&page.contains("Ticket Status Updated"),"customer bell renders reply and status notifications");
  var rejected=post(customer,"/customer/tickets",form("csrf","fixture-token","action","ticket","type","COMPLAINT","subject","","description","Invalid form"));
  check(rejected.body().contains("class=\"notify-badge\">"+dao.unread(1,true)+"</span>"),"validation error preserves unread bell count");
  long reply=((Number)scalar("SELECT notification_id FROM notification WHERE customer_id=1 AND related_ticket_id="+id+" AND notification_type='SUPPORT_REPLY'")).longValue();
  check(post(other,"/customer/notifications",form("csrf","fixture-token","action","open","id",""+reply,"customerId","1")).statusCode()==404,"customer cannot open another customer's notification with forged owner");
  check(!get(other,"/customer/notifications?customerId=1").body().contains("New Support Reply"),"forged list recipient ignored");
  count=dao.unread(1,true);opened=post(customer,"/customer/notifications",form("csrf","fixture-token","action","open","id",""+reply));
  check(opened.statusCode()==302&&opened.headers().firstValue("location").orElse("").equals("/customer/tickets?ticket="+id+"#ticket-"+id)&&dao.unread(1,true)==count-1,"customer opens relevant ticket and notification becomes read");
  for(String invalid:List.of("-1","0","abc","999999999","1 OR 1=1","9999999999999999999999"))check(post(customer,"/customer/notifications",form("csrf","fixture-token","action","open","id",invalid)).statusCode()==404,"invalid notification ID fails safely: "+invalid);
  check(post(customer,"/customer/notifications",form("csrf","fixture-token","action","read-all")).statusCode()==302&&dao.unread(1,true)==0,"mark all customer notifications read");
  check(dao.unread(3,false)>0,"mark all does not change staff notifications");
  new CustomerServicesDAO().reply(1,id,"Please investigate.");check(total("employee_id=3 AND related_ticket_id="+id+" AND notification_type='CUSTOMER_REPLY'")==1,"customer reply notifies assigned department");
  new TicketDAO().update(3,id,"ticket-status","ESCALATED",null);
  check(total("employee_id=3 AND related_ticket_id="+id+" AND notification_type='TICKET_ESCALATED'")==0,"escalation does not notify acting officer");
  new TicketDAO().assign(1,id,"LOAN_OFFICER");check(total("employee_id=2 AND related_ticket_id="+id)==1,"reassignment notifies new department");
  opened=post(card,"/employee/notifications",form("csrf","fixture-token","action","open","id",""+note));
  check(opened.headers().firstValue("location").orElse("").equals("/employee/notifications"),"old notification cannot reopen reassigned ticket");
  new TicketDAO().update(2,id,"ticket-status","IN_PROGRESS",null);new TicketDAO().update(2,id,"ticket-status","RESOLVED",null);
  check(total("customer_id=1 AND related_ticket_id="+id+" AND notification_type='TICKET_RESOLVED'")==1,"customer receives resolution notification");
  check(total("message LIKE '%ATM withdrawal failed%' OR message LIKE '%investigating your ATM%'")==0,"notifications omit ticket description and reply content");
  execute("INSERT INTO notification(customer_id,notification_type,title,message) VALUES(1,'TEST','<script>alert(1)</script>','<img src=x onerror=alert(1)>')");fixture.commit();
  page=get(customer,"/customer/notifications").body();check(page.contains("&lt;script&gt;")&&!page.contains("<script>alert(1)</script>"),"notification titles and messages escaped");
  for(String sql:List.of("INSERT INTO notification(notification_type,title,message) VALUES('TEST','x','x')","INSERT INTO notification(customer_id,employee_id,notification_type,title,message) VALUES(1,3,'TEST','x','x')")){try{execute(sql);throw new AssertionError("Recipient check absent");}catch(SQLException expected){check(true,"database enforces exactly one recipient");}}
 }
 static void complaintClosure()throws Exception {
  execute("INSERT INTO employee(employee_id,name,email,password,role,status) VALUES(91,'Second Service','second-service@example.invalid','unused','CUSTOMER_SERVICE_OFFICER','ACTIVE'),(92,'Inactive Service','inactive-service@example.invalid','unused','CUSTOMER_SERVICE_OFFICER','INACTIVE')");fixture.commit();
  new CustomerServicesDAO().ticket(1,"COMPLAINT","Complaint closure verification","Verify existing workflow","HIGH");
  int id=((Number)scalar("SELECT MAX(ticket_id) FROM ticket")).intValue();
  check(total("related_ticket_id="+id+" AND employee_id IN (1,91)")==2&&total("related_ticket_id="+id)==2,"complaint notifies every active Customer Service officer only");
  try{new CustomerServicesDAO().ticket(1,"COMPLAINT","Invalid priority","Details","URGENT");throw new AssertionError("URGENT complaint accepted");}catch(IllegalArgumentException expected){check(true,"complaint priority restricted to LOW MEDIUM HIGH");}
  try{new TicketDAO().update(1,id,"ticket-status","CLOSED",null);throw new AssertionError("Premature closure");}catch(IllegalArgumentException expected){check(true,"staff must resolve before closure");}
  new TicketDAO().update(1,id,"ticket-response","IN_PROGRESS","Reviewing complaint");
  new TicketDAO().update(1,id,"ticket-status","RESOLVED",null);
  var staff=client(1,"CUSTOMER_SERVICE_OFFICER");
  check(get(staff,"/employee/dashboard?ticket="+id).body().contains("Close ticket"),"resolved complaint exposes staff close action");
  check(post(staff,"/employee/dashboard",form("csrf","fixture-token","action","ticket-status","id",""+id,"status","CLOSED")).statusCode()==302&&scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("CLOSED"),"staff closes resolved complaint through existing servlet");
  check(get(client(1,"CUSTOMER"),"/customer/tickets").body().contains("Status: CLOSED"),"customer sees staff closure in conversation");
 }
 static void regression()throws Exception {
  EndToEndTest.base=base;
  List<String> routes=List.of("/service/dashboard.jsp","/loan/dashboard.jsp","/card/dashboard.jsp","/investment/dashboard.jsp","/compliance/dashboard.jsp","/admin/dashboard.jsp");
  for(int i=0;i<roles.size();i++){
   var staff=EndToEndTest.login("staff"+i+"@example.invalid","Fixture-Login-Only-2026!",routes.get(i));staff.id=i+1;EndToEndTest.staff.put(roles.get(i),staff);
  }
  var customer=EndToEndTest.register("91");var other=EndToEndTest.register("92");var closure=EndToEndTest.register("93");
  EndToEndTest.execute("UPDATE account SET balance=100000 WHERE account_number=?",customer.account);
  EndToEndTest.transfersAndPayments(customer,other);EndToEndTest.scheduled(customer);EndToEndTest.cards(customer,other);EndToEndTest.loans(customer,other);EndToEndTest.investments(customer,other);EndToEndTest.requestsAndAccounts(closure,other);EndToEndTest.adminAndProfiles(customer,other);
  check(true,"existing banking CRUD regression: "+EndToEndTest.checks+" checks passed on temporary tables");
 }
}
