package vn.ttapp.model;

public class SeatClass {

    private Integer seatClassId;
    private String code;
    private String name;

    public SeatClass() {
    }

    public SeatClass(Integer seatClassId, String code, String name) {
        this.seatClassId = seatClassId;
        this.code = code;
        this.name = name;
    }

    public Integer getSeatClassId() {
        return seatClassId;
    }

    public void setSeatClassId(Integer seatClassId) {
        this.seatClassId = seatClassId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
