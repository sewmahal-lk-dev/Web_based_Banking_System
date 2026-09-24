import com.banking.util.DBConnection;
import java.sql.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Read-only fingerprint. Stores hashes and counts, never customer records or credentials. */
public class Task3DatabaseFingerprint {
 public static void main(String[] args) throws Exception {
  List<String> output=new ArrayList<>();
  try(Connection c=DBConnection.getConnection()) {
   c.setReadOnly(true);c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);c.setAutoCommit(false);
   List<String> tables=new ArrayList<>();
   try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SHOW FULL TABLES WHERE Table_type='BASE TABLE'")){while(r.next())tables.add(r.getString(1));}
   Collections.sort(tables);
   for(String table:tables){
    if(!table.matches("[A-Za-z0-9_]+"))throw new IllegalStateException("Unexpected table");
    String schema;List<String> rows=new ArrayList<>();
    try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SHOW CREATE TABLE `"+table+"`")){r.next();schema=r.getString(2);}
    try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM `"+table+"`")){
     while(r.next()){StringBuilder row=new StringBuilder();for(int i=1;i<=r.getMetaData().getColumnCount();i++){String value=r.getString(i);row.append(value==null?"NULL;":value.length()+":"+value+";");}rows.add(hash(row.toString()));}
    }
    Collections.sort(rows);output.add(table+" rows="+rows.size()+" schema="+hash(schema)+" data="+hash(String.join("\n",rows)));
   }
   c.rollback();
  }
  Files.write(Path.of(args[0]),output);System.out.println("Read-only fingerprint saved for "+output.size()+" tables.");
 }
 static String hash(String value)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}
}
