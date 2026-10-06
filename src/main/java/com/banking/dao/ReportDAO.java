package com.banking.dao;

import com.banking.model.AdminReport;
import com.banking.util.DBConnection;
import com.banking.util.Input;
import java.sql.*;
import java.time.*;
import java.util.*;

public class ReportDAO {
    public static final int MAX_ROWS = 5000;
    public static final Map<String,String> TYPES;
    static {
        Map<String,String> types = new LinkedHashMap<>();
        types.put("CUSTOMER", "Customer Report"); types.put("EMPLOYEE", "Employee Report");
        types.put("PRODUCT", "Banking Product Report"); types.put("AUDIT", "Audit Log Report");
        TYPES = Collections.unmodifiableMap(types);
    }
    public record Filter(String type, String status, String role, String productType, String action,
            LocalDate from, LocalDate to) implements java.io.Serializable {
        public Filter {
            Input.choice(type, TYPES.keySet().toArray(String[]::new));
            status = optional(status, 20); role = optional(role, 50);
            productType = optional(productType, 50); action = optional(action, 100);
            if (!status.isEmpty()) Input.choice(status, type.equals("CUSTOMER")
                    ? new String[]{"ACTIVE","INACTIVE","BLOCKED"} : new String[]{"ACTIVE","INACTIVE"});
            if (!role.isEmpty()) Input.choice(role, AdminDAO.ROLES);
            if (type.equals("AUDIT") && !status.isEmpty() || !type.equals("EMPLOYEE") && !role.isEmpty()
                    || !type.equals("PRODUCT") && !productType.isEmpty() || !type.equals("AUDIT") && !action.isEmpty())
                throw new IllegalArgumentException("A filter does not apply to this report type.");
            if (from != null && (from.getYear()<1000 || from.getYear()>9998)
                    || to != null && (to.getYear()<1000 || to.getYear()>9998))
                throw new IllegalArgumentException("Choose dates between years 1000 and 9998.");
            if (from != null && to != null && from.isAfter(to))
                throw new IllegalArgumentException("From date must not be after To date.");
        }
        private static String optional(String value, int max) {
            return value == null || value.isBlank() ? "" : Input.text(value, max, "filter");
        }
        public Map<String,String> labels() {
            Map<String,String> out = new LinkedHashMap<>();
            if (!status.isEmpty()) out.put("Status",status);
            if (!role.isEmpty()) out.put("Role",role);
            if (!productType.isEmpty()) out.put("Product type",productType);
            if (!action.isEmpty()) out.put("Action",action);
            if (from != null) out.put("From date",from.toString());
            if (to != null) out.put("To date (inclusive)",to.toString());
            return out;
        }
    }
    private record Definition(String table, String columns, String id, String date, List<String> headers) {}
    private Definition definition(String type) {
        return switch(type) {
            case "CUSTOMER" -> new Definition("customer r", "r.customer_id,r.name,r.email,r.phone,r.status,r.created_at", "r.customer_id", "r.created_at",
                    List.of("Customer ID","Name","Email","Phone","Status","Created date"));
            case "EMPLOYEE" -> new Definition("employee r", "r.employee_id,r.name,r.email,r.phone,r.role,r.status,r.created_at", "r.employee_id", "r.created_at",
                    List.of("Employee ID","Name","Email","Phone","Role","Status","Created date"));
            case "PRODUCT" -> new Definition("banking_product r", "r.product_id,r.product_name,r.product_type,r.description,r.status,r.created_at", "r.product_id", "r.created_at",
                    List.of("Product ID","Product name","Product type","Description","Status","Created date"));
            case "AUDIT" -> new Definition("audit_log r LEFT JOIN employee e ON e.employee_id=r.employee_id", "r.log_id,r.employee_id,e.name,r.action,r.details,r.action_time", "r.log_id", "r.action_time",
                    List.of("Log ID","Employee ID","Employee name","Action","Details","Action time"));
            default -> throw new IllegalArgumentException("Invalid report type.");
        };
    }
    public Map<String,Long> totals() throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            var row=Jdbc.one(c,"SELECT (SELECT COUNT(*) FROM customer) customers,(SELECT COUNT(*) FROM employee) employees,"
                    + "(SELECT COUNT(*) FROM banking_product) products,(SELECT COUNT(*) FROM audit_log) audit_records");
            Map<String,Long> result=new LinkedHashMap<>();
            for(String key:List.of("customers","employees","products","audit_records"))result.put(key,((Number)row.get(key)).longValue());
            return result;
        }
    }
    public List<String> productTypes() throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            return Jdbc.rows(c,"SELECT DISTINCT product_type FROM banking_product ORDER BY product_type LIMIT 200")
                    .stream().map(r->String.valueOf(r.get("product_type"))).toList();
        }
    }
    public AdminReport generate(int admin, String name, Filter filter) throws SQLException {
        Definition d=definition(filter.type());
        StringBuilder where=new StringBuilder(" WHERE 1=1"); List<Object> args=new ArrayList<>();
        if(!filter.status().isEmpty()){where.append(" AND r.status=?");args.add(filter.status());}
        if(!filter.role().isEmpty()){where.append(" AND r.role=?");args.add(filter.role());}
        if(!filter.productType().isEmpty()){where.append(" AND r.product_type=?");args.add(filter.productType());}
        if(!filter.action().isEmpty()){where.append(" AND r.action=?");args.add(filter.action());}
        if(filter.from()!=null){where.append(" AND ").append(d.date()).append(">=?");args.add(Timestamp.valueOf(filter.from().atStartOfDay()));}
        if(filter.to()!=null){where.append(" AND ").append(d.date()).append("<?");args.add(Timestamp.valueOf(filter.to().plusDays(1).atStartOfDay()));}
        return Jdbc.transaction(c->{
            Jdbc.staff(c,admin,"SYSTEM_ADMIN");
            List<List<String>> rows=new ArrayList<>();
            // Fetch at most one beyond the limit; never silently truncate a report.
            try(PreparedStatement ps=Jdbc.prepare(c,"SELECT "+d.columns()+" FROM "+d.table()+where+" ORDER BY "+d.id()+" DESC LIMIT "+(MAX_ROWS+1),args.toArray())) {
                ps.setQueryTimeout(30);
                try(ResultSet rs=ps.executeQuery()) {
                    while(rs.next()) {
                        if(rows.size()==MAX_ROWS)throw new IllegalArgumentException("More than 5,000 matching records. Narrow the filters and generate again.");
                        List<String> cells=new ArrayList<>();
                        for(int i=1;i<=d.headers().size();i++){String value=rs.getString(i);cells.add(value==null?"":value);}
                        rows.add(cells);
                    }
                }
            }
            AdminReport report=new AdminReport(filter.type(),TYPES.get(filter.type()),d.headers(),rows,filter.labels(),
                    name+" (System Administrator)",OffsetDateTime.now(),UUID.randomUUID().toString());
            Jdbc.audit(c,admin,"REPORT_GENERATED","Generated "+filter.type()+" report; records="+rows.size()+"; filters="+filter.labels());
            return report;
        });
    }
}
