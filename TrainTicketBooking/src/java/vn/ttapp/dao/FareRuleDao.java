package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.FareRule;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FareRuleDao {

    private FareRule map(ResultSet rs) throws SQLException {
        FareRule f = new FareRule();
        f.setFareRuleId(rs.getInt("fare_rule_id"));
        f.setRouteId(rs.getInt("route_id"));
        f.setSeatClassId(rs.getInt("seat_class_id"));
        f.setBasePrice(rs.getBigDecimal("base_price"));
        Date ef = rs.getDate("effective_from");
        Date et = rs.getDate("effective_to");
        f.setEffectiveFrom(ef != null ? ef.toLocalDate() : null);
        f.setEffectiveTo(et != null ? et.toLocalDate() : null);
        f.setSeatClassCode(rs.getString("seat_class_code"));
        f.setSeatClassName(rs.getString("seat_class_name"));
        return f;
    }

    public List<FareRule> findAll() throws SQLException {
        String sql = """
            SELECT f.fare_rule_id, f.route_id, f.seat_class_id, f.base_price, f.effective_from, f.effective_to,
                   sc.code AS seat_class_code, sc.name AS seat_class_name
            FROM dbo.FareRule f
            JOIN dbo.SeatClass sc ON sc.seat_class_id = f.seat_class_id
            ORDER BY f.route_id, sc.name, f.effective_from DESC
        """;
        List<FareRule> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public FareRule findById(int id) throws SQLException {
        String sql = """
            SELECT f.fare_rule_id, f.route_id, f.seat_class_id, f.base_price, f.effective_from, f.effective_to,
                   sc.code AS seat_class_code, sc.name AS seat_class_name
            FROM dbo.FareRule f
            JOIN dbo.SeatClass sc ON sc.seat_class_id = f.seat_class_id
            WHERE f.fare_rule_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean hasOverlap(int routeId, int seatClassId, LocalDate from, LocalDate to, Integer excludeId) throws SQLException {
        String sql = """
      SELECT 1
      FROM dbo.FareRule f
      WHERE f.route_id=? AND f.seat_class_id=?
        AND ( ? IS NULL OR f.effective_to   IS NULL OR f.effective_to   >= ? )
        AND ( f.effective_from IS NULL OR ? IS NULL OR f.effective_from <= ? )
        """ + (excludeId != null ? " AND f.fare_rule_id <> ?" : "");
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            ps.setInt(2, seatClassId);
            // điều kiện giao nhau NULL-safe
            ps.setDate(3, (to == null) ? null : Date.valueOf(to));
            ps.setDate(4, (to == null) ? null : Date.valueOf(to));
            ps.setDate(5, (from == null) ? null : Date.valueOf(from));
            ps.setDate(6, (from == null) ? null : Date.valueOf(from));
            if (excludeId != null) {
                ps.setInt(7, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Integer create(int routeId, int seatClassId, BigDecimal price, LocalDate from, LocalDate to) throws SQLException {
        String sql = """
        INSERT INTO dbo.FareRule(route_id, seat_class_id, base_price, effective_from, effective_to)
        OUTPUT INSERTED.fare_rule_id
        VALUES(?, ?, ?, ?, ?)
    """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            ps.setInt(2, seatClassId);
            ps.setBigDecimal(3, price);

            if (from == null) {
                ps.setNull(4, Types.DATE);       // cho phép NULL
            } else {
                ps.setDate(4, Date.valueOf(from));
            }

            if (to == null) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(to));
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public int update(FareRule f) throws SQLException {
        String sql = """
        UPDATE dbo.FareRule
        SET route_id = ?, seat_class_id = ?, base_price = ?, effective_from = ?, effective_to = ?
        WHERE fare_rule_id = ?
    """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, f.getRouteId());
            ps.setInt(2, f.getSeatClassId());
            ps.setBigDecimal(3, f.getBasePrice());

            if (f.getEffectiveFrom() == null) {
                ps.setNull(4, Types.DATE);   // cho phep NULL
            } else {
                ps.setDate(4, Date.valueOf(f.getEffectiveFrom()));
            }

            if (f.getEffectiveTo() == null) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(f.getEffectiveTo()));
            }

            ps.setInt(6, f.getFareRuleId());
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM dbo.FareRule WHERE fare_rule_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    // Tìm rule đang hiệu lực cho route + seatClass + ngày đi
    public FareRule findActiveRule(int routeId, int seatClassId, LocalDate travelDate) throws SQLException {
        String sql = """
        SELECT TOP (1)
               f.fare_rule_id, f.route_id, f.seat_class_id, f.base_price, f.effective_from, f.effective_to,
               sc.code AS seat_class_code, sc.name AS seat_class_name
        FROM dbo.FareRule f
        JOIN dbo.SeatClass sc ON sc.seat_class_id = f.seat_class_id
        WHERE f.route_id = ?
          AND f.seat_class_id = ?
          AND (f.effective_from IS NULL OR f.effective_from <= ?)   -- cho phep NULL
          AND (f.effective_to   IS NULL OR f.effective_to   >= ?)
        ORDER BY ISNULL(f.effective_from, '1900-01-01') DESC, f.fare_rule_id DESC  -- pick rule mới nhất
    """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            Date d = Date.valueOf(travelDate);
            ps.setInt(1, routeId);
            ps.setInt(2, seatClassId);
            ps.setDate(3, d);
            ps.setDate(4, d);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public BigDecimal getPrice(int routeId, int seatClassId, LocalDate travelDate) throws SQLException {
        FareRule rule = findActiveRule(routeId, seatClassId, travelDate);
        if (rule == null) {
            throw new SQLException("No active fare rule for route=" + routeId
                    + ", seatClass=" + seatClassId + ", date=" + travelDate);
        }
        return rule.getBasePrice();
    }

    public BigDecimal getPriceForTrip(int tripId, int seatClassId) throws SQLException {
        String sql = """
        SELECT tr.route_id, CAST(tr.depart_at AS DATE) AS travel_date
        FROM dbo.Trip tr
        WHERE tr.trip_id = ?
    """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Trip not found: " + tripId);
                }
                int routeId = rs.getInt("route_id");
                LocalDate travelDate = rs.getDate("travel_date").toLocalDate();
                return getPrice(routeId, seatClassId, travelDate);  // ✅ gọi hàm trong cùng class
            }
        }
    }

}
