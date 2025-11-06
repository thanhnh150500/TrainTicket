package vn.ttapp.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class BookingSummary {

    private Long bookingId;
    private String tripCode;
    private String trainName;
    private String seatCodes;     // "A01, A02, B03"
    private int itemCount;
    private BigDecimal totalAmount;
    private String status;        // DRAFT/HOLD/PAID/CANCELED/EXPIRED
    private OffsetDateTime createdAt;
    private OffsetDateTime paidAt; // null nếu chưa thanh toán

    // Getters/Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getTripCode() {
        return tripCode;
    }

    public void setTripCode(String tripCode) {
        this.tripCode = tripCode;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getSeatCodes() {
        return seatCodes;
    }

    public void setSeatCodes(String seatCodes) {
        this.seatCodes = seatCodes;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(OffsetDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
