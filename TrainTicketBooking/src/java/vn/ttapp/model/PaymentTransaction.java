package vn.ttapp.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentTransaction {

    private Long paymentId;
    private Long bookingId;
    private String method;
    private BigDecimal amount;
    private String currency = "VND"; 
    private String status; 
    private String providerTxnId;
    private String idempotencyKey;   
    private OffsetDateTime createdAt;
    private OffsetDateTime confirmedAt;
    private String rawPayload;

    // ====== Constructors ======
    public PaymentTransaction() {
    }

    public PaymentTransaction(Long bookingId, String method, BigDecimal amount, String idempotencyKey) {
        this.bookingId = bookingId;
        this.method = method;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.status = "INITIATED";
    }

    // ====== Getters & Setters ======
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProviderTxnId() {
        return providerTxnId;
    }

    public void setProviderTxnId(String providerTxnId) {
        this.providerTxnId = providerTxnId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(OffsetDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    // ====== Helper ======
    @Override
    public String toString() {
        return "PaymentTransaction{"
                + "paymentId=" + paymentId
                + ", bookingId=" + bookingId
                + ", method='" + method + '\''
                + ", amount=" + amount
                + ", currency='" + currency + '\''
                + ", status='" + status + '\''
                + ", providerTxnId='" + providerTxnId + '\''
                + ", idempotencyKey='" + idempotencyKey + '\''
                + ", createdAt=" + createdAt
                + ", confirmedAt=" + confirmedAt
                + '}';
    }
}
