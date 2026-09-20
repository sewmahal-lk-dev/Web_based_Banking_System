import com.banking.util.DBConnection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * LankaTrust database migration runner.
 *
 * Supports:
 * --apply-v2
 * --apply-v3
 * --apply-v4
 * --apply-v5
 * --apply-v6
 * --apply-v7
 *
 * A fresh database snapshot must be taken by Apply-Migration.ps1
 * before this runner is executed.
 */
public class ApplyMigration {

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            throw new IllegalArgumentException(
                    "Explicit --apply-v2, --apply-v3, --apply-v4, --apply-v5, --apply-v6 or --apply-v7 required. " +
                            "Take a fresh database snapshot first.");
        }

        switch (args[0]) {
            case "--apply-v7":
                try(Connection c=DBConnection.getConnection()) {
                    validateDatabase(c);
                    for(String sql:readStatements(Path.of("database/migrations/V7__notifications.sql")))
                        try(Statement st=c.createStatement()){st.execute(sql);}
                    System.out.println("V7 notifications applied.");
                }
                break;
            case "--apply-v6":
                try (Connection c=DBConnection.getConnection()) {
                    validateDatabase(c);
                    try(PreparedStatement ps=c.prepareStatement("SELECT COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='ticket' AND COLUMN_NAME='assigned_role'");ResultSet rs=ps.executeQuery()) {
                        if(rs.next())throw new IllegalStateException("assigned_role already exists; inspect schema before running V6 again.");
                    }
                    for(String sql:readStatements(Path.of("database/migrations/V6__ticket_department_routing.sql")))
                        try(Statement st=c.createStatement()){st.execute(sql);}
                    System.out.println("V6 ticket department routing applied.");
                }
                break;
            case "--apply-v5":
                try (Connection c = DBConnection.getConnection()) {
                    validateDatabase(c);
                    for(String sql:readStatements(Path.of("database/migrations/V5__ticket_conversations.sql")))
                        try(Statement st=c.createStatement()){st.execute(sql);}
                }
                break;
            case "--apply-v2":
                applyV2();
                break;

            case "--apply-v4":
                applyV4();
                break;

            case "--apply-v3":
                applyV3();
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported migration option: " + args[0]);
        }
    }

    private static void applyV4() throws Exception {
        try (Connection connection = DBConnection.getConnection()) {
            validateDatabase(connection);
            try (PreparedStatement ps = connection.prepareStatement("SELECT DATA_TYPE,CHARACTER_MAXIMUM_LENGTH,IS_NULLABLE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='payment' AND COLUMN_NAME='bill_reference'"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (!"varchar".equals(rs.getString(1)) || rs.getInt(2) != 100 || !"YES".equals(rs.getString(3))) throw new IllegalStateException("Existing bill_reference column does not match V4.");
                    System.out.println("V4 already applied.");
                    return;
                }
            }
            for (String sql : readStatements(Path.of("database/migrations/V4__bill_reference.sql"))) {
                try (Statement statement = connection.createStatement()) { statement.execute(sql); }
            }
            System.out.println("V4 bill reference migration applied.");
        }
    }

    private static void applyV2() throws Exception {

        Path migrationFile = Path.of("database/migrations/V2__complete_workflows.sql");

        if (!Files.exists(migrationFile)) {
            throw new IllegalStateException(
                    "V2 migration file was not found: " + migrationFile);
        }

        List<String> statements = readStatements(migrationFile);

        try (Connection connection = DBConnection.getConnection()) {

            validateDatabase(connection);

            for (String sql : statements) {

                String trimmed = sql.trim();

                if (!trimmed.startsWith("ALTER TABLE ")) {
                    throw new IllegalStateException(
                            "Unexpected V2 migration command.");
                }

                String[] parts = trimmed.split("\\s+");

                if (parts.length < 3) {
                    throw new IllegalStateException(
                            "Unable to determine V2 migration table.");
                }

                String table = parts[2];

                boolean applied;

                if ("payment".equals(table)) {

                    try (PreparedStatement ps = connection.prepareStatement(
                            "SELECT COLUMN_TYPE " +
                                    "FROM information_schema.COLUMNS " +
                                    "WHERE TABLE_SCHEMA = DATABASE() " +
                                    "AND TABLE_NAME = 'payment' " +
                                    "AND COLUMN_NAME = 'payment_type'");
                            ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new IllegalStateException(
                                    "payment.payment_type was not found.");
                        }

                        applied = rs.getString("COLUMN_TYPE")
                                .contains("INVESTMENT_PAYOUT");
                    }

                } else {

                    String column;

                    if ("card".equals(table)) {
                        column = "decision_note";
                    } else if ("service_request".equals(table)) {
                        column = "response";
                    } else {
                        column = "account_number";
                    }

                    try (ResultSet rs = connection.getMetaData().getColumns(
                            connection.getCatalog(),
                            null,
                            table,
                            column)) {

                        applied = rs.next();
                    }
                }

                if (applied) {
                    System.out.println(
                            "Already applied: " + table);
                    continue;
                }

                try (Statement statement = connection.createStatement()) {

                    statement.execute(trimmed);
                }

                System.out.println(
                        "Migrated: " + table);
            }
        }

        System.out.println(
                "V2 migration complete. " +
                        "No existing records reset or removed.");
    }

    private static void applyV3() throws Exception {

        Path migrationFile = Path.of("database/migrations/V3__cash_transactions.sql");

        if (!Files.exists(migrationFile)) {
            throw new IllegalStateException(
                    "V3 migration file was not found: " + migrationFile);
        }

        try (Connection connection = DBConnection.getConnection()) {

            validateDatabase(connection);

            if (cashTransactionTypesExist(connection)) {

                System.out.println(
                        "V3 already applied: " +
                                "CASH_DEPOSIT and CASH_WITHDRAWAL exist.");

                return;
            }

            List<String> statements = readStatements(migrationFile);

            for (String sql : statements) {

                String trimmed = sql.trim();

                if (!trimmed.startsWith("ALTER TABLE payment")) {
                    throw new IllegalStateException(
                            "Unexpected V3 migration command. " +
                                    "Only ALTER TABLE payment is allowed.");
                }

                try (Statement statement = connection.createStatement()) {

                    statement.execute(trimmed);
                }

                System.out.println(
                        "Migrated: payment.payment_type");
            }

            if (!cashTransactionTypesExist(connection)) {
                throw new IllegalStateException(
                        "V3 migration executed but cash transaction " +
                                "payment types were not found.");
            }
        }

        System.out.println(
                "V3 migration complete. " +
                        "CASH_DEPOSIT and CASH_WITHDRAWAL are enabled.");
    }

    private static boolean cashTransactionTypesExist(
            Connection connection) throws Exception {

        String sql = "SELECT COLUMN_TYPE " +
                "FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "AND TABLE_NAME = 'payment' " +
                "AND COLUMN_NAME = 'payment_type'";

        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                throw new IllegalStateException(
                        "payment.payment_type was not found.");
            }

            String columnType = rs.getString("COLUMN_TYPE");

            return columnType.contains("CASH_DEPOSIT")
                    && columnType.contains("CASH_WITHDRAWAL");
        }
    }

    private static List<String> readStatements(
            Path migrationFile) throws Exception {

        String migration = Files.readString(migrationFile)
                .replaceAll("(?m)^\\s*--.*$", "");

        return Arrays.stream(migration.split(";"))
                .map(String::trim)
                .filter(statement -> !statement.isBlank())
                .toList();
    }

    private static void validateDatabase(
            Connection connection) throws Exception {

        String database = connection.getCatalog();

        if (database == null ||
                (!"banking_system".equals(database)
                        && !database.matches("banking_test_[0-9]+"))) {

            throw new IllegalStateException(
                    "Unexpected database: " + database);
        }

        System.out.println(
                "Connected database: " + database);
    }
}