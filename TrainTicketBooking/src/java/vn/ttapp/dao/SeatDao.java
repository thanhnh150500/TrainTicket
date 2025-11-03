package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Seat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import vn.ttapp.model.SeatView;

public class SeatDao {

    // ----- Mapping cơ bản cho Seat (CRUD/hiển thị) -----
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

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    // ===== CRUD & Liệt kê =====
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
            ps.setString(2, normalizeCode(code));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean codeExistsExceptId(int carriageId, String code, int excludeSeatId) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Seat WHERE carriage_id = ? AND code = ? AND seat_id <> ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, carriageId);
            ps.setString(2, normalizeCode(code));
            ps.setInt(3, excludeSeatId);
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
        String codeNorm = normalizeCode(code);
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, carriageId);
            ps.setString(2, codeNorm);
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
        String codeNorm = normalizeCode(s.getCode());
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getCarriageId());
            ps.setString(2, codeNorm);
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

    public List<Seat> findByTripId(int tripId) throws SQLException {
        String sql = """
            SELECT s.seat_id, s.carriage_id, s.code, s.seat_class_id, s.position_info,
                   sc.code AS seat_class_code, sc.name AS seat_class_name,
                   c.code AS carriage_code, t.code AS train_code, t.name AS train_name
            FROM dbo.Trip tr
            JOIN dbo.Train t ON t.train_id = tr.train_id
            JOIN dbo.Carriage c ON c.train_id = t.train_id
            JOIN dbo.Seat s ON s.carriage_id = c.carriage_id
            JOIN dbo.SeatClass sc ON sc.seat_class_id = s.seat_class_id
            WHERE tr.trip_id = ?
            ORDER BY c.sort_order, s.code
        """;
        List<Seat> list = new ArrayList<>();
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    // SeatDao.java
    public List<SeatView> getSeatMapWithAvailabilityForTrain(int tripId, int trainId) throws SQLException {
        String sql = """
        SELECT
            s.seat_id,
            s.code                     AS seat_code,
            s.carriage_id,
            s.position_info,
            c.code                     AS carriage_code,
            c.sort_order               AS carriage_sort_order,
            sc.seat_class_id,
            sc.code                    AS seat_class_code,
            sc.name                    AS seat_class_name,

            /* Fallback 1: rule khớp ngày (nếu bạn dùng ngày, thêm điều kiện ở dưới) */
            fr_exact.price                                     AS price_exact,

            /* Fallback 2: rule bất kỳ mới nhất theo route + seat_class (bỏ điều kiện ngày) */
            fr_any.price                                       AS price_any,

            CASE WHEN booked.seat_id IS NOT NULL OR locked.seat_id IS NOT NULL
                 THEN 0 ELSE 1 END                            AS available,
            locked.expires_at                                  AS lock_expires_at,
            locked.booking_id                                  AS locking_booking_id
        FROM dbo.Carriage c
        JOIN dbo.Seat s        ON s.carriage_id    = c.carriage_id
        JOIN dbo.SeatClass sc  ON sc.seat_class_id = s.seat_class_id
        JOIN dbo.Trip trp      ON trp.train_id     = c.train_id AND trp.trip_id = ?

        LEFT JOIN (
            SELECT bi.seat_id
            FROM dbo.BookingItem bi
            JOIN dbo.Booking b ON b.booking_id = bi.booking_id
            WHERE bi.trip_id = ?
              AND (b.status = 'PAID'
                   OR (b.status='HOLD' AND b.hold_expires_at > SYSUTCDATETIME()))
        ) booked ON booked.seat_id = s.seat_id

        LEFT JOIN (
            SELECT sl.seat_id, sl.expires_at, sl.booking_id
            FROM dbo.SeatLock sl
            WHERE sl.trip_id    = ?
              AND sl.status     = 'LOCKED'
              AND sl.expires_at > SYSUTCDATETIME()
        ) locked ON locked.seat_id = s.seat_id

        /* Rule khớp ngày (nếu có cột ngày đi, mở comment 2 dòng dưới) */
        OUTER APPLY (
            SELECT TOP 1 f.base_price AS price
            FROM dbo.FareRule f
            WHERE f.route_id      = trp.route_id
              AND f.seat_class_id = s.seat_class_id
              -- AND (f.effective_from IS NULL OR f.effective_from <= trp.depart_date)
              -- AND (f.effective_to   IS NULL OR f.effective_to   >= trp.depart_date)
            ORDER BY
              CASE WHEN f.effective_from IS NULL THEN 1 ELSE 0 END,
              f.effective_from DESC
        ) fr_exact

        /* Rule bất kỳ mới nhất (không xét ngày) để phòng khi fr_exact = NULL */
        OUTER APPLY (
            SELECT TOP 1 f.base_price AS price
            FROM dbo.FareRule f
            WHERE f.route_id      = trp.route_id
              AND f.seat_class_id = s.seat_class_id
            ORDER BY
              CASE WHEN f.effective_from IS NULL THEN 1 ELSE 0 END,
              f.effective_from DESC
        ) fr_any

        WHERE c.train_id = ?
        ORDER BY ISNULL(c.sort_order, 9999), s.code;
    """;

        List<SeatView> list = new ArrayList<>();
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tripId);
            ps.setInt(2, tripId);
            ps.setInt(3, tripId);
            ps.setInt(4, trainId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SeatView v = new SeatView();
                    v.seatId = rs.getInt("seat_id");
                    v.seatCode = rs.getString("seat_code");
                    v.carriageId = rs.getInt("carriage_id");
                    v.carriageCode = rs.getString("carriage_code");
                    v.seatClassId = rs.getInt("seat_class_id");
                    v.seatClassCode = rs.getString("seat_class_code");
                    v.seatClassName = rs.getString("seat_class_name");

                    // Ưu tiên price_exact, rồi price_any
                    java.math.BigDecimal p1 = rs.getBigDecimal("price_exact");
                    java.math.BigDecimal p2 = rs.getBigDecimal("price_any");
                    v.price = (p1 != null ? p1 : p2);

                    v.available = rs.getInt("available") == 1;
                    v.lockExpiresAt = rs.getTimestamp("lock_expires_at");
                    Object bid = rs.getObject("locking_booking_id");
                    v.lockingBookingId = (bid == null ? null : ((Number) bid).longValue());

                    String pos = rs.getString("position_info");
                    int[] rc = parseRowCol(pos);
                    v.rowNo = rc[0] == 0 ? null : rc[0];
                    v.colNo = rc[1] == 0 ? null : rc[1];

                    list.add(v);
                }
            }
        }

        // Fallback 3 (server-side): lấp giá theo seatClass nếu vẫn còn null
        java.util.Map<Integer, java.math.BigDecimal> byClass = new java.util.HashMap<>();
        for (SeatView v : list) {
            if (v.price != null && v.price.signum() > 0) {
                byClass.putIfAbsent(v.seatClassId, v.price);
            }
        }
        for (SeatView v : list) {
            if (v.price == null && byClass.containsKey(v.seatClassId)) {
                v.price = byClass.get(v.seatClassId);
            }
        }

        return list;
    }
    
    public boolean seatBelongsToTrip(int seatId, int tripId) throws SQLException {
        String sql = """
        SELECT COUNT(*)
        FROM dbo.Seat s
        JOIN dbo.Carriage c ON s.carriage_id = c.carriage_id
        JOIN dbo.Trip t     ON t.train_id    = c.train_id
        WHERE s.seat_id = ? AND t.trip_id = ?
    """;
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            ps.setInt(2, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Integer> lockSeats(int tripId, List<Integer> seatIds, Long bookingId, int ttlMinutes) throws SQLException {
        String conflictSql = """
      SELECT s.seat_id
      FROM dbo.Seat s
      JOIN dbo.Carriage c ON c.carriage_id = s.carriage_id
      JOIN dbo.Trip tr ON tr.train_id = c.train_id
      WHERE tr.trip_id = ? AND s.seat_id IN (%s)
      AND (
         EXISTS(SELECT 1 FROM dbo.SeatLock sl
                WHERE sl.trip_id=tr.trip_id AND sl.seat_id=s.seat_id
                  AND sl.status='LOCKED' AND sl.expires_at>SYSUTCDATETIME())
         OR EXISTS(SELECT 1 FROM dbo.BookingItem bi
                   JOIN dbo.Booking b ON b.booking_id=bi.booking_id
                   WHERE bi.trip_id=tr.trip_id AND bi.seat_id=s.seat_id
                     AND (b.status='PAID' OR (b.status='HOLD' AND b.hold_expires_at>SYSUTCDATETIME())))
      )
    """;
        String insertSql = """
      INSERT INTO dbo.SeatLock(trip_id, seat_id, booking_id, locked_at, expires_at, status)
      OUTPUT INSERTED.seat_lock_id
      VALUES(?, ?, ?, SYSUTCDATETIME(), DATEADD(minute, ?, SYSUTCDATETIME()), 'LOCKED')
    """;
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                // check conflicts
                String in = seatIds.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("NULL");
                try (PreparedStatement chk = c.prepareStatement(String.format(conflictSql, in))) {
                    int i = 1;
                    chk.setInt(i++, tripId);
                    for (Integer sid : seatIds) {
                        chk.setInt(i++, sid);
                    }
                    try (ResultSet rs = chk.executeQuery()) {
                        if (rs.next()) {
                            c.rollback();
                            return List.of();
                        } // có xung đột -> fail
                    }
                }
                List<Integer> lockIds = new ArrayList<>();
                try (PreparedStatement ins = c.prepareStatement(insertSql)) {
                    for (Integer sid : seatIds) {
                        ins.setInt(1, tripId);
                        ins.setInt(2, sid);
                        if (bookingId == null) {
                            ins.setNull(3, Types.BIGINT);
                        } else {
                            ins.setLong(3, bookingId);
                        }
                        ins.setInt(4, ttlMinutes);
                        try (ResultSet rs = ins.executeQuery()) {
                            if (rs.next()) {
                                lockIds.add(rs.getInt(1));
                            }
                        }
                    }
                }
                c.commit();
                return lockIds;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public int releaseLock(int seatLockId) throws SQLException {
        String sql = "UPDATE dbo.SeatLock SET status='RELEASED' WHERE seat_lock_id=? AND status='LOCKED'";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatLockId);
            return ps.executeUpdate();
        }
    }

    private static int[] parseRowCol(String positionInfo) {
        int[] rc = new int[]{0, 0};
        if (positionInfo == null || positionInfo.isBlank()) {
            return rc;
        }
        String s = positionInfo.trim();

        // row=3,col=5
        try {
            if (s.contains("row") && s.contains("col")) {
                String[] parts = s.split("[,;]");
                for (String p : parts) {
                    String[] kv = p.split("=");
                    if (kv.length == 2) {
                        String k = kv[0].trim().toLowerCase();
                        int val = Integer.parseInt(kv[1].trim());
                        if (k.contains("row")) {
                            rc[0] = val;
                        }
                        if (k.contains("col")) {
                            rc[1] = val;
                        }
                    }
                }
                if (rc[0] != 0 || rc[1] != 0) {
                    return rc;
                }
            }
        } catch (Exception ignore) {
        }

        // "3,5" hoặc "3-5"
        try {
            String[] parts = s.split("[,\\-]");
            if (parts.length == 2) {
                rc[0] = Integer.parseInt(parts[0].trim());
                rc[1] = Integer.parseInt(parts[1].trim());
                return rc;
            }
        } catch (Exception ignore) {
        }

        try {
            s = s.toUpperCase();
            int rIdx = s.indexOf('R');
            int cIdx = s.indexOf('C');
            if (rIdx >= 0 && cIdx > rIdx) {
                rc[0] = Integer.parseInt(s.substring(rIdx + 1, cIdx).replaceAll("\\D", ""));
                rc[1] = Integer.parseInt(s.substring(cIdx + 1).replaceAll("\\D", ""));
                return rc;
            }
        } catch (Exception ignore) {
        }

        return rc;
    }
}
