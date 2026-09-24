package com.banking.dao;

import com.banking.util.*;
import java.sql.*;
import java.util.*;

public class AdminDAO {
    public static final String[] ROLES = { "LOAN_OFFICER", "CARD_SERVICES_OFFICER", "INVESTMENT_OFFICER",
            "CUSTOMER_SERVICE_OFFICER", "COMPLIANCE_RISK_OFFICER", "SYSTEM_ADMIN" };

    public List<Map<String,Object>> recentActivity(int admin)throws SQLException {
        return Jdbc.transaction(c->{
            Jdbc.staff(c,admin,"SYSTEM_ADMIN");
            return Jdbc.rows(c,"SELECT l.log_id,l.action,l.details,l.action_time,l.employee_id,e.name AS actor_name "
                +"FROM audit_log l LEFT JOIN employee e ON e.employee_id=l.employee_id "
                +"WHERE l.action IN ('EMPLOYEE_CREATE','EMPLOYEE_UPDATE','employee-status','PRODUCT_CREATE','PRODUCT_UPDATE','CUSTOMER_STATUS','REPORT_GENERATED') "
                +"ORDER BY l.log_id DESC LIMIT 5");
        });
    }

    public static String email(String value) {
        String email = Input.text(value, 150, "email").toLowerCase(Locale.ROOT);
        if (!email.matches("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"))
            throw new IllegalArgumentException("Enter a valid email address.");
        return email;
    }

    static void uniqueEmail(Connection c, String email, int id, boolean employee) throws SQLException {
        if (!Jdbc
                .rows(c, "SELECT customer_id FROM customer WHERE email=? AND customer_id<>?", email, employee ? -1 : id)
                .isEmpty()
                || !Jdbc.rows(c, "SELECT employee_id FROM employee WHERE email=? AND employee_id<>?", email,
                        employee ? id : -1).isEmpty())
            throw new IllegalArgumentException("Email is already in use.");
    }

    public long employee(int admin, Integer id, String name, String email, String phone, String role, String status,
            String password) throws SQLException {
        final String fullName = Input.text(name, 100, "name"), mail = email(email),
                contact = Input.text(phone, 20, "phone");
        Input.choice(role, ROLES);
        Input.choice(status, "ACTIVE", "INACTIVE");
        if (!contact.matches("[+0-9 ()-]{7,20}"))
            throw new IllegalArgumentException("Invalid phone number.");
        if (id == null && (password == null || password.length() < 8
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72))
            throw new IllegalArgumentException("Use an 8-character minimum password, at most 72 UTF-8 bytes.");
        if (id != null && id == admin)
            throw new IllegalArgumentException(
                    "You cannot change your own administrator access through employee management.");
        return Jdbc.transaction(c -> {
            Jdbc.staff(c, admin, "SYSTEM_ADMIN");
            uniqueEmail(c, mail, id == null ? -1 : id, true);
            long saved;
            if (id == null)
                saved = Jdbc.insert(c,
                        "INSERT INTO employee(name,email,phone,role,status,password) VALUES(?,?,?,?,?,?)", fullName,
                        mail, contact, role, status, PasswordUtil.hashPassword(password));
            else {
                var previous=Jdbc.one(c,"SELECT role,status FROM employee WHERE employee_id=? FOR UPDATE",id);
                Jdbc.exactlyOne(c, "UPDATE employee SET name=?,email=?,phone=?,role=?,status=? WHERE employee_id=?",
                        fullName, mail, contact, role, status, id);
                saved = id;
                if(!role.equals(previous.get("role"))||!status.equals(previous.get("status")))NotificationDAO.employeeAccess(c,id,admin);
            }
            Jdbc.audit(c, admin, id == null ? "EMPLOYEE_CREATE" : "EMPLOYEE_UPDATE", "Employee " + saved);
            return saved;
        });
    }

    public long product(int admin, Integer id, String name, String type, String description, String status)
            throws SQLException {
        final String title = Input.text(name, 100, "product name"), kind = Input.text(type, 50, "product type"),
                text = Input.text(description, 500, "description");
        Input.choice(status, "ACTIVE", "INACTIVE");
        return Jdbc.transaction(c -> {
            Jdbc.staff(c, admin, "SYSTEM_ADMIN");
            long saved;
            if (id == null)
                saved = Jdbc.insert(c,
                        "INSERT INTO banking_product(product_name,product_type,description,status) VALUES(?,?,?,?)",
                        title, kind, text, status);
            else {
                Jdbc.exactlyOne(c,
                        "UPDATE banking_product SET product_name=?,product_type=?,description=?,status=? WHERE product_id=?",
                        title, kind, text, status, id);
                saved = id;
            }
            Jdbc.audit(c, admin, id == null ? "PRODUCT_CREATE" : "PRODUCT_UPDATE", "Product " + saved);
            return saved;
        });
    }

    public void customerStatus(int admin, int customer, String status) throws SQLException {
        Input.choice(status, "ACTIVE", "INACTIVE");
        Jdbc.transaction(c -> {
            Jdbc.staff(c, admin, "SYSTEM_ADMIN");
            Jdbc.one(c, "SELECT customer_id FROM customer WHERE customer_id=? FOR UPDATE", customer);
            if ("INACTIVE".equals(status))
                AccountWorkflowDAO.deactivate(c, customer);
            else
                Jdbc.exactlyOne(c, "UPDATE customer SET status='ACTIVE' WHERE customer_id=? AND status='INACTIVE'",
                        customer);
            if("ACTIVE".equals(status))NotificationDAO.profileChanged(c,customer);
            Jdbc.audit(c, admin, "CUSTOMER_STATUS", "Customer " + customer + "; " + status);
            return null;
        });
    }
}
