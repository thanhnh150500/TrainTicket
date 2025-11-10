package vn.ttapp.dao;

import vn.ttapp.config.Db;
import vn.ttapp.model.Region;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegionDao {

    public List<Region> findAll() throws SQLException {
        String sql = "SELECT region_id, code, name FROM dbo.Region ORDER BY name";
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Region> list = new ArrayList<>();
            while (rs.next()) {
                Region r = new Region();
                r.setRegionId(rs.getInt("region_id"));
                r.setCode(rs.getString("code"));
                r.setName(rs.getNString("name"));
                list.add(r);
            }
            return list;
        }
    }
}
