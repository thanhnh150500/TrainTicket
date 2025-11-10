package vn.ttapp.model;

public class Station {

    private Integer stationId;
    private Integer cityId;
    private String code;
    private String name;
    private String address;
    private String cityName; // từ join với City

    public Station() {
    }

    public Station(Integer stationId, Integer cityId, String code, String name, String address) {
        this.stationId = stationId;
        this.cityId = cityId;
        this.code = code;
        this.name = name;
        this.address = address;
    }

    public Integer getStationId() {
        return stationId;
    }

    public void setStationId(Integer stationId) {
        this.stationId = stationId;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public String toString() {
        return "Station{"
                + "stationId=" + stationId
                + ", cityId=" + cityId
                + ", code='" + code + '\''
                + ", name='" + name + '\''
                + ", address='" + address + '\''
                + ", cityName='" + cityName + '\''
                + '}';
    }
}
