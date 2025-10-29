package vn.ttapp.model;

public class Seat {

    private Integer seatId;
    private Integer carriageId;
    private String code;
    private Integer seatClassId;
    private String positionInfo;

    // view props (JOIN)
    private String seatClassCode;
    private String seatClassName;
    private String carriageCode;
    private String trainCode;
    private String trainName;

    public Seat() {
    }

    public Seat(Integer seatId, Integer carriageId, String code, Integer seatClassId, String positionInfo) {
        this.seatId = seatId;
        this.carriageId = carriageId;
        this.code = code;
        this.seatClassId = seatClassId;
        this.positionInfo = positionInfo;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getSeatClassId() {
        return seatClassId;
    }

    public void setSeatClassId(Integer seatClassId) {
        this.seatClassId = seatClassId;
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

    public String getCarriageCode() {
        return carriageCode;
    }

    public void setCarriageCode(String carriageCode) {
        this.carriageCode = carriageCode;
    }

    public String getTrainCode() {
        return trainCode;
    }

    public void setTrainCode(String trainCode) {
        this.trainCode = trainCode;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }
}
