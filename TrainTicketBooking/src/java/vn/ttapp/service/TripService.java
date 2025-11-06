package vn.ttapp.service;

import vn.ttapp.dao.TripDao;
import vn.ttapp.dao.RouteDao;
import vn.ttapp.dao.TrainDao;
import vn.ttapp.dao.TripStaffDao;

import vn.ttapp.model.Trip;
import vn.ttapp.model.User;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.sql.Date;
import java.sql.Time;

public class TripService {

    private final TripDao dao = new TripDao();
    private final RouteDao routeDao = new RouteDao();
    private final TrainDao trainDao = new TrainDao();
    private final TripStaffDao staffDao = new TripStaffDao();

    private static final Set<String> ALLOWED_STATUS
            = Set.of("SCHEDULED", "RUNNING", "CANCELED", "FINISHED");

    public static class SearchResult {

        public List<Trip> outbound; // chiều đi
        public List<Trip> inbound;  // chiều về (rỗng nếu oneway)
    }

    public SearchResult searchTripsByStationIds(
            String tripType,
            Integer originStationId,
            Integer destStationId,
            LocalDate departDate,
            LocalTime departTime,
            LocalDate returnDate,
            LocalTime returnTime
    ) throws SQLException {

        // ---- Validate input cơ bản ----
        if (originStationId == null || destStationId == null) {
            throw new IllegalArgumentException("originStationId/destStationId không được null");
        }
        if (departDate == null) {
            throw new IllegalArgumentException("departDate không được null");
        }

        final boolean isRoundTrip = "ROUNDTRIP".equalsIgnoreCase(
                (tripType == null || tripType.isBlank()) ? "ONEWAY" : tripType.trim()
        );

        SearchResult sr = new SearchResult();

        // ---- Chiều đi ----
        sr.outbound = dao.searchByStationId(
                originStationId,
                destStationId,
                Date.valueOf(departDate),
                (departTime != null ? Time.valueOf(departTime) : null)
        );

        // ---- Chiều về (nếu có) ----
        if (isRoundTrip && returnDate != null) {
            sr.inbound = dao.searchReturnByStationIds(
                    originStationId,
                    destStationId,
                    Date.valueOf(returnDate),
                    (returnTime != null ? Time.valueOf(returnTime) : null)
            );
        } else {
            sr.inbound = Collections.emptyList(); // gọn cho JSP
        }

        return sr;
    }

    // ================= CRUD & tiện ích =================
    public Trip findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public List<Trip> findAll() throws SQLException {
        return dao.findAll();
    }

    /**
     * Lấy toàn bộ staff FNB để phân công
     */
    public List<User> getAllStaffFNB() throws SQLException {
        return staffDao.findAllStaffFNB();
    }

    /**
     * Tạo trip với validate route/train/time/status
     */
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

    // ====== Phân công & truy vấn staff theo trip ======
    public List<User> getStaffByTrip(int tripId) throws SQLException {
        return staffDao.findByTripId(tripId);
    }

    public void assignStaff(int tripId, List<String> staffIds, String staffRole) throws SQLException {
        staffDao.assignStaffToTrip(tripId, staffIds, staffRole);
    }
}
