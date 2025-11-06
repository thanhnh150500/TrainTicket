package vn.ttapp.dao;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class SearchContext implements Serializable {

    private String tripType;          // ONEWAY / ROUNDTRIP
    private Integer originStationId;
    private Integer destStationId;
    private String originName;
    private String destName;
    private LocalDate departDate;
    private LocalTime departTime;     // optional
    private LocalDate returnDate;     // optional

    // 👇 THÊM MỚI
    private Integer pax = 1;          // mặc định 1 khách

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

    // 👇 GETTER/SETTER CHO PAX
    public Integer getPax() {
        return pax;
    }

    public void setPax(Integer pax) {
        this.pax = (pax == null || pax < 1) ? 1 : pax;
    }

    // (tuỳ chọn) alias tiện cho JSP/JS
    public Integer getOriginId() {
        return originStationId;
    }

    public Integer getDestId() {
        return destStationId;
    }
}
