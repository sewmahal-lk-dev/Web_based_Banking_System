import com.banking.util.DBConnection;
import java.sql.*;

/** Read-only schema inspection. Does not query customer records. */
class InspectSchema {
    public static void main(String[] args) throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.setReadOnly(true);
            if (args.length > 0 && "products".equals(args[0])) {
                try (PreparedStatement ps = con.prepareStatement("SELECT product_id, product_name, product_type, description, status FROM banking_product"); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        for (int i=1;i<=5;i++) System.out.print(rs.getString(i) + " | ");
                        System.out.println();
                    }
                }
                return;
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, EXTRA " +
                    "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() ORDER BY TABLE_NAME, ORDINAL_POSITION");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    for (int i = 1; i <= 6; i++) System.out.print(rs.getString(i) + " | ");
                    System.out.println();
                }
            }
        }
    }
}
