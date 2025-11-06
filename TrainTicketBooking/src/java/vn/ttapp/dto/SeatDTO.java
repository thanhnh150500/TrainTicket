package vn.ttapp.dto;

public class SeatDTO {

    private Integer seatId;
    private String code;
    private String positionInfo;
    private String seatClassCode;
    private String seatClassName;

    public SeatDTO() {
    }

    public SeatDTO(Integer seatId, String code, String positionInfo, String seatClassCode, String seatClassName) {
        this.seatId = seatId;
        this.code = code;
        this.positionInfo = positionInfo;
        this.seatClassCode = seatClassCode;
        this.seatClassName = seatClassName;
    }

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPositionInfo() {
        return positionInfo;
    }

    public void setPositionInfo(String positionInfo) {
        this.positionInfo = positionInfo;
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
}
