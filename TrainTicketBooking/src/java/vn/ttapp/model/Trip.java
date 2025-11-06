/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Trip {
    public Integer tripId;
    public Integer routeId;
    public Integer trainId;
    public LocalDateTime departAt;
    public LocalDateTime arriveAt;
    public String  status;
    private String routeCode;
    private String originName;
    private String destName;
    private String trainCode;
    private String trainName;
    private String departAtStr;
    public Trip() {
    }

    public Trip(Integer tripId, Integer routeId, Integer trainId, LocalDateTime departAt, LocalDateTime arriveAt, String status) {
        this.tripId = tripId;
        this.routeId = routeId;
        this.trainId = trainId;
        this.departAt = departAt;
        this.arriveAt = arriveAt;
        this.status = status;
    }

    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public Integer getTrainId() {
        return trainId;
    }

    public void setTrainId(Integer trainId) {
        this.trainId = trainId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
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

    public String getDepartAtStr() {
        return departAtStr;
    }

    public void setDepartAtStr(String departAtStr) {
        this.departAtStr = departAtStr;
    }
    
    /**
     * Helper: trả về departAt dưới dạng java.util.Date để JSP fmt:formatDate xử lý dễ dàng
     */
    public Date getDepartAtDate() {
        if (this.departAt == null) return null;
        try {
            return Date.from(this.departAt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper: trả về arriveAt dưới dạng java.util.Date để JSP fmt:formatDate xử lý dễ dàng
     */
    public Date getArriveAtDate() {
        if (this.arriveAt == null) return null;
        try {
            return Date.from(this.arriveAt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            return null;
        }
    }
    
}
