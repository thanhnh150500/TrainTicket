package vn.ttapp.model;

import java.sql.Timestamp;

public class TripInfo {

    private Integer tripId;
    private String trainCode;
    private String trainName;
    private Timestamp departAt;
    private Timestamp arriveAt;

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
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

    public Timestamp getDepartAt() {
        return departAt;
    }

    public void setDepartAt(Timestamp departAt) {
        this.departAt = departAt;
    }

    public Timestamp getArriveAt() {
        return arriveAt;
    }

    public void setArriveAt(Timestamp arriveAt) {
        this.arriveAt = arriveAt;
    }

    @Override
    public String toString() {
        return "TripInfo{"
                + "tripId=" + tripId
                + ", trainCode='" + trainCode + '\''
                + ", trainName='" + trainName + '\''
                + ", departAt=" + departAt
                + ", arriveAt=" + arriveAt
                + '}';
    }
}
