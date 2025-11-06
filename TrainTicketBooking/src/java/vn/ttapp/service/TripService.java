// vn/ttapp/service/TripService.java
package vn.ttapp.service;

import vn.ttapp.dao.TripDao;
import vn.ttapp.dao.RouteDao;
import vn.ttapp.dao.TrainDao;
import vn.ttapp.model.Trip;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class TripService {

    private final TripDao dao = new TripDao();
    private final RouteDao routeDao = new RouteDao();
    private final TrainDao trainDao = new TrainDao();

    private static final Set<String> ALLOWED_STATUS
            = Set.of("SCHEDULED", "RUNNING", "CANCELED", "FINISHED");

    public Trip findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public List<Trip> findAll() throws SQLException {
        return dao.findAll();
    }

    public Integer create(Integer routeId, Integer trainId,
            LocalDateTime departAt, LocalDateTime arriveAt,
            String status) throws SQLException {
        if (routeId == null || trainId == null || departAt == null || arriveAt == null
                || status == null || status.isBlank()) {
            return null;
        }
        if (!routeDao.existsById(routeId)) {
            return null;
        }
        if (!trainDao.existsById(trainId)) {
            return null;
        }
        if (!arriveAt.isAfter(departAt)) {
            return null;
        }
        status = status.trim().toUpperCase();
        if (!ALLOWED_STATUS.contains(status)) {
            return null;
        }
        return dao.create(routeId, trainId, departAt, arriveAt, status);
    }

    public boolean update(Trip t) throws SQLException {
        if (t.getTripId() == null || t.getRouteId() == null || t.getTrainId() == null
                || t.getDepartAt() == null || t.getArriveAt() == null
                || t.getStatus() == null || t.getStatus().isBlank()) {
            return false;
        }
        if (!routeDao.existsById(t.getRouteId())) {
            return false;
        }
        if (!trainDao.existsById(t.getTrainId())) {
            return false;
        }
        if (!t.getArriveAt().isAfter(t.getDepartAt())) {
            return false;
        }
        t.setStatus(t.getStatus().trim().toUpperCase());
        if (!ALLOWED_STATUS.contains(t.getStatus())) {
            return false;
        }
        return dao.update(t) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }
}
