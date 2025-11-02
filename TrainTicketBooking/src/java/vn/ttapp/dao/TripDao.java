package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Trip;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TripDao {

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

    public Trip findById(int id) throws SQLException {
        String sql = """
            SELECT t.trip_id, t.route_id, t.train_id, t.depart_at, t.arrive_at, t.status,
                   r.code AS route_code,
                   s1.name AS origin_name,
                   s2.name AS dest_name,
                   tr.code AS train_code,
                   tr.name AS train_name
            FROM dbo.Trip t
            JOIN dbo.Route r ON r.route_id = t.route_id
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
            JOIN dbo.Route r ON r.route_id = t.route_id
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

    public Integer create(int routeId, int trainId, LocalDateTime departAt, LocalDateTime arriveAt, String status) throws SQLException {
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

    public List<Trip> searchByStationId(
            int originStationId, int destStationId,
            Date departDate, Time departTimeOrNull
    ) throws SQLException {
        if (departDate == null) {
            throw new IllegalArgumentException("departDate must not be null");
        }

        String sql = """
        SELECT t.trip_id, t.route_id, t.train_id, t.depart_at, t.arrive_at, t.[status],
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
          AND t.[status] = 'SCHEDULED'
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

    public List<Trip> searchReturnByStationIds(int originStationId, int destStationId,
            Date returnDate, Time returnTimeOrNull) throws SQLException {
        return searchByStationId(destStationId, originStationId, returnDate, returnTimeOrNull);
    }
}
