package com.banking.dao;

import com.banking.model.TransferReceipt;
import com.banking.util.DBConnection;
import java.sql.*;
import java.util.Optional;

public class TransferReceiptDAO {
    public Optional<TransferReceipt> findCompleted(int customerId, String reference) throws SQLException {
        String sql="SELECT p.payment_id,p.reference_number,p.payment_date,p.account_number,p.recipient,p.amount,p.payment_type,p.status "
                + "FROM payment p JOIN account a ON a.account_number=p.account_number "
                + "WHERE a.customer_id=? AND p.reference_number=? AND p.payment_type='TRANSFER' AND p.status='COMPLETED'";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,customerId);ps.setString(2,reference);
            try(ResultSet rs=ps.executeQuery()) {
                if(!rs.next())return Optional.empty();
                Timestamp timestamp=rs.getTimestamp("payment_date");
                return Optional.of(new TransferReceipt(rs.getLong("payment_id"),rs.getString("reference_number"),
                        timestamp==null?null:timestamp.toLocalDateTime(),rs.getString("account_number"),rs.getString("recipient"),
                        rs.getBigDecimal("amount"),rs.getString("payment_type"),rs.getString("status")));
            }
        }
    }
}
