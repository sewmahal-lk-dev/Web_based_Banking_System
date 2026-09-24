package com.banking.dao;

import com.banking.util.DBConnection;
import java.sql.*;

public class SessionDAO {
    public boolean active(int id,String role) throws SQLException {
        String sql="CUSTOMER".equals(role) ? "SELECT customer_id FROM customer WHERE customer_id=? AND status='ACTIVE'" : "SELECT employee_id FROM employee WHERE employee_id=? AND status='ACTIVE' AND role=?";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,id);if(!"CUSTOMER".equals(role))ps.setString(2,role);try(ResultSet rs=ps.executeQuery()){return rs.next();}
        }
    }
}
