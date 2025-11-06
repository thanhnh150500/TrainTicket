package vn.ttapp.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SeatView {

    public int seatId;
    public String seatCode;
    public Integer  seatClassId;
    public String seatClassCode;
    public String seatClassName;
    public int carriageId;
    public String carriageCode;
    public boolean available;
    public Timestamp lockExpiresAt;
    public Long lockingBookingId;
    public BigDecimal price;
    public Integer rowNo;
    public Integer colNo;

    // ===== Getters / Setters =====
    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public int getSeatClassId() {
        return seatClassId;
    }

    public void setSeatClassId(int seatClassId) {
        this.seatClassId = seatClassId;
    }

    public String getSeatClassCode() {
        return seatClassCode;
    }

    public void setSeatClassCode(String seatClassCode) {
        this.seatClassCode = seatClassCode;
    }

    public String getSeatClassName() {
        return seatClassName;
    }

    public void setSeatClassName(String seatClassName) {
        this.seatClassName = seatClassName;
    }

    public int getCarriageId() {
        return carriageId;
    }

    public void setCarriageId(int carriageId) {
        this.carriageId = carriageId;
    }

    public String getCarriageCode() {
        return carriageCode;
    }

    public void setCarriageCode(String carriageCode) {
        this.carriageCode = carriageCode;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Timestamp getLockExpiresAt() {
        return lockExpiresAt;
    }

    public void setLockExpiresAt(Timestamp lockExpiresAt) {
        this.lockExpiresAt = lockExpiresAt;
    }

    public Long getLockingBookingId() {
        return lockingBookingId;
    }

    public void setLockingBookingId(Long lockingBookingId) {
        this.lockingBookingId = lockingBookingId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }

    public Integer getColNo() {
        return colNo;
    }

    public void setColNo(Integer colNo) {
        this.colNo = colNo;
    }
}
