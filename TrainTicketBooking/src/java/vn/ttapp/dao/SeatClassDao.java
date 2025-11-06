package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.SeatClass;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatClassDao {

    private SeatClass map(ResultSet rs) throws SQLException {
        SeatClass sc = new SeatClass();
        sc.setSeatClassId(rs.getInt("seat_class_id"));
        sc.setCode(rs.getString("code"));
        sc.setName(rs.getString("name"));
        return sc;
    }

    public List<SeatClass> findAll() throws SQLException {
        String sql = """
            SELECT seat_class_id, code, name
            FROM dbo.SeatClass
            ORDER BY name
        """;
        List<SeatClass> list = new ArrayList<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public SeatClass findById(int id) throws SQLException {
        String sql = """
            SELECT seat_class_id, code, name
            FROM dbo.SeatClass WHERE seat_class_id = ?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public SeatClass findByCode(String code) throws SQLException {
        String sql = """
            SELECT seat_class_id, code, name
            FROM dbo.SeatClass WHERE code = ?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean codeExists(String code) throws SQLException {
        String sql = "SELECT 1 FROM dbo.SeatClass WHERE code = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public Integer create(String code, String name) throws SQLException {
        String sql = """
            INSERT INTO dbo.SeatClass(code, name)
            OUTPUT INSERTED.seat_class_id
            VALUES(?, ?)
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setNString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public int update(SeatClass sc) throws SQLException {
        String sql = """
            UPDATE dbo.SeatClass
            SET code = ?, name = ?
            WHERE seat_class_id = ?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sc.getCode());
            ps.setNString(2, sc.getName());
            ps.setInt(3, sc.getSeatClassId());
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        String sql = "DELETE FROM dbo.SeatClass WHERE seat_class_id = ?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
