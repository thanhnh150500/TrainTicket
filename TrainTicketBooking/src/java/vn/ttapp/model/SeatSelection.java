package vn.ttapp.model;

public class SeatSelection {

    public Integer seatId;
    public Integer carriageId;
    public String seatCode;
    public String seatClassName;
    private Integer seatClassId;
    public int price;

    public SeatSelection() {
    }

    public SeatSelection(Integer seatId, Integer carriageId, String seatCode, String seatClassName, int price) {
        this.seatId = seatId;
        this.carriageId = carriageId;
        this.seatCode = seatCode;
        this.seatClassId = seatClassId;
        this.seatClassName = seatClassName;
        this.price = price;
    }

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    public Integer getCarriageId() {
        return carriageId;
    }

    public void setCarriageId(Integer carriageId) {
        this.carriageId = carriageId;
    }

    public Integer getSeatClassId() {
        return seatClassId;
    }

    public void setSeatClassId(Integer seatClassId) {
        this.seatClassId = seatClassId;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode;
    }

    public String getSeatClassName() {
        return seatClassName;
    }

    public void setSeatClassName(String seatClassName) {
        this.seatClassName = seatClassName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

}
