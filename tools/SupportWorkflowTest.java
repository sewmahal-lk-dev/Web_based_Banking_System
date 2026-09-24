import com.banking.util.DBConnection;
import com.banking.dao.*;
import java.sql.*;
import java.util.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.lang.reflect.*;
import jakarta.servlet.http.*;
import org.apache.catalina.startup.Tomcat;

/** Uses connection-local MySQL TEMPORARY tables only. Permanent tables are never written. */
public class SupportWorkflowTest {
 static Connection fixture; static boolean failAudit,failPayment,failNotification; static String base; static int checks;
 static final List<String> roles=List.of("CUSTOMER_SERVICE_OFFICER","LOAN_OFFICER","CARD_SERVICES_OFFICER","INVESTMENT_OFFICER","COMPLIANCE_RISK_OFFICER","SYSTEM_ADMIN");
 static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;System.out.println("PASS "+name);}
 static void execute(String sql)throws Exception{try(Statement s=fixture.createStatement()){s.execute(sql);}}
 static Object scalar(String sql)throws Exception{try(Statement s=fixture.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getObject(1);}}
 static HttpClient client(int id,String role)throws Exception{
  HttpClient c=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
  c.send(HttpRequest.newBuilder(URI.create(base+"/fixture?id="+id+"&role="+role)).build(),HttpResponse.BodyHandlers.ofString());return c;
 }
 static HttpResponse<String> get(HttpClient c,String path)throws Exception{return c.send(HttpRequest.newBuilder(URI.create(base+path)).build(),HttpResponse.BodyHandlers.ofString());}
 static HttpResponse<String> post(HttpClient c,String path,String body)throws Exception{return c.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());}
 static HttpClient freshClient(){return HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();}
 static String form(String... values){List<String> parts=new ArrayList<>();for(int i=0;i<values.length;i+=2)parts.add(java.net.URLEncoder.encode(values[i],java.nio.charset.StandardCharsets.UTF_8)+"="+java.net.URLEncoder.encode(values[i+1],java.nio.charset.StandardCharsets.UTF_8));return String.join("&",parts);}
 static String csrf(HttpClient client,String path)throws Exception {
  String html=get(client,path).body();var matcher=java.util.regex.Pattern.compile("name=\"csrf-token\" content=\"([^\"]+)\"").matcher(html);
  if(matcher.find())return matcher.group(1);
  matcher=java.util.regex.Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(html);
  if(!matcher.find())throw new AssertionError("Missing CSRF token");return matcher.group(1);
 }
 static void verifyAuthenticationAndAdmin()throws Exception {
  String password="Fixture-Login-Only-2026!",hash=com.banking.util.PasswordUtil.hashPassword(password);
  // These names still refer exclusively to the connection-local temporary tables.
  execute("UPDATE customer SET password='"+hash+"'");execute("UPDATE employee SET password='"+hash+"'");fixture.commit();
  List<String> routes=List.of("/service/dashboard.jsp","/loan/dashboard.jsp","/card/dashboard.jsp","/investment/dashboard.jsp","/compliance/dashboard.jsp","/admin/dashboard.jsp");
  HttpClient customer=freshClient();var customerLogin=post(customer,"/login",form("email","one@example.invalid","password",password));
  check(customerLogin.statusCode()==302&&customerLogin.headers().firstValue("location").orElse("").endsWith("/customer/dashboard"),"real customer password login and redirect");
  check(get(customer,"/customer/dashboard").statusCode()==200,"authenticated customer dashboard");
  check(get(customer,"/employee/dashboard").statusCode()==403,"customer denied staff dashboard");
  for(int i=0;i<roles.size();i++) {
   HttpClient staff=freshClient();var login=post(staff,"/login",form("email","staff"+i+"@example.invalid","password",password));
   check(login.statusCode()==302&&login.headers().firstValue("location").orElse("").endsWith(routes.get(i)),"real password login and redirect: "+roles.get(i));
   var page=get(staff,routes.get(i));check(page.statusCode()==200&&!page.body().contains("Unable to load"),"role route Jasper renders: "+roles.get(i));
   check(get(staff,"/customer/dashboard").statusCode()==403,"staff denied customer route");
   for(int j=0;j<routes.size();j++)if(i!=j)check(get(staff,routes.get(j)).statusCode()==403,"cross-role route denied: "+roles.get(i)+" -> "+roles.get(j));
   if(i==4||i==5)check(get(staff,routes.get(i)+"?q=TICKET").body().contains("value=\"TICKET\""),"audit query renders on one valid JSP expression");
   if(i==5) {
    check(page.body().contains("employee-save")&&page.body().contains("product-save")&&page.body().contains("customer-status"),"admin employee product and customer management visible");
    String token=csrf(staff,routes.get(i));
    check(post(staff,routes.get(i),form("action","employee-save","csrf","wrong")).statusCode()==403,"admin CSRF enforced");
    String[] fields={"action","employee-save","csrf",token,"name","Created Fixture","email","created@example.invalid","phone","0771234567","role","LOAN_OFFICER","status","ACTIVE","password",password};
    check(post(staff,routes.get(i),form(fields)).statusCode()==302,"admin creates employee");
    int employee=((Number)scalar("SELECT employee_id FROM employee WHERE email='created@example.invalid'")).intValue();
    check(com.banking.util.PasswordUtil.checkPassword(password,(String)scalar("SELECT password FROM employee WHERE employee_id="+employee)),"created password is hashed");
    check(post(staff,routes.get(i),form(fields)).statusCode()==200,"duplicate employee email rejected");
    for(String[] invalid:List.of(new String[]{"email","bad-email"},new String[]{"phone","bad"},new String[]{"password","short"})) {
     String[] bad=fields.clone();for(int k=0;k<bad.length;k+=2){if(bad[k].equals("email"))bad[k+1]="validation@example.invalid";if(bad[k].equals(invalid[0]))bad[k+1]=invalid[1];}
     check(post(staff,routes.get(i),form(bad)).statusCode()==200,"employee validation preserved: "+invalid[0]);
    }
    check(post(staff,routes.get(i),form("action","employee-save","csrf",token,"id",String.valueOf(employee),"name","Updated Fixture","email","created@example.invalid","phone","0771234567","role","LOAN_OFFICER","status","ACTIVE")).statusCode()==302,"admin updates employee");
    check(get(staff,routes.get(i)).body().contains("Updated Fixture"),"admin reads updated employee");
    check(post(staff,routes.get(i),form("action","employee-status","csrf",token,"id",String.valueOf(employee),"status","INACTIVE","reason","Fixture soft delete")).statusCode()==302&&scalar("SELECT status FROM employee WHERE employee_id="+employee).equals("INACTIVE"),"employee soft delete retains record");
    check(post(freshClient(),"/login",form("email","created@example.invalid","password",password)).statusCode()==200,"inactive employee cannot log in");
    check(post(staff,routes.get(i),form("action","product-save","csrf",token,"name","Fixture Product","type","SAVINGS","description","Fixture description","status","ACTIVE")).statusCode()==302,"admin creates product");
    String product=scalar("SELECT MAX(product_id) FROM banking_product").toString();
    check(post(staff,routes.get(i),form("action","product-save","csrf",token,"id",product,"name","Updated Product","type","SAVINGS","description","Updated description","status","INACTIVE")).statusCode()==302&&scalar("SELECT status FROM banking_product WHERE product_id="+product).equals("INACTIVE"),"admin updates and soft deactivates product");
    check(get(staff,routes.get(i)).body().contains("Updated Product"),"admin reads product");
    check(post(staff,routes.get(i),form("action","customer-status","csrf",token,"id","2","status","INACTIVE")).statusCode()==302&&scalar("SELECT status FROM customer WHERE customer_id=2").equals("INACTIVE"),"admin customer status management");
    check(((Number)scalar("SELECT COUNT(*) FROM audit_log WHERE action='EMPLOYEE_CREATE'")).intValue()==1,"admin operation audit retained");
   }
   check(get(staff,"/logout").statusCode()==302&&get(staff,routes.get(i)).statusCode()==302,"logout invalidates staff session");
  }
  check(post(freshClient(),"/login",form("email","staff5@example.invalid","password","wrong")).statusCode()==200,"incorrect password rejected");
 }
 static void verifyBillPayments()throws Exception {
  execute("INSERT INTO account(account_number,customer_id,account_type,balance,status,open_date) VALUES(900,1,'SAVINGS',10000,'ACTIVE',CURDATE())");fixture.commit();
  HttpClient customer=client(1,"CUSTOMER");
  String page=get(customer,"/customer/payments").body();
  check(page.contains("name=\"billReference\"")&&page.contains("value=\"fixture-token\""),"bill reference form and corrected CSRF token render");
  check(get(customer,"/customer/transactions").body().contains("Legacy Provider"),"legacy NULL-reference payment renders");
  String body=form("csrf","fixture-token","recipient","CEB / Electricity","billReference","  CEB/123-456  ","amount","5000.00");
  check(post(customer,"/customer/payments",body).statusCode()==302,"new bill payment succeeds");
  check(new java.math.BigDecimal("5000.00").compareTo((java.math.BigDecimal)scalar("SELECT balance FROM account WHERE account_number=900"))==0,"bill deducts exact amount");
  check(scalar("SELECT bill_reference FROM payment WHERE reference_number<>'LEGACY-FIXTURE' ORDER BY payment_id DESC LIMIT 1").equals("CEB/123-456"),"trimmed bill reference persisted in MySQL fixture");
  check(get(customer,"/customer/transactions").body().contains("Consumer / Bill No: CEB/123-456"),"bill reference visible in history");
  int count=((Number)scalar("SELECT COUNT(*) FROM payment")).intValue();
  for(String ref:List.of("","   ","<script>","a".repeat(101),"/", "12\n34")){
   check(post(customer,"/customer/payments",form("csrf","fixture-token","recipient","CEB","billReference",ref,"amount","1")).statusCode()==200,"invalid bill reference rejected");
  }
  check(post(customer,"/customer/payments",form("csrf","fixture-token","recipient","CEB","amount","1")).statusCode()==200,"missing bill reference rejected");
  var insufficient=post(customer,"/customer/payments",body.replace("5000.00","6000.00"));
  check(insufficient.statusCode()==200&&insufficient.body().contains("Insufficient account balance"),"insufficient funds reported");
  failPayment=true;try{new PaymentDAO().makeBillPayment(1,"CEB","123456",new java.math.BigDecimal("10"));throw new AssertionError("Expected insert failure");}catch(SQLException expected){}finally{failPayment=false;}
  check(((Number)scalar("SELECT COUNT(*) FROM payment")).intValue()==count&&new java.math.BigDecimal("5000.00").compareTo((java.math.BigDecimal)scalar("SELECT balance FROM account WHERE account_number=900"))==0,"invalid input insufficient funds and insert failure preserve balance and payment count");
  String date=java.time.LocalDateTime.now().plusDays(1).withNano(0).toString();
  long scheduled=new ScheduledPaymentDAO().save(1,null,"Water","WATER-123","25.00",date);
  new ScheduledPaymentDAO().save(1,scheduled,"Water","WATER/456","30.00",date);
  check(scalar("SELECT bill_reference FROM payment WHERE payment_id="+scheduled).equals("WATER/456"),"scheduled bill reference created and updated");
  check(get(customer,"/customer/payments").body().contains("WATER/456"),"scheduled reference renders");
  execute("UPDATE payment SET scheduled_date=NOW() WHERE payment_id="+scheduled);fixture.commit();new ScheduledPaymentDAO().execute(1,scheduled);
  check(scalar("SELECT status FROM payment WHERE payment_id="+scheduled).equals("COMPLETED"),"scheduled bill executes with stored reference");
 }
 static void verifyAuditSearch()throws Exception {
  execute("INSERT INTO audit_log(action,details) VALUES('CARD_BLOCK','Fixture blocked'),('CARD_UNBLOCK','Fixture unblocked'),('CARD_APPROVE','Fixture approved'),('INVESTMENT_REQUEST','Fixture requested'),('INVESTMENT_FUND','Fixture funded'),('NOTE','Fixture card details'),('CARDXBLOCK','Underscore decoy')");fixture.commit();
  for(String role:List.of("SYSTEM_ADMIN","COMPLIANCE_RISK_OFFICER")) {
   int employee=role.equals("SYSTEM_ADMIN")?6:5;String route=employee==6?"/admin/dashboard.jsp":"/compliance/dashboard.jsp";HttpClient staff=client(employee,role);
   for(String keyword:List.of("CARD","CARD_BLOCK","INVESTMENT","card","")) {
    var response=get(staff,route+"?q="+keyword);String html=response.body();
    check(response.statusCode()==200&&html.contains("action=\""+route+"#Audit-log\"")&&html.contains("id=\"Audit-log\""),"audit form targets current dashboard Audit-log: "+role+" / "+keyword);
    var rows=new EmployeeDAO().dashboard(role,keyword).get("Audit log");
    if(keyword.equals("CARD_BLOCK"))check(rows.size()==1&&rows.get(0).get("action").equals("CARD_BLOCK"),"literal underscore filters CARD_BLOCK only");
    else if(keyword.equals("INVESTMENT"))check(rows.size()==2,"investment request and funding both found");
    else if(!keyword.isEmpty())check(rows.size()==5&&rows.stream().allMatch(r->(r.get("action")+" "+r.get("details")).toLowerCase().contains("card")),"case-insensitive partial action/details matching");
    else check(rows.size()==((Number)scalar("SELECT COUNT(*) FROM audit_log")).intValue(),"empty search returns normal audit records");
    String auditHtml=html.substring(html.indexOf("id=\"Audit-log\""));
    if(keyword.equals("CARD_BLOCK"))check(auditHtml.contains("CARD_BLOCK")&&!auditHtml.contains("CARD_UNBLOCK")&&!auditHtml.contains("CARDXBLOCK"),"rendered audit records are filtered");
    if(keyword.equals("INVESTMENT"))check(auditHtml.contains("INVESTMENT_REQUEST")&&auditHtml.contains("INVESTMENT_FUND")&&!auditHtml.contains("CARD_BLOCK"),"rendered investment search excludes unrelated records");
   }
  }
 }
 static HttpClient login(String email)throws Exception {
  HttpClient c=freshClient();
  check(post(c,"/login",form("email",email,"password","Fixture-Login-Only-2026!")).statusCode()==302,"password login: "+email);
  return c;
 }
 static String ticketForm(String html,int id,String action)throws Exception {
  var article=java.util.regex.Pattern.compile("<article[^>]*id=\"support-ticket-"+id+"\"[^>]*>(.*?)</article>",java.util.regex.Pattern.DOTALL).matcher(html);
  check(article.find(),"ticket article visibly rendered: "+id);
  var forms=java.util.regex.Pattern.compile("<form[^>]*>(.*?)</form>",java.util.regex.Pattern.DOTALL).matcher(article.group(1));
  while(forms.find())if(forms.group(1).contains("value=\""+action+"\""))return forms.group(1);
  throw new AssertionError("Rendered form missing: "+action);
 }
 static void verifyDepartmentRouting()throws Exception {
  HttpClient customer=login("one@example.invalid");
  check(post(customer,"/customer/tickets",form("csrf",csrf(customer,"/customer/tickets"),"action","ticket","type","COMPLAINT","subject","ATM Withdrawal Issue","description","ATM withdrawal failed but money was deducted.")).statusCode()==302,"routing 1: customer creates ATM Withdrawal Issue");
  int id=((Number)scalar("SELECT MAX(ticket_id) FROM ticket")).intValue();
  check(scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("OPEN"),"routing 2: status OPEN");
  HttpClient service=login("staff0@example.invalid");String path="/service/dashboard.jsp",token=csrf(service,path);
  String rendered=get(service,path).body();String assignForm=ticketForm(rendered,id,"ticket-assign");
  check(assignForm.contains("name=\"assignedRole\"")&&assignForm.contains("Assign Ticket")&&assignForm.contains("value=\"CARD_SERVICES_OFFICER\""),"routing 3-5: visible assignment dropdown and button");
  for(String role:roles.subList(0,5))check(assignForm.contains("value=\""+role+"\""),"assignment option rendered: "+role);
  check(!assignForm.contains("SYSTEM_ADMIN"),"admin excluded from assignment dropdown");
  for(String invalid:List.of("SYSTEM_ADMIN","CUSTOMER","arbitrary", "CARD_SERVICES_OFFICER' OR 1=1")) {
   post(service,path,form("csrf",token,"action","ticket-assign","id",String.valueOf(id),"assignedRole",invalid));
   check(scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("OPEN"),"invalid destination rejected: "+invalid);
  }
  check(post(service,path,form("csrf","wrong","action","ticket-assign","id",String.valueOf(id),"assignedRole","CARD_SERVICES_OFFICER")).statusCode()==403,"assignment CSRF enforced");
  String assign=form("csrf",token,"action","ticket-assign","id",String.valueOf(id),"assignedRole","CARD_SERVICES_OFFICER");
  failAudit=true;try{post(service,path,assign);}finally{failAudit=false;}
  check(scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("OPEN")&&((Number)scalar("SELECT COUNT(*) FROM ticket_message WHERE ticket_id="+id)).intValue()==0,"assignment audit failure rolls back status and timeline");
  check(post(service,path,assign).statusCode()==302,"routing 6: submit rendered Assign Ticket form");
  check(scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("ASSIGNED")&&scalar("SELECT assigned_role FROM ticket WHERE ticket_id="+id).equals("CARD_SERVICES_OFFICER"),"routing 7-8: ASSIGNED and department persisted in MySQL");
  check(get(service,path).body().contains("Ticket assigned to Card Services successfully."),"assignment success flash visible");
  check(((Number)scalar("SELECT COUNT(*) FROM audit_log WHERE action='TICKET_ASSIGN' AND employee_id=1 AND details LIKE '%destination CARD_SERVICES_OFFICER%'")).intValue()==1,"assignment audit identifies actor ticket and destination");
  check(get(service,"/logout").statusCode()==302,"routing 9: service officer logs out");
  HttpClient card=login("staff2@example.invalid");String cardPath="/card/dashboard.jsp",cardToken=csrf(card,cardPath);
  String cardPage=get(card,cardPath+"?ticket="+id).body();
  check(cardPage.contains("Assigned Support Tickets")&&cardPage.contains("ATM Withdrawal Issue")&&cardPage.contains("<details open>"),"routing 10-12: card queue and opened ticket visibly render");
  check(!cardPage.contains("value=\"ticket-assign\""),"card officer has no assignment controls");
  ticketForm(cardPage,id,"ticket-status");
  check(post(card,cardPath,form("csrf",cardToken,"action","ticket-status","id",String.valueOf(id),"status","IN_PROGRESS")).statusCode()==302,"routing 13: card changes status using rendered form");
  String response="Card Services is investigating your ATM transaction.";
  ticketForm(get(card,cardPath).body(),id,"ticket-reply");
  check(post(card,cardPath,form("csrf",cardToken,"action","ticket-reply","id",String.valueOf(id),"reason",response)).statusCode()==302,"routing 14: card sends reply using rendered form");
  HttpClient loan=login("staff1@example.invalid");String loanToken=csrf(loan,"/loan/dashboard.jsp");
  check(!get(loan,"/loan/dashboard.jsp?ticket="+id).body().contains("ATM Withdrawal Issue"),"loan cannot view card department ticket");
  for(String action:List.of("ticket-response","ticket-reply","ticket-status","ticket-assign"))post(loan,"/loan/dashboard.jsp",form("csrf",loanToken,"action",action,"id",String.valueOf(id),"status","RESOLVED","reason","Forbidden loan reply","assignedRole","LOAN_OFFICER","role","CARD_SERVICES_OFFICER"));
  check(scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("IN_PROGRESS")&&scalar("SELECT assigned_role FROM ticket WHERE ticket_id="+id).equals("CARD_SERVICES_OFFICER")&&((Number)scalar("SELECT COUNT(*) FROM ticket_message WHERE body='Forbidden loan reply'")).intValue()==0,"loan cannot modify or reassign card ticket even with forged role");
  String customerToken=csrf(customer,"/customer/tickets");
  check(post(customer,"/employee/dashboard",form("csrf",customerToken,"action","ticket-assign","id",String.valueOf(id),"assignedRole","LOAN_OFFICER")).statusCode()==403,"customer denied staff assignment endpoint");
  post(customer,"/customer/tickets",form("csrf",customerToken,"action","ticket-assign","id",String.valueOf(id),"assignedRole","LOAN_OFFICER"));
  check(scalar("SELECT assigned_role FROM ticket WHERE ticket_id="+id).equals("CARD_SERVICES_OFFICER"),"customer route rejects assignment action");
  check(get(card,"/logout").statusCode()==302,"routing 15: card logs out");
  customer=login("one@example.invalid");String customerPage=get(customer,"/customer/tickets").body();
  check(customerPage.contains("Card Services")&&customerPage.contains(response)&&customerPage.contains("Card Services Officer")&&customerPage.contains("IN PROGRESS")&&customerPage.contains("ticket-progress")&&customerPage.contains("Assigned to Card Services"),"routing 16-17: customer sees department status reply history and timeline");
  check(!customerPage.contains("value=\"ticket-assign\""),"customer has no assignment control");
  card=login("staff2@example.invalid");cardToken=csrf(card,cardPath);
  ticketForm(get(card,cardPath).body(),id,"ticket-status");
  check(post(card,cardPath,form("csrf",cardToken,"action","ticket-status","id",String.valueOf(id),"status","RESOLVED")).statusCode()==302,"routing 18: authorized card officer resolves");
  check(scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("RESOLVED")&&get(customer,"/customer/tickets").body().contains("Status: RESOLVED"),"routing 19: customer sees RESOLVED");
  check(post(customer,"/customer/tickets",form("csrf",csrf(customer,"/customer/tickets"),"action","ticket-close","id",String.valueOf(id))).statusCode()==302,"routing 20: customer closes");
  check(scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("CLOSED"),"routing 21: CLOSED persisted");
  service=login("staff0@example.invalid");token=csrf(service,path);
  post(service,path,form("csrf",token,"action","ticket-assign","id",String.valueOf(id),"assignedRole","LOAN_OFFICER"));
  check(scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("CLOSED")&&scalar("SELECT assigned_role FROM ticket WHERE ticket_id="+id).equals("CARD_SERVICES_OFFICER"),"closed ticket cannot be reassigned");
  // Exercise each remaining department through its actual dashboard and rendered forms.
  List<String> paths=List.of("/service/dashboard.jsp","/loan/dashboard.jsp","/card/dashboard.jsp","/investment/dashboard.jsp","/compliance/dashboard.jsp");
  for(int index:List.of(0,1,3,4)) {
   String destination=roles.get(index);new CustomerServicesDAO().ticket(1,"INQUIRY","Routing "+destination,"Department queue test");
   int routed=((Number)scalar("SELECT MAX(ticket_id) FROM ticket")).intValue();
   check(post(service,path,form("csrf",token,"action","ticket-assign","id",String.valueOf(routed),"assignedRole",destination)).statusCode()==302,"assign to "+destination);
   HttpClient staff=login("staff"+index+"@example.invalid");String route=paths.get(index),staffToken=csrf(staff,route);
   String page=get(staff,route+"?ticket="+routed).body();ticketForm(page,routed,"ticket-reply");
   check(page.contains("Routing "+destination),"assigned ticket visibly listed: "+destination);
   check(post(staff,route,form("csrf",staffToken,"action","ticket-response","id",String.valueOf(routed),"status","IN_PROGRESS","reason","Reply from "+destination)).statusCode()==302,"department processes and replies: "+destination);
   check(post(staff,route,form("csrf",staffToken,"action","ticket-status","id",String.valueOf(routed),"status","RESOLVED")).statusCode()==302,"department resolves: "+destination);
  }
  new CustomerServicesDAO().ticket(1,"INQUIRY","Reassign active ticket","Reassignment test");
  int moved=((Number)scalar("SELECT MAX(ticket_id) FROM ticket")).intValue();
  for(String destination:List.of("CARD_SERVICES_OFFICER","LOAN_OFFICER"))check(post(service,path,form("csrf",token,"action","ticket-assign","id",String.valueOf(moved),"assignedRole",destination)).statusCode()==302,"service reassigns active ticket: "+destination);
  check(!get(card,cardPath).body().contains("Reassign active ticket")&&get(loan,"/loan/dashboard.jsp").body().contains("Reassign active ticket"),"reassignment removes old queue access and grants new queue access");
 }
 public static void main(String[] args)throws Exception{
  // Read schema metadata, then shadow EVERY base table on this connection before any DML.
  fixture=DBConnection.getConnection();List<String> tables=new ArrayList<>();
  try(Statement s=fixture.createStatement();ResultSet r=s.executeQuery("SHOW FULL TABLES WHERE Table_type='BASE TABLE'")){while(r.next())tables.add(r.getString(1));}
  for(String name:tables){if(!name.matches("[a-z_]+"))throw new AssertionError("Unexpected table name");try(Statement st=fixture.createStatement();ResultSet rs=st.executeQuery("SHOW CREATE TABLE `"+name+"`")) {
    rs.next();String ddl=rs.getString(2).replace("CREATE TABLE", "CREATE TEMPORARY TABLE").replaceAll("(?m)^  CONSTRAINT[^\n]*FOREIGN KEY[^\n]*\n", "").replace(",\n)", "\n)");
    execute(ddl);
   }}
  for(String required:List.of("customer","employee","ticket","audit_log"))if(!tables.contains(required))throw new AssertionError("Missing fixture table");
  if(!tables.contains("ticket_message")) execute("CREATE TEMPORARY TABLE ticket_message(message_id BIGINT AUTO_INCREMENT PRIMARY KEY,ticket_id INT NOT NULL,sender_label VARCHAR(100) NOT NULL,body TEXT NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
  try(Statement st=fixture.createStatement();ResultSet rs=st.executeQuery("SHOW COLUMNS FROM ticket LIKE 'assigned_role'")) {
   if(!rs.next()) for(String statement:Files.readString(Path.of("database/migrations/V6__ticket_department_routing.sql")).replaceAll("(?m)^--.*$", "").split(";")) if(!statement.isBlank())execute(statement);
  }
  if(!tables.contains("notification")) {
   String ddl=Files.readString(Path.of("database/migrations/V7__notifications.sql")).replace("CREATE TABLE notification", "CREATE TEMPORARY TABLE notification").replaceAll("(?m)^  CONSTRAINT notification_(customer|employee|ticket) .*\n", "");
   execute(ddl);
  }
  try {
   // All DDL below targets the shadowed temporary payment table, never permanent data.
   execute("INSERT INTO payment(account_number,payment_type,recipient,amount,status,reference_number) VALUES(900,'BILL_PAYMENT','Legacy Provider',1,'COMPLETED','LEGACY-FIXTURE')");
   if (!fixture.getMetaData().getColumns(null,null,"payment","bill_reference").next()) {
    String migration=Files.readString(Path.of("database/migrations/V4__bill_reference.sql")).replaceAll("(?m)^--.*$", "").trim();
    execute(migration);
   }
   check(scalar("SELECT bill_reference IS NULL FROM payment WHERE reference_number='LEGACY-FIXTURE'").toString().equals("1"),"V4 preserves existing payment with NULL reference");
   // MySQL cannot reference one temporary table twice in a query. Empty twin for history subqueries.
   execute("CREATE TEMPORARY TABLE support_account_copy LIKE account");
   execute("CREATE TEMPORARY TABLE support_repayment_copy LIKE loan_repayment");
   execute("INSERT INTO customer(customer_id,name,email,password,status) VALUES(1,'Fixture Customer','one@example.invalid','unused','ACTIVE'),(2,'Other Customer','two@example.invalid','unused','ACTIVE')");
   for(int i=0;i<roles.size();i++)execute("INSERT INTO employee(employee_id,name,email,password,role,status) VALUES("+(i+1)+",'Fixture Staff','staff"+i+"@example.invalid','unused','"+roles.get(i)+"','ACTIVE')");
   for(Driver d:Collections.list(DriverManager.getDrivers()))DriverManager.deregisterDriver(d);
   DriverManager.registerDriver(new Driver(){
    public Connection connect(String u,Properties p){return (Connection)java.lang.reflect.Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class[]{Connection.class},(o,m,a)->{
     if(m.getName().equals("close"))return null;
     if(m.getName().equals("prepareStatement")&&failNotification&&((String)a[0]).startsWith("INSERT INTO notification"))throw new SQLException("Injected notification failure");
     if(m.getName().equals("prepareStatement")&&failPayment&&((String)a[0]).startsWith("INSERT INTO payment"))throw new SQLException("Injected payment insert failure");
     if(m.getName().equals("prepareStatement")&&failAudit&&((String)a[0]).startsWith("INSERT INTO audit_log"))throw new SQLException("Injected fixture audit failure");
     if(m.getName().equals("prepareStatement") && ((String)a[0]).contains("FROM loan_repayment r WHERE")) {
      execute("DELETE FROM support_repayment_copy");execute("INSERT INTO support_repayment_copy SELECT * FROM loan_repayment");
      a=a.clone();a[0]=((String)a[0]).replace("FROM loan_repayment r WHERE","FROM support_repayment_copy r WHERE");
     }
     if(m.getName().equals("prepareStatement") && ((String)a[0]).contains("FROM account r ")) {
      // Refresh the connection-local twin; MySQL disallows reopening one temporary table in a query.
      execute("DELETE FROM support_account_copy");execute("INSERT INTO support_account_copy SELECT * FROM account");
      a=a.clone();a[0]=((String)a[0]).replace("FROM account r ","FROM support_account_copy r ");
     }
     try{return m.invoke(fixture,a);}catch(InvocationTargetException e){throw e.getCause();}
    });}
    public boolean acceptsURL(String u){return true;}public DriverPropertyInfo[] getPropertyInfo(String u,Properties p){return new DriverPropertyInfo[0];}public int getMajorVersion(){return 1;}public int getMinorVersion(){return 0;}public boolean jdbcCompliant(){return false;}public java.util.logging.Logger getParentLogger(){return java.util.logging.Logger.getGlobal();}
   });
   Tomcat t=new Tomcat();t.setBaseDir(Path.of("target/support-tomcat").toAbsolutePath().toString());t.setPort(0);t.getConnector().setProperty("address","127.0.0.1");
   var context=t.addWebapp("",Path.of("target/WebBasedBankingSystem").toAbsolutePath().toString());context.setParentClassLoader(SupportWorkflowTest.class.getClassLoader());var loader=new org.apache.catalina.loader.WebappLoader();loader.setDelegate(true);context.setLoader(loader);
   Tomcat.addServlet(context,"fixture",new HttpServlet(){protected void doGet(HttpServletRequest q,HttpServletResponse r){var s=q.getSession();s.setAttribute("userId",Integer.valueOf(q.getParameter("id")));s.setAttribute("role",q.getParameter("role"));s.setAttribute("userType","CUSTOMER".equals(q.getParameter("role"))?"CUSTOMER":"EMPLOYEE");s.setAttribute("csrf","fixture-token");s.setAttribute("userName","Fixture Customer");}});context.addServletMappingDecoded("/fixture","fixture");
   try{
    t.start();base="http://127.0.0.1:"+t.getConnector().getLocalPort();HttpClient owner=client(1,"CUSTOMER"),other=client(2,"CUSTOMER"),officer=client(1,roles.get(0));
    check(get(HttpClient.newHttpClient(),"/customer/tickets").statusCode()==302,"anonymous access rejected");
    for(String page:List.of("tickets","requests","cards","loans","investments","settings","transactions","products","dashboard","accounts","transfer","payments")){var pageResponse=get(owner,"/customer/"+page);check(pageResponse.statusCode()==200&&!pageResponse.body().contains("Unable to load"),"customer JSP HTTP 200: "+page);}
    String body="csrf=fixture-token&action=ticket&type=COMPLAINT&subject=Fixture+complaint&description=Please+investigate&priority=HIGH";
    check(post(owner,"/customer/tickets",body.replace("csrf=fixture-token&", "")).statusCode()==403,"missing CSRF rejected");
    check(post(owner,"/customer/tickets",body.replace("fixture-token","wrong")).statusCode()==403,"CSRF rejected");check(((Number)scalar("SELECT COUNT(*) FROM ticket")).intValue()==0,"invalid CSRF creates no ticket");
    check(post(owner,"/customer/tickets",body).statusCode()==302,"customer creates complaint");
    int id=((Number)scalar("SELECT MAX(ticket_id) FROM ticket")).intValue();
    check(scalar("SELECT ticket_type FROM ticket WHERE ticket_id="+id).equals("COMPLAINT")&&scalar("SELECT customer_id FROM ticket WHERE ticket_id="+id).toString().equals("1"),"complaint category and session owner stored");
    check(scalar("SELECT priority FROM ticket WHERE ticket_id="+id).equals("HIGH"),"complaint priority stored");
    check(((Number)scalar("SELECT COUNT(*) FROM notification WHERE related_ticket_id="+id+" AND employee_id=1 AND title='New Customer Complaint' AND message LIKE '%Fixture Customer%'")).intValue()==1,"named complaint notification sent to Customer Service");
    check(((Number)scalar("SELECT COUNT(*) FROM notification WHERE related_ticket_id="+id+" AND employee_id<>1")).intValue()==0,"complaint creation excludes all unrelated roles");
    get(owner,"/customer/tickets");get(owner,"/customer/tickets");
    check(((Number)scalar("SELECT COUNT(*) FROM ticket")).intValue()==1&&((Number)scalar("SELECT COUNT(*) FROM notification WHERE related_ticket_id="+id)).intValue()==1,"refresh after redirect duplicates neither complaint nor notification");
    String complaintPage=get(owner,"/customer/tickets?type=COMPLAINT").body();
    check(complaintPage.contains("Submit Complaint")&&complaintPage.contains("name=\"type\" value=\"COMPLAINT\"")&&complaintPage.contains("name=\"priority\""),"dedicated complaint entry uses existing ticket form and priority");
    check(post(owner,"/customer/tickets",body.replace("priority=HIGH","priority=INVALID")).statusCode()==200&&((Number)scalar("SELECT COUNT(*) FROM ticket")).intValue()==1,"invalid priority rejected without creating ticket");
    check(get(owner,"/customer/tickets").body().contains("Fixture complaint"),"customer reads own ticket");check(!get(other,"/customer/tickets").body().contains("Fixture complaint"),"other customer cannot read ticket");
    String edit="csrf=fixture-token&action=ticket-update&id="+id+"&subject=Updated+complaint&description=More+details";
    check(post(other,"/customer/tickets",edit).statusCode()==200&&scalar("SELECT subject FROM ticket WHERE ticket_id="+id).equals("Fixture complaint"),"other customer cannot update ticket");
    post(other,"/customer/tickets","csrf=fixture-token&action=ticket-close&id="+id);check(scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("OPEN"),"other customer cannot close ticket");
    check(post(owner,"/customer/tickets",edit).statusCode()==302,"owner edits OPEN unassigned ticket");
    check(get(officer,"/employee/dashboard").body().contains("Updated complaint")&&get(officer,"/employee/dashboard").body().contains("one@example.invalid"),"officer sees complaint and customer details");
    String reply=form("csrf","fixture-token","action","ticket-reply","id",String.valueOf(id),"reply","Customer reply <script>alert(1)</script>");
    post(other,"/customer/tickets",reply);
    check(((Number)scalar("SELECT COUNT(*) FROM ticket_message")).intValue()==0,"other customer cannot reply");
    check(post(owner,"/customer/tickets",reply.replace("fixture-token","wrong")).statusCode()==403,"reply CSRF enforced");
    check(post(owner,"/customer/tickets",reply).statusCode()==302,"owner sends reply");
    String thread=get(owner,"/customer/tickets").body();
    check(thread.contains("&lt;script&gt;")&&!thread.contains("<script>alert(1)</script>"),"reply is escaped");
    check(thread.contains("aria-current=\"step\"")&&thread.contains("Send Reply"),"support tracker and reply composer render");
    check(get(officer,"/employee/dashboard").body().contains("Customer reply"),"officer sees customer reply");
    String respond="csrf=fixture-token&action=ticket-response&id="+id+"&status=IN_PROGRESS&reason=Investigating";
    for(int i=1;i<roles.size();i++){HttpClient staff=client(i+1,roles.get(i));post(staff,"/employee/dashboard",respond);check(scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("OPEN"),"unauthorized role cannot respond: "+roles.get(i));}
    check(post(officer,"/employee/dashboard",respond).statusCode()==302,"officer processes ticket");
    post(owner,"/customer/tickets",edit.replace("Updated+complaint","Forbidden+edit"));check(scalar("SELECT subject FROM ticket WHERE ticket_id="+id).equals("Updated complaint"),"processed ticket cannot be edited by customer");
    check(post(officer,"/employee/dashboard",respond.replace("IN_PROGRESS","RESOLVED").replace("Investigating","Issue+resolved")).statusCode()==302,"officer resolves with response");
    check(get(owner,"/customer/tickets").body().contains("Issue resolved"),"customer reads staff response");
    check(post(owner,"/customer/tickets","csrf=fixture-token&action=ticket-close&id="+id).statusCode()==302&&scalar("SELECT status FROM ticket WHERE ticket_id="+id).equals("CLOSED"),"soft close retains record");
    check(((Number)scalar("SELECT COUNT(*) FROM audit_log")).intValue()==6,"create update reply process resolve close audited");
    check(((Number)scalar("SELECT COUNT(*) FROM ticket_message")).intValue()==5,"both officer replies and status events retained with customer reply");
    post(owner,"/customer/tickets",reply);
    check(((Number)scalar("SELECT COUNT(*) FROM ticket_message")).intValue()==5,"closed ticket rejects replies");
    check(!get(owner,"/customer/tickets").body().contains("Send Reply"),"closed ticket hides reply composer");
    failAudit=true;try{new CustomerServicesDAO().ticket(1,"INQUIRY","Rollback fixture","Details");throw new AssertionError("Expected failure");}catch(SQLException expected){}finally{failAudit=false;}
    check(((Number)scalar("SELECT COUNT(*) FROM ticket")).intValue()==1,"audit failure rolls back ticket creation");
    for(int i=0;i<roles.size();i++)check(get(client(i+1,roles.get(i)),"/employee/dashboard").statusCode()==200,"staff Jasper HTTP 200: "+roles.get(i));
    check(get(owner,"/customer/dashboard").body().contains("/customer/tickets"),"dashboard support link targets ticket page");
    verifyAuthenticationAndAdmin();
    if(!Arrays.asList(args).contains("--support-only"))verifyBillPayments();
    verifyAuditSearch();
    verifyDepartmentRouting();
    NotificationWorkflowTest.verify();
    NotificationWorkflowTest.complaintClosure();
    if(!Arrays.asList(args).contains("--support-only"))NotificationWorkflowTest.regression();
    Files.writeString(Path.of("target/notification-test-url.txt"),base);
    Process browser=new ProcessBuilder("node","tools/notification-browser.mjs",base).inheritIO().start();
    if(!browser.waitFor(90,java.util.concurrent.TimeUnit.SECONDS)){browser.destroyForcibly();throw new AssertionError("Browser test timeout");}
    check(browser.exitValue()==0,"real browser notification interactions");
    System.out.println("ALL "+checks+" SUPPORT CHECKS PASSED; only connection-local temporary fixtures used.");
   }finally{t.stop();t.destroy();}
  }finally{fixture.close();}
 }
}


