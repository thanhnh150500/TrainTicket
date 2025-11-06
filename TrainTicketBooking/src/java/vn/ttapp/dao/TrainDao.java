package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Train;
import vn.ttapp.model.Carriage;
import vn.ttapp.model.Seat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrainDao {

    // ==== Map cơ bản ====
    private Train mapTrain(ResultSet rs) throws SQLException {
        Train t = new Train();
        t.setTrainId(rs.getInt("train_id"));
        t.setCode(rs.getString("code"));
        t.setName(rs.getString("name"));
        return t;
    }

    private Carriage mapCarriageBase(ResultSet rs) throws SQLException {
        Carriage c = new Carriage();
        c.setCarriageId(rs.getInt("carriage_id"));
        c.setTrainId(rs.getInt("train_id"));
        c.setCode(rs.getString("code"));
        c.setSeatClassId((Integer) rs.getObject("seat_class_id"));
        c.setSortOrder((Integer) rs.getObject("sort_order"));
        return c;
    }

    private Carriage mapCarriageWithJoinedFields(ResultSet rs) throws SQLException {
        Carriage c = new Carriage();
        c.setCarriageId(rs.getInt("carriage_id"));
        c.setTrainId(rs.getInt("train_id"));
        c.setCode(rs.getString("carriage_code"));
        c.setSeatClassId((Integer) rs.getObject("seat_class_id"));
        c.setSortOrder((Integer) rs.getObject("sort_order"));
        c.setSeatClassCode(rs.getString("seat_class_code"));
        c.setSeatClassName(rs.getString("seat_class_name"));
        return c;
    }

    private Seat mapSeatWithJoinedFields(ResultSet rs) throws SQLException {
        Seat s = new Seat();
        s.setSeatId(rs.getInt("seat_id"));
        s.setCarriageId(rs.getInt("carriage_id"));
        s.setCode(rs.getString("code"));
        s.setSeatClassId((Integer) rs.getObject("seat_class_id"));
        s.setPositionInfo(rs.getString("position_info"));

        s.setSeatClassCode(rs.getString("seat_class_code"));
        s.setSeatClassName(rs.getString("seat_class_name"));
        s.setCarriageCode(rs.getString("carriage_code"));
        s.setTrainCode(rs.getString("train_code"));
        s.setTrainName(rs.getString("train_name"));
        return s;
    }

    // ==== READ cơ bản ====
    public Train findById(int id) throws SQLException {
        String sql = """
            SELECT train_id, code, name
            FROM dbo.Train
            WHERE train_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapTrain(rs) : null;
            }
        }
    }

    public Train findByCode(String code) throws SQLException {
        String sql = """
            SELECT train_id, code, name
            FROM dbo.Train
            WHERE code = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapTrain(rs) : null;
            }
        }
    }

    public List<Train> findAll() throws SQLException {
        String sql = """
            SELECT train_id, code, name
            FROM dbo.Train
            ORDER BY train_id DESC
        """;
        List<Train> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapTrain(rs));
            }
        }
        return list;
    }

    // ==== Exists / Create / Update / Delete ====
    public boolean codeExists(String code) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Train WHERE code = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Cho TripService
     */
    public boolean existsById(int trainId) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Train WHERE train_id = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Integer create(String code, String name) throws SQLException {
        String sql = """
            INSERT INTO dbo.Train(code, name)
            OUTPUT INSERTED.train_id
            VALUES(?, ?)
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setNString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        // fallback nếu OUTPUT không hoạt động
        Train t = findByCode(code);
        return (t != null ? t.getTrainId() : null);
    }

    public int update(Train t) throws SQLException {
        String sql = """
            UPDATE dbo.Train
            SET code = ?, name = ?
            WHERE train_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getCode());
            ps.setNString(2, t.getName());
            ps.setInt(3, t.getTrainId());
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        String sql = "DELETE FROM dbo.Train WHERE train_id = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    // ==== Thêm 2 hàm thô để ráp DTO ở Service ====
    public List<Carriage> findCarriagesByTrain(int trainId) throws SQLException {
        String sql = """
            SELECT
              ca.carriage_id,
              ca.train_id,
              ca.code          AS carriage_code,
              ca.seat_class_id,
              ca.sort_order,
              sc.code          AS seat_class_code,
              sc.name          AS seat_class_name
            FROM dbo.Carriage ca
            LEFT JOIN dbo.SeatClass sc ON sc.seat_class_id = ca.seat_class_id
            WHERE ca.train_id = ?
            ORDER BY ISNULL(ca.sort_order, 999999), ca.carriage_id
        """;
        List<Carriage> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Carriage cr = new Carriage();
                    cr.setCarriageId(rs.getInt("carriage_id"));
                    cr.setTrainId(rs.getInt("train_id"));
                    cr.setCode(rs.getString("carriage_code"));
                    cr.setSeatClassId((Integer) rs.getObject("seat_class_id"));
                    cr.setSortOrder((Integer) rs.getObject("sort_order"));
                    cr.setSeatClassCode(rs.getString("seat_class_code"));
                    cr.setSeatClassName(rs.getString("seat_class_name"));
                    list.add(cr);
                }
            }
        }
        return list;
    }

    public List<Seat> findSeatsByTrain(int trainId) throws SQLException {
        String sql = """
            SELECT
              se.seat_id,
              se.carriage_id,
              se.code,
              se.seat_class_id,
              se.position_info,
              sc.code     AS seat_class_code,
              sc.name     AS seat_class_name,
              ca.code     AS carriage_code,
              t.code      AS train_code,
              t.name      AS train_name
            FROM dbo.Seat se
            JOIN dbo.Carriage ca ON ca.carriage_id = se.carriage_id
            JOIN dbo.Train    t  ON t.train_id     = ca.train_id
            LEFT JOIN dbo.SeatClass sc ON sc.seat_class_id = se.seat_class_id
            WHERE ca.train_id = ?
            ORDER BY se.carriage_id, se.code
        """;
        List<Seat> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSeatWithJoinedFields(rs));
                }
            }
        }
        return list;
    }
}
