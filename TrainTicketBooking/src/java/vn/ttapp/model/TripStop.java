
package vn.ttapp.model;

import java.time.LocalDateTime;

public class TripStop {
    public Integer tripStopId;
    public Integer tripId;
    public Integer stationId;
    public LocalDateTime arrTime; // nullable
    public LocalDateTime depTime; // nullable
    public Integer stopOrder;

    public TripStop() {
    }

    public TripStop(Integer tripStopId, Integer tripId, Integer stationId, LocalDateTime arrTime, LocalDateTime depTime, Integer stopOrder) {
        this.tripStopId = tripStopId;
        this.tripId = tripId;
        this.stationId = stationId;
        this.arrTime = arrTime;
        this.depTime = depTime;
        this.stopOrder = stopOrder;
    }

    public Integer getTripStopId() {
        return tripStopId;
    }

    public void setTripStopId(Integer tripStopId) {
        this.tripStopId = tripStopId;
    }

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public Integer getStationId() {
        return stationId;
    }

    public void setStationId(Integer stationId) {
        this.stationId = stationId;
    }

    public LocalDateTime getArrTime() {
        return arrTime;
    }

    public void setArrTime(LocalDateTime arrTime) {
        this.arrTime = arrTime;
    }

    public LocalDateTime getDepTime() {
        return depTime;
    }

    public void setDepTime(LocalDateTime depTime) {
        this.depTime = depTime;
    }

    public Integer getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(Integer stopOrder) {
        this.stopOrder = stopOrder;
    }
    
}
