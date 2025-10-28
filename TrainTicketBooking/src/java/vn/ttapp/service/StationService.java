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
            return null;     // city invalid
        }
        if (stationDao.codeExists(code)) {
            return null;                        // duplicate code
        }
        return stationDao.create(cityId, code, name, address);
    }

    public boolean update(Station s) throws SQLException {
        if (s.getCityId() == null || !cityDao.existsById(s.getCityId())) {
            return false;
        }
        Station existed = stationDao.findByCode(s.getCode());
        if (existed != null && !existed.getStationId().equals(s.getStationId())) {
            return false;
        }
        return stationDao.update(s) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return stationDao.delete(id) > 0;
    }
}
