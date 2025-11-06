package vn.ttapp.dao;

import vn.ttapp.model.PaymentTransaction;
import vn.ttapp.config.Db;

import java.sql.*;

public class PaymentTransactionDao {

    public long createInitiated(PaymentTransaction p) throws SQLException {
        String sql = """
            INSERT INTO dbo.PaymentTransaction(booking_id, method, amount, currency, status, idempotency_key)
            VALUES (?, ?, ?, 'VND', 'INITIATED', ?)
        """;
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.getBookingId());
            ps.setString(2, p.getMethod()); // VNPAY / VIETQR / CASH
            ps.setBigDecimal(3, p.getAmount());
            ps.setString(4, p.getIdempotencyKey());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0L;
    }

    public void markSuccess(long paymentId) throws SQLException {
        String sql = """
            UPDATE dbo.PaymentTransaction
            SET status='SUCCESS', confirmed_at=SYSUTCDATETIME()
            WHERE payment_id=?
        """;
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, paymentId);
            ps.executeUpdate();
        }
    }
}
