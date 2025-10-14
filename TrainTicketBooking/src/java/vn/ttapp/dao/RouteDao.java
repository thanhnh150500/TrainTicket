package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Route;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteDao {

    private Route map(ResultSet rs) throws SQLException {
        Route r = new Route();
        r.setRouteId(rs.getInt("route_id"));
        r.setOriginStationId(rs.getInt("origin_station_id"));
        r.setDestStationId(rs.getInt("dest_station_id"));
        r.setCode(rs.getString("code"));
        r.setOriginName(rs.getString("origin_name"));
        r.setDestName(rs.getString("dest_name"));
        return r;
    }

    public List<Route> findAll() throws SQLException {
        String sql = """
            SELECT r.route_id, r.origin_station_id, r.dest_station_id, r.code,
                   so.name AS origin_name, sd.name AS dest_name
            FROM dbo.Route r
            JOIN dbo.Station so ON so.station_id = r.origin_station_id
            JOIN dbo.Station sd ON sd.station_id = r.dest_station_id
            ORDER BY r.code
        """;
        List<Route> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Route findById(int id) throws SQLException {
        String sql = """
            SELECT r.route_id, r.origin_station_id, r.dest_station_id, r.code,
                   so.name AS origin_name, sd.name AS dest_name
            FROM dbo.Route r
            JOIN dbo.Station so ON so.station_id = r.origin_station_id
            JOIN dbo.Station sd ON sd.station_id = r.dest_station_id
            WHERE r.route_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean codeExists(String code) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Route WHERE code = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Integer create(int originId, int destId, String code) throws SQLException {
        String sql = """
            INSERT INTO dbo.Route(origin_station_id, dest_station_id, code)
            OUTPUT INSERTED.route_id
            VALUES(?, ?, ?)
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, originId);
            ps.setInt(2, destId);
            ps.setString(3, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public int update(Route r) throws SQLException {
        String sql = """
            UPDATE dbo.Route
            SET origin_station_id = ?, dest_station_id = ?, code = ?
            WHERE route_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, r.getOriginStationId());
            ps.setInt(2, r.getDestStationId());
            ps.setString(3, r.getCode());
            ps.setInt(4, r.getRouteId());
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM dbo.Route WHERE route_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
    
    public Route findByIdWithStations(int routeId) throws SQLException {
        String sql = """
            SELECT r.route_id, r.code,
                   s1.station_id AS origin_id, s1.name AS origin_name,
                   s2.station_id AS dest_id,   s2.name AS dest_name
            FROM dbo.Route r
            JOIN dbo.Station s1 ON s1.station_id = r.origin_station_id
            JOIN dbo.Station s2 ON s2.station_id = r.dest_station_id
            WHERE r.route_id = ?
        """;
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Route r = new Route();
                r.setRouteId(rs.getInt("route_id"));
                r.setCode(rs.getString("code"));
                r.setOriginName(rs.getString("origin_name"));
                r.setDestName(rs.getString("dest_name"));
                return r;
            }
        }
    }
}
