package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Trip;
import vn.ttapp.model.TripView;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TripDao {

    /* =======================
       Mappers cho Trip
       ======================= */
    private Trip mapBasic(ResultSet rs) throws SQLException {
        Trip t = new Trip();
        t.setTripId(rs.getInt("trip_id"));
        t.setRouteId(rs.getInt("route_id"));
        t.setTrainId(rs.getInt("train_id"));
        Timestamp d = rs.getTimestamp("depart_at");
        Timestamp a = rs.getTimestamp("arrive_at");
        t.setDepartAt(d != null ? d.toLocalDateTime() : null);
        t.setArriveAt(a != null ? a.toLocalDateTime() : null);
        t.setStatus(rs.getString("status"));
        return t;
    }

    private Trip mapWithJoins(ResultSet rs) throws SQLException {
        Trip t = mapBasic(rs);
        t.setRouteCode(rs.getString("route_code"));
        t.setOriginName(rs.getString("origin_name"));
        t.setDestName(rs.getString("dest_name"));
        t.setTrainCode(rs.getString("train_code"));
        t.setTrainName(rs.getString("train_name"));
        return t;
    }

    /* =======================
       Mapper cho TripView
       ======================= */
    private TripView mapTripView(ResultSet rs) throws SQLException {
        TripView v = new TripView();
        v.setTripId(rs.getInt("trip_id"));
        v.setRouteId(rs.getInt("route_id"));
        v.setOriginId(rs.getInt("origin_station_id"));
        v.setDestId(rs.getInt("dest_station_id"));

        v.setTrainCode(rs.getString("train_code"));
        v.setTrainName(rs.getString("train_name"));
        v.setOriginName(rs.getNString("origin_name"));
        v.setDestName(rs.getNString("dest_name"));
        v.setStatus(rs.getString("status"));

        v.setOriginCityId((Integer) rs.getObject("origin_city_id"));
        v.setOriginCityName(rs.getNString("origin_city_name"));
        v.setOriginRegionId((Integer) rs.getObject("origin_region_id"));
        v.setOriginRegionName(rs.getNString("origin_region_name"));

        Timestamp d = rs.getTimestamp("depart_at");
        Timestamp a = rs.getTimestamp("arrive_at");
        v.setDepartAt(d != null ? d.toLocalDateTime() : null);
        v.setArriveAt(a != null ? a.toLocalDateTime() : null);
        return v;
    }

    /* =======================
       CRUD cơ bản (Trip)
       ======================= */
    public Trip findById(int id) throws SQLException {
        String sql = """
            SELECT t.trip_id, t.route_id, t.train_id, t.depart_at, t.arrive_at, t.status,
                   r.code AS route_code,
                   s1.name AS origin_name,
                   s2.name AS dest_name,
                   tr.code AS train_code,
                   tr.name AS train_name
            FROM dbo.Trip t
            JOIN dbo.Route r  ON r.route_id = t.route_id
            JOIN dbo.Station s1 ON s1.station_id = r.origin_station_id
            JOIN dbo.Station s2 ON s2.station_id = r.dest_station_id
            JOIN dbo.Train tr ON tr.train_id = t.train_id
            WHERE t.trip_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapWithJoins(rs) : null;
            }
        }
    }

    public List<Trip> findAll() throws SQLException {
        String sql = """
            SELECT t.trip_id, t.route_id, t.train_id, t.depart_at, t.arrive_at, t.status,
                   r.code AS route_code,
                   s1.name AS origin_name,
                   s2.name AS dest_name,
                   tr.code AS train_code,
                   tr.name AS train_name
            FROM dbo.Trip t
            JOIN dbo.Route r  ON r.route_id = t.route_id
            JOIN dbo.Station s1 ON s1.station_id = r.origin_station_id
            JOIN dbo.Station s2 ON s2.station_id = r.dest_station_id
            JOIN dbo.Train tr ON tr.train_id = t.train_id
            ORDER BY t.depart_at DESC, t.trip_id DESC
        """;
        List<Trip> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapWithJoins(rs));
            }
        }
        return list;
    }

    public Integer create(int routeId, int trainId, LocalDateTime departAt,
            LocalDateTime arriveAt, String status) throws SQLException {
        String sql = """
            INSERT INTO dbo.Trip(route_id, train_id, depart_at, arrive_at, status)
            OUTPUT INSERTED.trip_id
            VALUES(?, ?, ?, ?, ?)
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            ps.setInt(2, trainId);
            ps.setTimestamp(3, Timestamp.valueOf(departAt));
            ps.setTimestamp(4, Timestamp.valueOf(arriveAt));
            ps.setString(5, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return null;
    }

    public int update(Trip t) throws SQLException {
        String sql = """
            UPDATE dbo.Trip
            SET route_id=?, train_id=?, depart_at=?, arrive_at=?, status=?
            WHERE trip_id=?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, t.getRouteId());
            ps.setInt(2, t.getTrainId());
            ps.setTimestamp(3, Timestamp.valueOf(t.getDepartAt()));
            ps.setTimestamp(4, Timestamp.valueOf(t.getArriveAt()));
            ps.setString(5, t.getStatus());
            ps.setInt(6, t.getTripId());
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        String sql = "DELETE FROM dbo.Trip WHERE trip_id=?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    /* =======================
       Tìm chuyến theo ga + ngày (Trip)
       ======================= */
    public List<Trip> searchByStationId(
            int originStationId, int destStationId,
            Date departDate, Time departTimeOrNull
    ) throws SQLException {
        if (departDate == null) {
            throw new IllegalArgumentException("departDate must not be null");
        }

        String sql = """
            SELECT t.trip_id, t.route_id, t.train_id, t.depart_at, t.arrive_at, t.status,
                   r.code AS route_code,
                   s1.name AS origin_name,
                   s2.name AS dest_name,
                   tr.code AS train_code,
                   tr.name AS train_name
            FROM dbo.Trip t
            JOIN dbo.Route r  ON r.route_id = t.route_id
            JOIN dbo.Station s1 ON s1.station_id = r.origin_station_id
            JOIN dbo.Station s2 ON s2.station_id = r.dest_station_id
            JOIN dbo.Train tr ON tr.train_id = t.train_id
            WHERE r.origin_station_id = ?
              AND r.dest_station_id   = ?
              AND t.status IN ('SCHEDULED','RUNNING')
              AND t.depart_at >= ?
              AND t.depart_at <  ?
            ORDER BY t.depart_at ASC, t.trip_id ASC
        """;

        List<Trip> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            LocalDateTime start = departDate.toLocalDate().atStartOfDay();
            if (departTimeOrNull != null) {
                var lt = departTimeOrNull.toLocalTime();
                start = start.withHour(lt.getHour()).withMinute(lt.getMinute()).withSecond(0).withNano(0);
            }
            LocalDateTime end = departDate.toLocalDate().plusDays(1).atStartOfDay();

            ps.setInt(1, originStationId);
            ps.setInt(2, destStationId);
            ps.setTimestamp(3, Timestamp.valueOf(start));
            ps.setTimestamp(4, Timestamp.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapWithJoins(rs));
                }
            }
        }
        return list;
    }

    public List<Trip> searchReturnByStationIds(
            int originStationId, int destStationId,
            Date returnDate, Time returnTimeOrNull
    ) throws SQLException {
        return searchByStationId(destStationId, originStationId, returnDate, returnTimeOrNull);
    }

    /* =======================
       Tất cả chuyến trong 1 ngày (Trip & TripView)
       ======================= */
    /**
     * Liệt kê toàn bộ chuyến trong ngày (không filter) – trả về Trip
     */
    public List<Trip> findTripsInDay(LocalDate day) throws SQLException {
        String sql = """
            SELECT t.trip_id, t.route_id, t.train_id, t.depart_at, t.arrive_at, t.status,
                   r.code AS route_code,
                   s1.name AS origin_name,
                   s2.name AS dest_name,
                   tr.code AS train_code,
                   tr.name AS train_name
            FROM dbo.Trip t
            JOIN dbo.Route r  ON r.route_id = t.route_id
            JOIN dbo.Station s1 ON s1.station_id = r.origin_station_id
            JOIN dbo.Station s2 ON s2.station_id = r.dest_station_id
            JOIN dbo.Train tr ON tr.train_id = t.train_id
            WHERE t.depart_at >= ? AND t.depart_at < ?
              AND t.status IN ('SCHEDULED','RUNNING')
            ORDER BY t.depart_at ASC, t.trip_id ASC
        """;
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<Trip> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapWithJoins(rs));
                }
            }
        }
        return list;
    }

    /**
     * Liệt kê chuyến trong ngày (có filter) – trả về TripView để hiển thị cho
     * customer. Các filter là tùy chọn: regionId / cityId / originStationId /
     * destStationId
     */
    public List<TripView> findTripsInDay(
            LocalDate day,
            Integer regionId,
            Integer cityId,
            Integer originStationId,
            Integer destStationId
    ) throws SQLException {

        String sql = """
            SELECT
              t.trip_id,
              t.route_id,
              r.origin_station_id,
              r.dest_station_id,
              t.depart_at,
              t.arrive_at,
              t.status,
              tr.code  AS train_code,
              tr.name  AS train_name,
              s1.name  AS origin_name,
              s2.name  AS dest_name,
              c1.city_id          AS origin_city_id,
              c1.name             AS origin_city_name,
              rg.region_id        AS origin_region_id,
              rg.name             AS origin_region_name
            FROM dbo.Trip t
            JOIN dbo.Route   r   ON r.route_id = t.route_id
            JOIN dbo.Station s1  ON s1.station_id = r.origin_station_id
            JOIN dbo.City    c1  ON c1.city_id   = s1.city_id
            JOIN dbo.Region  rg  ON rg.region_id = c1.region_id
            JOIN dbo.Station s2  ON s2.station_id = r.dest_station_id
            JOIN dbo.Train   tr  ON tr.train_id  = t.train_id
            WHERE t.depart_at >= ? AND t.depart_at < ?
              AND t.status IN ('SCHEDULED','RUNNING')
              AND (? IS NULL OR rg.region_id  = ?)
              AND (? IS NULL OR c1.city_id    = ?)
              AND (? IS NULL OR s1.station_id = ?)
              AND (? IS NULL OR s2.station_id = ?)
            ORDER BY t.depart_at ASC, t.trip_id ASC
        """;

        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<TripView> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            int i = 1;
            ps.setTimestamp(i++, Timestamp.valueOf(start));
            ps.setTimestamp(i++, Timestamp.valueOf(end));

            if (regionId == null) {
                ps.setNull(i++, Types.INTEGER);
                ps.setNull(i++, Types.INTEGER);
            } else {
                ps.setInt(i++, regionId);
                ps.setInt(i++, regionId);
            }

            if (cityId == null) {
                ps.setNull(i++, Types.INTEGER);
                ps.setNull(i++, Types.INTEGER);
            } else {
                ps.setInt(i++, cityId);
                ps.setInt(i++, cityId);
            }

            if (originStationId == null) {
                ps.setNull(i++, Types.INTEGER);
                ps.setNull(i++, Types.INTEGER);
            } else {
                ps.setInt(i++, originStationId);
                ps.setInt(i++, originStationId);
            }

            if (destStationId == null) {
                ps.setNull(i++, Types.INTEGER);
                ps.setNull(i++, Types.INTEGER);
            } else {
                ps.setInt(i++, destStationId);
                ps.setInt(i++, destStationId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTripView(rs));
                }
            }
        }
        return list;
    }
}
