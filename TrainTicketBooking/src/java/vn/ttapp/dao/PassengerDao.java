package vn.ttapp.dao;

import vn.ttapp.model.Passenger;
import vn.ttapp.config.Db;

import java.sql.*;
import java.util.List;

public class PassengerDao {

    public void batchInsert(List<Passenger> pax) throws SQLException {
        String sql = """
            INSERT INTO dbo.Passenger(booking_id, full_name, birth_date, id_number, phone, email)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Passenger p : pax) {
                ps.setLong(1, p.getBookingId());
                ps.setString(2, p.getFullName());
                if (p.getBirthDate() != null) {
                    ps.setDate(3, Date.valueOf(p.getBirthDate()));
                } else {
                    ps.setNull(3, Types.DATE);
                }
                ps.setString(4, p.getIdNumber());
                ps.setString(5, p.getPhone());
                ps.setString(6, p.getEmail());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
