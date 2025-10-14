package vn.ttapp.service;

import vn.ttapp.dao.RouteDao;
import vn.ttapp.model.Route;

import java.sql.SQLException;
import java.util.List;

public class RouteService {

    private final RouteDao dao = new RouteDao();

    public List<Route> findAll() throws SQLException {
        return dao.findAll();
    }

    public Route findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public Integer create(int originId, int destId, String code) throws SQLException {
        if (originId == destId) {
            return null;
        }
        if (dao.codeExists(code)) {
            return null;
        }
        return dao.create(originId, destId, code);
    }

    public boolean update(Route r) throws SQLException {
        if (r.getOriginStationId().equals(r.getDestStationId())) {
            return false;
        }
        Route existed = dao.findById(r.getRouteId());
        if (dao.codeExists(r.getCode())) {
            // nếu đổi sang code đang tồn tại ở route khác → fail
            if (existed == null || !existed.getCode().equals(r.getCode())) {
                return false;
            }
        }
        return dao.update(r) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }
}
