import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import org.apache.catalina.Session;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import com.banking.util.DBConnection;

/** Starts an isolated loopback Tomcat, reads existing records, and never submits valid mutations. */
class SmokeTest {
    static int checks;
    static void check(boolean value,String label){if(!value)throw new AssertionError(label);checks++;System.out.println("PASS "+label);}
    public static void main(String[] args)throws Exception {
        Tomcat tomcat=new Tomcat();tomcat.setBaseDir(Path.of("target/smoke-tomcat").toAbsolutePath().toString());tomcat.setPort(0);
        tomcat.getConnector().setProperty("address","127.0.0.1");
        Context context=tomcat.addWebapp("/bank",Path.of("target/WebBasedBankingSystem").toAbsolutePath().toString());
        context.setParentClassLoader(SmokeTest.class.getClassLoader());
        try {
            tomcat.start();String base="http://127.0.0.1:"+tomcat.getConnector().getLocalPort()+"/bank";
            HttpClient client=HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
            for(String route:List.of("/customer/dashboard","/customer/cards","/customer/settings","/customer/accounts.jsp","/employee/dashboard","/admin/dashboard.jsp"))check(get(client,base+route,null).statusCode()==302,"unauthenticated "+route);
            for(String route:List.of("/login.jsp","/register.jsp"))check(get(client,base+route,null).statusCode()==200,"public JSP "+route);
            Map<String,Integer> ids=new LinkedHashMap<>();
            try(Connection c=DBConnection.getConnection()) {
                c.setReadOnly(true);
                try(PreparedStatement ps=c.prepareStatement("SELECT customer_id FROM customer WHERE status='ACTIVE' ORDER BY customer_id LIMIT 1");ResultSet rs=ps.executeQuery()){if(rs.next())ids.put("CUSTOMER",rs.getInt(1));}
                try(PreparedStatement ps=c.prepareStatement("SELECT role,MIN(employee_id) FROM employee WHERE status='ACTIVE' GROUP BY role");ResultSet rs=ps.executeQuery()){while(rs.next())ids.put(rs.getString(1),rs.getInt(2));}
            }
            for(var entry:ids.entrySet()) {
                Session session=context.getManager().createSession(null);session.getSession().setAttribute("userId",entry.getValue());session.getSession().setAttribute("role",entry.getKey());session.getSession().setAttribute("userType","CUSTOMER".equals(entry.getKey())?"CUSTOMER":"EMPLOYEE");session.getSession().setAttribute("userName","<script>fixture</script>");session.getSession().setAttribute("userEmail","fixture@example.invalid");session.getSession().setAttribute("csrf","smoke-token");
                String cookie="JSESSIONID="+session.getId();
                if("CUSTOMER".equals(entry.getKey())) {
                    for(String section:List.of("dashboard","accounts","transfer","payments","cards","loans","investments","requests","settings","transactions")) {
                        var result=get(client,base+"/customer/"+section,cookie);
                        check(result.statusCode()==200,"customer render "+section);
                        check(!result.body().contains("<script>fixture</script>"),"HTML escaping "+section);
                        check(result.body().contains("customer-sidebar"),"shared sidebar "+section);
                        check(!result.body().contains("Unable to load")&&!result.body().contains("temporarily unavailable"),"database queries "+section);
                    }
                    check(get(client,base+"/admin/dashboard.jsp",cookie).statusCode()==403,"customer cannot access staff");
                    check(get(client,base+"/customer/transfer.jsp",cookie).statusCode()==302,"direct JSP routes through controller");
                    var result=client.send(HttpRequest.newBuilder(URI.create(base+"/customer/transfer")).header("Cookie",cookie).POST(HttpRequest.BodyPublishers.ofString("amount=1&receiverAccount=1")).header("Content-Type","application/x-www-form-urlencoded").build(),HttpResponse.BodyHandlers.ofString());
                    check(result.statusCode()==403,"missing CSRF rejected");
                } else {
                    var result=get(client,base+"/employee/dashboard",cookie);check(result.statusCode()==200,"staff render "+entry.getKey());check(!result.body().contains("Unable to load"),"staff queries "+entry.getKey());
                    check(get(client,base+"/customer/accounts",cookie).statusCode()==403,"staff cannot access customer "+entry.getKey());
                    if(!"SYSTEM_ADMIN".equals(entry.getKey()))check(get(client,base+"/admin/dashboard.jsp",cookie).statusCode()==403,"staff role isolation "+entry.getKey());
                }
                session.expire();
            }
            check(ids.containsKey("CUSTOMER"),"existing customer available for smoke checks");
            System.out.println("Completed "+checks+" read-only smoke checks. No database records changed.");
        } finally {tomcat.stop();tomcat.destroy();}
    }
    static HttpResponse<String> get(HttpClient client,String url,String cookie)throws Exception {
        var request=HttpRequest.newBuilder(URI.create(url));if(cookie!=null)request.header("Cookie",cookie);
        return client.send(request.GET().build(),HttpResponse.BodyHandlers.ofString());
    }
}
