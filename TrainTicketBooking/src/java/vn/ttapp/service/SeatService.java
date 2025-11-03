package vn.ttapp.service;

import vn.ttapp.dao.SeatDao;
import vn.ttapp.model.Seat;
import vn.ttapp.model.SeatView;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SeatService {

    private final SeatDao dao = new SeatDao();

    /* ========= Admin/CRUD ========= */
    public List<Seat> findAll() throws SQLException {
        return dao.findAll();
    }

    public Seat findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public Integer create(int carriageId, String code, int seatClassId, String positionInfo) throws SQLException {
        if (dao.codeExists(carriageId, code)) {
            return null;
        }
        return dao.create(carriageId, code, seatClassId, positionInfo);
    }

    public boolean update(Seat s) throws SQLException {
        if (dao.codeExistsExceptId(s.getCarriageId(), s.getCode(), s.getSeatId())) {
            return false;
        }
        return dao.update(s) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }

    /* ========= Customer / Seat Map ========= */
    public List<Seat> findByTripId(int tripId) throws SQLException {
        return dao.findByTripId(tripId);
    }

 
    public List<SeatView> getSeatMapWithAvailabilityForTrain(int tripId, int trainId) throws SQLException {
        return dao.getSeatMapWithAvailabilityForTrain(tripId, trainId);
    }

    public boolean seatBelongsToTrip(int seatId, int tripId) throws SQLException {
        if (seatId <= 0 || tripId <= 0) {
            return false;
        }
        return dao.seatBelongsToTrip(seatId, tripId);
    }


    public Integer holdOneSeat(int tripId, int seatId, Long bookingIdOrNull, int ttlMinutes) throws SQLException {
        if (!seatBelongsToTrip(seatId, tripId)) {
            return null;
        }
        var lockIds = dao.lockSeats(tripId, Collections.singletonList(seatId), bookingIdOrNull, ttlMinutes);
        return (lockIds != null && !lockIds.isEmpty()) ? lockIds.get(0) : null;
    }

    public List<Integer> holdSeats(int tripId, List<Integer> seatIds, Long bookingIdOrNull, int ttlMinutes) throws SQLException {
        if (seatIds == null || seatIds.isEmpty()) {
            return List.of();
        }
        for (Integer sid : seatIds) {
            if (sid == null || sid <= 0 || !seatBelongsToTrip(sid, tripId)) {
                return List.of();
            }
        }
        return dao.lockSeats(tripId, seatIds, bookingIdOrNull, ttlMinutes);
    }

    public boolean releaseLock(int seatLockId) throws SQLException {
        return dao.releaseLock(seatLockId) > 0;
    }

    public List<Seat> getSeatMap(int tripId) throws SQLException {
        return findByTripId(tripId);
    }
}
