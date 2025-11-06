package vn.ttapp.dao;

import vn.ttapp.config.Db;

import java.sql.*;
import java.util.List;

public class SeatLockDao {

    public boolean hasActiveConflicts(int tripId, List<Integer> seatIds) throws SQLException {
        if (seatIds.isEmpty()) {
            return false;
        }
        String in = seatIds.stream().map(x -> "?").reduce((a, b) -> a + "," + b).orElse("?");
        String sqlLock = "SELECT TOP 1 seat_id FROM dbo.SeatLock WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE trip_id=? AND seat_id IN (" + in + ") AND status='LOCKED' AND expires_at>SYSUTCDATETIME()";
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sqlLock)) {
            int idx = 1;
            ps.setInt(idx++, tripId);
            for (Integer id : seatIds) {
                ps.setInt(idx++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        String sqlPaid = "SELECT TOP 1 bi.seat_id FROM dbo.BookingItem bi "
                + "JOIN dbo.Booking b ON b.booking_id=bi.booking_id "
                + "WHERE bi.trip_id=? AND bi.seat_id IN (" + in + ") AND b.status='PAID'";
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sqlPaid)) {
            int idx = 1;
            ps.setInt(idx++, tripId);
            for (Integer id : seatIds) {
                ps.setInt(idx++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insertLocks(int tripId, long bookingId, List<Integer> seatIds) throws SQLException {
        if (seatIds.isEmpty()) {
            return;
        }
        String sql = """
            INSERT INTO dbo.SeatLock(trip_id, seat_id, booking_id, locked_at, expires_at, status)
            VALUES (?, ?, ?, SYSUTCDATETIME(), DATEADD(MINUTE, 2, SYSUTCDATETIME()), 'LOCKED')
        """;
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            for (Integer seatId : seatIds) {
                ps.setInt(1, tripId);
                ps.setInt(2, seatId);
                ps.setLong(3, bookingId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void releaseLocks(long bookingId) throws SQLException {
        String sql = "UPDATE dbo.SeatLock SET status='RELEASED' WHERE booking_id=? AND status='LOCKED'";
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            ps.executeUpdate();
        }
    }
}
