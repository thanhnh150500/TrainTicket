package vn.ttapp.model;

public class Train {

    private Integer trainId;
    private String code;
    private String name;

    public Train() {
    }

    public Train(Integer trainId, String code, String name) {
        this.trainId = trainId;
        this.code = code;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
