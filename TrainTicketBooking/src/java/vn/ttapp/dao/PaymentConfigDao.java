package vn.ttapp.dao;

import vn.ttapp.model.PaymentConfig;

import java.sql.*;

public class PaymentConfigDao {

    public static PaymentConfig getActiveConfig(Connection cn) throws SQLException {
        if (cn == null || cn.isClosed()) {
            throw new SQLException("Connection is null or closed in getActiveConfig()");
        }

        String sql = """
            SELECT TOP 1 payment_cfg_id, bank_code, bank_name, account_no, account_name, bin_code
            FROM dbo.PaymentConfig
            WHERE is_active = 1
            ORDER BY payment_cfg_id DESC
        """;

        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                PaymentConfig cfg = new PaymentConfig();
                cfg.setPaymentCfgId(rs.getInt("payment_cfg_id"));
                cfg.setBankCode(rs.getString("bank_code"));
                cfg.setBankName(rs.getString("bank_name"));
                cfg.setAccountNo(rs.getString("account_no"));
                cfg.setAccountName(rs.getString("account_name"));
                cfg.setBinCode(rs.getString("bin_code"));
                return cfg;
            }
        }

        return null;
    }
}
