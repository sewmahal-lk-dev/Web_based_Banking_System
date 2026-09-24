import com.banking.util.DBConnection;
import java.sql.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Consistent read-only SQL snapshot. Restore into an EMPTY database only. */
class DatabaseSnapshot {
    public static void main(String[] args) throws Exception {
        Path dir=Path.of(args[0]);Files.createDirectories(dir);
        try(Connection c=DBConnection.getConnection()) {
            c.setReadOnly(true);c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);c.setAutoCommit(false);
            List<String> tables=new ArrayList<>();
            try(PreparedStatement ps=c.prepareStatement("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_TYPE='BASE TABLE' ORDER BY TABLE_NAME");ResultSet rs=ps.executeQuery()){while(rs.next())tables.add(rs.getString(1));}
            StringBuilder schema=new StringBuilder("-- Restore into an empty database. No DROP/TRUNCATE statements.\nSET FOREIGN_KEY_CHECKS=0;\n");
            StringBuilder data=new StringBuilder("SET FOREIGN_KEY_CHECKS=0;\nSTART TRANSACTION;\n");
            for(String table:tables){
                if(!table.matches("[A-Za-z0-9_]+"))throw new IllegalStateException("Unexpected identifier");
                try(Statement ps=c.createStatement();ResultSet rs=ps.executeQuery("SHOW CREATE TABLE `"+table+"`")){rs.next();schema.append(rs.getString(2)).append(";\n");}
                int count=0;
                try(Statement ps=c.createStatement();ResultSet rs=ps.executeQuery("SELECT * FROM `"+table+"`")){
                    while(rs.next()) {count++;data.append("INSERT INTO `").append(table).append("` VALUES (");
                        for(int i=1;i<=rs.getMetaData().getColumnCount();i++){if(i>1)data.append(',');String value=rs.getString(i);data.append(value==null?"NULL":"CONVERT(X'"+HexFormat.of().formatHex(value.getBytes(StandardCharsets.UTF_8))+"' USING utf8mb4)");}
                        data.append(");\n");
                    }
                }
                System.out.println(table+": "+count+" rows backed up");
            }
            schema.append("SET FOREIGN_KEY_CHECKS=1;\n");data.append("COMMIT;\nSET FOREIGN_KEY_CHECKS=1;\n");
            Files.writeString(dir.resolve("schema.sql"),schema);Files.writeString(dir.resolve("data.sql"),data);c.rollback();
        }
    }
}
