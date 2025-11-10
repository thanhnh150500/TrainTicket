package vn.ttapp.model;

/**
 * City model khớp bảng dbo.City: - city_id (INT, PK) - region_id (INT, FK ->
 * Region) - code (NVARCHAR(20)) - name (NVARCHAR(100))
 */
public class City {

    private Integer cityId;
    private Integer regionId;
    private String code;
    private String name;

    public City() {
    }

    public City(Integer cityId, String name) {
        this.cityId = cityId;
        this.name = name;
    }

    public City(Integer cityId, Integer regionId, String code, String name) {
        this.cityId = cityId;
        this.regionId = regionId;
        this.code = code;
        this.name = name;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Integer getRegionId() {
        return regionId;
    }

    public void setRegionId(Integer regionId) {
        this.regionId = regionId;
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

    /* Optional tiện lợi cho debug/log */
    @Override
    public String toString() {
        return "City{"
                + "cityId=" + cityId
                + ", regionId=" + regionId
                + ", code='" + code + '\''
                + ", name='" + name + '\''
                + '}';
    }
}
