package vn.ttapp.model;

import java.math.BigDecimal;

/**
 * Mô hình chi tiết ghế trong đơn Booking.
 */
public class BookingItem {

    private Long bookingItemId;    // BIGINT IDENTITY
    private Long bookingId;        // FK → Booking
    private Integer tripId;        // FK → Trip
    private Integer seatId;        // FK → Seat
    private Integer seatClassId;   // FK → SeatClass
    private Long passengerId;      // FK → Passenger (nullable)
    private String segment;        // "OUTBOUND" | "RETURN"

    private BigDecimal basePrice = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal amount = BigDecimal.ZERO;

    // --------- Optional fields cho hiển thị UI ---------
    private String seatCode;        // Mã ghế hiển thị "C02-05A"
    private String carriageCode;    // Toa "C02"
    private String seatClassName;   // "Ghế mềm điều hòa"
    private String passengerName;   // Tên hành khách

    // ====== Constructors ======
    public BookingItem() {
    }

    public BookingItem(Long bookingId, Integer tripId, Integer seatId, Integer seatClassId,
            String segment, BigDecimal basePrice) {
        this.bookingId = bookingId;
        this.tripId = tripId;
        this.seatId = seatId;
        this.seatClassId = seatClassId;
        this.segment = segment;
        this.basePrice = basePrice;
        this.amount = basePrice;
    }

    // ====== Getters & Setters ======
    public Long getBookingItemId() {
        return bookingItemId;
    }

    public void setBookingItemId(Long bookingItemId) {
        this.bookingItemId = bookingItemId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    public Integer getSeatClassId() {
        return seatClassId;
    }

    public void setSeatClassId(Integer seatClassId) {
        this.seatClassId = seatClassId;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public String getCarriageCode() {
        return carriageCode;
    }

    public void setCarriageCode(String carriageCode) {
        this.carriageCode = carriageCode;
    }

    public String getSeatClassName() {
        return seatClassName;
    }

    public void setSeatClassName(String seatClassName) {
        this.seatClassName = seatClassName;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    @Override
    public String toString() {
        return "BookingItem{"
                + "bookingItemId=" + bookingItemId
                + ", bookingId=" + bookingId
                + ", tripId=" + tripId
                + ", seatId=" + seatId
                + ", seatClassId=" + seatClassId
                + ", passengerId=" + passengerId
                + ", segment='" + segment + '\''
                + ", basePrice=" + basePrice
                + ", discountAmount=" + discountAmount
                + ", amount=" + amount
                + ", seatCode='" + seatCode + '\''
                + ", carriageCode='" + carriageCode + '\''
                + ", seatClassName='" + seatClassName + '\''
                + ", passengerName='" + passengerName + '\''
                + '}';
    }
}
