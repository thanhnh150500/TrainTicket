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
import java.util.Objects;

public class BookingService {

    private final SeatDao seatDao;
    private final FareRuleDao fareDao;
    private final BookingDao bookingDao;

    public BookingService(SeatDao seatDao, FareRuleDao fareDao, BookingDao bookingDao) {
        this.seatDao = Objects.requireNonNull(seatDao);
        this.fareDao = Objects.requireNonNull(fareDao);
        this.bookingDao = Objects.requireNonNull(bookingDao);
    }

    public Booking createDraftFromHolds(
            String contactEmail,
            String contactPhone,
            int tripId,
            int routeId,
            String segment,
            LocalDate travelDate,
            List<SeatHold> holds 
    ) throws Exception {

        if (holds == null || holds.isEmpty()) {
            throw new IllegalArgumentException("No seat holds / selected seats.");
        }
        if (contactEmail == null || contactEmail.isBlank()) {
            throw new IllegalArgumentException("Contact email is required.");
        }

        final String seg = "RETURN".equalsIgnoreCase(segment) ? "RETURN" : "OUTBOUND";

        BigDecimal subtotal = BigDecimal.ZERO;
        List<BookingItem> items = new ArrayList<>(holds.size());

        for (SeatHold h : holds) {
            int seatId = h.getSeatId();
            Seat seat = seatDao.findById(seatId);
            if (seat == null) {
                throw new IllegalStateException("Seat not found: " + seatId);
            }

            BigDecimal basePrice = fareDao.getPrice(routeId, seat.getSeatClassId(), travelDate);
            if (basePrice == null) {
                throw new IllegalStateException("Base price not found (routeId=" + routeId
                        + ", seatClassId=" + seat.getSeatClassId() + ", date=" + travelDate + ")");
            }

            BookingItem bi = new BookingItem();
            bi.setTripId(tripId);
            bi.setSeatId(seat.getSeatId());
            bi.setSeatClassId(seat.getSeatClassId());
            bi.setSeatCode(seat.getCode());      // field phụ trợ để hiển thị
            bi.setSegment(seg);
            bi.setBasePrice(basePrice);
            bi.setDiscountAmount(BigDecimal.ZERO);
            bi.setAmount(basePrice);

            subtotal = subtotal.add(basePrice);
            items.add(bi);
        }
        
        return bookingDao.createDraftWithItems(
                contactEmail,
                contactPhone,
                tripId,
                subtotal,
                items
        );
    }

    public Booking findById(Long bookingId) throws Exception {
        return bookingDao.findById(bookingId);
    }
}
