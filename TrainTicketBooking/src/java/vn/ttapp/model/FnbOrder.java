/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class FnbOrder {

    // Enum cho các trạng thái để code sạch hơn
    public enum OrderStatus {
        CREATED, DELIVERED, CANCELED
    }

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED
    }

    private Long orderId;
    private Integer tripId;
    private String seatLabel;
    private OrderStatus orderStatus;
    private double totalAmount;
    private String paymentMethod; // "QR", "CASH"
    private PaymentStatus paymentStatus;
    private Long paymentId;
    private String qrPayload;
    private Timestamp qrExpireAt;
    private UUID createdByUserId; // UUID của staff
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // =======================================================
    // ==> (THÊM MỚI) CÁC TRƯỜNG ĐỂ JOIN BÁO CÁO
    // =======================================================
    private String createdByStaffName; // Tên staff tạo
    private List<FnbOrderItem> items; // Danh sách món ăn
    private Trip trip; // Thông tin chuyến tàu
    // =======================================================

    public FnbOrder() {
    }

    // ... (Getters/Setters cũ của bạn) ...
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Integer getTripId() { return tripId; }
    public void setTripId(Integer tripId) { this.tripId = tripId; }
    public String getSeatLabel() { return seatLabel; }
    public void setSeatLabel(String seatLabel) { this.seatLabel = seatLabel; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getQrPayload() { return qrPayload; }
    public void setQrPayload(String qrPayload) { this.qrPayload = qrPayload; }
    public Timestamp getQrExpireAt() { return qrExpireAt; }
    public void setQrExpireAt(Timestamp qrExpireAt) { this.qrExpireAt = qrExpireAt; }
    public UUID getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(UUID createdByUserId) { this.createdByUserId = createdByUserId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // =======================================================
    // ==> (THÊM MỚI) GETTERS/SETTERS CHO BÁO CÁO
    // =======================================================
    public String getCreatedByStaffName() {
        return createdByStaffName;
    }
    public void setCreatedByStaffName(String createdByStaffName) {
        this.createdByStaffName = createdByStaffName;
    }
    public List<FnbOrderItem> getItems() {
        return items;
    }
    public void setItems(List<FnbOrderItem> items) {
        this.items = items;
    }
    public Trip getTrip() {
        return trip;
    }
    public void setTrip(Trip trip) {
        this.trip = trip;
    }
    // =======================================================
}