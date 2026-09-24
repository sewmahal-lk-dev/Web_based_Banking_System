import com.banking.dao.*;
import com.banking.model.AdminReport;
import com.banking.util.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;

/** Isolated integration checks: existing test database only, temporary fixtures cleaned up. */
public class AdminReportTest {
    static int checks,admin;static String base="http://127.0.0.1:8772/bank",csrf;
    static HttpClient client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).followRedirects(HttpClient.Redirect.NEVER).build();
    static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;System.out.println("PASS "+label);}
    static Object scalar(String sql,Object...args)throws Exception{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);try(ResultSet r=p.executeQuery()){r.next();return r.getObject(1);}}}
    static void execute(String sql,Object...args)throws Exception{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);p.executeUpdate();}}
    static long count(String table)throws Exception{return ((Number)scalar("SELECT COUNT(*) FROM "+table)).longValue();}
    static HttpResponse<String> get(String path)throws Exception{return client.send(HttpRequest.newBuilder(URI.create(base+path)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    static String form(Map<String,String> fields){return fields.entrySet().stream().map(e->URLEncoder.encode(e.getKey(),java.nio.charset.StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),java.nio.charset.StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));}
    static HttpResponse<String> post(String path,Map<String,String> fields)throws Exception{return client.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form(fields))).build(),HttpResponse.BodyHandlers.ofString());}
    static String extract(String value,String regex){Matcher m=Pattern.compile(regex).matcher(value);if(!m.find())throw new AssertionError("Missing "+regex);return m.group(1);}
    public static void main(String[] args)throws Exception {
        String db=Files.readString(Path.of("database/test-database.txt")).trim();if(!db.matches("banking_test_[0-9]+"))throw new IllegalStateException("Test DB required");
        System.setProperty("bank.db.url","jdbc:mysql://localhost:3306/"+db+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Colombo");
        String marker="reports-test-"+UUID.randomUUID(),email=marker+"@example.invalid",password="Reports-Test-Only-2026!";
        execute("INSERT INTO employee(name,email,phone,password,role,status) VALUES(?,?,?,?, 'SYSTEM_ADMIN','ACTIVE')","Report Integration Admin",email,"0771234567",PasswordUtil.hashPassword(password));
        admin=((Number)scalar("SELECT employee_id FROM employee WHERE email=?",email)).intValue();
        Tomcat server=new Tomcat();server.setBaseDir(Path.of("target/report-tomcat").toAbsolutePath().toString());server.setPort(8772);server.getConnector().setProperty("address","127.0.0.1");
        Context context=server.addWebapp("/bank",Path.of("target/WebBasedBankingSystem").toAbsolutePath().toString());context.setParentClassLoader(AdminReportTest.class.getClassLoader());
        long product=0,employee=0;
        try {
            server.start();check(get("/admin/reports").statusCode()==302,"anonymous report page denied");check(get("/admin/reports/pdf").statusCode()==302,"anonymous PDF denied");
            check(post("/login",Map.of("email",email,"password",password)).statusCode()==302,"actual administrator password login");
            String page=get("/admin/reports").body();check(page.contains("Generate and download administrative reports."),"Reports JSP renders");csrf=extract(page,"name=\"csrf\" value=\"([^\"]+)\"");
            check(post("/admin/reports",Map.of("type","CUSTOMER")).statusCode()==403,"CSRF required for generation");
            check(get("/admin/reports/pdf").statusCode()==409,"PDF requires generated snapshot");
            long audits=count("audit_log");get("/admin/reports");check(count("audit_log")==audits,"simple views do not write audit records");
            for(String type:ReportDAO.TYPES.keySet()) {
                String table=switch(type){case "CUSTOMER"->"customer";case "EMPLOYEE"->"employee";case "PRODUCT"->"banking_product";default->"audit_log";};
                long expected=count(table);
                check(post("/admin/reports",Map.of("csrf",csrf,"type",type)).statusCode()==302,type+" report generated");
                page=get("/admin/reports").body();check(page.contains("Records: <strong>"+expected+"</strong>"),type+" preview count matches DB snapshot");
                String token=extract(page,"/admin/reports/pdf\\?report=([^\"]+)");
                var response=client.send(HttpRequest.newBuilder(URI.create(base+"/admin/reports/pdf?report="+token)).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
                check(response.statusCode()==200&&response.headers().firstValue("content-type").orElse("").contains("application/pdf"),type+" PDF endpoint");
                try(var doc=Loader.loadPDF(response.body())) {
                    String text=new PDFTextStripper().getText(doc);check(text.contains(ReportDAO.TYPES.get(type))&&text.contains("Total records: "+expected),type+" PDF opens with matching count");
                    if(type.equals("EMPLOYEE")){check(text.replaceAll("\\s+", "").contains(email),"PDF contains real DB employee data");check(doc.getNumberOfPages()>1,"multipage PDF");PDFTextStripper last=new PDFTextStripper();last.setStartPage(2);last.setEndPage(2);check(last.getText(doc).replaceAll("\\s+", " ").contains("Employee ID"),"PDF repeats table headers");}
                    if(type.equals("CUSTOMER")){Files.write(Path.of("verification/admin-customer-report.pdf"),response.body());javax.imageio.ImageIO.write(new PDFRenderer(doc).renderImageWithDPI(0,110),"png",Path.of("verification/admin-report-pdf.png").toFile());}
                }
                if(expected>25){String second=get("/admin/reports?page=2").body();check(second.contains("Page 2 of"),type+" preview pagination");}
            }
            check(post("/admin/reports",Map.of("csrf",csrf,"type","CUSTOMER","from","2026-09-24","to","2026-09-23")).statusCode()==400,"reversed date range rejected");
            check(post("/admin/reports",Map.of("csrf",csrf,"type","UNKNOWN")).statusCode()==400,"unknown report rejected");
            check(post("/admin/reports",Map.of("csrf",csrf,"type","CUSTOMER","role","SYSTEM_ADMIN")).statusCode()==400,"inapplicable filter rejected");
            ReportDAO dao=new ReportDAO();
            for(String type:List.of("CUSTOMER","EMPLOYEE","PRODUCT")) {
                var filter=new ReportDAO.Filter(type,"ACTIVE",type.equals("EMPLOYEE")?"SYSTEM_ADMIN":"","","",null,null);
                AdminReport report=dao.generate(admin,"Report Test",filter);
                String table=type.equals("CUSTOMER")?"customer":type.equals("EMPLOYEE")?"employee":"banking_product";
                long expected=((Number)scalar("SELECT COUNT(*) FROM "+table+" WHERE status='ACTIVE'"+(type.equals("EMPLOYEE")?" AND role='SYSTEM_ADMIN'":""))).longValue();
                check(report.rows().size()==expected,type+" status/role filters match DB");
            }
            String productType=String.valueOf(scalar("SELECT product_type FROM banking_product LIMIT 1"));
            check(dao.generate(admin,"Report Test",new ReportDAO.Filter("PRODUCT","","",productType,"",null,null)).rows().size()==((Number)scalar("SELECT COUNT(*) FROM banking_product WHERE product_type=?",productType)).longValue(),"product type exact filter");
            LocalDate today=LocalDate.now();
            long expected=((Number)scalar("SELECT COUNT(*) FROM audit_log WHERE action='REPORT_GENERATED' AND action_time>=? AND action_time<?",Timestamp.valueOf(today.atStartOfDay()),Timestamp.valueOf(today.plusDays(1).atStartOfDay()))).longValue();
            check(dao.generate(admin,"Report Test",new ReportDAO.Filter("AUDIT","","","","REPORT_GENERATED",today,today)).rows().size()==expected,"inclusive same-day action/date filter");
            check(dao.generate(admin,"Report Test",new ReportDAO.Filter("AUDIT","","","","' OR 1=1 --",null,null)).rows().isEmpty(),"SQL injection treated as literal filter");
            var totals=dao.totals();check(totals.get("customers")==count("customer")&&totals.get("employees")==count("employee")&&totals.get("products")==count("banking_product")&&totals.get("audit_records")==count("audit_log"),"all summary totals match database");
            for(String role:List.of("CUSTOMER","LOAN_OFFICER","CARD_SERVICES_OFFICER","INVESTMENT_OFFICER","CUSTOMER_SERVICE_OFFICER","COMPLIANCE_RISK_OFFICER")) {
                int user=((Number)scalar(role.equals("CUSTOMER")?"SELECT customer_id FROM customer WHERE status='ACTIVE' LIMIT 1":"SELECT employee_id FROM employee WHERE status='ACTIVE' AND role=? LIMIT 1",role.equals("CUSTOMER")?new Object[]{}:new Object[]{role})).intValue();
                var s=context.getManager().createSession(null);s.getSession().setAttribute("userId",user);s.getSession().setAttribute("role",role);s.getSession().setAttribute("userType",role.equals("CUSTOMER")?"CUSTOMER":"EMPLOYEE");s.getSession().setAttribute("csrf","test");
                HttpClient denied=HttpClient.newHttpClient();
                for(String path:List.of("/admin/reports","/admin/reports/pdf?report=anything"))check(denied.send(HttpRequest.newBuilder(URI.create(base+path)).header("Cookie","JSESSIONID="+s.getId()).GET().build(),HttpResponse.BodyHandlers.discarding()).statusCode()==403,role+" denied "+path);
                check(denied.send(HttpRequest.newBuilder(URI.create(base+"/admin/reports")).header("Cookie","JSESSIONID="+s.getId()).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString("csrf=test&type=CUSTOMER")).build(),HttpResponse.BodyHandlers.discarding()).statusCode()==403,role+" denied generation");
            }
            AdminDAO management=new AdminDAO();product=management.product(admin,null,marker,"TEST","Report regression fixture","ACTIVE");management.product(admin,(int)product,marker,"TEST","Updated fixture","INACTIVE");check("INACTIVE".equals(scalar("SELECT status FROM banking_product WHERE product_id=?",product)),"existing product create/update works");
            employee=management.employee(admin,null,"Report Test Officer",marker+"-officer@example.invalid","0771234567","LOAN_OFFICER","ACTIVE",password);management.employee(admin,(int)employee,"Updated Officer",marker+"-officer@example.invalid","0771234567","CARD_SERVICES_OFFICER","INACTIVE",null);check("INACTIVE".equals(scalar("SELECT status FROM employee WHERE employee_id=?",employee)),"existing employee create/update works");
            try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("INSERT INTO audit_log(employee_id,action,details) VALUES(?,?,?)")){c.setAutoCommit(false);for(int i=0;i<5001;i++){ps.setInt(1,admin);ps.setString(2,marker);ps.setString(3,"Limit fixture");ps.addBatch();}ps.executeBatch();c.commit();}
            try{dao.generate(admin,"Report Test",new ReportDAO.Filter("AUDIT","","","",marker,null,null));throw new AssertionError("limit not enforced");}catch(IllegalArgumentException expectedError){check(expectedError.getMessage().contains("5,000"),"large reports rejected without truncation");}
            execute("DELETE FROM audit_log WHERE employee_id=? AND action=?",admin,marker);
            // Browser may reuse this authenticated session while this explicitly isolated test server runs.
            CookieManager cm=(CookieManager)client.cookieHandler().orElseThrow();String cookie=cm.getCookieStore().getCookies().stream().filter(c->c.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
            Files.writeString(Path.of("target/report-session.txt"),cookie);
            Files.writeString(Path.of("verification/admin-report-tests.json"),"{\"checks\":"+checks+",\"passed\":true}");System.out.println("REPORT CHECKS PASSED: "+checks);
            if(args.length>0&&args[0].equals("preview"))System.in.read();
        } finally {
            server.stop();server.destroy();execute("DELETE FROM audit_log WHERE employee_id=?",admin);
            if(product!=0)execute("DELETE FROM banking_product WHERE product_id=?",product);
            if(employee!=0)execute("DELETE FROM employee WHERE employee_id=?",employee);
            execute("DELETE FROM employee WHERE employee_id=?",admin);
        }
    }
}
