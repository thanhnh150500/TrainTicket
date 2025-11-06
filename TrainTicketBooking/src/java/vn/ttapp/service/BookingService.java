package vn.ttapp.service;

import vn.ttapp.dao.BookingDao;
import vn.ttapp.dao.FareRuleDao;
import vn.ttapp.dao.SeatDao;
import vn.ttapp.model.Booking;
import vn.ttapp.model.BookingItem;
import vn.ttapp.model.Seat;
import vn.ttapp.model.SeatHold;
import vn.ttapp.model.BookingSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

public class BookingService {

    private final SeatDao seatDao;
    private final FareRuleDao fareDao;
    private final BookingDao bookingDao;

    public BookingService(SeatDao seatDao, FareRuleDao fareDao, BookingDao bookingDao) {
        this.seatDao = Objects.requireNonNull(seatDao, "seatDao");
        this.fareDao = Objects.requireNonNull(fareDao, "fareDao");
        this.bookingDao = Objects.requireNonNull(bookingDao, "bookingDao");
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
        if (travelDate == null) {
            throw new IllegalArgumentException("travelDate is required.");
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
                throw new IllegalStateException(
                        "Base price not found (routeId=" + routeId
                        + ", seatClassId=" + seat.getSeatClassId()
                        + ", date=" + travelDate + ")"
                );
            }

            BookingItem bi = new BookingItem();
            bi.setTripId(tripId);
            bi.setSeatId(seat.getSeatId());
            bi.setSeatClassId(seat.getSeatClassId());
            bi.setSeatCode(seat.getCode()); // để hiển thị
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

    public Booking createDraftFromHoldsForUser(
            UUID userId,
            String contactEmail,
            String contactPhone,
            int tripId,
            int routeId,
            String segment,
            LocalDate travelDate,
            List<SeatHold> holds
    ) throws Exception {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required.");
        }

        // Tái sử dụng logic tính items/subtotal rồi gọi DAO overload (bạn cần hiện thực ở BookingDao)
        final String seg = "RETURN".equalsIgnoreCase(segment) ? "RETURN" : "OUTBOUND";
        if (holds == null || holds.isEmpty()) {
            throw new IllegalArgumentException("No seat holds / selected seats.");
        }
        if (contactEmail == null || contactEmail.isBlank()) {
            throw new IllegalArgumentException("Contact email is required.");
        }
        if (travelDate == null) {
            throw new IllegalArgumentException("travelDate is required.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<BookingItem> items = new ArrayList<>(holds.size());

        for (SeatHold h : holds) {
            Seat seat = seatDao.findById(h.getSeatId());
            if (seat == null) {
                throw new IllegalStateException("Seat not found: " + h.getSeatId());
            }

            BigDecimal basePrice = fareDao.getPrice(routeId, seat.getSeatClassId(), travelDate);
            if (basePrice == null) {
                throw new IllegalStateException(
                        "Base price not found (routeId=" + routeId
                        + ", seatClassId=" + seat.getSeatClassId()
                        + ", date=" + travelDate + ")"
                );
            }

            BookingItem bi = new BookingItem();
            bi.setTripId(tripId);
            bi.setSeatId(seat.getSeatId());
            bi.setSeatClassId(seat.getSeatClassId());
            bi.setSeatCode(seat.getCode());
            bi.setSegment(seg);
            bi.setBasePrice(basePrice);
            bi.setDiscountAmount(BigDecimal.ZERO);
            bi.setAmount(basePrice);

            subtotal = subtotal.add(basePrice);
            items.add(bi);
        }

        if (bookingDao instanceof vn.ttapp.dao.BookingDao.Impl impl
                && hasCreateForUser(impl)) {
            // Gọi bằng reflection để không phá interface, hoặc bạn có thể chính thức thêm vào interface.
            try {
                var m = impl.getClass().getMethod(
                        "createDraftWithItemsForUser",
                        UUID.class, String.class, String.class, int.class, BigDecimal.class, List.class
                );
                return (Booking) m.invoke(impl, userId, contactEmail, contactPhone, tripId, subtotal, items);
            } catch (ReflectiveOperationException e) {
                // fallback về method cũ (user_id = NULL)
            }
        }

        // Fallback: vẫn tạo booking không gắn user (sẽ không hiện ở lịch sử theo user)
        return bookingDao.createDraftWithItems(contactEmail, contactPhone, tripId, subtotal, items);
    }

    private boolean hasCreateForUser(vn.ttapp.dao.BookingDao.Impl impl) {
        try {
            impl.getClass().getMethod(
                    "createDraftWithItemsForUser",
                    UUID.class, String.class, String.class, int.class, BigDecimal.class, List.class
            );
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public Booking findById(Long bookingId) throws Exception {
        return bookingDao.findById(bookingId);
    }

    // ====== Các hàm phục vụ Booking History ======
    public long countUserBookings(UUID userId) throws Exception {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required.");
        }
        return bookingDao.countByUser(userId);
    }

    public List<BookingSummary> listUserBookings(UUID userId, String status, int page, int pageSize) throws Exception {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required.");
        }
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        return bookingDao.listSummariesByUser(userId, trimOrNull(status), page, pageSize);
    }

    private String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }

    public BookingService() {
        this(   new vn.ttapp.dao.SeatDao(),
                new vn.ttapp.dao.FareRuleDao(),
                new vn.ttapp.dao.BookingDao.Impl());
    }

}
