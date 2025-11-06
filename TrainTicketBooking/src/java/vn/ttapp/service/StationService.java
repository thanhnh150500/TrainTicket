package vn.ttapp.service;

import vn.ttapp.dao.CityDao;
import vn.ttapp.dao.StationDao;
import vn.ttapp.model.Station;

import java.sql.SQLException;
import java.util.List;

public class StationService {

    private final StationDao stationDao = new StationDao();
    private final CityDao cityDao = new CityDao();

    public List<Station> findAll() throws SQLException {
        return stationDao.findAll();
    }

    public Station findById(int id) throws SQLException {
        return stationDao.findById(id);
    }

    public Integer create(Integer cityId, String code, String name, String address) throws SQLException {
        if (cityId == null || !cityDao.existsById(cityId)) {
            return null; // city invalid
        }

        String normCode = normalizeCode(code);
        String normName = normalizeName(name);
        String normAddr = normalizeAddress(address);

        if (normCode == null || normName == null) {
            return null;
        }
        if (stationDao.codeExists(normCode)) {
            return null; // duplicate code
        }

        return stationDao.create(cityId, normCode, normName, normAddr);
    }

    public boolean update(Station s) throws SQLException {
        if (s.getStationId() == null || s.getCityId() == null || !cityDao.existsById(s.getCityId())) {
            return false;
        }

        String normCode = normalizeCode(s.getCode());
        String normName = normalizeName(s.getName());
        String normAddr = normalizeAddress(s.getAddress());
        if (normCode == null || normName == null) {
            return false;
        }

        Station existed = stationDao.findByCode(normCode);
        if (existed != null && !existed.getStationId().equals(s.getStationId())) {
            return false; // code đã thuộc về bản ghi khác
        }

        s.setCode(normCode);
        s.setName(normName);
        s.setAddress(normAddr);

        return stationDao.update(s) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return stationDao.delete(id) > 0;
    }

    // ------- helpers -------
    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String s = code.trim().toUpperCase();
        if (s.isEmpty() || s.length() > 20) {
            return null;
        }
        // chỉ cho chữ/số/gạch dưới/gạch ngang
        if (!s.matches("[A-Z0-9\\-_]+")) {
            return null;
        }
        return s;
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String s = name.trim();
        if (s.isEmpty() || s.length() > 150) {
            return null;
        }
        return s;
    }

    private String normalizeAddress(String addr) {
        if (addr == null) {
            return null;
        }
        String s = addr.trim();
        return s.isEmpty() ? null : (s.length() > 255 ? s.substring(0, 255) : s);
    }
}
