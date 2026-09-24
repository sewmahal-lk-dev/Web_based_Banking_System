import com.banking.util.DBConnection;
import java.sql.*;
/** Read-only verification of the complaint submitted through deployed Chrome. */
public class VerifyDeployedComplaint {
 public static void main(String[] args)throws Exception {
  int id=Integer.parseInt(args[0]);
  try(Connection c=DBConnection.getConnection()) {
   try(PreparedStatement p=c.prepareStatement("SELECT t.ticket_type,t.status,t.priority,c.name FROM ticket t JOIN customer c ON c.customer_id=t.customer_id WHERE t.ticket_id=?")) {
    p.setInt(1,id);try(ResultSet r=p.executeQuery()) {
     if(!r.next()||!"COMPLAINT".equals(r.getString(1))||!"OPEN".equals(r.getString(2))||!"HIGH".equals(r.getString(3))||!"Complaint UI Verification".equals(r.getString(4)))throw new AssertionError("Complaint persistence");
    }
   }
   try(PreparedStatement p=c.prepareStatement("SELECT (SELECT COUNT(*) FROM employee WHERE role='CUSTOMER_SERVICE_OFFICER' AND status='ACTIVE'),COUNT(*),COUNT(DISTINCT n.employee_id),SUM(CASE WHEN e.role='CUSTOMER_SERVICE_OFFICER' AND e.status='ACTIVE' AND n.title='New Customer Complaint' AND n.is_read=FALSE THEN 1 ELSE 0 END) FROM notification n LEFT JOIN employee e ON e.employee_id=n.employee_id WHERE n.related_ticket_id=?")) {
    p.setInt(1,id);try(ResultSet r=p.executeQuery()) {r.next();int expected=r.getInt(1);if(expected==0||r.getInt(2)!=expected||r.getInt(3)!=expected||r.getInt(4)!=expected)throw new AssertionError("Complaint recipient mismatch");System.out.println("PASS deployed complaint #"+id+": COMPLAINT / OPEN / HIGH; authenticated verification customer; exactly one unread notification for each of "+expected+" active Customer Service officers, no unrelated recipients.");}
   }
  }
 }
}
