/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.model;

public class FnbCategory {

    private Integer categoryId;
    private String name;
    private boolean isActive;

    public FnbCategory() {
    }

    public FnbCategory(Integer categoryId, String name, boolean isActive) {
        this.categoryId = categoryId;
        this.name = name;
        this.isActive = isActive;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
