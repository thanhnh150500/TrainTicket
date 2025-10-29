/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.model;

public class FnbItem {

    private Integer itemId;
    private Integer categoryId;
    private String code;
    private String name;
    private double price;
    private boolean isActive;

    // Dùng khi join để hiển thị
    private String categoryName;

    public FnbItem() {
    }

    public FnbItem(Integer itemId, Integer categoryId, String code, String name, double price, boolean isActive) {
        this.itemId = itemId;
        this.categoryId = categoryId;
        this.code = code;
        this.name = name;
        this.price = price;
        this.isActive = isActive;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
