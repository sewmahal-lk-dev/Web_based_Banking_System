package com.banking.dao;

import com.banking.util.DBConnection;
import java.sql.*;
import java.util.*;

/** Internal parameterized SQL helpers. SQL text must come only from application constants. */
final class Jdbc {
    private Jdbc(){}
    interface Work<T>{T run(Connection c)throws SQLException;}
    static <T>T transaction(Work<T> work)throws SQLException {
        try(Connection c=DBConnection.getConnection()){c.setAutoCommit(false);try{T result=work.run(c);c.commit();return result;}catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException rollback){e.addSuppressed(rollback);}throw e;}}
    }
    static PreparedStatement prepare(Connection c,String sql,Object...args)throws SQLException {PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);for(int i=0;i<args.length;i++)ps.setObject(i+1,args[i]);return ps;}
    static List<Map<String,Object>> rows(Connection c,String sql,Object...args)throws SQLException {try(PreparedStatement ps=prepare(c,sql,args);ResultSet rs=ps.executeQuery()){return TransactionDAO.rows(rs);}}
    static Map<String,Object> one(Connection c,String sql,Object...args)throws SQLException {var rows=rows(c,sql,args);if(rows.isEmpty())throw new IllegalArgumentException("Record not found, not owned by you, or no longer eligible. Refresh the page.");return rows.get(0);}
    static int update(Connection c,String sql,Object...args)throws SQLException {try(PreparedStatement ps=prepare(c,sql,args)){return ps.executeUpdate();}}
    static void exactlyOne(Connection c,String sql,Object...args)throws SQLException {if(update(c,sql,args)!=1)throw new IllegalArgumentException("Record changed or is not eligible. Refresh and try again.");}
    static long insert(Connection c,String sql,Object...args)throws SQLException {try(PreparedStatement ps=prepare(c,sql,args)){if(ps.executeUpdate()!=1)throw new SQLException("Insert failed");try(ResultSet rs=ps.getGeneratedKeys()){if(!rs.next())throw new SQLException("Generated ID missing");return rs.getLong(1);}}}
    static void staff(Connection c,int employee,String role)throws SQLException {one(c,"SELECT employee_id FROM employee WHERE employee_id=? AND role=? AND status='ACTIVE' FOR UPDATE",employee,role);}
    static void customer(Connection c,int customer)throws SQLException {one(c,"SELECT customer_id FROM customer WHERE customer_id=? AND status='ACTIVE' FOR UPDATE",customer);}
    static void audit(Connection c,Integer employee,String action,String details)throws SQLException {exactlyOne(c,"INSERT INTO audit_log(employee_id,action,details) VALUES(?,?,?)",employee,action,details.substring(0,Math.min(500,details.length())));}
}
