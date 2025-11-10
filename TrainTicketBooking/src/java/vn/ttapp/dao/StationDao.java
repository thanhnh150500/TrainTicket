package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Station;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StationDao {

    /* ================= MAPPER ================= */
    private Station map(ResultSet rs) throws SQLException {
        Station s = new Station();
        // Dùng getObject(..., Integer.class) để giữ được null
        s.setStationId(rs.getObject("station_id", Integer.class));
        s.setCityId(rs.getObject("city_id", Integer.class));
        s.setCode(rs.getString("code"));
        s.setName(rs.getNString("name"));
        s.setAddress(rs.getNString("address"));
        s.setCityName(rs.getNString("city_name"));
        return s;
    }

    /* ================= BASIC QUERIES ================= */
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
        if (code == null || code.isBlank()) {
            return null;
        }
        String sql = """
            SELECT s.station_id, s.city_id, s.code, s.name, s.address,
                   c.name AS city_name
            FROM dbo.Station s
            JOIN dbo.City c ON c.city_id = s.city_id
            WHERE UPPER(LTRIM(RTRIM(s.code))) = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code.trim().toUpperCase());
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
        if (code == null || code.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM dbo.Station WHERE UPPER(LTRIM(RTRIM(code))) = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existsById(int stationId) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Station WHERE station_id = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, stationId);
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
            ps.setObject(1, cityId, Types.INTEGER);
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
        // Fallback khi DB không trả OUTPUT vì bất kỳ lý do gì
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
            ps.setObject(1, s.getCityId(), Types.INTEGER);
            ps.setString(2, s.getCode());
            ps.setNString(3, s.getName());
            if (s.getAddress() == null || s.getAddress().isBlank()) {
                ps.setNull(4, Types.NVARCHAR);
            } else {
                ps.setNString(4, s.getAddress());
            }
            ps.setObject(5, s.getStationId(), Types.INTEGER);
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

    /* ===== Exact theo tên (bỏ dấu, không phân biệt hoa/thường) ===== */
    public Station findByNameExact(String name) throws SQLException {
        if (name == null || name.isBlank()) {
            return null;
        }
        String sql = """
            SELECT s.station_id, s.city_id, s.code, s.name, s.address, c.name AS city_name
            FROM dbo.Station s
            JOIN dbo.City c ON c.city_id = s.city_id
            WHERE LTRIM(RTRIM(s.name)) COLLATE SQL_Latin1_General_CP1_CI_AI = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setNString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Integer findIdByNameExact(String name) throws SQLException {
        if (name == null || name.isBlank()) {
            return null;
        }
        String sql = """
            SELECT station_id
            FROM dbo.Station
            WHERE LTRIM(RTRIM(name)) COLLATE SQL_Latin1_General_CP1_CI_AI = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setNString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /* ===== Fallback “loose” theo tên/tỉnh/mã ===== */
    public Integer findIdByNameLoose(String input) throws SQLException {
        if (input == null || input.isBlank()) {
            return null;
        }

        String term = input.trim();
        String norm = term.replaceAll("\\s+", "");
        String start = term + "%";
        String any = "%" + term + "%";

        String codePrefix = norm.toUpperCase();
        String codeLike;
        if (codePrefix.matches("^GA\\d{1,3}$")) {
            codeLike = codePrefix + "%";
        } else if (codePrefix.matches("^\\d{1,3}$")) {
            codeLike = "GA" + codePrefix + "%";
        } else if (codePrefix.startsWith("GA")) {
            codeLike = codePrefix + "%";
        } else {
            codeLike = "GA%";
        }

        String sql = """
            SELECT TOP (1) s.station_id,
                   CASE
                     WHEN s.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? THEN 0
                     WHEN c.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? THEN 1
                     WHEN s.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? THEN 2
                     WHEN c.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? THEN 3
                     WHEN UPPER(LTRIM(RTRIM(s.code))) LIKE ? THEN 4
                     ELSE 9
                   END AS score
            FROM dbo.Station s
            JOIN dbo.City c ON c.city_id = s.city_id
            WHERE s.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?
               OR c.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?
               OR UPPER(LTRIM(RTRIM(s.code))) LIKE ?
            ORDER BY score, s.name
        """;

        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            // ORDER score params (5)
            ps.setNString(1, start);
            ps.setNString(2, start);
            ps.setNString(3, any);
            ps.setNString(4, any);
            ps.setString(5, codeLike);

            // WHERE params (3)
            ps.setNString(6, any);
            ps.setNString(7, any);
            ps.setString(8, codeLike);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /* ===== Suggest autocomplete (ưu tiên đầu từ, rồi chứa từ, rồi code) ===== */
    public List<Station> suggestByNameOrCode(String q, int limit) throws SQLException {
        String term = (q == null) ? "" : q.trim();
        String norm = term.replaceAll("\\s+", "");
        String start = term + "%";
        String word = "% " + term + "%";
        String any = "%" + term + "%";
        int lim = Math.max(1, limit);

        String codePrefix = norm.toUpperCase();
        String codeLike;
        if (codePrefix.matches("^GA\\d{1,3}$")) {
            codeLike = codePrefix + "%";
        } else if (codePrefix.matches("^\\d{1,3}$")) {
            codeLike = "GA" + codePrefix + "%";
        } else if (codePrefix.startsWith("GA")) {
            codeLike = codePrefix + "%";
        } else {
            codeLike = "GA%";
        }

        String sql = """
            SELECT TOP (?) s.station_id, s.city_id, s.code, s.name, s.address, c.name AS city_name,
                   CASE
                     WHEN s.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? THEN 0
                     WHEN c.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? THEN 1
                     WHEN s.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? THEN 2
                     WHEN c.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? THEN 3
                     WHEN UPPER(LTRIM(RTRIM(s.code))) LIKE ? THEN 4
                     WHEN s.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? THEN 5
                     WHEN c.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? THEN 6
                     ELSE 9
                   END AS score
            FROM dbo.Station s
            JOIN dbo.City c ON c.city_id = s.city_id
            WHERE s.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?
               OR c.name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?
               OR UPPER(LTRIM(RTRIM(s.code))) LIKE ?
            ORDER BY score, c.name, s.name
        """;

        List<Station> out = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lim);

            // ORDER score params (7)
            ps.setNString(2, start);
            ps.setNString(3, start);
            ps.setNString(4, word);
            ps.setNString(5, word);
            ps.setString(6, codeLike);
            ps.setNString(7, any);
            ps.setNString(8, any);

            // WHERE params (3)
            ps.setNString(9, any);
            ps.setNString(10, any);
            ps.setString(11, codeLike);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }
        return out;
    }

    /* ===== Theo vùng / thành phố ===== */
    public List<Station> findByRegionCode(String regionCode) throws SQLException {
        String sql = """
            SELECT s.station_id, s.city_id, s.code, s.name, s.address, c.name AS city_name
            FROM dbo.Station s
            JOIN dbo.City  c ON c.city_id = s.city_id
            JOIN dbo.Region r ON r.region_id = c.region_id
            WHERE r.code = ?
            ORDER BY c.name, s.name
        """;
        List<Station> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, regionCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<Station> findByCityId(int cityId) throws SQLException {
        String sql = """
            SELECT s.station_id, s.city_id, s.code, s.name, s.address, c.name AS city_name
            FROM dbo.Station s
            JOIN dbo.City c ON c.city_id = s.city_id
            WHERE s.city_id = ?
            ORDER BY s.name
        """;
        List<Station> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /* ===== Helpers nhỏ ===== */
    public String findNameById(int id) throws SQLException {
        String sql = "SELECT name FROM dbo.Station WHERE station_id = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getNString(1) : null;
            }
        }
    }

    public Integer findIdByCode(String code) throws SQLException {
        if (code == null || code.isBlank()) {
            return null;
        }
        String sql = "SELECT station_id FROM dbo.Station WHERE UPPER(LTRIM(RTRIM(code))) = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
}
