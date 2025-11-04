package vn.ttapp.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Booking {

    private Long bookingId;             // BIGINT IDENTITY
    private UUID userId;                // UNIQUEIDENTIFIER (FK Users)
    private String contactEmail;        // NVARCHAR(255)
    private String contactPhone;        // NVARCHAR(32)
    private String status;              // NVARCHAR(20): DRAFT / HOLD / PAID / CANCELED / EXPIRED
    private BigDecimal subtotal;        // DECIMAL(18,2)
    private BigDecimal discountTotal;   // DECIMAL(18,2)
    private BigDecimal totalAmount;     // DECIMAL(18,2)
    private OffsetDateTime createdAt;   // DATETIME2
    private OffsetDateTime updatedAt;   // DATETIME2
    private OffsetDateTime holdExpiresAt; // NULL nếu không giữ chỗ
    private OffsetDateTime paidAt;      // DATETIME2 NULL — thời điểm thanh toán

    // ====== Constructors ======
    public Booking() {
    }

    // ====== Getters & Setters ======
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(BigDecimal discountTotal) {
        this.discountTotal = discountTotal;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(OffsetDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(OffsetDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
