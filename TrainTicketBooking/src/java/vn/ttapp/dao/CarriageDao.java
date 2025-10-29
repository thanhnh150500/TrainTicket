package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Carriage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarriageDao {

    private Carriage map(ResultSet rs) throws SQLException {
        Carriage x = new Carriage();
        x.setCarriageId(rs.getInt("carriage_id"));
        x.setTrainId(rs.getInt("train_id"));
        x.setCode(rs.getString("code"));
        x.setSeatClassId(rs.getInt("seat_class_id"));
        x.setSortOrder(rs.getInt("sort_order"));
        x.setTrainCode(rs.getString("train_code"));
        x.setTrainName(rs.getString("train_name"));
        x.setSeatClassCode(rs.getString("seat_class_code"));
        x.setSeatClassName(rs.getString("seat_class_name"));
        return x;
    }

    public List<Carriage> findAll() throws SQLException {
        String sql = """
            SELECT c.carriage_id, c.train_id, c.code, c.seat_class_id, c.sort_order,
                   t.code AS train_code, t.name AS train_name,
                   sc.code AS seat_class_code, sc.name AS seat_class_name
            FROM dbo.Carriage c
            JOIN dbo.Train t ON t.train_id = c.train_id
            JOIN dbo.SeatClass sc ON sc.seat_class_id = c.seat_class_id
            ORDER BY t.code, c.sort_order, c.code
        """;
        List<Carriage> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Carriage findById(int id) throws SQLException {
        String sql = """
            SELECT c.carriage_id, c.train_id, c.code, c.seat_class_id, c.sort_order,
                   t.code AS train_code, t.name AS train_name,
                   sc.code AS seat_class_code, sc.name AS seat_class_name
            FROM dbo.Carriage c
            JOIN dbo.Train t ON t.train_id = c.train_id
            JOIN dbo.SeatClass sc ON sc.seat_class_id = c.seat_class_id
            WHERE c.carriage_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }
// vn/ttapp/dao/CarriageDao.java (bổ sung)

    public Carriage findByTrainAndCode(int trainId, String code) throws SQLException {
        String sql = """
        SELECT c.carriage_id, c.train_id, c.code, c.seat_class_id, c.sort_order,
               t.code AS train_code, t.name AS train_name,
               sc.code AS seat_class_code, sc.name AS seat_class_name
        FROM dbo.Carriage c
        JOIN dbo.Train t ON t.train_id = c.train_id
        JOIN dbo.SeatClass sc ON sc.seat_class_id = c.seat_class_id
        WHERE c.train_id = ? AND c.code = ?
    """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean codeExists(int trainId, String code) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Carriage WHERE train_id = ? AND code = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Integer create(int trainId, String code, int seatClassId, int sortOrder) throws SQLException {
        String sql = """
            INSERT INTO dbo.Carriage(train_id, code, seat_class_id, sort_order)
            OUTPUT INSERTED.carriage_id
            VALUES(?, ?, ?, ?)
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            ps.setString(2, code);
            ps.setInt(3, seatClassId);
            ps.setInt(4, sortOrder);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public int update(Carriage x) throws SQLException {
        String sql = """
            UPDATE dbo.Carriage
            SET train_id = ?, code = ?, seat_class_id = ?, sort_order = ?
            WHERE carriage_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, x.getTrainId());
            ps.setString(2, x.getCode());
            ps.setInt(3, x.getSeatClassId());
            ps.setInt(4, x.getSortOrder());
            ps.setInt(5, x.getCarriageId());
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM dbo.Carriage WHERE carriage_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    public boolean existsById(int id) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Carriage WHERE carriage_id = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

}
