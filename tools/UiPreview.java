import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import org.apache.catalina.Session;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import com.banking.util.DBConnection;
/** Isolated loopback visual test server. Reads test fixtures only; never deployed. */
class UiPreview {
 public static void main(String[] args)throws Exception {
  String db=Files.readString(Path.of("database/test-database.txt")).trim();
  if(!db.matches("banking_test_[0-9]+"))throw new IllegalStateException("Test database required");
  System.setProperty("bank.db.url","jdbc:mysql://localhost:3306/"+db+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
  Tomcat server=new Tomcat();server.setBaseDir(Path.of("target/ui-tomcat").toAbsolutePath().toString());server.setPort(8765);server.getConnector().setProperty("address","127.0.0.1");
  Context context=server.addWebapp("/bank",Path.of("target/WebBasedBankingSystem").toAbsolutePath().toString());context.setParentClassLoader(UiPreview.class.getClassLoader());server.start();
  List<String> cookies=new ArrayList<>();
  try(Connection c=DBConnection.getConnection()){c.setReadOnly(true);
   for(String role:List.of("CUSTOMER","LOAN_OFFICER","CARD_SERVICES_OFFICER","INVESTMENT_OFFICER","CUSTOMER_SERVICE_OFFICER","COMPLIANCE_RISK_OFFICER","SYSTEM_ADMIN")){
    String sql="CUSTOMER".equals(role)?"SELECT customer_id,name,email FROM customer WHERE status='ACTIVE' ORDER BY customer_id DESC LIMIT 1":"SELECT employee_id,name,email FROM employee WHERE status='ACTIVE' AND role=? ORDER BY employee_id DESC LIMIT 1";
    try(PreparedStatement ps=c.prepareStatement(sql)){if(!"CUSTOMER".equals(role))ps.setString(1,role);try(ResultSet rs=ps.executeQuery()){rs.next();Session session=context.getManager().createSession(null);var s=session.getSession();s.setAttribute("userId",rs.getInt(1));s.setAttribute("userName",rs.getString(2));s.setAttribute("userEmail",rs.getString(3));s.setAttribute("role",role);s.setAttribute("userType","CUSTOMER".equals(role)?"CUSTOMER":"EMPLOYEE");s.setAttribute("csrf",UUID.randomUUID().toString());cookies.add(role+"="+session.getId());}}
   }
  }Files.write(Path.of("target/ui-sessions.txt"),cookies);
  try{System.in.read();}finally{server.stop();server.destroy();}
 }
}
