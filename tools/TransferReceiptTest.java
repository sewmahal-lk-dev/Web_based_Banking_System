import com.banking.util.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import java.math.BigDecimal;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

/** Copies table definitions only into a new isolated test DB; never changes live banking records. */
public class TransferReceiptTest {
    static int checks;static String base="http://127.0.0.1:8773/bank",password="Receipt-Test-Only-2026!";
    static class User {HttpClient client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).followRedirects(HttpClient.Redirect.NEVER).build();String csrf;}
    static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;System.out.println("PASS "+label);}
    static void execute(String sql,Object...args)throws Exception{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);p.executeUpdate();}}
    static Object scalar(String sql,Object...args)throws Exception{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);try(ResultSet r=p.executeQuery()){r.next();return r.getObject(1);}}}
    static String extract(String value,String regex){Matcher m=Pattern.compile(regex).matcher(value);if(!m.find())throw new AssertionError("Missing "+regex);return m.group(1);}
    static HttpResponse<String> get(User user,String path)throws Exception{return user.client.send(HttpRequest.newBuilder(URI.create(base+path)).GET().build(),HttpResponse.BodyHandlers.ofString());}
    static HttpResponse<String> post(User user,String path,Map<String,String> fields)throws Exception{Map<String,String> values=new LinkedHashMap<>(fields);if(user.csrf!=null)values.put("csrf",user.csrf);String data=values.entrySet().stream().map(e->URLEncoder.encode(e.getKey(),java.nio.charset.StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),java.nio.charset.StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));return user.client.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(data)).build(),HttpResponse.BodyHandlers.ofString());}
    static User login(String email)throws Exception{User user=new User();check(post(user,"/login",Map.of("email",email,"password",password)).statusCode()==302,"actual password login");String page=get(user,"/customer/transfer").body();user.csrf=extract(page,"name=\"csrf\" value=\"([^\"]+)\"");return user;}
    static BigDecimal balance(long account)throws Exception{return (BigDecimal)scalar("SELECT balance FROM account WHERE account_number=?",account);}
    public static void main(String[] args)throws Exception {
        String db="banking_test_receipts_"+System.currentTimeMillis();
        List<String> ddl=new ArrayList<>();
        // Read authoritative schema metadata before switching to the isolated database.
        try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()) {
            for(String table:List.of("customer","employee","account","payment","audit_log"))try(ResultSet rs=s.executeQuery("SHOW CREATE TABLE "+table)){rs.next();ddl.add(rs.getString(2));}
            s.executeUpdate("CREATE DATABASE `"+db+"` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
        System.setProperty("bank.db.url","jdbc:mysql://localhost:3306/"+db+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Colombo");
        Tomcat server=new Tomcat();boolean started=false;
        try {
            for(String sql:ddl)execute(sql);
            String hash=PasswordUtil.hashPassword(password);
            execute("INSERT INTO customer(customer_id,name,email,phone,password,status) VALUES(1,'Receipt Sender','sender@example.invalid','0771000001',?,'ACTIVE'),(2,'Receipt Receiver','receiver@example.invalid','0771000002',?,'ACTIVE')",hash,hash);
            execute("INSERT INTO account(account_number,customer_id,account_type,balance,status,open_date) VALUES(9100000610,1,'SAVINGS',100,'ACTIVE',CURRENT_DATE),(9100000001,2,'SAVINGS',40,'ACTIVE',CURRENT_DATE)");
            execute("INSERT INTO employee(employee_id,name,email,phone,password,role,status) VALUES(1,'Receipt Staff','staff@example.invalid','0771000003',?,'SYSTEM_ADMIN','ACTIVE')",hash);
            server.setBaseDir(Path.of("target/receipt-tomcat").toAbsolutePath().toString());server.setPort(8773);server.getConnector().setProperty("address","127.0.0.1");
            Context context=server.addWebapp("/bank",Path.of("target/WebBasedBankingSystem").toAbsolutePath().toString());context.setParentClassLoader(TransferReceiptTest.class.getClassLoader());server.start();started=true;
            check(get(new User(),"/customer/transfer/receipt?reference=anything").statusCode()==302,"anonymous receipt denied");
            User sender=login("sender@example.invalid"),receiver=login("receiver@example.invalid");
            var sent=post(sender,"/customer/transfer",Map.of("receiverAccount","9100000001","amount","1.25"));
            check(sent.statusCode()==302,"valid transfer completes");String redirect=sent.headers().firstValue("location").orElseThrow();String reference=extract(redirect,"receipt=([^&]+)");
            check(reference.equals(scalar("SELECT reference_number FROM payment WHERE account_number=9100000610 AND payment_type='TRANSFER'")),"redirect uses exact committed reference");
            check(balance(9100000610L).compareTo(new BigDecimal("98.75"))==0&&balance(9100000001L).compareTo(new BigDecimal("41.25"))==0,"balances moved exactly once");
            String page=get(sender,"/customer/transfer?receipt="+reference).body();check(page.contains("Transfer completed successfully.")&&page.contains("Download Receipt")&&page.contains("reference="+reference),"success confirmation has exact receipt button");
            String path="/customer/transfer/receipt?reference="+reference;
            var download=sender.client.send(HttpRequest.newBuilder(URI.create(base+path+"&amount=999999&recipient=1234")).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
            check(download.statusCode()==200&&download.headers().firstValue("content-type").orElse("").equals("application/pdf"),"PDF downloads");
            check(download.headers().firstValue("content-disposition").orElse("").contains("LankaTrust-Transfer-Receipt-"+reference+".pdf"),"useful attachment filename");
            check(download.headers().firstValue("cache-control").orElse("").contains("no-store"),"receipt not cached");
            try(var doc=Loader.loadPDF(download.body())){
                String text=new PDFTextStripper().getText(doc);check(doc.getNumberOfPages()==1,"PDF opens as one page");
                check(text.contains(reference)&&text.contains("LKR 1.25")&&!text.contains("999999"),"reference and amount match DB, browser data ignored");
                String timestamp=((Timestamp)scalar("SELECT payment_date FROM payment WHERE reference_number=?",reference)).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                check(text.contains(timestamp),"timestamp matches stored transfer");check(text.contains("**** 0610")&&text.contains("**** 0001")&&!text.contains("9100000610")&&!text.contains("9100000001"),"real accounts correctly masked");
                check(text.contains("COMPLETED")&&text.contains("TRANSFER"),"real transfer type and status");
                Files.write(Path.of("verification/transfer-receipt.pdf"),download.body());javax.imageio.ImageIO.write(new PDFRenderer(doc).renderImageWithDPI(0,110),"png",Path.of("verification/transfer-receipt.png").toFile());
            }
            for(int i=0;i<3;i++){check(get(sender,path).statusCode()==200,"repeat receipt download "+i);get(sender,"/customer/transfer?receipt="+reference);}
            check(((Number)scalar("SELECT COUNT(*) FROM payment")).intValue()==1&&balance(9100000610L).compareTo(new BigDecimal("98.75"))==0&&balance(9100000001L).compareTo(new BigDecimal("41.25"))==0,"downloads and confirmation refresh never repeat transfer");
            check(get(receiver,path).statusCode()==404,"receiver cannot download sender receipt");
            check(get(sender,"/customer/transfer/receipt?reference=not-found").statusCode()==404,"unknown reference denied");
            check(get(sender,"/customer/transfer/receipt").statusCode()==400,"missing reference rejected");
            check(get(sender,"/customer/transfer/receipt?reference=%27%20OR%201%3D1--").statusCode()==404,"reference SQL injection denied");
            String history=get(sender,"/customer/transactions").body();check(history.contains("reference="+reference)&&history.contains("Download Receipt"),"outgoing history includes receipt action");
            check(!get(receiver,"/customer/transactions").body().contains("Download Receipt"),"incoming history has no sender receipt action");
            var other=post(receiver,"/customer/transfer",Map.of("receiverAccount","9100000610","amount","0.50"));String otherReference=extract(other.headers().firstValue("location").orElseThrow(),"receipt=([^&]+)");
            check(get(sender,"/customer/transfer/receipt?reference="+otherReference).statusCode()==404,"changing URL to another customer's outgoing transfer denied");
            for(String status:List.of("PENDING","FAILED","CANCELLED")){
                execute("INSERT INTO payment(account_number,payment_type,recipient,amount,status,reference_number) VALUES(9100000610,'TRANSFER','9100000001',1,?,?)",status,"TEST-"+status);
                check(get(sender,"/customer/transfer/receipt?reference=TEST-"+status).statusCode()==404,status+" transfer cannot get receipt");
            }
            execute("INSERT INTO payment(account_number,payment_type,recipient,amount,status,reference_number) VALUES(9100000610,'BILL_PAYMENT','Test bill',1,'COMPLETED','TEST-BILL')");
            check(get(sender,"/customer/transfer/receipt?reference=TEST-BILL").statusCode()==404,"non-transfer receipt denied");
            User staff=new User();post(staff,"/login",Map.of("email","staff@example.invalid","password",password));check(get(staff,path).statusCode()==403,"staff receipt access denied");
            long count=((Number)scalar("SELECT COUNT(*) FROM payment")).longValue();
            check(post(sender,"/customer/transfer/receipt",Map.of("reference",reference)).statusCode()==405,"receipt endpoint does not accept transfer POST");
            check(((Number)scalar("SELECT COUNT(*) FROM payment")).longValue()==count,"receipt POST made no transaction");
            String failed=post(sender,"/customer/transfer",Map.of("receiverAccount","9100000001","amount","99999")).body();check(!failed.contains("Download Receipt"),"failed transfer has no success receipt");
            check(((Number)scalar("SELECT COUNT(*) FROM payment")).longValue()==count,"failed transfer has no payment record");
            var missingOptional=new com.banking.model.TransferReceipt(1,reference,null,"9100000610",null,new BigDecimal("1.25"),"TRANSFER","COMPLETED");
            try(var doc=Loader.loadPDF(TransferReceiptPdf.create(missingOptional))){String text=new PDFTextStripper().getText(doc);check(!text.contains("Date & Time")&&!text.contains("To Account"),"missing optional stored values are omitted, not invented");}
            CookieManager cm=(CookieManager)sender.client.cookieHandler().orElseThrow();String cookie=cm.getCookieStore().getCookies().stream().filter(c->c.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
            Files.writeString(Path.of("target/receipt-session.txt"),cookie+"\n"+reference);
            Files.writeString(Path.of("verification/transfer-receipt-tests.json"),"{\"checks\":"+checks+",\"passed\":true}");System.out.println("RECEIPT CHECKS PASSED: "+checks);
            if(args.length>0&&args[0].equals("preview"))System.in.read();
        }finally{
            if(started){server.stop();server.destroy();}
            if(!db.matches("banking_test_receipts_[0-9]+"))throw new IllegalStateException("Unsafe test database name");
            try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()){s.executeUpdate("DROP DATABASE `"+db+"`");}
        }
    }
}
