package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Train;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    public static class SeatMeta {

        private int seatId;
        private String code;
        private String seatClassCode;
        private String seatClassName;
        private String positionInfo;

        public int getSeatId() {
            return seatId;
        }

        public void setSeatId(int seatId) {
            this.seatId = seatId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getSeatClassCode() {
            return seatClassCode;
        }

        public void setSeatClassCode(String seatClassCode) {
            this.seatClassCode = seatClassCode;
        }

        public String getSeatClassName() {
            return seatClassName;
        }

        public void setSeatClassName(String seatClassName) {
            this.seatClassName = seatClassName;
        }

        public String getPositionInfo() {
            return positionInfo;
        }

        public void setPositionInfo(String positionInfo) {
            this.positionInfo = positionInfo;
        }
    }

    public static class CarriageMeta {

        private int carriageId;
        private String carriageCode;
        private String seatClassCode;
        private String seatClassName;
        private int seatCount;
        private int sortOrder;
        private java.util.List<SeatMeta> seats = new java.util.ArrayList<>();

        public int getCarriageId() {
            return carriageId;
        }

        public void setCarriageId(int carriageId) {
            this.carriageId = carriageId;
        }

        public String getCarriageCode() {
            return carriageCode;
        }

        public void setCarriageCode(String carriageCode) {
            this.carriageCode = carriageCode;
        }

        public String getSeatClassCode() {
            return seatClassCode;
        }

        public void setSeatClassCode(String seatClassCode) {
            this.seatClassCode = seatClassCode;
        }

        public String getSeatClassName() {
            return seatClassName;
        }

        public void setSeatClassName(String seatClassName) {
            this.seatClassName = seatClassName;
        }

        public int getSeatCount() {
            return seatCount;
        }

        public void setSeatCount(int seatCount) {
            this.seatCount = seatCount;
        }

        public int getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(int sortOrder) {
            this.sortOrder = sortOrder;
        }

        public java.util.List<SeatMeta> getSeats() {
            return seats;
        }

        public void setSeats(java.util.List<SeatMeta> seats) {
            this.seats = seats;
        }
    }

    public static class TrainMeta {

        private int trainId;
        private String code;
        private String name;
        private java.util.List<CarriageMeta> carriages = new java.util.ArrayList<>();
        private int totalCarriages;
        private int totalSeats;

        public int getTrainId() {
            return trainId;
        }

        public void setTrainId(int trainId) {
            this.trainId = trainId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public java.util.List<CarriageMeta> getCarriages() {
            return carriages;
        }

        public void setCarriages(java.util.List<CarriageMeta> carriages) {
            this.carriages = carriages;
        }

        public int getTotalCarriages() {
            return totalCarriages;
        }

        public void setTotalCarriages(int totalCarriages) {
            this.totalCarriages = totalCarriages;
        }

        public int getTotalSeats() {
            return totalSeats;
        }

        public void setTotalSeats(int totalSeats) {
            this.totalSeats = totalSeats;
        }
    }

    public TrainMeta findDetail(int trainId) throws SQLException {
        // 1) Train (bắt buộc có)
        TrainMeta meta = new TrainMeta();
        String sqlTrain = "SELECT train_id, code, name FROM dbo.Train WHERE train_id = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sqlTrain)) {
            ps.setInt(1, trainId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                meta.setTrainId(rs.getInt("train_id"));
                meta.setCode(rs.getString("code"));
                meta.setName(rs.getString("name"));
            }
        }

        // 2) Carriages + seatCount (LEFT JOIN để vẫn thấy toa dù chưa có ghế)
        String sqlCar = """
            SELECT ca.carriage_id,
                   ca.code            AS carriage_code,
                   ca.sort_order      AS sort_order,
                   sc.code            AS seat_class_code,
                   sc.name            AS seat_class_name,
                   COUNT(se.seat_id)  AS seat_count
            FROM dbo.Carriage ca
            LEFT JOIN dbo.SeatClass sc ON sc.seat_class_id = ca.seat_class_id
            LEFT JOIN dbo.Seat se      ON se.carriage_id   = ca.carriage_id
            WHERE ca.train_id = ?
            GROUP BY ca.carriage_id, ca.code, ca.sort_order, sc.code, sc.name
            ORDER BY CASE WHEN ca.sort_order IS NULL THEN 999999 ELSE ca.sort_order END,
                     ca.carriage_id
        """;

        Map<Integer, CarriageMeta> carIndex = new LinkedHashMap<>();
        int totalSeats = 0, totalCarriages = 0;

        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sqlCar)) {
            ps.setInt(1, trainId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CarriageMeta cm = new CarriageMeta();
                    cm.setCarriageId(rs.getInt("carriage_id"));
                    cm.setCarriageCode(rs.getString("carriage_code"));
                    cm.setSortOrder(rs.getObject("sort_order") == null ? 0 : rs.getInt("sort_order"));
                    cm.setSeatClassCode(rs.getString("seat_class_code"));
                    cm.setSeatClassName(rs.getString("seat_class_name"));
                    cm.setSeatCount(rs.getInt("seat_count"));

                    meta.getCarriages().add(cm);
                    carIndex.put(cm.getCarriageId(), cm);

                    totalCarriages++;
                    totalSeats += cm.getSeatCount();
                }
            }
        }

        meta.setTotalCarriages(totalCarriages);
        meta.setTotalSeats(totalSeats);

        if (meta.getCarriages().isEmpty()) {
            // Không có toa => trả về train meta rỗng danh sách
            return meta;
        }

        // 3) Seats (chi tiết từng ghế của các toa)
        String sqlSeat = """
            SELECT se.seat_id,
                   se.code,
                   se.position_info,
                   sc.code AS seat_class_code,
                   sc.name AS seat_class_name,
                   se.carriage_id
            FROM dbo.Seat se
            LEFT JOIN dbo.SeatClass sc ON sc.seat_class_id = se.seat_class_id
            WHERE se.carriage_id IN (
                SELECT carriage_id FROM dbo.Carriage WHERE train_id = ?
            )
            ORDER BY se.carriage_id, se.code
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sqlSeat)) {
            ps.setInt(1, trainId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SeatMeta sm = new SeatMeta();
                    sm.setSeatId(rs.getInt("seat_id"));
                    sm.setCode(rs.getString("code"));
                    sm.setPositionInfo(rs.getString("position_info"));
                    sm.setSeatClassCode(rs.getString("seat_class_code"));
                    sm.setSeatClassName(rs.getString("seat_class_name"));

                    int cid = rs.getInt("carriage_id");
                    CarriageMeta cm = carIndex.get(cid);
                    if (cm != null) {
                        cm.getSeats().add(sm);
                    }
                }
            }
        }

        // (không cần cập nhật lại totalSeats vì đã tính bằng COUNT ở phần carriage)
        return meta;
    }
}
