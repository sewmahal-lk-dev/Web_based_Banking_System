package com.banking.dao;

import com.banking.util.*;
import java.sql.*;
import java.nio.file.*;
import java.net.*;
import java.util.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;

/** Admin-specific assertions plus the complete banking notification regression suite. */
public class AdminDashboardTest extends BankingNotificationTest {
    static void verifyAdmin()throws Exception{
        execute("INSERT INTO employee(employee_id,name,email,password,role,status) VALUES(20,'Inactive admin','inactiveadmin@example.invalid','unused','SYSTEM_ADMIN','INACTIVE')");
        long before=total();
        int registered=new CustomerDAO().createCustomerAndAccount("New <Admin Test>","Address","Colombo","10000","0779999933","adminregistration@example.invalid","1990-01-01",PasswordUtil.hashPassword(PASSWORD),"SAVINGS");
        check(total()==before+3,"Registration commits account-owner notice and two active admin notices");
        check(count("notification_type='CUSTOMER_REGISTERED' AND message=?","New customer New <Admin Test> has registered.")==2,"Registration message uses saved customer name");
        check(count("employee_id=20")==0,"Inactive admin receives no registration");
        check(count("notification_type='CUSTOMER_REGISTERED' AND employee_id NOT IN (6,8)")==0,"Registration is admin-only");
        fails(()->new CustomerDAO().createCustomerAndAccount("Duplicate","Address","Colombo","10000","0779999934","adminregistration@example.invalid","1990-01-01","unused","SAVINGS"),"Failed duplicate registration");
        long customers=((Number)scalar("SELECT COUNT(*) FROM customer")).longValue();
        execute("CREATE TRIGGER admin_note_failure BEFORE INSERT ON notification FOR EACH ROW BEGIN IF NEW.notification_type='CUSTOMER_REGISTERED' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Synthetic admin notification failure'; END IF; END");
        try{fails(()->new CustomerDAO().createCustomerAndAccount("Rollback registration","Address","Colombo","10000","0779999935","rollbackadmin@example.invalid","1990-01-01","unused","SAVINGS"),"Registration admin notice failure");}
        finally{execute("DROP TRIGGER admin_note_failure");}
        check(((Number)scalar("SELECT COUNT(*) FROM customer")).longValue()==customers,"Registration and owner notification roll back atomically");
        check(count("employee_id IN (6,8) AND (notification_type LIKE 'LOAN_%' OR notification_type LIKE 'INVESTMENT_%' OR notification_type LIKE 'CARD_%' OR notification_type LIKE 'PAYMENT_%' OR related_ticket_id IS NOT NULL)")==0,"All banking and support regression events stayed out of Admin inboxes");
        AdminDAO dao=new AdminDAO();before=count("employee_id=6 AND notification_type='EMPLOYEE_ACCESS'");long otherBefore=count("employee_id=8 AND notification_type='EMPLOYEE_ACCESS'");
        dao.employee(6,7,"Staff 7","staff7@example.invalid","0771234567","LOAN_OFFICER","ACTIVE",null);
        check(count("employee_id=6 AND notification_type='EMPLOYEE_ACCESS'")==before&&count("employee_id=8 AND notification_type='EMPLOYEE_ACCESS'")==otherBefore+1,"Access change excludes acting Admin");
        before=total();dao.employee(6,7,"Staff 7 renamed","staff7@example.invalid","0771234567","LOAN_OFFICER","ACTIVE",null);check(total()==before,"Ordinary name edit stays silent");
        before=total();
        for(int i=0;i<6;i++)dao.product(6,null,"Recent product "+i,"SAVINGS","Administrative detail "+i,"ACTIVE");
        check(total()==before,"Product CRUD produces activity without notifications");
        var activity=dao.recentActivity(6);
        check(activity.size()==5,"Activity limited to latest five");
        check(((Number)activity.get(0).get("log_id")).longValue()>((Number)activity.get(4).get("log_id")).longValue(),"Activity ordered newest first");
        check(activity.stream().allMatch(a->"Staff 6".equals(a.get("actor_name"))&&a.get("action_time")!=null),"Activity uses joined actor names and real timestamps");
        fails(()->dao.recentActivity(2),"Non-admin administrative activity query");
        User admin=login("staff6@example.invalid",false),other=login("staff8@example.invalid",false);
        String page=get(admin,"/admin/dashboard.jsp").body();
        check(page.contains("Recent Notifications")&&page.contains("Recent Administrative Activity"),"Existing Admin route renders both sections");
        String recent=page.substring(page.indexOf("<div class=\"admin-overview\">"),page.indexOf("<section class=\"staff-welcome\">"));
        check(recent.contains("New Customer Registration")&&recent.contains("New &lt;Admin Test&gt;"),"Recent notes use escaped actual database content");
        check(recent.split("class=\"notify-item ",-1).length-1==Math.min(5,notes.list(6,false,0,100).size()),"Dashboard shows at most five recipient-owned notifications");
        check(recent.split("<li>",-1).length-1==5&&recent.contains("Staff 6")&&recent.contains(String.valueOf(activity.get(0).get("details"))),"Recent activity displays actual audit details");
        check(recent.contains("/employee/notifications")&&recent.contains("/employee/dashboard#Audit-log"),"View All uses existing notification and audit routes");
        check(page.contains("class=\"notify-badge\">"+notes.unread(6,false)+"</span>"),"Admin bell unread count matches DB");
        long note=((Number)scalar("SELECT MAX(notification_id) FROM notification WHERE employee_id=6 AND notification_type='CUSTOMER_REGISTERED'")).longValue();
        long unread=notes.unread(6,false);var opened=post(admin,"/employee/notifications",Map.of("action","open","id",""+note));
        check(opened.statusCode()==302&&opened.headers().firstValue("location").orElse("").endsWith("#Customers"),"Registration notice opens existing customer management");
        check(notes.unread(6,false)==unread-1,"Admin reading decreases unread count once");
        check(get(admin,"/employee/dashboard").body().contains("notify-item is-read"),"Dashboard displays read state after opening");
        before=total();for(int i=0;i<3;i++)get(admin,"/employee/dashboard");check(total()==before,"Admin refresh creates no notifications");
        check(post(other,"/employee/notifications",Map.of("action","open","id",""+note)).statusCode()==404,"Other Admin cannot open actor's private notification");
        long otherUnread=notes.unread(8,false);post(admin,"/employee/notifications",Map.of("action","read-all"));
        check(notes.unread(6,false)==0&&notes.unread(8,false)==otherUnread,"Admin read-all is recipient-scoped");
        check(!get(admin,"/employee/dashboard").body().contains("class=\"notify-badge\""),"Zero unread removes Admin badge");
        check(get(admin,"/employee/dashboard?q=PRODUCT_CREATE").statusCode()==200,"Existing audit search works");
        check(get(admin,"/admin/reports").statusCode()==200,"Reports page loads");before=total();
        check(post(admin,"/admin/reports",Map.of("type","CUSTOMER")).statusCode()==302,"Existing report generation works");
        check(total()==before&&"REPORT_GENERATED".equals(dao.recentActivity(6).get(0).get("action")),"Report generation appears in activity without bell spam");
        check(!get(login("staff2@example.invalid",false),"/employee/dashboard").body().contains("admin-overview"),"Other role dashboard unchanged");
        for(String group:List.of("Customers","Employees","Products","Audit-log"))check(get(admin,"/employee/dashboard").body().contains("id=\""+group+"\""),"Existing management group retained: "+group);
        // Leave fresh unread notices and enough activity for a real-browser layout check.
        for(int i=0;i<6;i++)new CustomerDAO().createCustomerAndAccount("Recent Customer "+i,"Address","Colombo","10000","077999994"+i,"recentadmin"+i+"@example.invalid","1990-01-01","unused","SAVINGS");
        CookieManager cookies=(CookieManager)admin.client.cookieHandler().orElseThrow();
        Files.writeString(Path.of("target/admin-dashboard-session.txt"),cookies.getCookieStore().getCookies().stream().filter(c->c.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue());
    }
    public static void main(String[] args)throws Exception{
        String db="banking_test_admin_dashboard_"+System.currentTimeMillis();List<String> ddl=new ArrayList<>();
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
            Context context=server.addWebapp("/bank",Path.of("target/WebBasedBankingSystem").toAbsolutePath().toString());context.setParentClassLoader(AdminDashboardTest.class.getClassLoader());server.start();started=true;http();verifyAdmin();
            Files.writeString(Path.of("verification/admin-dashboard-tests.json"),"{\"checks\":"+checks+",\"passed\":true}");System.out.println("ADMIN DASHBOARD AND REGRESSION CHECKS PASSED: "+checks);
            if(args.length>0&&args[0].equals("preview"))System.in.read();
        }finally{
            if(started){server.stop();server.destroy();}
            if(!db.matches("banking_test_admin_dashboard_[0-9]+"))throw new IllegalStateException("Unsafe test DB name");
            try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()){s.executeUpdate("DROP DATABASE `"+db+"`");}
            System.clearProperty("bank.db.url");
        }
    }
}
