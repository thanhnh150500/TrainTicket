package vn.ttapp.model;

import java.time.OffsetDateTime;

public class Payment {
    public Integer paymentId;      // identity
    public Integer bookingId;
    public String  gateway;        // MOCK/VNPAY/MOMO...
    public int     amount;         // VND
    public PaymentStatus status;   // INIT/SUCCESS/FAILED
    public String  payload;        // raw callback/signature/txnId...
    public OffsetDateTime createdAt;
}
