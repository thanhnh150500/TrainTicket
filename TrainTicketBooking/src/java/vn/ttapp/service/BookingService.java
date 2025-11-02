package vn.ttapp.service;

import vn.ttapp.dao.BookingDao;
import vn.ttapp.dao.FareRuleDao;
import vn.ttapp.dao.SeatDao;
import vn.ttapp.model.Booking;
import vn.ttapp.model.BookingItem;
import vn.ttapp.model.Seat;
import vn.ttapp.model.SeatHold;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingService {

    private final SeatDao seatDao;
    private final FareRuleDao fareDao;   // dùng đúng kiểu FareRuleDao
    private final BookingDao bookingDao;

    public BookingService(SeatDao seatDao, FareRuleDao fareDao, BookingDao bookingDao) {
        this.seatDao = seatDao;
        this.fareDao = fareDao;
        this.bookingDao = bookingDao;
    }

    /**
     * Tạo booking DRAFT từ danh sách SeatHold còn hiệu lực. - Tính giá theo
     * SeatClass/FareRule. - Tạo header + items. - Gắn các hold vào booking (để
     * không dùng lại).
     */
    public Booking createDraftFromHolds(
            String contactEmail,
            String contactPhone,
            int tripId,
            int routeId,
            String segment, // "OUTBOUND" | "RETURN"
            LocalDate travelDate,
            List<SeatHold> holds
    ) throws Exception {

        if (holds == null || holds.isEmpty()) {
            throw new IllegalArgumentException("No seat holds");
        }

        // 2) Tính giá và build BookingItem list
        BigDecimal subtotal = BigDecimal.ZERO;
        List<BookingItem> items = new ArrayList<>();
        for (SeatHold h : holds) {
            Seat seat = seatDao.findById(h.seatId);
            if (seat == null) {
                throw new IllegalStateException("Seat not found: " + h.seatId);
            }
            BigDecimal basePrice = fareDao.getPrice(routeId, seat.getSeatClassId(), travelDate);

            BookingItem bi = new BookingItem();
            bi.setTripId(tripId);
            bi.setSeatId(seat.getSeatId());
            bi.setSeatClassId(seat.getSeatClassId());
            bi.setSeatCode(seat.getCode());
            bi.setSegment("RETURN".equalsIgnoreCase(segment) ? "RETURN" : "OUTBOUND");
            bi.setBasePrice(basePrice);
            bi.setDiscountAmount(BigDecimal.ZERO);
            bi.setAmount(basePrice);

            subtotal = subtotal.add(basePrice);
            items.add(bi);
        }

        // 3) Tạo DRAFT + Items + Link holds trong 1 transaction ở DAO
        return bookingDao.createDraftWithItems(contactEmail, contactPhone, tripId, subtotal, items, holds);
    }

    public Booking findById(Long bookingId) throws Exception {
        return bookingDao.findById(bookingId);
    }
}
