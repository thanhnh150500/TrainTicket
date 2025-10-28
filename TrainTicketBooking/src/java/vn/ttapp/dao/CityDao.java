package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.City;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CityDao {

    public List<City> findAll() throws SQLException {
        String sql = """
            SELECT city_id, code, name
            FROM dbo.City
            ORDER BY name
        """;
        List<City> list = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                City x = new City();
                x.setCityId(rs.getInt("city_id"));
                x.setCode(rs.getString("code"));
                x.setName(rs.getString("name"));
                list.add(x);
            }
        }
        return list;
    }

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
