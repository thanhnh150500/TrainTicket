// vn/ttapp/model/Carriage.java
package vn.ttapp.model;

public class Carriage {

    private Integer carriageId;
    private Integer trainId;
    private String code;
    private Integer seatClassId;
    private Integer sortOrder;

    // view-only
    private String trainCode;
    private String trainName;
    private String seatClassCode;
    private String seatClassName;

    public Carriage() {
    }

    public Carriage(Integer carriageId, Integer trainId, String code, Integer seatClassId, Integer sortOrder) {
        this.carriageId = carriageId;
        this.trainId = trainId;
        this.code = code;
        this.seatClassId = seatClassId;
        this.sortOrder = sortOrder;
    }

    public Integer getCarriageId() {
        return carriageId;
    }

    public void setCarriageId(Integer carriageId) {
        this.carriageId = carriageId;
    }

    public Integer getTrainId() {
        return trainId;
    }

    public void setTrainId(Integer trainId) {
        this.trainId = trainId;
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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
