package vn.ttapp.service;

import vn.ttapp.dao.SeatDao;
import vn.ttapp.model.Seat;
import vn.ttapp.model.SeatView;   // ✅ dùng SeatView độc lập

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

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
            return null; // trùng mã ghế trong cùng toa
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

    /**
     * Lấy seat map kèm trạng thái (FREE/HELD/SOLD) + price cho 1 trip.
     */
    public List<SeatView> getSeatMapWithAvailability(int tripId) throws SQLException {
        // ✅ YÊU CẦU: SeatDao.getSeatsWithAvailability cũng trả List<SeatView>
        return dao.getSeatsWithAvailability(tripId);
    }

    /**
     * Kiểm tra ghế có thuộc trip (chống sửa URL).
     */
    public boolean seatBelongsToTrip(int seatId, int tripId) throws SQLException {
        return dao.seatBelongsToTrip(seatId, tripId);
    }

    /**
     * Giữ (lock) một ghế với TTL phút; trả về seat_lock_id hoặc null nếu
     * conflict.
     */
    public Integer holdOneSeat(int tripId, int seatId, Long bookingIdOrNull, int ttlMinutes) throws SQLException {
        if (!dao.seatBelongsToTrip(seatId, tripId)) {
            return null;
        }
        var lockIds = dao.lockSeats(tripId, Collections.singletonList(seatId), bookingIdOrNull, ttlMinutes);
        return (lockIds != null && !lockIds.isEmpty()) ? lockIds.get(0) : null;
    }

    /**
     * Giữ (lock) nhiều ghế cùng lúc (all-or-nothing theo DAO): - Nếu có xung
     * đột hoặc ghế không thuộc trip -> trả về danh sách rỗng. - Thành công ->
     * trả về danh sách seat_lock_id theo số ghế đã lock.
     */
    public List<Integer> holdSeats(int tripId, List<Integer> seatIds, Long bookingIdOrNull, int ttlMinutes) throws SQLException {
        if (seatIds == null || seatIds.isEmpty()) {
            return List.of();
        }
        for (Integer sid : seatIds) {
            if (sid == null || !dao.seatBelongsToTrip(sid, tripId)) {
                return List.of();
            }
        }
        return dao.lockSeats(tripId, seatIds, bookingIdOrNull, ttlMinutes);
    }

    /**
     * Nhả khoá thủ công (không bắt buộc vì TTL tự hết).
     */
    public boolean releaseLock(int seatLockId) throws SQLException {
        return dao.releaseLock(seatLockId) > 0;
    }

    /**
     * (Tuỳ chọn) Sơ đồ ghế không kèm trạng thái.
     */
    public List<Seat> getSeatMap(int tripId) throws SQLException {
        return findByTripId(tripId);
    }
}
