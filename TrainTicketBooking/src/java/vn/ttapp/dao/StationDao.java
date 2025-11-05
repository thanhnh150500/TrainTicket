package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Station;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StationDao {

    // Accent-insensitive + case-insensitive (phù hợp bộ dữ liệu VN thông dụng)
    private static final String VN_AI = "SQL_Latin1_General_CP1_CI_AI";

    private static String trimOrNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String trimUpperOrNull(String s) {
        String t = trimOrNull(s);
        return (t == null) ? null : t.toUpperCase();
    }

    private static void bindNullableString(PreparedStatement ps, int idx, String v) throws SQLException {
        if (v == null) {
            ps.setNull(idx, Types.VARCHAR);
        } else {
            ps.setString(idx, v);
        }
    }

    private static void bindNullableNString(PreparedStatement ps, int idx, String v) throws SQLException {
        if (v == null) {
            ps.setNull(idx, Types.NVARCHAR);
        } else {
            ps.setNString(idx, v);
        }
    }

    private Station map(ResultSet rs) throws SQLException {
        Station s = new Station();
        s.setStationId(rs.getObject("station_id", Integer.class));
        s.setCityId(rs.getObject("city_id", Integer.class));
        s.setCode(rs.getString("code"));
        s.setName(rs.getString("name"));
        s.setAddress(rs.getString("address"));
        s.setCityName(rs.getString("city_name"));
        return s;
    }

    /* =========================
       CRUD
     ========================= */
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

    /**
     * Tìm theo code
     */
    public Station findByCode(String code) throws SQLException {
        String norm = trimUpperOrNull(code);
        if (norm == null) {
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
            ps.setString(1, norm);
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
        String norm = trimUpperOrNull(code);
        if (norm == null) {
            return false;
        }

        String sql = "SELECT 1 FROM dbo.Station WHERE UPPER(LTRIM(RTRIM(code))) = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, norm);
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

            // code & name bắt buộc (nếu bạn muốn optional thì đổi bindNullable*)
            bindNullableString(ps, 2, trimOrNull(code));
            bindNullableNString(ps, 3, trimOrNull(name));
            bindNullableNString(ps, 4, trimOrNull(address));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        // fallback nếu OUTPUT không chạy
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
            bindNullableString(ps, 2, trimOrNull(s.getCode()));
            bindNullableNString(ps, 3, trimOrNull(s.getName()));
            bindNullableNString(ps, 4, trimOrNull(s.getAddress()));
            ps.setObject(5, s.getStationId(), Types.INTEGER);
            return ps.executeUpdate();
        }
    }

    public int delete(int id) throws SQLException {
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM dbo.Station WHERE station_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    /* =========================
       Resolve by name
     ========================= */
    /**
     * Exact theo tên
     */
    public Station findByNameExact(String name) throws SQLException {
        String n = trimOrNull(name);
        if (n == null) {
            return null;
        }

        String sql = """
            SELECT s.station_id, s.city_id, s.code, s.name, s.address, c.name AS city_name
            FROM dbo.Station s
            JOIN dbo.City c ON c.city_id = s.city_id
            WHERE LTRIM(RTRIM(s.name)) COLLATE """ + VN_AI + " = ?\n";

        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bindNullableNString(ps, 1, n);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Trả về ID theo tên (exact).
     */
    public Integer findIdByNameExact(String name) throws SQLException {
        String n = trimOrNull(name);
        if (n == null) {
            return null;
        }

        String sql = """
            SELECT station_id
            FROM dbo.Station
            WHERE LTRIM(RTRIM(name)) COLLATE """ + VN_AI + " = ?\n";

        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bindNullableNString(ps, 1, n);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /**
     * Fallback “loose”: ưu tiên bắt đầu bằng, sau đó chứa bất kỳ; match
     * name/city/code. Hỗ trợ code dạng GA01..GA63 hoặc chỉ số (01 -> GA01%).
     */
    public Integer findIdByNameLoose(String input) throws SQLException {
        String term = trimOrNull(input);
        if (term == null) {
            return null;
        }

        String norm = term.replaceAll("\\s+", "");
        String start = term + "%";
        String any = "%" + term + "%";

        // chuẩn hoá pattern code
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
                     WHEN s.name COLLATE """ + VN_AI + " LIKE ? THEN 0\n"
                + "  WHEN c.name COLLATE " + VN_AI + " LIKE ? THEN 1\n"
                + "  WHEN s.name COLLATE " + VN_AI + " LIKE ? THEN 2\n"
                + "  WHEN c.name COLLATE " + VN_AI + " LIKE ? THEN 3\n"
                + "  WHEN UPPER(LTRIM(RTRIM(s.code))) LIKE ? THEN 4\n"
                + "  ELSE 9 END AS score\n"
                + "FROM dbo.Station s\n"
                + "JOIN dbo.City c ON c.city_id = s.city_id\n"
                + "WHERE s.name COLLATE " + VN_AI + " LIKE ?\n"
                + "   OR c.name COLLATE " + VN_AI + " LIKE ?\n"
                + "   OR UPPER(LTRIM(RTRIM(s.code))) LIKE ?\n"
                + "ORDER BY score, s.name\n";

        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            // ORDER score params
            bindNullableNString(ps, 1, start);
            bindNullableNString(ps, 2, start);
            bindNullableNString(ps, 3, any);
            bindNullableNString(ps, 4, any);
            bindNullableString(ps, 5, codeLike);

            // WHERE params
            bindNullableNString(ps, 6, any);
            bindNullableNString(ps, 7, any);
            bindNullableString(ps, 8, codeLike);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /* =========================
       Suggest (autocomplete)
     ========================= */
    public List<Station> suggestByNameOrCode(String q, int limit) throws SQLException {
        String term = trimOrNull(q);
        if (term == null) {
            term = ""; // allow full list on empty
        }
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
                     WHEN s.name COLLATE """ + VN_AI + " LIKE ? THEN 0\n"
                + "  WHEN c.name COLLATE " + VN_AI + " LIKE ? THEN 1\n"
                + "  WHEN s.name COLLATE " + VN_AI + " LIKE ? THEN 2\n"
                + "  WHEN c.name COLLATE " + VN_AI + " LIKE ? THEN 3\n"
                + "  WHEN UPPER(LTRIM(RTRIM(s.code))) LIKE ? THEN 4\n"
                + "  WHEN s.name COLLATE " + VN_AI + " LIKE ? THEN 5\n"
                + "  WHEN c.name COLLATE " + VN_AI + " LIKE ? THEN 6\n"
                + "  ELSE 9 END AS score\n"
                + "FROM dbo.Station s\n"
                + "JOIN dbo.City c ON c.city_id = s.city_id\n"
                + "WHERE s.name COLLATE " + VN_AI + " LIKE ?\n"
                + "   OR c.name COLLATE " + VN_AI + " LIKE ?\n"
                + "   OR UPPER(LTRIM(RTRIM(s.code))) LIKE ?\n"
                + "ORDER BY score, c.name, s.name\n";

        List<Station> out = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, lim);

            // ORDER score params
            bindNullableNString(ps, 2, start);
            bindNullableNString(ps, 3, start);
            bindNullableNString(ps, 4, word);
            bindNullableNString(ps, 5, word);
            bindNullableString(ps, 6, codeLike);
            bindNullableNString(ps, 7, any);
            bindNullableNString(ps, 8, any);

            // WHERE params
            bindNullableNString(ps, 9, any);
            bindNullableNString(ps, 10, any);
            bindNullableString(ps, 11, codeLike);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }
        return out;
    }

    /* =========================
       Other filters
     ========================= */
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
            bindNullableString(ps, 1, trimOrNull(regionCode));
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

    public String findNameById(int id) throws SQLException {
        String sql = "SELECT name FROM dbo.Station WHERE station_id = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getNString(1) : null;
            }
        }
    }

    /**
     * Trả về ID theo code (trim + upper; an toàn null).
     */
    public Integer findIdByCode(String code) throws SQLException {
        String norm = trimUpperOrNull(code);
        if (norm == null) {
            return null;
        }

        String sql = "SELECT station_id FROM dbo.Station WHERE UPPER(LTRIM(RTRIM(code))) = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, norm);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
}
