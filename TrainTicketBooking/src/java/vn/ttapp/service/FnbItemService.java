/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.service;

import vn.ttapp.dao.FnbItemDao;
import vn.ttapp.dao.FnbCategoryDao;
import vn.ttapp.model.FnbItem;
import java.sql.SQLException;
import java.util.List;

public class FnbItemService {
    private final FnbItemDao dao = new FnbItemDao();
    private final FnbCategoryDao catDao = new FnbCategoryDao();

    public List<FnbItem> getAll() throws SQLException {
        return dao.findAll();
    }

    public FnbItem getById(int id) throws SQLException {
        return dao.findById(id);
    }

    public boolean save(FnbItem x) throws SQLException {
        if (x.getName() == null || x.getName().isBlank() || x.getCode() == null || x.getCode().isBlank())
            return false;
        x.setCode(x.getCode().trim().toUpperCase());

        if (x.getItemId() == null) {
            if (dao.codeExists(x.getCode())) return false;
            return dao.insert(x) > 0;
        } else {
            return dao.update(x) > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }
}

