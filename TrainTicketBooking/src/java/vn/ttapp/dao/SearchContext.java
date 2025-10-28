/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.dao;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author New User
 */
public class SearchContext implements Serializable {

    private String tripType;          // ONEWAY / ROUNDTRIP
    private Integer originStationId;
    private Integer destStationId;
    private String originName;
    private String destName;
    private LocalDate departDate;
    private LocalTime departTime;     // optional
    private LocalDate returnDate;     // optional

    // ===== GETTER / SETTER =====
    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public Integer getOriginStationId() {
        return originStationId;
    }

    public void setOriginStationId(Integer originStationId) {
        this.originStationId = originStationId;
    }

    public Integer getDestStationId() {
        return destStationId;
    }

    public void setDestStationId(Integer destStationId) {
        this.destStationId = destStationId;
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

    public LocalDate getDepartDate() {
        return departDate;
    }

    public void setDepartDate(LocalDate departDate) {
        this.departDate = departDate;
    }

    public LocalTime getDepartTime() {
        return departTime;
    }

    public void setDepartTime(LocalTime departTime) {
        this.departTime = departTime;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
}
