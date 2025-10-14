package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Station;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StationDao {

    private Station map(ResultSet rs) throws SQLException {
        Station s = new Station();
        s.setStationId(rs.getInt("station_id"));
        if (rs.wasNull()) {
            s.setStationId(null);
        }
        s.setCityId(rs.getInt("city_id"));
        if (rs.wasNull()) {
            s.setCityId(null);
        }
        s.setCode(rs.getString("code"));
        s.setName(rs.getString("name"));
        s.setAddress(rs.getString("address"));
        s.setCityName(rs.getString("city_name"));
        return s;
    }

    public Station findById(int id) throws SQLException {
        String sql = """
            SELECT s.station_id, s.city_id, s.code, s.name, s.address,
                   c.name AS city_name
            FROM dbo.Station s
            JOIN dbo.City c ON c.city_id = s.city_id
            WHERE s.station_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Station findByCode(String code) throws SQLException {
        String sql = """
            SELECT s.station_id, s.city_id, s.code, s.name, s.address,
                   c.name AS city_name
            FROM dbo.Station s
            JOIN dbo.City c ON c.city_id = s.city_id
            WHERE s.code = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Station> findAll() throws SQLException {
        String sql = """
            SELECT s.station_id, s.city_id, s.code, s.name, s.address,
                   c.name AS city_name
            FROM dbo.Station s
            JOIN dbo.City c ON c.city_id = s.city_id
            ORDER BY c.name, s.name
        """;
        List<Station> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public boolean codeExists(String code) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Station WHERE code = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Integer create(Integer cityId, String code, String name, String address) throws SQLException {
        String sql = """
            INSERT INTO dbo.Station(city_id, code, name, address)
            OUTPUT INSERTED.station_id
            VALUES(?, ?, ?, ?)
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cityId);
            ps.setString(2, code);
            ps.setNString(3, name);
            if (address == null || address.isBlank()) {
                ps.setNull(4, Types.NVARCHAR);
            } else {
                ps.setNString(4, address);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        Station s = findByCode(code);
        return s != null ? s.getStationId() : null;
    }

    public int update(Station s) throws SQLException {
        String sql = """
            UPDATE dbo.Station
            SET city_id = ?, code = ?, name = ?, address = ?
            WHERE station_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getCityId());
            ps.setString(2, s.getCode());
            ps.setNString(3, s.getName());
            if (s.getAddress() == null || s.getAddress().isBlank()) {
                ps.setNull(4, Types.NVARCHAR);
            } else {
                ps.setNString(4, s.getAddress());
            }
            ps.setInt(5, s.getStationId());
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        String sql = "DELETE FROM dbo.Station WHERE station_id = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
