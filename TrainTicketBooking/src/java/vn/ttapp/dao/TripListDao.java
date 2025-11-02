package vn.ttapp.dao;

import jakarta.servlet.http.HttpServletRequest;
import vn.ttapp.config.Db;
import vn.ttapp.model.DayTabVm;
import vn.ttapp.model.TripCardVm;

import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TripListDao {

    public List<TripCardVm> listTripsByRouteAndDay(
            int originStationId,
            int destStationId,
            LocalDate departDate,
            LocalTime departAfterOrNull
    ) throws SQLException {

        if (departDate == null) {
            return Collections.emptyList();
        }

        try (Connection cn = Db.getConnection()) {

            // 1) Tìm route_id theo cặp ga
            Integer routeId = null;
            String findRouteSql = "SELECT route_id FROM dbo.[Route] WHERE origin_station_id=? AND dest_station_id=?";
            try (PreparedStatement ps = cn.prepareStatement(findRouteSql)) {
                ps.setInt(1, originStationId);
                ps.setInt(2, destStationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        routeId = rs.getInt(1);
                    }
                }
            }
            if (routeId == null) {
                return Collections.emptyList();
            }

            // 2) Tính time window [start, end)
            LocalDateTime start = departDate.atStartOfDay();
            if (departAfterOrNull != null) {
                start = start.withHour(departAfterOrNull.getHour())
                        .withMinute(departAfterOrNull.getMinute())
                        .withSecond(0).withNano(0);
            }
            LocalDateTime end = departDate.plusDays(1).atStartOfDay();

            // 3) Truy vấn chuyến + ghế còn lại + giá thấp nhất
            String sql = """
                WITH seats_total AS (
                    SELECT c.train_id, COUNT(*) AS total
                    FROM dbo.Seat s
                    JOIN dbo.Carriage c ON c.carriage_id = s.carriage_id
                    GROUP BY c.train_id
                ),
                locked AS (
                    SELECT trip_id, COUNT(*) AS cnt
                    FROM dbo.SeatLock
                    WHERE status = 'LOCKED' AND expires_at > SYSUTCDATETIME()
                    GROUP BY trip_id
                ),
                booked AS (
                    SELECT bi.trip_id, COUNT(DISTINCT bi.seat_id) AS cnt
                    FROM dbo.BookingItem bi
                    JOIN dbo.Booking b ON b.booking_id = bi.booking_id
                    WHERE b.status IN ('HOLD','PAID')
                    GROUP BY bi.trip_id
                )
                SELECT
                    t.trip_id,
                    tr.code                        AS train_code,
                    t.depart_at,
                    t.arrive_at,
                    ISNULL(st.total,0)             AS total_seats,
                    ISNULL(lk.cnt,0)               AS locked_cnt,
                    ISNULL(bk.cnt,0)               AS booked_cnt,
                    (
                        SELECT MIN(fr.base_price)
                        FROM dbo.FareRule fr
                        WHERE fr.route_id = t.route_id
                          AND fr.effective_from <= ?
                          AND (fr.effective_to IS NULL OR fr.effective_to >= ?)
                    )                               AS price_from
                FROM dbo.Trip t
                JOIN dbo.Train tr        ON tr.train_id = t.train_id
                LEFT JOIN seats_total st ON st.train_id = t.train_id
                LEFT JOIN locked lk      ON lk.trip_id = t.trip_id
                LEFT JOIN booked bk      ON bk.trip_id = t.trip_id
                WHERE t.route_id = ?
                  AND t.depart_at >= ?      -- start of window
                  AND t.depart_at <  ?      -- end of window (exclusive)
                ORDER BY t.depart_at ASC, t.trip_id ASC
            """;

            List<TripCardVm> out = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                // Giá áp theo ngày đi (chỉ cần Date)
                ps.setDate(1, java.sql.Date.valueOf(departDate));
                ps.setDate(2, java.sql.Date.valueOf(departDate));
                // Route + time window
                ps.setInt(3, routeId);
                ps.setTimestamp(4, Timestamp.valueOf(start));
                ps.setTimestamp(5, Timestamp.valueOf(end));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Timestamp dts = rs.getTimestamp("depart_at");
                        Timestamp ats = rs.getTimestamp("arrive_at");
                        // Null-safe: bỏ bản ghi thiếu thời gian
                        if (dts == null || ats == null) {
                            continue;
                        }

                        LocalDateTime departAt = dts.toLocalDateTime();
                        LocalDateTime arriveAt = ats.toLocalDateTime();

                        int total = rs.getInt("total_seats");
                        int locked = rs.getInt("locked_cnt");
                        int booked = rs.getInt("booked_cnt");
                        int remain = Math.max(0, total - locked - booked);

                        TripCardVm vm = new TripCardVm();
                        vm.tripId = rs.getInt("trip_id");
                        vm.trainCode = rs.getString("train_code");
                        vm.departTime = departAt;
                        vm.arriveTime = arriveAt;
                        vm.durationMin = (int) Duration.between(departAt, arriveAt).toMinutes();
                        vm.availableSeats = remain;
                        vm.minPrice = rs.getBigDecimal("price_from"); // có thể null

                        out.add(vm);
                    }
                }
            }

            return out;
        }
    }

    /**
     * Alias tiện dùng trong Servlet khác.
     */
    public List<TripCardVm> queryTripsForDate(int originId, int destId, LocalDate viewDate)
            throws SQLException {
        return listTripsByRouteAndDay(originId, destId, viewDate, null);
    }

    /**
     * Sinh dải ngày cho UI: từ (viewDate - 1) đến (viewDate + 7). Đồng thời set
     * prev/next URL lên request để dùng cho nút điều hướng.
     */
    public List<DayTabVm> buildDayTabs(HttpServletRequest req, LocalDate date,
            int originId, int destId) {
        String ctx = req.getContextPath();
        List<DayTabVm> days = new ArrayList<>();

        // hiển thị từ (date - 1) đến (date + 7)
        for (int i = -1; i <= 7; i++) {
            LocalDate d = date.plusDays(i);
            String url = ctx + "/trips?originId=" + originId
                    + "&destId=" + destId
                    + "&date=" + d;         // yyyy-MM-dd

            // KHÔNG set ddMMyyyy/weekday ở đây nữa
            DayTabVm tab = new DayTabVm();
            tab.setDate(d);
            tab.setActive(d.equals(date));
            tab.setUrl(url);

            days.add(tab);
        }

        req.setAttribute("prevDateUrl",
                ctx + "/trips?originId=" + originId + "&destId=" + destId + "&date=" + date.minusDays(1));
        req.setAttribute("nextDateUrl",
                ctx + "/trips?originId=" + originId + "&destId=" + destId + "&date=" + date.plusDays(1));

        return days;
    }

}
