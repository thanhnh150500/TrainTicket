package vn.ttapp.model;

import java.time.LocalDateTime;

public class TripView {

    private int tripId;
    private int routeId;
    private int originId;
    private int destId;

    private String trainCode;
    private String trainName;
    private String originName;
    private String destName;
    private String status;

    private Integer originCityId;
    private String originCityName;
    private Integer originRegionId;
    private String originRegionName;

    private LocalDateTime departAt;
    private LocalDateTime arriveAt;

    // getters & setters
    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public int getOriginId() {
        return originId;
    }

    public void setOriginId(int originId) {
        this.originId = originId;
    }

    public int getDestId() {
        return destId;
    }

    public void setDestId(int destId) {
        this.destId = destId;
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

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getDestName() {
        return destName;
    }

    public void setDestName(String destName) {
        this.destName = destName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getOriginCityId() {
        return originCityId;
    }

    public void setOriginCityId(Integer originCityId) {
        this.originCityId = originCityId;
    }

    public String getOriginCityName() {
        return originCityName;
    }

    public void setOriginCityName(String originCityName) {
        this.originCityName = originCityName;
    }

    public Integer getOriginRegionId() {
        return originRegionId;
    }

    public void setOriginRegionId(Integer originRegionId) {
        this.originRegionId = originRegionId;
    }

    public String getOriginRegionName() {
        return originRegionName;
    }

    public void setOriginRegionName(String originRegionName) {
        this.originRegionName = originRegionName;
    }

    public LocalDateTime getDepartAt() {
        return departAt;
    }

    public void setDepartAt(LocalDateTime departAt) {
        this.departAt = departAt;
    }

    public LocalDateTime getArriveAt() {
        return arriveAt;
    }

    public void setArriveAt(LocalDateTime arriveAt) {
        this.arriveAt = arriveAt;
    }
}
