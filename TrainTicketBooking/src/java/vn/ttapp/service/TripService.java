package vn.ttapp.service;

import vn.ttapp.dao.TripDao;
import vn.ttapp.model.Trip;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.sql.Date;
import java.sql.Time;

public class TripService {

    private final TripDao dao = new TripDao();

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

    // CRUD đơn giản
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
