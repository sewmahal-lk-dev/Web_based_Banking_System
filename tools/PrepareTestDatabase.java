import com.banking.util.DBConnection;
import java.sql.*;
import java.nio.file.*;

/** Creates a NEW empty test database. Refuses existing names. Never drops or truncates anything. */
class PrepareTestDatabase {
    public static void main(String[] args)throws Exception {
        String name=args[0];if(!name.matches("banking_test_[0-9]+"))throw new IllegalArgumentException("Use a dedicated banking_test_TIMESTAMP name");
        try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()) {
            s.executeUpdate("CREATE DATABASE `"+name+"` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            s.execute("USE `"+name+"`");
            for(String file:new String[]{"database/schema-v1.sql","database/migrations/V2__complete_workflows.sql"}) {
                String sql=Files.readString(Path.of(file)).replaceAll("(?m)^--.*$","");
                for(String command:sql.split(";"))if(!command.isBlank())s.execute(command);
            }
        }
        Files.writeString(Path.of("database/test-database.txt"),name);
        System.out.println("Created isolated test schema: "+name);
    }
}
