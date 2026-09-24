import com.banking.dao.EmployeeDAO;
import com.banking.dao.InvestmentDAO;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/** Transactional JDBC fixtures only: no network or database access. */
public class InvestmentSafetyTest {
    static State saved, working;
    static int commits, rollbacks, writes, checks;
    static String failAt;
    static boolean accountLocked;
    static class State {
        BigDecimal balance;
        String status = "PENDING";
        Integer officer = 3;
        int payments, audits;
        State(String balance) { this.balance = new BigDecimal(balance); }
        State copy() {
            State s = new State(balance.toPlainString());
            s.status = status; s.officer = officer; s.payments = payments; s.audits = audits;
            return s;
        }
    }
    interface Operation { void run() throws Exception; }
    static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        checks++; System.out.println("PASS " + label);
    }
    static void reset(String balance) {
        saved = new State(balance); commits = rollbacks = writes = 0; failAt = null;
    }
    static void approve() throws Exception { new InvestmentDAO().process(3, 1, true, "Reviewed funding"); }
    static void rejects(Operation operation, String message) throws Exception {
        State before = saved.copy();
        int previousCommits = commits, previousRollbacks = rollbacks;
        try { operation.run(); throw new AssertionError("Expected rejection"); }
        catch (IllegalArgumentException | SQLException e) {
            check(e.getMessage().contains(message), "failure reports " + message);
            check(commits == previousCommits && rollbacks == previousRollbacks + 1,
                    "failure rolls back without committing");
            check(saved.balance.equals(before.balance) && saved.status.equals(before.status)
                    && Objects.equals(saved.officer, before.officer)
                    && saved.payments == before.payments && saved.audits == before.audits,
                    "failure preserves account, investment, payment and audit state");
        }
    }
    public static void main(String[] args) throws Exception {
        Class.forName("com.banking.util.DBConnection");
        for (Driver driver : Collections.list(DriverManager.getDrivers())) DriverManager.deregisterDriver(driver);
        DriverManager.registerDriver(new Driver() {
            public Connection connect(String url, Properties p) { return connection(); }
            public boolean acceptsURL(String url) { return true; }
            public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) { return new DriverPropertyInfo[0]; }
            public int getMajorVersion() { return 1; }
            public int getMinorVersion() { return 0; }
            public boolean jdbcCompliant() { return false; }
            public Logger getParentLogger() { return Logger.getGlobal(); }
        });
        reset("94666.67");
        rejects(InvestmentSafetyTest::approve, "Insufficient account balance");
        check(writes == 0, "insufficient balance is detected before any write");
        reset("150000.00"); saved.officer = null;
        new EmployeeDAO().act(3, "INVESTMENT_OFFICER", "investment-assign", "1", null, "Review assignment");
        check(saved.officer == 3 && saved.status.equals("PENDING") && saved.balance.equals(new BigDecimal("150000.00"))
                && saved.audits == 1, "assignment records officer and audit without funding");
        rejects(() -> new EmployeeDAO().act(3, "INVESTMENT_OFFICER", "investment-assign", "1", null, "Again"), "Record is not eligible");
        approve();
        check(saved.status.equals("ACTIVE") && saved.officer == 3 && saved.balance.equals(new BigDecimal("30000.00"))
                && saved.payments == 1 && saved.audits == 2 && commits == 2,
                "assigned officer funds 120000 and activates investment with payment and audit");
        rejects(InvestmentSafetyTest::approve, "Record not found");
        reset("120000.00"); approve();
        check(saved.balance.signum() == 0 && saved.status.equals("ACTIVE"), "exact balance funds successfully");
        for (String failure : List.of("payment", "activation", "audit")) {
            reset("150000.00"); failAt = failure;
            rejects(InvestmentSafetyTest::approve, "Simulated " + failure);
            check(writes >= 2, "rollback tested after debit for " + failure);
        }
        reset("94666.67"); new InvestmentDAO().process(3, 1, false, "Rejected after review");
        check(saved.status.equals("REJECTED") && saved.payments == 0
                && saved.balance.equals(new BigDecimal("94666.67")), "rejection does not require funding");
        System.out.println("Completed " + checks + " investment checks. No database accessed.");
    }
    static Connection connection() {
        working = saved.copy(); accountLocked = false;
        return proxy(Connection.class, (p,m,a) -> switch(m.getName()) {
            case "prepareStatement" -> statement((String)a[0]);
            case "commit" -> { saved = working.copy(); commits++; yield null; }
            case "rollback" -> { working = saved.copy(); rollbacks++; yield null; }
            default -> defaultValue(m.getReturnType());
        });
    }
    static PreparedStatement statement(String sql) {
        Map<Integer,Object> params = new HashMap<>();
        return proxy(PreparedStatement.class, (p,m,a) -> {
            if (m.getName().startsWith("set") && a != null && a.length >= 2) { params.put((Integer)a[0], a[1]); return null; }
            if (m.getName().equals("executeQuery")) {
                if (sql.contains("FROM employee")) {
                    if (!Objects.equals(params.get(1), 3)) return result(Map.of());
                    return result(sql.startsWith("SELECT role") ? Map.of("role", "INVESTMENT_OFFICER") : Map.of("employee_id", 3));
                }
                if (sql.contains("FROM customer")) return result(Map.of("customer_id", 1));
                if (sql.contains("FROM account")) {
                    if (!sql.contains("FOR UPDATE") || !Objects.equals(params.get(1), 4975567220610L)
                            || !Objects.equals(params.get(2), 1)) throw new AssertionError("Funding account must be owned and locked");
                    accountLocked = true;
                    return result(Map.of("account_number", 4975567220610L, "balance", working.balance));
                }
                if (sql.contains("FROM investment")) {
                    if (!working.status.equals("PENDING")) return result(Map.of());
                    if (sql.startsWith("SELECT amount")) return result(Map.of("amount", new BigDecimal("120000.00"), "term_months", 6));
                    return result(Map.of("customer_id", 1, "account_number", 4975567220610L));
                }
                throw new AssertionError("Unexpected query: " + sql);
            }
            if (m.getName().equals("executeUpdate")) {
                writes++;
                if (sql.startsWith("UPDATE account")) {
                    if (!accountLocked) throw new AssertionError("Account not locked");
                    BigDecimal amount = (BigDecimal)params.get(1);
                    if (working.balance.compareTo(amount) < 0) return 0;
                    working.balance = working.balance.subtract(amount); return 1;
                }
                if (sql.startsWith("INSERT INTO payment")) {
                    fail("payment");
                    if (!Objects.equals(params.get(2), "INVESTMENT_FUNDING") || !Objects.equals(params.get(5), "INV-FUND-1"))
                        throw new AssertionError("Incorrect funding payment");
                    working.payments++; return 1;
                }
                if (sql.startsWith("INSERT INTO audit_log")) { fail("audit"); working.audits++; return 1; }
                if (sql.startsWith("UPDATE investment")) {
                    if (sql.contains("SET employee_id")) {
                        if (working.officer != null || !working.status.equals("PENDING")) return 0;
                        working.officer = (Integer)params.get(1); return 1;
                    }
                    if (sql.contains("status='ACTIVE'")) {
                        fail("activation"); working.status = "ACTIVE";
                        if (!Objects.equals(params.get(2), java.sql.Date.valueOf(java.time.LocalDate.now().plusMonths(6))))
                            throw new AssertionError("Incorrect maturity date");
                    } else working.status = "REJECTED";
                    working.officer = (Integer)params.get(1); return 1;
                }
                throw new AssertionError("Unexpected write: " + sql);
            }
            return defaultValue(m.getReturnType());
        });
    }
    static void fail(String stage) throws SQLException { if (stage.equals(failAt)) throw new SQLException("Simulated " + stage); }
    static ResultSet result(Map<String,Object> row) {
        List<String> keys = new ArrayList<>(row.keySet()); boolean[] first = {true};
        return proxy(ResultSet.class, (p,m,a) -> switch(m.getName()) {
            case "next" -> { boolean found = first[0] && !row.isEmpty(); first[0] = false; yield found; }
            case "getMetaData" -> proxy(ResultSetMetaData.class, (q,n,b) -> switch(n.getName()) {
                case "getColumnCount" -> keys.size(); case "getColumnLabel" -> keys.get((Integer)b[0]-1);
                default -> defaultValue(n.getReturnType());
            });
            case "getObject", "getString" -> row.get(keys.get((Integer)a[0]-1));
            default -> defaultValue(m.getReturnType());
        });
    }
    static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
    static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }
}
