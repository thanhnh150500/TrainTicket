/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author dotri
 */
public class FareRule {
    public Integer fareRuleId;
    public Integer routeId;
    public Integer seatClassId;
    public BigDecimal basePrice;
    public LocalDate   effectiveFrom;
    public LocalDate   effectiveTo; // nullable
    private String seatClassCode;
    private String seatClassName;
    public FareRule() {
    }

    public FareRule(Integer fareRuleId, Integer routeId, Integer seatClassId, BigDecimal basePrice, LocalDate effectiveFrom, LocalDate effectiveTo) {
        this.fareRuleId = fareRuleId;
        this.routeId = routeId;
        this.seatClassId = seatClassId;
        this.basePrice = basePrice;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

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
    
}
