package com.banking.dao;
import com.banking.util.*;
import java.sql.*;
import java.nio.file.*;
import java.util.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;

/** Sort tests include more than the former 200-row cap and non-ID registration order. */
public class CustomerSortTest extends AdminDashboardTest {
    static void seedSort()throws Exception {
        for(int i=1;i<=230;i++)execute("INSERT INTO customer(customer_id,name,email,password,status,created_at) VALUES(?,?,?,?,?,?)",1000+i,String.format("Sort Test %03d",231-i),"sort"+i+"@example.invalid","unused",i%3==0?"INACTIVE":"ACTIVE",Timestamp.valueOf("2020-01-"+String.format("%02d",1+i%28)+" 12:00:00"));
        execute("UPDATE customer SET created_at='2050-01-01 12:00:00' WHERE customer_id=1010");
        execute("UPDATE customer SET created_at=NULL WHERE customer_id=1020");
        var records=new EmployeeDAO().dashboard("SYSTEM_ADMIN").get("Customers");
        check(records.size()>230,"All customers loaded beyond former 200-row cap");
        check(((Number)records.get(0).get("customer_id")).intValue()==1010,"Database default uses registration timestamp rather than highest ID");
        check(((Number)records.get(records.size()-1).get("customer_id")).intValue()==1020,"Missing legacy registration timestamp sorts last by default");
        StringBuilder json=new StringBuilder("{");boolean first=true;
        var orders=new LinkedHashMap<String,String>();orders.put("newest","created_at DESC,customer_id DESC");orders.put("oldest","created_at ASC,customer_id ASC");orders.put("name-asc","name ASC,customer_id ASC");orders.put("name-desc","name DESC,customer_id ASC");orders.put("id-desc","customer_id DESC");orders.put("id-asc","customer_id ASC");
        try(Connection c=DBConnection.getConnection()){
            for(var entry:orders.entrySet())for(String filter:List.of("all","search","status","both")){
                String where=switch(filter){case "search"->" WHERE name LIKE '%Sort Test 0%'";case "status"->" WHERE status='INACTIVE'";case "both"->" WHERE status='INACTIVE' AND name LIKE '%Sort Test 0%'";default->"";};
                var rows=Jdbc.rows(c,"SELECT customer_id FROM customer"+where+" ORDER BY "+entry.getValue());
                if(!first)json.append(',');first=false;json.append('"').append(entry.getKey()).append('-').append(filter).append("\":[");
                for(int i=0;i<rows.size();i++){if(i>0)json.append(',');json.append(rows.get(i).get("customer_id"));}json.append(']');
            }
        }
        Files.writeString(Path.of("target/customer-sort-expected.json"),json.append('}').toString());
        User admin=login("staff6@example.invalid",false);String page=get(admin,"/employee/dashboard").body();
        check(page.contains("data-customer-id=\"1010\"")&&page.contains("data-customer-created="),"Customer sorting metadata rendered from saved values");
        check(!page.contains("<dt>created at</dt>"),"Registration metadata does not add a visible detail field");
    }
    public static void main(String[] args)throws Exception{
        String db="banking_test_customer_sort_"+System.currentTimeMillis();List<String> ddl=new ArrayList<>();
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
            Context context=server.addWebapp("/bank",Path.of("target/WebBasedBankingSystem").toAbsolutePath().toString());context.setParentClassLoader(CustomerSortTest.class.getClassLoader());server.start();started=true;http();verifyAdmin();seedSort();
            Files.writeString(Path.of("verification/customer-sort-tests.json"),"{\"checks\":"+checks+",\"passed\":true}");System.out.println("CUSTOMER SORT AND REGRESSION CHECKS PASSED: "+checks);
            if(args.length>0&&args[0].equals("preview"))System.in.read();
        }finally{
            if(started){server.stop();server.destroy();}
            if(!db.matches("banking_test_customer_sort_[0-9]+"))throw new IllegalStateException("Unsafe test DB name");
            try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()){s.executeUpdate("DROP DATABASE `"+db+"`");}
            System.clearProperty("bank.db.url");
        }
    }
}
