package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Seat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDao {

    private Seat map(ResultSet rs) throws SQLException {
        Seat s = new Seat();
        s.setSeatId(rs.getInt("seat_id"));
        s.setCarriageId(rs.getInt("carriage_id"));
        s.setCode(rs.getString("code"));
        s.setSeatClassId(rs.getInt("seat_class_id"));
        s.setPositionInfo(rs.getString("position_info"));
        s.setSeatClassCode(rs.getString("seat_class_code"));
        s.setSeatClassName(rs.getString("seat_class_name"));
        s.setCarriageCode(rs.getString("carriage_code"));
        s.setTrainCode(rs.getString("train_code"));
        s.setTrainName(rs.getString("train_name"));
        return s;
    }

    public List<Seat> findAll() throws SQLException {
        String sql = """
            SELECT s.seat_id, s.carriage_id, s.code, s.seat_class_id, s.position_info,
                   sc.code AS seat_class_code, sc.name AS seat_class_name,
                   c.code AS carriage_code, t.code AS train_code, t.name AS train_name
            FROM dbo.Seat s
            JOIN dbo.Carriage c ON c.carriage_id = s.carriage_id
            JOIN dbo.Train t ON t.train_id = c.train_id
            JOIN dbo.SeatClass sc ON sc.seat_class_id = s.seat_class_id
            ORDER BY t.code, c.code, s.code
        """;
        List<Seat> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Seat findById(int id) throws SQLException {
        String sql = """
            SELECT s.seat_id, s.carriage_id, s.code, s.seat_class_id, s.position_info,
                   sc.code AS seat_class_code, sc.name AS seat_class_name,
                   c.code AS carriage_code, t.code AS train_code, t.name AS train_name
            FROM dbo.Seat s
            JOIN dbo.Carriage c ON c.carriage_id = s.carriage_id
            JOIN dbo.Train t ON t.train_id = c.train_id
            JOIN dbo.SeatClass sc ON sc.seat_class_id = s.seat_class_id
            WHERE s.seat_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean codeExists(int carriageId, String code) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Seat WHERE carriage_id = ? AND code = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, carriageId);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Integer create(int carriageId, String code, int seatClassId, String positionInfo) throws SQLException {
        String sql = """
            INSERT INTO dbo.Seat(carriage_id, code, seat_class_id, position_info)
            OUTPUT INSERTED.seat_id
            VALUES(?, ?, ?, ?)
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, carriageId);
            ps.setString(2, code);
            ps.setInt(3, seatClassId);
            if (positionInfo == null || positionInfo.isBlank()) {
                ps.setNull(4, Types.NVARCHAR);
            } else {
                ps.setNString(4, positionInfo);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public int update(Seat s) throws SQLException {
        String sql = """
            UPDATE dbo.Seat
            SET carriage_id = ?, code = ?, seat_class_id = ?, position_info = ?
            WHERE seat_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getCarriageId());
            ps.setString(2, s.getCode());
            ps.setInt(3, s.getSeatClassId());
            if (s.getPositionInfo() == null || s.getPositionInfo().isBlank()) {
                ps.setNull(4, Types.NVARCHAR);
            } else {
                ps.setNString(4, s.getPositionInfo());
            }
            ps.setInt(5, s.getSeatId());
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM dbo.Seat WHERE seat_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
