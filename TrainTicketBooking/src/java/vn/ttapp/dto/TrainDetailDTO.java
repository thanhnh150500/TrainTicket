package vn.ttapp.dto;

import java.util.ArrayList;
import java.util.List;

public class TrainDetailDTO {

    private Integer trainId;
    private String code;
    private String name;
    private int totalCarriages;
    private int totalSeats;
    private List<CarriageDTO> carriages = new ArrayList<>();

    public TrainDetailDTO() {
    }

    public TrainDetailDTO(Integer trainId, String code, String name,
            int totalCarriages, int totalSeats, List<CarriageDTO> carriages) {
        this.trainId = trainId;
        this.code = code;
        this.name = name;
        this.totalCarriages = totalCarriages;
        this.totalSeats = totalSeats;
        if (carriages != null) {
            this.carriages = carriages;
        }
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

    public int getTotalCarriages() {
        return totalCarriages;
    }

    public void setTotalCarriages(int totalCarriages) {
        this.totalCarriages = totalCarriages;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public List<CarriageDTO> getCarriages() {
        return carriages;
    }

    public void setCarriages(List<CarriageDTO> carriages) {
        this.carriages = (carriages != null ? carriages : new ArrayList<>());
    }
}
