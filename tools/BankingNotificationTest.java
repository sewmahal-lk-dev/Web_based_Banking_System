package com.banking.dao;

import com.banking.util.*;
import java.sql.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;

/** Real MySQL transactions and HTTP sessions, exclusively in a new disposable test database. */
public class BankingNotificationTest {
    static int checks;
    static final String PASSWORD="Notification-Test-Only-2026!",BASE="http://127.0.0.1:8774/bank";
    static final long ACCOUNT=910000000001L,OTHER=910000000002L;
    static final NotificationDAO notes=new NotificationDAO();
    interface Action {void run()throws Exception;}
    static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;System.out.println("PASS "+label);}
    static void execute(String sql,Object...args)throws Exception{try(Connection c=DBConnection.getConnection()){Jdbc.update(c,sql,args);}}
    static Object scalar(String sql,Object...args)throws Exception{try(Connection c=DBConnection.getConnection()){return Jdbc.one(c,sql,args).values().iterator().next();}}
    static long count(String where,Object...args)throws Exception{return ((Number)scalar("SELECT COUNT(*) FROM notification WHERE "+where,args)).longValue();}
    static long total()throws Exception{return count("1=1");}
    static long customer(String type)throws Exception{return count("customer_id=1 AND notification_type=?",type);}
    static int id(String table,String key)throws Exception{return ((Number)scalar("SELECT MAX("+key+") FROM "+table)).intValue();}
    static void fails(Action action,String label)throws Exception{long before=total();try{action.run();throw new AssertionError("Expected failure: "+label);}catch(IllegalArgumentException|SQLException|SecurityException expected){}check(total()==before,label+" creates no notifications");}
    static void changed(Action action,String type,String label)throws Exception{long before=customer(type);action.run();check(customer(type)==before+1,label);}
    static void submission(Action action,String type,List<Integer> recipients,String label)throws Exception{
        long before=total();action.run();
        check(total()==before+recipients.size(),label+" notifies only relevant active staff");
        for(int staff:recipients)check(count("employee_id=? AND notification_type=?",staff,type)>0,label+" recipient "+staff);
    }
    static void fixtures()throws Exception{
        String hash=PasswordUtil.hashPassword(PASSWORD);
        for(int i=1;i<=3;i++)execute("INSERT INTO customer(customer_id,name,email,password,status) VALUES(?,?,?,?, 'ACTIVE')",i,"Customer "+i,"customer"+i+"@example.invalid",hash);
        execute("INSERT INTO account(account_number,customer_id,account_type,balance,status,open_date) VALUES(?,1,'SAVINGS',500000,'ACTIVE',CURDATE()),(?,2,'CURRENT',500000,'ACTIVE',CURDATE())",ACCOUNT,OTHER);
        String[] roles={"CUSTOMER_SERVICE_OFFICER","LOAN_OFFICER","CARD_SERVICES_OFFICER","INVESTMENT_OFFICER","COMPLIANCE_RISK_OFFICER","SYSTEM_ADMIN","LOAN_OFFICER","SYSTEM_ADMIN","LOAN_OFFICER"};
        for(int i=0;i<roles.length;i++)execute("INSERT INTO employee(employee_id,name,email,phone,password,role,status) VALUES(?,?,?,?,?,?,?)",i+1,"Staff "+(i+1),"staff"+(i+1)+"@example.invalid","0771234567",hash,roles[i],i==8?"INACTIVE":"ACTIVE");
    }
    static void loans()throws Exception{
        LoanDAO dao=new LoanDAO();
        submission(()->dao.apply(1,"PERSONAL","1200","1"),"LOAN_SUBMITTED",List.of(2,7),"Loan application");int loan=id("loan","loan_id");
        check(count("employee_id=9")==0,"inactive officer excluded");
        changed(()->dao.decision(2,loan,true,"Approved"),"LOAN_APPROVED","Loan approval belongs to owner");
        fails(()->dao.decision(2,loan,true,"Approved"),"Repeated loan approval");
        fails(()->dao.accept(2,loan),"Other customer cannot accept loan");
        changed(()->dao.accept(1,loan),"LOAN_ACTIVE","Loan acceptance/disbursement");
        fails(()->dao.accept(1,loan),"Repeated loan acceptance");
        int repayment=id("loan_repayment","repayment_id");
        changed(()->new LoanRepaymentDAO().pay(1,repayment),"LOAN_CLOSED","Final installment closes loan");
        fails(()->new LoanRepaymentDAO().pay(1,repayment),"Repeated installment");
        int rejected=(int)dao.apply(1,"PERSONAL","1000","2");
        changed(()->dao.decision(2,rejected,false,"Not eligible"),"LOAN_REJECTED","Loan rejection");
        int cancelled=(int)dao.apply(1,"PERSONAL","1000","2");
        changed(()->dao.cancel(1,cancelled),"LOAN_CANCELLED","Loan cancellation");
        fails(()->dao.cancel(1,cancelled),"Repeated loan cancellation");
        fails(()->dao.apply(1,"PERSONAL","-1","2"),"Invalid application");
        check(count("customer_id=2 AND notification_type LIKE 'LOAN_%'")==0,"Loan decisions never sent to another customer");
    }
    static void investments()throws Exception{
        InvestmentDAO dao=new InvestmentDAO();
        submission(()->dao.apply(1,"FIXED_DEPOSIT","1000","1"),"INVESTMENT_SUBMITTED",List.of(4),"Investment submission");int investment=id("investment","investment_id");
        changed(()->dao.process(4,investment,true,"Funded"),"INVESTMENT_ACTIVE","Investment approval/funding");
        fails(()->dao.process(4,investment,true,"Funded"),"Repeated investment approval");
        fails(()->dao.withdraw(2,investment),"Other customer investment payout");
        changed(()->dao.withdraw(1,investment),"INVESTMENT_CLOSED","Investment withdrawal/payout");
        fails(()->dao.withdraw(1,investment),"Repeated investment payout");
        int reject=(int)dao.apply(1,"SAVINGS_PLAN","1000","1");
        changed(()->dao.process(4,reject,false,"Rejected"),"INVESTMENT_REJECTED","Investment rejection");
        int cancel=(int)dao.apply(1,"SAVINGS_PLAN","1000","1");
        changed(()->dao.cancel(1,cancel),"INVESTMENT_CANCELLED","Investment cancellation");
        int mature=(int)dao.apply(1,"FIXED_DEPOSIT","1000","1");dao.process(4,mature,true,"Funded");
        fails(()->dao.mature(4,mature),"Premature maturity");
        execute("UPDATE investment SET start_date=DATE_SUB(CURDATE(),INTERVAL 1 MONTH),maturity_date=CURDATE() WHERE investment_id=?",mature);
        changed(()->dao.mature(4,mature),"INVESTMENT_CLOSED","Officer maturity payout");
        int unfunded=(int)dao.apply(1,"FIXED_DEPOSIT","1000000","1");
        fails(()->dao.process(4,unfunded,true,"Fund"),"Insufficient investment funds");dao.cancel(1,unfunded);
    }
    static void cards()throws Exception{
        CardDAO dao=new CardDAO();
        for(String type:List.of("DEBIT","CREDIT")){
            submission(()->dao.apply(1,type),"CARD_SUBMITTED",List.of(3),type+" card request");int card=id("card","card_id");
            fails(()->dao.apply(1,type),"Duplicate pending "+type+" request");
            changed(()->dao.process(3,card,"approve","Approved"),"CARD_ACTIVE",type+" card approval");
            fails(()->dao.process(3,card,"approve","Approved"),"Repeated card approval");
            fails(()->dao.customerAction(2,card,"block",null),"Other customer card action");
            changed(()->dao.customerAction(1,card,"block",null),"CARD_BLOCKED","Customer card block");
            changed(()->dao.process(3,card,"unblock","Restored"),"CARD_ACTIVE","Card unblock");
            changed(()->dao.process(3,card,"block","Review"),"CARD_BLOCKED","Officer card block");
            changed(()->dao.process(3,card,"unblock","Restored"),"CARD_ACTIVE","Second genuine unblock still notifies");
            long before=total();dao.customerAction(1,card,"limit","100");check(total()==before,"Routine card limit edit stays silent");
            changed(()->dao.process(3,card,"close","Closed"),"CARD_CANCELLED","Officer card closure");
            int rejected=(int)dao.apply(1,type);
            changed(()->dao.process(3,rejected,"reject","Rejected"),"CARD_CANCELLED","Card rejection uses existing CANCELLED state");
            check(count("customer_id=1 AND message LIKE ?","%#"+rejected+" request has been rejected.%")==1,"Rejected card message describes decision");
            int cancelled=(int)dao.apply(1,type);
            changed(()->dao.customerAction(1,cancelled,"cancel",null),"CARD_CANCELLED","Customer card request cancellation");
        }
    }
    static void payments()throws Exception{
        long before=customer("PAYMENT_COMPLETED"),received=count("customer_id=2 AND notification_type='PAYMENT_RECEIVED'");
        String reference=new TransferDAO().transferMoney(1,OTHER,new BigDecimal("25.50"));
        check(customer("PAYMENT_COMPLETED")==before+1&&count("customer_id=2 AND notification_type='PAYMENT_RECEIVED'")==received+1,"Transfer notifies sender and actual receiver");
        long all=total();
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try{List<Future<?>> futures=new ArrayList<>();for(int i=0;i<2;i++)futures.add(pool.submit(()->{try{Jdbc.transaction(c->{NotificationDAO.payment(c,reference);return null;});}catch(SQLException e){throw new RuntimeException(e);}}));for(var f:futures)f.get(15,TimeUnit.SECONDS);}finally{pool.shutdownNow();}
        check(total()==all,"Concurrent repeated delivery of one payment is idempotent");
        changed(()->new PaymentDAO().makeBillPayment(1,"Power","BILL-123",new BigDecimal("10")),"PAYMENT_COMPLETED","Bill payment");
        changed(()->new CashTransactionDAO().deposit(1,ACCOUNT,new BigDecimal("20"),"Test deposit"),"PAYMENT_COMPLETED","Cash deposit");
        changed(()->new CashTransactionDAO().withdraw(1,ACCOUNT,new BigDecimal("10"),"Test withdrawal"),"PAYMENT_COMPLETED","Cash withdrawal");
        fails(()->new TransferDAO().transferMoney(1,OTHER,new BigDecimal("999999")),"Insufficient transfer funds");
        fails(()->new PaymentDAO().makeBillPayment(1,"Power","BILL-123",new BigDecimal("999999")),"Insufficient bill funds");
        fails(()->new CashTransactionDAO().deposit(2,ACCOUNT,new BigDecimal("10"),"Wrong role"),"Wrong teller role");
        ScheduledPaymentDAO scheduled=new ScheduledPaymentDAO();
        String date=java.time.LocalDateTime.now().plusDays(1).toString();
        long payment=scheduled.save(1,null,"Power","BILL-456","10",date);
        execute("UPDATE payment SET scheduled_date=DATE_SUB(NOW(),INTERVAL 1 DAY) WHERE payment_id=?",payment);
        changed(()->scheduled.execute(1,payment),"PAYMENT_COMPLETED","Scheduled bill execution");
        fails(()->scheduled.execute(1,payment),"Repeated scheduled execution");
        long cancelled=scheduled.save(1,null,"Power","BILL-789","10",date);
        changed(()->scheduled.cancel(1,cancelled),"PAYMENT_CANCELLED","Scheduled payment cancellation");
        fails(()->scheduled.cancel(1,cancelled),"Repeated scheduled cancellation");
    }
    static void requestsAndAccounts()throws Exception{
        ServiceRequestDAO dao=new ServiceRequestDAO();
        submission(()->dao.create(1,"ACCOUNT_OPENING","Open current account",null,"CURRENT",null),"REQUEST_SUBMITTED",List.of(1),"Service request");int request=id("service_request","request_id");
        changed(()->dao.process(1,request,"PROCESSING","Under review"),"REQUEST_PROCESSING","Service request processing");
        long before=total();dao.process(1,request,"PROCESSING","Under review");check(total()==before,"Repeated identical processing does not notify");
        changed(()->dao.process(1,request,"PROCESSING","Documents checked"),"REQUEST_PROCESSING","Changed processing response notifies");
        long accounts=customer("ACCOUNT_ACTIVE");
        changed(()->dao.process(1,request,"COMPLETED","Ready"),"REQUEST_COMPLETED","Account-opening request completion");
        check(customer("ACCOUNT_ACTIVE")==accounts+1,"New account activation notifies actual owner");
        fails(()->dao.process(1,request,"COMPLETED","Ready"),"Repeated request completion");
        long opened=((Number)scalar("SELECT account_number FROM service_request WHERE request_id=?",request)).longValue();
        EmployeeDAO employee=new EmployeeDAO();
        changed(()->employee.act(5,"COMPLIANCE_RISK_OFFICER","account-status",Long.toString(opened),"FROZEN","Risk review"),"ACCOUNT_FROZEN","Account freeze");
        check(count("notification_type='ACCOUNT_FROZEN' AND employee_id IN (6,8)")==2,"Account freeze alerts active administrators");
        fails(()->employee.act(5,"COMPLIANCE_RISK_OFFICER","account-status",Long.toString(opened),"FROZEN","Risk review"),"Repeated freeze");
        changed(()->employee.act(5,"COMPLIANCE_RISK_OFFICER","account-status",Long.toString(opened),"ACTIVE","Cleared"),"ACCOUNT_ACTIVE","Account reactivation");
        int close=(int)dao.create(1,"ACCOUNT_CLOSURE","Close empty account",Long.toString(opened),null,null);
        changed(()->dao.process(1,close,"COMPLETED","Closed"),"ACCOUNT_CLOSED","Account closure");
        int reject=(int)dao.create(1,"ACCOUNT_OPENING","Open account",null,"SAVINGS",null);
        changed(()->dao.process(1,reject,"REJECTED","Not eligible"),"REQUEST_REJECTED","Request rejection");
        int cancel=(int)dao.create(1,"ACCOUNT_OPENING","Open account",null,"SAVINGS",null);
        changed(()->dao.update(1,cancel,null,true),"REQUEST_CANCELLED","Customer request cancellation");
        int profile=(int)dao.create(1,"PROFILE_UPDATE","Update email",null,null,"updated@example.invalid");
        changed(()->dao.process(1,profile,"COMPLETED","Updated"),"REQUEST_COMPLETED","Profile service request completion");
        execute("UPDATE customer SET email='customer1@example.invalid' WHERE customer_id=1");
        int statement=(int)dao.create(1,"ACCOUNT_STATEMENT","Statement",Long.toString(ACCOUNT),null,null);
        changed(()->dao.process(1,statement,"COMPLETED","Available"),"REQUEST_COMPLETED","Statement service completion");
        int cheque=(int)dao.create(2,"CHEQUE_BOOK","Cheque book",Long.toString(OTHER),null,null);dao.process(1,cheque,"COMPLETED","Collect at branch");
        check(count("customer_id=2 AND notification_type='REQUEST_COMPLETED'")==1,"Cheque request completion belongs to second customer");
    }
    static void admin()throws Exception{
        AdminDAO dao=new AdminDAO();long before=total();
        dao.employee(6,7,"Staff 7","staff7@example.invalid","0771234567","LOAN_OFFICER","ACTIVE",null);
        check(total()==before,"Ordinary employee edit does not notify");
        dao.employee(6,7,"Staff 7","staff7@example.invalid","0771234567","INVESTMENT_OFFICER","ACTIVE",null);
        check(count("notification_type='EMPLOYEE_ACCESS'")==1,"Role change alerts the other administrator");
        before=total();dao.employee(6,7,"Staff 7","staff7@example.invalid","0771234567","INVESTMENT_OFFICER","ACTIVE",null);check(total()==before,"Repeated access edit stays silent");
        new EmployeeDAO().act(6,"SYSTEM_ADMIN","employee-status","7","INACTIVE","Access removed");
        check(count("notification_type='EMPLOYEE_ACCESS'")==2,"Legacy employee status action alerts administrators");
        dao.customerStatus(6,3,"INACTIVE");dao.customerStatus(6,3,"ACTIVE");
        check(count("customer_id=3 AND notification_type LIKE 'PROFILE_%'")==2,"Customer access deactivation and activation");
        fails(()->dao.customerStatus(6,3,"ACTIVE"),"Repeated profile activation");
    }
    static void support()throws Exception{
        CustomerServicesDAO customers=new CustomerServicesDAO();TicketDAO tickets=new TicketDAO();
        customers.ticket(1,"COMPLAINT","Support regression","Private description");int ticket=id("ticket","ticket_id");
        check(count("employee_id=1 AND notification_type='TICKET_CREATED' AND related_ticket_id=?",ticket)==1,"Existing ticket creation notification");
        tickets.assign(1,ticket,"CARD_SERVICES_OFFICER");
        check(count("customer_id=1 AND notification_type='TICKET_ASSIGNED'")==1&&count("employee_id=3 AND notification_type='TICKET_ASSIGNED'")==1,"Existing ticket assignment recipients");
        tickets.update(3,ticket,"ticket-status","IN_PROGRESS",null);long before=total();tickets.update(3,ticket,"ticket-status","IN_PROGRESS",null);check(total()==before,"Repeated support status still deduplicated");
        tickets.update(3,ticket,"ticket-reply",null,"Private staff reply");customers.reply(1,ticket,"Private customer reply");
        check(customer("SUPPORT_REPLY")==1&&count("employee_id=3 AND notification_type='CUSTOMER_REPLY'")==1,"Existing support replies work both directions");
        tickets.update(3,ticket,"ticket-status","RESOLVED",null);check(customer("TICKET_RESOLVED")==1,"Existing support resolution");
        long note=((Number)scalar("SELECT notification_id FROM notification WHERE customer_id=1 AND notification_type='TICKET_RESOLVED'")).longValue();
        check(notes.open(1,true,note).equals("/customer/tickets?ticket="+ticket+"#ticket-"+ticket),"Existing ticket notification navigation");
        check(count("message LIKE '%Private%'")==0,"Support messages retain existing privacy");
    }
    static void remainingEntryPoints()throws Exception{
        int registered=new CustomerDAO().createCustomerAndAccount("Registered customer","Address","Colombo","10000","0779999911","registered@example.invalid","1990-01-01",PasswordUtil.hashPassword(PASSWORD),"SAVINGS");
        check(count("customer_id=? AND notification_type='ACCOUNT_ACTIVE'",registered)==1,"Registration account activation notification");
        int loan=(int)new LoanDAO().apply(1,"PERSONAL","1000","1");
        changed(()->new EmployeeDAO().act(2,"LOAN_OFFICER","loan-reject",""+loan,null,"Rejected"),"LOAN_REJECTED","Legacy loan decision entrypoint");
        int card=(int)new CardDAO().apply(1,"DEBIT");new CardDAO().process(3,card,"approve","Approved");
        changed(()->new CustomerServicesDAO().blockCard(1,card),"CARD_BLOCKED","Legacy customer card block entrypoint");
        new CardDAO().process(3,card,"unblock","Restored");
        changed(()->new EmployeeDAO().act(3,"CARD_SERVICES_OFFICER","card-block",""+card,null,"Blocked"),"CARD_BLOCKED","Legacy officer card block entrypoint");
        changed(()->new CardDAO().customerAction(1,card,"close",null),"CARD_CANCELLED","Customer card closure");
        new CustomerServicesDAO().request(1,"ACCOUNT_STATEMENT","Legacy request");int request=id("service_request","request_id");
        check(count("employee_id=1 AND notification_type='REQUEST_SUBMITTED' AND message LIKE ?","%#"+request+" received%") == 1,"Legacy service submission notifies queue");
        changed(()->new EmployeeDAO().act(1,"CUSTOMER_SERVICE_OFFICER","request-review",""+request,"PROCESSING","Reviewed"),"REQUEST_PROCESSING","Legacy service processing entrypoint");
        long before=total();new EmployeeDAO().act(1,"CUSTOMER_SERVICE_OFFICER","request-review",""+request,"PROCESSING","Reviewed");check(total()==before,"Repeated legacy service status stays silent");
        new AccountWorkflowDAO().deactivate(3,PASSWORD);
        check(count("customer_id=3 AND notification_type='PROFILE_INACTIVE'")==2,"Self-service profile deactivation notification");
        new AdminDAO().customerStatus(6,3,"ACTIVE");
        check(count("employee_id IS NOT NULL AND customer_id IS NOT NULL")==0,"Every note has one recipient domain");
        check(count("customer_id=2 AND notification_type LIKE 'CARD_%'")==0&&count("customer_id=2 AND notification_type LIKE 'INVESTMENT_%'")==0,"Cards and investments never notify unrelated customer");
    }
    static void rollback()throws Exception{
        long loans=((Number)scalar("SELECT COUNT(*) FROM loan")).longValue();long before=total();
        execute("CREATE TRIGGER notification_failure BEFORE INSERT ON notification FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic notification failure'");
        try{
            fails(()->new LoanDAO().apply(1,"PERSONAL","1000","1"),"Notification failure rolls back application");
            check(((Number)scalar("SELECT COUNT(*) FROM loan")).longValue()==loans,"Failed notification leaves no application");
            BigDecimal balance=(BigDecimal)scalar("SELECT balance FROM account WHERE account_number=?",ACCOUNT);
            long payments=((Number)scalar("SELECT COUNT(*) FROM payment")).longValue();
            fails(()->new TransferDAO().transferMoney(1,OTHER,new BigDecimal("5")),"Notification failure rolls back transfer");
            check(balance.equals(scalar("SELECT balance FROM account WHERE account_number=?",ACCOUNT))&&payments==((Number)scalar("SELECT COUNT(*) FROM payment")).longValue(),"Failed notification restores balance and payment table");
        }finally{execute("DROP TRIGGER notification_failure");}
        check(total()==before,"No partial notifications survived rollback");
    }
    static class User {HttpClient client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).followRedirects(HttpClient.Redirect.NEVER).build();String csrf;}
    static HttpResponse<String> get(User u,String path)throws Exception{return u.client.send(HttpRequest.newBuilder(URI.create(BASE+path)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    static HttpResponse<String> post(User u,String path,Map<String,String> values)throws Exception{
        var fields=new LinkedHashMap<>(values);if(u.csrf!=null)fields.put("csrf",u.csrf);
        String body=fields.entrySet().stream().map(e->URLEncoder.encode(e.getKey(),java.nio.charset.StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),java.nio.charset.StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
        return u.client.send(HttpRequest.newBuilder(URI.create(BASE+path)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    static User login(String email,boolean customer)throws Exception{
        User u=new User();check(post(u,"/login",Map.of("email",email,"password",PASSWORD)).statusCode()==302,"Actual password login: "+email);
        String page=get(u,customer?"/customer/dashboard":"/employee/dashboard").body();Matcher m=Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(page);if(!m.find()){m=Pattern.compile("name=\"csrf-token\" content=\"([^\"]+)\"").matcher(page);check(m.find(),"Authenticated CSRF token renders");}else check(true,"Authenticated CSRF form renders");u.csrf=m.group(1);return u;
    }
    static void http()throws Exception{
        User customer=login("customer1@example.invalid",true),other=login("customer2@example.invalid",true),staff=login("staff2@example.invalid",false);
        long before=total();
        for(String page:List.of("/customer/dashboard","/customer/loans","/customer/investments","/customer/cards","/customer/requests","/customer/accounts","/customer/transactions","/customer/notifications"))for(int i=0;i<2;i++)check(get(customer,page).statusCode()==200,"Refresh renders "+page);
        for(int employee:List.of(1,2,3,4,5,6,8)){User u=login("staff"+employee+"@example.invalid",false);check(get(u,"/employee/dashboard").statusCode()==200,"Staff dashboard renders "+employee);}
        check(total()==before,"All customer/staff GET refreshes produce zero notifications");
        String page=get(customer,"/customer/dashboard").body();check(page.contains("class=\"notify-badge\">"+notes.unread(1,true)+"</span>"),"Bell badge matches database unread count");
        long note=((Number)scalar("SELECT MAX(notification_id) FROM notification WHERE customer_id=1 AND notification_type='LOAN_APPROVED'")).longValue();
        long unread=notes.unread(1,true);
        check(post(other,"/customer/notifications",Map.of("action","open","id",""+note,"customerId","1")).statusCode()==404,"Forged recipient cannot open another customer's notification");
        check(notes.unread(1,true)==unread,"Unauthorized open does not mark victim notification read");
        var opened=post(customer,"/customer/notifications",Map.of("action","open","id",""+note));check(opened.statusCode()==302&&opened.headers().firstValue("location").orElse("").endsWith("/customer/loans"),"Loan notification opens existing loans route");
        check(notes.unread(1,true)==unread-1,"Open marks exactly one notification read");
        post(customer,"/customer/notifications",Map.of("action","open","id",""+note));check(notes.unread(1,true)==unread-1,"Repeated open is idempotent");
        long staffUnread=notes.unread(2,false);post(customer,"/customer/notifications",Map.of("action","read-all"));check(notes.unread(1,true)==0&&notes.unread(2,false)==staffUnread,"Read-all only affects logged-in recipient");
        check(get(customer,"/employee/notifications").statusCode()==403&&get(staff,"/customer/notifications").statusCode()==403,"Customer/staff notification routes stay isolated");
        check(get(new User(),"/customer/notifications").statusCode()==302,"Anonymous notifications denied");
        for(var entry:Map.of("INVESTMENT_ACTIVE","investments","CARD_ACTIVE","cards","REQUEST_COMPLETED","requests","PAYMENT_COMPLETED","transactions","ACCOUNT_ACTIVE","accounts").entrySet()){
            long item=((Number)scalar("SELECT MAX(notification_id) FROM notification WHERE customer_id=1 AND notification_type=?",entry.getKey())).longValue();check(notes.open(1,true,item).equals("/customer/"+entry.getValue()),"Notification routes safely to "+entry.getValue());
        }
        before=total();
        var submitted=post(customer,"/customer/loans",Map.of("action","apply","type","PERSONAL","amount","1000","months","1"));check(submitted.statusCode()==302,"HTTP application uses POST/redirect/GET");
        get(customer,"/customer/loans");get(customer,"/customer/loans");check(total()==before+1,"Refreshing application confirmation does not resend officer notice");
        int loan=id("loan","loan_id");
        var decision=post(staff,"/employee/dashboard",Map.of("action","loan-approve","id",""+loan,"reason","Approved"));check(decision.statusCode()==302,"HTTP officer decision succeeds");
        before=total();post(staff,"/employee/dashboard",Map.of("action","loan-approve","id",""+loan,"reason","Approved"));check(total()==before,"Repeated HTTP decision creates no duplicate");
        execute("INSERT INTO notification(customer_id,notification_type,title,message) VALUES(1,'TEST','<script>alert(1)</script>','<img src=x onerror=alert(1)>')");
        page=get(customer,"/customer/notifications").body();check(page.contains("&lt;script&gt;")&&!page.contains("<script>alert(1)</script>"),"Existing notification UI escapes stored text");
    }
    public static void main(String[] args)throws Exception{
        String db="banking_test_notifications_"+System.currentTimeMillis();List<String> ddl=new ArrayList<>();
        // Copy definitions only, in foreign-key order. No production records are read or modified.
        try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()){
            for(String table:List.of("customer","employee","account","card","debit_card","credit_card","loan","loan_repayment","investment","payment","service_request","ticket","ticket_message","notification","audit_log","banking_product"))try(ResultSet r=s.executeQuery("SHOW CREATE TABLE "+table)){r.next();ddl.add(r.getString(2));}
            s.executeUpdate("CREATE DATABASE `"+db+"` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
        System.setProperty("bank.db.url","jdbc:mysql://localhost:3306/"+db+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Colombo");
        Tomcat server=new Tomcat();boolean started=false;
        try{
            for(String sql:ddl)execute(sql);fixtures();loans();investments();cards();payments();requestsAndAccounts();admin();support();remainingEntryPoints();rollback();
            server.setBaseDir(Path.of("target/notification-tomcat").toAbsolutePath().toString());server.setPort(8774);server.getConnector().setProperty("address","127.0.0.1");
            Context context=server.addWebapp("/bank",Path.of("target/WebBasedBankingSystem").toAbsolutePath().toString());context.setParentClassLoader(BankingNotificationTest.class.getClassLoader());server.start();started=true;http();
            Files.writeString(Path.of("verification/banking-notification-tests.json"),"{\"checks\":"+checks+",\"passed\":true}");System.out.println("BANKING NOTIFICATION CHECKS PASSED: "+checks);
        }finally{
            if(started){server.stop();server.destroy();}
            if(!db.matches("banking_test_notifications_[0-9]+"))throw new IllegalStateException("Unsafe test DB name");
            try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()){s.executeUpdate("DROP DATABASE `"+db+"`");}
            System.clearProperty("bank.db.url");
        }
    }
}
