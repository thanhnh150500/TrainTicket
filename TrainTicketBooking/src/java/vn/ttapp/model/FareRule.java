package vn.ttapp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FareRule {

    private Integer fareRuleId;
    private Integer routeId;
    private Integer seatClassId;
    private BigDecimal basePrice;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo; // nullable

    // view-only fields
    private String seatClassCode;
    private String seatClassName;
    private String routeCode;   // <- nên thêm để hiển thị
    private String originName;  // <- tuỳ chọn
    private String destName;    // <- tuỳ chọn

    public FareRule() {
    }

    public FareRule(Integer fareRuleId, Integer routeId, Integer seatClassId,
            BigDecimal basePrice, LocalDate effectiveFrom, LocalDate effectiveTo) {
        this.fareRuleId = fareRuleId;
        this.routeId = routeId;
        this.seatClassId = seatClassId;
        this.basePrice = basePrice;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    // getters/setters (giữ như cũ + thêm 3 field mới)
    public Integer getFareRuleId() {
        return fareRuleId;
    }

    public void setFareRuleId(Integer fareRuleId) {
        this.fareRuleId = fareRuleId;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public Integer getSeatClassId() {
        return seatClassId;
    }

    public void setSeatClassId(Integer seatClassId) {
        this.seatClassId = seatClassId;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
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
}
