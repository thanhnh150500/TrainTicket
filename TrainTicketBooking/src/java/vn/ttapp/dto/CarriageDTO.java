package vn.ttapp.dto;

import java.util.ArrayList;
import java.util.List;

public class CarriageDTO {

    private Integer carriageId;
    private String code;
    private Integer sortOrder;
    private String seatClassCode;
    private String seatClassName;
    private List<SeatDTO> seats = new ArrayList<>();

    public CarriageDTO() {
    }

    public CarriageDTO(Integer carriageId, String code, Integer sortOrder,
            String seatClassCode, String seatClassName, List<SeatDTO> seats) {
        this.carriageId = carriageId;
        this.code = code;
        this.sortOrder = sortOrder;
        this.seatClassCode = seatClassCode;
        this.seatClassName = seatClassName;
        if (seats != null) {
            this.seats = seats;
        }
    }

    public Integer getCarriageId() {
        return carriageId;
    }

    public void setCarriageId(Integer carriageId) {
        this.carriageId = carriageId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

    public List<SeatDTO> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatDTO> seats) {
        this.seats = (seats != null ? seats : new ArrayList<>());
    }
}
