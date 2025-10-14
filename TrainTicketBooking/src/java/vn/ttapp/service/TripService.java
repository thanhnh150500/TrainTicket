package vn.ttapp.service;

import vn.ttapp.dao.TripDao;
import vn.ttapp.model.Trip;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TripService {

    private final TripDao dao = new TripDao();

    public Trip findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public List<Trip> findAll() throws SQLException {
        return dao.findAll();
    }

    public Integer create(int routeId, int trainId, LocalDateTime departAt, LocalDateTime arriveAt, String status)
            throws SQLException {
        return dao.create(routeId, trainId, departAt, arriveAt, status);
    }

    public boolean update(Trip t) throws SQLException {
        return dao.update(t) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }
}
