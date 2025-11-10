package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.City;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CityDao {

    /* ========= Mapper ========= */
    private City map(ResultSet rs) throws SQLException {
        City x = new City();
        x.setCityId(rs.getInt("city_id"));
        x.setRegionId(rs.getInt("region_id"));
        x.setCode(rs.getString("code"));
        x.setName(rs.getNString("name"));
        return x;
    }

    /* ========= Queries ========= */
    /**
     * Lấy toàn bộ thành phố (có region_id)
     */
    public List<City> findAll() throws SQLException {
        String sql = """
            SELECT city_id, region_id, code, name
            FROM dbo.City
            ORDER BY name
        """;
        List<City> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Lọc theo vùng; regionId = null → trả tất cả
     */
    public List<City> findByRegion(Integer regionId) throws SQLException {
        String sql = """
            SELECT city_id, region_id, code, name
            FROM dbo.City
            WHERE (? IS NULL OR region_id = ?)
            ORDER BY name
        """;
        List<City> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            if (regionId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, regionId);
                ps.setInt(2, regionId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy city theo ID
     */
    public City findById(int cityId) throws SQLException {
        String sql = """
            SELECT city_id, region_id, code, name
            FROM dbo.City
            WHERE city_id = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cityId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Lấy city theo code (ví dụ HNI, HCM, DNG...)
     */
    public City findByCode(String code) throws SQLException {
        String sql = """
            SELECT city_id, region_id, code, name
            FROM dbo.City
            WHERE code = ?
        """;
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Kiểm tra tồn tại theo ID
     */
    public boolean existsById(int cityId) throws SQLException {
        String sql = "SELECT 1 FROM dbo.City WHERE city_id = ?";
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cityId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
