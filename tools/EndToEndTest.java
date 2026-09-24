import com.banking.util.*;
import com.banking.dao.AdminDAO;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;

/** Real HTTP/JSP/MySQL workflow tests. Refuses any database except banking_test_TIMESTAMP. */
public class EndToEndTest {
    static int checks;static String base;static final String RUN=Long.toString(System.currentTimeMillis());
    static final String PASSWORD="Test-Only-"+RUN+"!";
    static final Map<String,User> staff=new LinkedHashMap<>();
    static class User {
        final HttpClient client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).followRedirects(HttpClient.Redirect.NEVER).build();
        String csrf,email;int id;long account;
    }
    static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;System.out.println("PASS "+label);}
    static Map<String,String> data(String...values){Map<String,String> map=new LinkedHashMap<>();for(int i=0;i<values.length;i+=2)map.put(values[i],values[i+1]);return map;}
    static HttpResponse<String> get(User user,String path)throws Exception{return user.client.send(HttpRequest.newBuilder(URI.create(base+path)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    static HttpResponse<String> post(User user,String path,Map<String,String> values)throws Exception{
        Map<String,String> copy=new LinkedHashMap<>(values);if(user.csrf!=null)copy.put("csrf",user.csrf);
        String body=copy.entrySet().stream().map(e->URLEncoder.encode(e.getKey(),StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
        return user.client.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    static void ok(User user,String path,String...values)throws Exception{var response=post(user,path,data(values));if(response.statusCode()!=302){Files.writeString(Path.of("target/e2e-last-error.html"),response.body());throw new AssertionError("Expected redirect for "+path+" "+Arrays.toString(values.length>2?Arrays.copyOf(values,2):values)+"; HTTP "+response.statusCode());}check(true,"operation "+path+" "+(values.length>1?values[1]:""));}
    static void fail(User user,String path,String...values)throws Exception{var response=post(user,path,data(values));check(response.statusCode()==200 || response.statusCode()==403,"invalid operation rejected "+path);check(!response.body().contains("Your changes were saved successfully"),"failed operation has no success message");}
    static Object scalar(String sql,Object...values)throws Exception{try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){for(int i=0;i<values.length;i++)ps.setObject(i+1,values[i]);try(ResultSet rs=ps.executeQuery()){if(!rs.next())return null;return rs.getObject(1);}}}
    static long number(String sql,Object...values)throws Exception{return ((Number)scalar(sql,values)).longValue();}
    static void execute(String sql,Object...values)throws Exception{try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){for(int i=0;i<values.length;i++)ps.setObject(i+1,values[i]);ps.executeUpdate();}}
    static BigDecimal balance(User user)throws Exception{return (BigDecimal)scalar("SELECT balance FROM account WHERE account_number=?",user.account);}
    static void equalMoney(BigDecimal expected,BigDecimal actual,String label){check(expected.compareTo(actual)==0,label);}
    static User login(String email,String password,String destination)throws Exception{
        User user=new User();user.email=email;var response=post(user,"/login",data("email",email,"password",password));
        check(response.statusCode()==302 && response.headers().firstValue("location").orElse("").endsWith(destination),"login redirect "+destination);
        String page=get(user,destination.startsWith("/customer")?"/customer/settings":"/employee/dashboard").body();
        Matcher matcher=Pattern.compile("(?:name=\"csrf\" value=\"|name=\"csrf-token\" content=\")([^\"]+)").matcher(page);
        check(matcher.find(),"protected JSP renders CSRF token");user.csrf=matcher.group(1);return user;
    }
    static User register(String suffix)throws Exception{
        User anonymous=new User();String email="e2e-"+RUN+"-"+suffix+"@example.invalid",phone="0"+RUN.substring(3)+suffix.replaceAll("[^0-9]","");
        var response=post(anonymous,"/register",data("name","Test "+suffix,"email",email,"phone",phone,"accountType","SAVINGS","password",PASSWORD,"confirmPassword",PASSWORD,"city","Colombo"));
        check(response.statusCode()==302,"registration creates customer "+suffix);User user=login(email,PASSWORD,"/customer/dashboard");
        user.id=(int)number("SELECT customer_id FROM customer WHERE email=?",email);user.account=number("SELECT account_number FROM account WHERE customer_id=?",user.id);
        check(PasswordUtil.checkPassword(PASSWORD,(String)scalar("SELECT password FROM customer WHERE customer_id=?",user.id)),"registration stores BCrypt hash");return user;
    }
    static long latest(String table,String key,int customer)throws Exception{return number("SELECT MAX("+key+") FROM "+table+" WHERE customer_id=?",customer);}
    static String status(String table,String key,long id)throws Exception{return (String)scalar("SELECT status FROM "+table+" WHERE "+key+"=?",id);}
    public static void main(String[] args)throws Exception{
        String db=Files.readString(Path.of("database/test-database.txt")).trim();if(!db.matches("banking_test_[0-9]+"))throw new IllegalStateException("Not a dedicated test database");
        System.setProperty("bank.db.url","jdbc:mysql://localhost:3306/"+db+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Colombo");
        check(db.equals(scalar("SELECT DATABASE()")),"isolated database selected");
        for(String role:AdminDAO.ROLES)execute("INSERT INTO employee(name,email,phone,password,role,status) VALUES(?,?,?,?,?,'ACTIVE')","E2E "+role,"staff-"+RUN+"-"+role+"@example.invalid","0771234567",PasswordUtil.hashPassword(PASSWORD),role);
        Tomcat server=new Tomcat();server.setBaseDir(Path.of("target/e2e-tomcat").toAbsolutePath().toString());server.setPort(0);server.getConnector().setProperty("address","127.0.0.1");Context context=server.addWebapp("/bank",Path.of("target/WebBasedBankingSystem").toAbsolutePath().toString());context.setParentClassLoader(EndToEndTest.class.getClassLoader());
        try{
            server.start();base="http://127.0.0.1:"+server.getConnector().getLocalPort()+"/bank";
            Map<String,String> routes=Map.of("LOAN_OFFICER","/loan/dashboard.jsp","CARD_SERVICES_OFFICER","/card/dashboard.jsp","INVESTMENT_OFFICER","/investment/dashboard.jsp","CUSTOMER_SERVICE_OFFICER","/service/dashboard.jsp","COMPLIANCE_RISK_OFFICER","/compliance/dashboard.jsp","SYSTEM_ADMIN","/admin/dashboard.jsp");
            for(String role:AdminDAO.ROLES){User user=login("staff-"+RUN+"-"+role+"@example.invalid",PASSWORD,routes.get(role));user.id=(int)number("SELECT employee_id FROM employee WHERE email=?",user.email);staff.put(role,user);}
            User customer=register("1"),receiver=register("2"),closure=register("3");
            // Only these fresh test fixtures receive starting balances. The real banking database is never selected.
            execute("UPDATE account SET balance=100000 WHERE account_number=?",customer.account);
            security(customer,routes);transfersAndPayments(customer,receiver);scheduled(customer);cards(customer,receiver);loans(customer,receiver);investments(customer,receiver);requestsAndAccounts(closure,receiver);adminAndProfiles(customer,receiver);
            for(String section:List.of("dashboard","accounts","transfer","payments","cards","loans","investments","requests","settings","transactions","products")){var page=get(customer,"/customer/"+section);check(page.statusCode()==200,"final customer JSP "+section);check(!page.body().contains("Unable to load")&&!page.body().contains("could not be loaded"),"final queries "+section);}
            for(User user:staff.values())check(get(user,"/employee/dashboard").statusCode()==200,"populated employee dashboard");
            check(get(customer,"/logout").statusCode()==302,"logout redirect");check(get(customer,"/customer/accounts").statusCode()==302,"logout invalidates session");
            Files.writeString(Path.of("target/e2e-result.txt"),"PASS: "+checks+" checks; database="+db+"; run="+RUN+"\n");System.out.println("ALL "+checks+" END-TO-END CHECKS PASSED");
        }finally{server.stop();server.destroy();}
    }
    static void security(User customer,Map<String,String> routes)throws Exception{
        User anonymous=new User();for(String path:List.of("/customer/cards","/customer/accounts.jsp","/employee/dashboard","/admin/dashboard.jsp"))check(get(anonymous,path).statusCode()==302,"anonymous protection "+path);
        for(var entry:staff.entrySet()){check(get(entry.getValue(),"/customer/accounts").statusCode()==403,"staff blocked from customer "+entry.getKey());for(var route:routes.entrySet())if(!entry.getKey().equals(route.getKey()))check(get(entry.getValue(),route.getValue()).statusCode()==403,"cross-role denied "+entry.getKey()+" -> "+route.getKey());}
        check(get(customer,"/admin/dashboard.jsp").statusCode()==403,"customer denied admin");String token=customer.csrf;customer.csrf="wrong";check(post(customer,"/customer/cards",data("action","apply","type","DEBIT")).statusCode()==403,"invalid CSRF denied");customer.csrf=token;
        check(get(customer,"/customer/transfer.jsp").statusCode()==302,"direct JSP routes through controller");
        check(post(new User(),"/login",data("email",customer.email,"password","incorrect")).statusCode()==200,"incorrect credentials rejected");
    }
    static void transfersAndPayments(User customer,User receiver)throws Exception{
        BigDecimal before=balance(customer);ok(customer,"/customer/transfer","receiverAccount",Long.toString(receiver.account),"amount","100.00","customerId",Integer.toString(receiver.id));equalMoney(before.subtract(new BigDecimal("100")),balance(customer),"transfer debits session account");equalMoney(new BigDecimal("100"),balance(receiver),"transfer credits receiver");
        fail(customer,"/customer/transfer","receiverAccount",Long.toString(customer.account),"amount","10");fail(customer,"/customer/transfer","receiverAccount",Long.toString(receiver.account),"amount","0.001");fail(customer,"/customer/transfer","receiverAccount","99999999999999","amount","10");fail(customer,"/customer/transfer","receiverAccount",Long.toString(receiver.account),"amount","1000000");
        before=balance(customer);ok(customer,"/customer/payments","billReference","CEB/123-456","recipient","Demo utility","amount","25.50");equalMoney(before.subtract(new BigDecimal("25.50")),balance(customer),"bill payment debits exact amount");
        fail(customer,"/customer/payments","billReference","CEB/123-456","recipient","","amount","10");fail(customer,"/customer/payments","billReference","CEB/123-456","recipient","Utility","amount","-1");
        check(get(receiver,"/customer/transactions").body().contains("+ LKR 100.00"),"receiver history includes incoming transfer");
        // Receiver overflow occurs after sender debit; MySQL must roll the whole transfer back.
        User overflow=register("4");execute("UPDATE account SET balance=9999999999999.99 WHERE account_number=?",overflow.account);before=balance(customer);
        fail(customer,"/customer/transfer","receiverAccount",Long.toString(overflow.account),"amount","10");equalMoney(before,balance(customer),"MySQL transfer rollback after receiver update failure");
    }
    static void scheduled(User customer)throws Exception{
        String date=LocalDateTime.now().plusDays(2).withSecond(0).withNano(0).toString();BigDecimal before=balance(customer);
        ok(customer,"/customer/payment-plans","action","schedule","billReference","CEB/123-456","recipient","Scheduled demo","amount","30","date",date);long id=number("SELECT MAX(p.payment_id) FROM payment p JOIN account a ON a.account_number=p.account_number WHERE a.customer_id=?",customer.id);equalMoney(before,balance(customer),"scheduling does not debit");
        ok(customer,"/customer/payment-plans","action","update","id",Long.toString(id),"billReference","CEB/123-456","recipient","Updated scheduled","amount","35","date",date);equalMoney(new BigDecimal("35"),(BigDecimal)scalar("SELECT amount FROM payment WHERE payment_id=?",id),"scheduled amount updated");
        post(customer,"/customer/payment-plans",data("action","execute","id",Long.toString(id)));check("PENDING".equals(status("payment","payment_id",id)),"future payment cannot execute");
        execute("UPDATE payment SET scheduled_date=DATE_SUB(NOW(),INTERVAL 1 MINUTE) WHERE payment_id=?",id);ok(customer,"/customer/payment-plans","action","execute","id",Long.toString(id));equalMoney(before.subtract(new BigDecimal("35")),balance(customer),"due bill executes once");
        post(customer,"/customer/payment-plans",data("action","execute","id",Long.toString(id)));equalMoney(before.subtract(new BigDecimal("35")),balance(customer),"scheduled replay does not debit twice");
        ok(customer,"/customer/payment-plans","action","schedule","billReference","CEB/123-456","recipient","Cancel demo","amount","10","date",date);id=number("SELECT MAX(p.payment_id) FROM payment p JOIN account a ON a.account_number=p.account_number WHERE a.customer_id=?",customer.id);ok(customer,"/customer/payment-plans","action","cancel","id",Long.toString(id));check("CANCELLED".equals(status("payment","payment_id",id)),"pending schedule cancelled");
    }
    static void cards(User customer,User other)throws Exception{
        User officer=staff.get("CARD_SERVICES_OFFICER");ok(customer,"/customer/cards","action","apply","type","DEBIT");long id=number("SELECT MAX(card_id) FROM card WHERE account_number=?",customer.account);check(number("SELECT COUNT(*) FROM debit_card WHERE card_id=?",id)==1,"debit subtype created");
        fail(other,"/customer/cards","action","cancel","id",Long.toString(id));ok(officer,"/employee/dashboard","action","card-approve","id",Long.toString(id),"reason","Demo approval");check("ACTIVE".equals(status("card","card_id",id)),"officer activates card");
        ok(customer,"/customer/cards","action","limit","id",Long.toString(id),"limit","10000");equalMoney(new BigDecimal("10000"),(BigDecimal)scalar("SELECT daily_limit FROM card WHERE card_id=?",id),"customer limit update");
        ok(customer,"/customer/cards","action","block","id",Long.toString(id));ok(officer,"/employee/dashboard","action","card-unblock","id",Long.toString(id),"reason","Identity verified");ok(customer,"/customer/cards","action","close","id",Long.toString(id));check("CANCELLED".equals(status("card","card_id",id)),"card safely closed");
        ok(customer,"/customer/cards","action","apply","type","CREDIT");id=number("SELECT MAX(card_id) FROM card WHERE account_number=?",customer.account);check(number("SELECT COUNT(*) FROM credit_card WHERE card_id=?",id)==1,"credit subtype created");ok(customer,"/customer/cards","action","cancel","id",Long.toString(id));
        ok(customer,"/customer/cards","action","apply","type","CREDIT");id=number("SELECT MAX(card_id) FROM card WHERE account_number=?",customer.account);ok(officer,"/employee/dashboard","action","card-reject","id",Long.toString(id),"reason","Demo rejection");check("CANCELLED".equals(status("card","card_id",id)),"officer rejects pending card without deletion");
        check(get(customer,"/customer/cards").body().contains("**** "),"card numbers masked in customer view");
    }
    static void loans(User customer,User other)throws Exception{
        User officer=staff.get("LOAN_OFFICER");ok(customer,"/customer/loans","action","apply","type","PERSONAL","amount","10000","months","2");long id=latest("loan","loan_id",customer.id);
        ok(customer,"/customer/loans","action","update","id",Long.toString(id),"amount","12000","months","2");fail(other,"/customer/loans","action","cancel","id",Long.toString(id));
        ok(officer,"/employee/dashboard","action","loan-approve","id",Long.toString(id),"reason","Demo approved");BigDecimal before=balance(customer);ok(customer,"/customer/loans","action","accept","id",Long.toString(id));equalMoney(before.add(new BigDecimal("12000")),balance(customer),"loan principal disbursed once");
        check(number("SELECT COUNT(*) FROM loan_repayment WHERE loan_id=?",id)==2,"monthly loan schedule generated");equalMoney(new BigDecimal("12240"),(BigDecimal)scalar("SELECT SUM(amount) FROM loan_repayment WHERE loan_id=?",id),"flat simple interest schedule total");
        fail(customer,"/customer/loans","action","accept","id",Long.toString(id));equalMoney(before.add(new BigDecimal("12000")),balance(customer),"loan acceptance replay cannot disburse twice");
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT repayment_id FROM loan_repayment WHERE loan_id=? ORDER BY repayment_id")){ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){while(rs.next())ok(customer,"/customer/loans","action","repay","id",rs.getString(1));}}
        check("CLOSED".equals(status("loan","loan_id",id)),"fully repaid loan closes");
        ok(customer,"/customer/loans","action","apply","type","STUDENT","amount","1000","months","1");id=latest("loan","loan_id",customer.id);ok(customer,"/customer/loans","action","cancel","id",Long.toString(id));check("CANCELLED".equals(status("loan","loan_id",id)),"loan application cancelled");
        ok(customer,"/customer/loans","action","apply","type","STUDENT","amount","1000","months","1");id=latest("loan","loan_id",customer.id);ok(officer,"/employee/dashboard","action","loan-reject","id",Long.toString(id),"reason","Demo rejection");check("REJECTED".equals(status("loan","loan_id",id)),"officer loan rejection saved");
    }
    static void investments(User customer,User other)throws Exception{
        User officer=staff.get("INVESTMENT_OFFICER");BigDecimal before=balance(customer);ok(customer,"/customer/investments","action","apply","type","FIXED_DEPOSIT","amount","1000","months","12");long id=latest("investment","investment_id",customer.id);
        ok(customer,"/customer/investments","action","update","id",Long.toString(id),"amount","2000","months","12");fail(other,"/customer/investments","action","cancel","id",Long.toString(id));
        ok(officer,"/employee/dashboard","action","investment-approve","id",Long.toString(id),"reason","Demo funded");equalMoney(before.subtract(new BigDecimal("2000")),balance(customer),"investment approval debits funding account");
        ok(customer,"/customer/investments","action","withdraw","id",Long.toString(id));equalMoney(before,balance(customer),"early investment withdrawal returns principal only");fail(customer,"/customer/investments","action","withdraw","id",Long.toString(id));equalMoney(before,balance(customer),"investment payout replay prevented");
        ok(customer,"/customer/investments","action","apply","type","SAVINGS_PLAN","amount","1000","months","12");id=latest("investment","investment_id",customer.id);ok(officer,"/employee/dashboard","action","investment-approve","id",Long.toString(id),"reason","Maturity test");
        execute("UPDATE investment SET start_date=DATE_SUB(CURDATE(),INTERVAL 365 DAY),maturity_date=CURDATE() WHERE investment_id=?",id);ok(officer,"/employee/dashboard","action","investment-mature","id",Long.toString(id),"reason","Matured");equalMoney(before.add(new BigDecimal("60")),balance(customer),"maturity payout includes actual-days simple interest");
        ok(customer,"/customer/investments","action","apply","type","FIXED_DEPOSIT","amount","1000","months","1");id=latest("investment","investment_id",customer.id);ok(customer,"/customer/investments","action","cancel","id",Long.toString(id));check("CANCELLED".equals(status("investment","investment_id",id)),"pending investment cancelled");
        ok(customer,"/customer/investments","action","apply","type","FIXED_DEPOSIT","amount","1000","months","1");id=latest("investment","investment_id",customer.id);ok(officer,"/employee/dashboard","action","investment-reject","id",Long.toString(id),"reason","Demo rejected");check("REJECTED".equals(status("investment","investment_id",id)),"investment rejected");
        // Unique ledger reference forces failure after the debit; all work must roll back.
        ok(customer,"/customer/investments","action","apply","type","FIXED_DEPOSIT","amount","1000","months","1");id=latest("investment","investment_id",customer.id);execute("INSERT INTO payment(account_number,payment_type,recipient,amount,status,reference_number) VALUES(?,'INVESTMENT_FUNDING','Test duplicate reference',1,'FAILED',?)",customer.account,"INV-FUND-"+id);before=balance(customer);
        fail(officer,"/employee/dashboard","action","investment-approve","id",Long.toString(id),"reason","Rollback test");equalMoney(before,balance(customer),"MySQL investment debit rolls back on ledger failure");check("PENDING".equals(status("investment","investment_id",id)),"failed investment funding retains pending state");ok(customer,"/customer/investments","action","cancel","id",Long.toString(id));
    }
    static void requestsAndAccounts(User customer,User other)throws Exception{
        User officer=staff.get("CUSTOMER_SERVICE_OFFICER");
        ok(customer,"/customer/requests","action","request","type","ACCOUNT_OPENING","accountType","CURRENT","description","Open current account");long id=latest("service_request","request_id",customer.id);
        ok(customer,"/customer/requests","action","request-update","id",Long.toString(id),"description","Updated opening details");fail(other,"/customer/requests","action","request-cancel","id",Long.toString(id));
        ok(officer,"/employee/dashboard","action","request-review","id",Long.toString(id),"status","PROCESSING","reason","Reviewing");ok(officer,"/employee/dashboard","action","request-review","id",Long.toString(id),"status","COMPLETED","reason","Verified");long additional=number("SELECT account_number FROM service_request WHERE request_id=?",id);check("CURRENT".equals(scalar("SELECT account_type FROM account WHERE account_number=?",additional)),"service request opens real current account");
        for(String type:List.of("ACCOUNT_STATEMENT","CHEQUE_BOOK")){ok(customer,"/customer/requests","action","request","type",type,"account",Long.toString(additional),"description","Please process");id=latest("service_request","request_id",customer.id);ok(officer,"/employee/dashboard","action","request-review","id",Long.toString(id),"status","COMPLETED","reason","Collect from branch");check("COMPLETED".equals(status("service_request","request_id",id)),"request fulfilled "+type);if("ACCOUNT_STATEMENT".equals(type)){check(get(customer,"/customer/statement?id="+id).statusCode()==200,"statement download");check(get(other,"/customer/statement?id="+id).statusCode()==404,"statement ownership enforced");}}
        ok(customer,"/customer/requests","action","request","type","PROFILE_UPDATE","email","updated-"+RUN+"@example.invalid","description","Update email");id=latest("service_request","request_id",customer.id);ok(officer,"/employee/dashboard","action","request-review","id",Long.toString(id),"status","COMPLETED","reason","Verified identity");check(scalar("SELECT email FROM customer WHERE customer_id=?",customer.id).equals("updated-"+RUN+"@example.invalid"),"profile request updates email");
        ok(customer,"/customer/requests","action","request","type","ACCOUNT_OPENING","accountType","SAVINGS","description","Cancel this");id=latest("service_request","request_id",customer.id);ok(customer,"/customer/requests","action","request-cancel","id",Long.toString(id));check("CANCELLED".equals(status("service_request","request_id",id)),"service request cancellation");
        ok(customer,"/customer/requests","action","ticket","type","INQUIRY","subject","Question","description","Initial question");id=latest("ticket","ticket_id",customer.id);ok(customer,"/customer/requests","action","ticket-update","id",Long.toString(id),"subject","Updated question","description","More details");ok(officer,"/employee/dashboard","action","ticket-status","id",Long.toString(id),"status","IN_PROGRESS");ok(officer,"/employee/dashboard","action","ticket-response","id",Long.toString(id),"status","RESOLVED","reason","Here is your answer");check(get(customer,"/customer/requests").body().contains("Here is your answer"),"customer sees support response");ok(customer,"/customer/requests","action","ticket-close","id",Long.toString(id));check("CLOSED".equals(status("ticket","ticket_id",id)),"ticket safely closed");
        fail(customer,"/customer/settings","action","deactivate","currentPassword",PASSWORD);
        for(long account:new long[]{customer.account,additional}){ok(customer,"/customer/requests","action","request","type","ACCOUNT_CLOSURE","account",Long.toString(account),"description","Close zero-balance account");id=latest("service_request","request_id",customer.id);ok(officer,"/employee/dashboard","action","request-review","id",Long.toString(id),"status","COMPLETED","reason","No outstanding obligations");check("CLOSED".equals(status("account","account_number",account)),"account closure preserves record");}
        ok(customer,"/customer/settings","action","deactivate","currentPassword",PASSWORD);check("INACTIVE".equals(status("customer","customer_id",customer.id)),"customer safely deactivated");check(get(customer,"/customer/accounts").statusCode()==302,"deactivation invalidates session");
        ok(staff.get("SYSTEM_ADMIN"),"/employee/dashboard","action","customer-status","id",Integer.toString(customer.id),"status","ACTIVE","reason","Customer returns");check("ACTIVE".equals(status("customer","customer_id",customer.id)),"admin reactivates customer");
    }
    static void adminAndProfiles(User customer,User other)throws Exception{
        User admin=staff.get("SYSTEM_ADMIN"),compliance=staff.get("COMPLIANCE_RISK_OFFICER");String email="newstaff-"+RUN+"@example.invalid";
        ok(admin,"/employee/dashboard","action","employee-save","name","New officer","email",email,"phone","0779876543","role","LOAN_OFFICER","status","ACTIVE","password",PASSWORD);long id=number("SELECT employee_id FROM employee WHERE email=?",email);
        User newOfficer=login(email,PASSWORD,"/loan/dashboard.jsp");ok(admin,"/employee/dashboard","action","employee-save","id",Long.toString(id),"name","Changed officer","email",email,"phone","0779876543","role","CARD_SERVICES_OFFICER","status","ACTIVE");check(get(newOfficer,"/employee/dashboard").statusCode()==302,"role change revokes stale session");
        ok(admin,"/employee/dashboard","action","employee-status","id",Long.toString(id),"status","INACTIVE","reason","Demo deactivation");check("INACTIVE".equals(status("employee","employee_id",id)),"employee deactivated without deletion");
        ok(admin,"/employee/dashboard","action","product-save","name","Demo savings "+RUN,"type","SAVINGS","description","University savings product","status","ACTIVE");long product=number("SELECT MAX(product_id) FROM banking_product");check(get(customer,"/customer/products").body().contains("Demo savings "+RUN),"customer can read active products");
        ok(admin,"/employee/dashboard","action","product-save","id",Long.toString(product),"name","Updated product "+RUN,"type","SAVINGS","description","Updated description","status","INACTIVE");check(!get(customer,"/customer/products").body().contains("Updated product "+RUN),"inactive products hidden from customer");
        fail(admin,"/employee/dashboard","action","employee-save","id",Integer.toString(admin.id),"name","Unsafe self change","email",admin.email,"phone","0771234567","role","LOAN_OFFICER","status","INACTIVE");
        ok(compliance,"/employee/dashboard","action","account-status","id",Long.toString(other.account),"status","FROZEN","reason","Demo review");check("FROZEN".equals(status("account","account_number",other.account)),"compliance freezes account");fail(other,"/customer/payments","billReference","CEB/123-456","recipient","Utility","amount","1");ok(compliance,"/employee/dashboard","action","account-status","id",Long.toString(other.account),"status","ACTIVE","reason","Review completed");
        check(get(compliance,"/employee/dashboard?q=PRODUCT_UPDATE").body().contains("PRODUCT_UPDATE"),"audit search works");
        ok(customer,"/customer/settings","action","profile","name","<script>unsafe</script>","phone","0"+RUN.substring(3)+"8","address","Test address","city","Kandy","postalCode","20000");check(get(customer,"/customer/settings").body().contains("&lt;script&gt;unsafe&lt;/script&gt;"),"profile stored and safely escaped");
        String oldPhone=(String)scalar("SELECT phone FROM customer WHERE customer_id=?",other.id);fail(customer,"/customer/settings","action","profile","name","Duplicate phone","phone",oldPhone,"address","","city","","postalCode","");
        ok(other,"/customer/settings","action","password","currentPassword",PASSWORD,"newPassword",PASSWORD+"New","confirmPassword",PASSWORD+"New");check(get(other,"/customer/accounts").statusCode()==302,"password change signs out current session");login(other.email,PASSWORD+"New","/customer/dashboard");
        check(number("SELECT COUNT(*) FROM audit_log")>20,"workflow audit records persisted");
    }
}
