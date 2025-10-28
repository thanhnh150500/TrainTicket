package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Train;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainDao {

    private Train map(ResultSet rs) throws SQLException {
        Train t = new Train();
        t.setTrainId(rs.getInt("train_id"));
        t.setCode(rs.getString("code"));
        t.setName(rs.getString("name"));
        return t;
    }

    // ========== READ ==========
    public Train findById(int id) throws SQLException {
        String sql = """
                     SELECT train_id, code, name
                     FROM dbo.Train WHERE train_id = ?
                     """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Train findByCode(String code) throws SQLException {
        String sql = """
                     SELECT train_id, code, name
                     FROM dbo.Train WHERE code = ?
                     """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
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
                list.add(map(rs));
            }
        }
        return list;
    }

    public boolean codeExists(String code) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Train WHERE code = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
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
        Train t = findByCode(code);
        return t != null ? t.getTrainId() : null;
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
        // ==== META STRUCTS ====
//        public static class SeatMeta {
//
//            public int seatId;
//            public String code;
//            public String seatClassCode;
//            public String seatClassName;
//            public String positionInfo;
//        }
//
//        public static class CarriageMeta {
//
//            public int carriageId;
//            public String carriageCode;
//            public String seatClassCode;
//            public String seatClassName;
//            public int seatCount;
//            public int sortOrder;           // <-- THÊM
//            public List<SeatMeta> seats = new ArrayList<>();
//        }
//
//        public static class TrainMeta {
//
//            public int trainId;
//            public String code;
//            public String name;
//            public List<CarriageMeta> carriages = new ArrayList<>();
//            public int totalCarriages;
//            public int totalSeats;
//        }
//
//        
//        public TrainMeta findDetail(int trainId) throws SQLException {
//            TrainMeta meta = new TrainMeta();
//
//            // 1) Train
//            String sqlTrain = "SELECT train_id, code, name FROM dbo.Train WHERE train_id=?";
//            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sqlTrain)) {
//                ps.setInt(1, trainId);
//                try (ResultSet rs = ps.executeQuery()) {
//                    if (!rs.next()) {
//                        return null;
//                    }
//                    meta.trainId = rs.getInt("train_id");
//                    meta.code = rs.getString("code");
//                    meta.name = rs.getString("name");
//                }
//            }
//
//            // 2) Carriages (kèm sort_order & seat_count)
//            String sqlCar = """
//            SELECT ca.carriage_id,
//                   ca.code            AS carriage_code,
//                   ca.sort_order      AS sort_order,
//                   sc.code            AS seat_class_code,
//                   sc.name            AS seat_class_name,
//                   COUNT(se.seat_id)  AS seat_count
//            FROM dbo.Carriage ca
//            JOIN dbo.SeatClass sc ON sc.seat_class_id = ca.seat_class_id
//            LEFT JOIN dbo.Seat se ON se.carriage_id = ca.carriage_id
//            WHERE ca.train_id = ?
//            GROUP BY ca.carriage_id, ca.code, ca.sort_order, sc.code, sc.name
//            ORDER BY ca.sort_order, ca.carriage_id
//        """;
//            Map<Integer, CarriageMeta> carIndex = new HashMap<>();
//            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sqlCar)) {
//                ps.setInt(1, trainId);
//                try (ResultSet rs = ps.executeQuery()) {
//                    int totalSeats = 0, totalCarriages = 0;
//                    while (rs.next()) {
//                        CarriageMeta cm = new CarriageMeta();
//                        cm.carriageId = rs.getInt("carriage_id");
//                        cm.carriageCode = rs.getString("carriage_code");
//                        cm.sortOrder = rs.getInt("sort_order");
//                        cm.seatClassCode = rs.getString("seat_class_code");
//                        cm.seatClassName = rs.getString("seat_class_name");
//                        cm.seatCount = rs.getInt("seat_count");
//                        meta.carriages.add(cm);
//                        carIndex.put(cm.carriageId, cm);
//                        totalCarriages++;
//                        totalSeats += cm.seatCount;
//                    }
//                    meta.totalCarriages = totalCarriages;
//                    meta.totalSeats = totalSeats;
//                }
//            }
//
//            if (meta.carriages.isEmpty()) {
//                return meta;
//            }
//
//            // 3) Seats per carriage (đầy đủ thông tin)
//            String sqlSeat = """
//            SELECT se.seat_id,
//                   se.code,
//                   se.position_info,
//                   sc.code AS seat_class_code,
//                   sc.name AS seat_class_name,
//                   se.carriage_id
//            FROM dbo.Seat se
//            JOIN dbo.SeatClass sc ON sc.seat_class_id = se.seat_class_id
//            WHERE se.carriage_id IN (SELECT carriage_id FROM dbo.Carriage WHERE train_id=?)
//            ORDER BY se.carriage_id, se.code
//        """;
//            try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sqlSeat)) {
//                ps.setInt(1, trainId);
//                try (ResultSet rs = ps.executeQuery()) {
//                    while (rs.next()) {
//                        SeatMeta sm = new SeatMeta();
//                        sm.seatId = rs.getInt("seat_id");
//                        sm.code = rs.getString("code");
//                        sm.positionInfo = rs.getString("position_info");
//                        sm.seatClassCode = rs.getString("seat_class_code");
//                        sm.seatClassName = rs.getString("seat_class_name");
//
//                        int cid = rs.getInt("carriage_id");
//                        CarriageMeta cm = carIndex.get(cid);
//                        if (cm != null) {
//                            cm.seats.add(sm);
//                        }
//                    }
//                }
//            }
//            return meta;
//        }
    }

