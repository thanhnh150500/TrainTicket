/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.model;

/**
 *
 * @author dotri
 */
public class Route {
    private Integer routeId;
    private Integer originStationId;
    private Integer destStationId;
    private String code;
    private String originName;
    private String destName;
    public Route() {
    }

    public Route(Integer routeId, Integer originStationId, Integer destStationId, String code) {
        this.routeId = routeId;
        this.originStationId = originStationId;
        this.destStationId = destStationId;
        this.code = code;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
