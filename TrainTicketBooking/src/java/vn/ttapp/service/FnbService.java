/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.service;

import vn.ttapp.dao.FnbCategoryDao;
import vn.ttapp.model.FnbCategory;
import java.sql.SQLException;
import java.util.List;

public class FnbService {

    private final FnbCategoryDao categoryDao = new FnbCategoryDao();

    public List<FnbCategory> getAllCategories() throws SQLException {
        return categoryDao.findAll();
    }

    public FnbCategory getCategoryById(int id) throws SQLException {
        return categoryDao.findById(id);
    }

    public boolean saveCategory(FnbCategory c) throws SQLException {
        if (c.getName() == null || c.getName().isBlank()) {
            return false;
        }
        if (c.getCategoryId() == null) {
            return categoryDao.insert(c.getName().trim(), c.isActive()) > 0;
        } else {
            return categoryDao.update(c) > 0;
        }
    }

    public boolean deleteCategory(int id) throws SQLException {
        return categoryDao.delete(id) > 0;
    }
}
