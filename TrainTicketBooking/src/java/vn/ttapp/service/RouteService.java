package vn.ttapp.service;

import vn.ttapp.dao.RouteDao;
import vn.ttapp.dao.StationDao;
import vn.ttapp.model.Route;

import java.sql.SQLException;
import java.util.List;

public class RouteService {

    private final RouteDao dao = new RouteDao();
    private final StationDao stationDao = new StationDao();

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
        if (stationDao.findById(originId) == null || stationDao.findById(destId) == null) {
            return null;
        }

        String normCode = normalizeCode(code);
        if (normCode == null) {
            return null;
        }

        if (dao.codeExists(normCode)) {
            return null;
        }
        return dao.create(originId, destId, normCode);
    }

    public boolean update(Route r) throws SQLException {
        if (r.getRouteId() == null) {
            return false;
        }
        if (r.getOriginStationId() == null || r.getDestStationId() == null) {
            return false;
        }
        if (r.getOriginStationId().equals(r.getDestStationId())) {
            return false;
        }

        if (stationDao.findById(r.getOriginStationId()) == null
                || stationDao.findById(r.getDestStationId()) == null) {
            return false;
        }

        String normCode = normalizeCode(r.getCode());
        if (normCode == null) {
            return false;
        }

        Route byCode = dao.findByCode(normCode);
        if (byCode != null && !byCode.getRouteId().equals(r.getRouteId())) {
            return false; // code đã thuộc route khác
        }

        r.setCode(normCode);
        return dao.update(r) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }

    // --- helpers ---
    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String s = code.trim().toUpperCase();
        if (s.isEmpty() || s.length() > 40) {
            return null;
        }
        if (!s.matches("[A-Z0-9\\-]+")) {
            return null; // ví dụ: HNI-HCM
        }
        return s;
    }
}
