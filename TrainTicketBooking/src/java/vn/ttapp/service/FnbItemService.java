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
    private final FnbCategoryDao catDao = new FnbCategoryDao(); // (Giả sử bạn có DAO này)

    public List<FnbItem> getAll() throws SQLException {
        // Hàm findAll() trong DAO đã được sửa
        return dao.findAll();
    }

    public FnbItem getById(int id) throws SQLException {
        return dao.findById(id);
    }

    public boolean save(FnbItem x) throws SQLException {
        if (x.getName() == null || x.getName().isBlank() || x.getCode() == null || x.getCode().isBlank())
            return false;
        x.setCode(x.getCode().trim().toUpperCase());

        // Logic không cần đổi, vì 'x' đã chứa image_url
        if (x.getItemId() == null) {
            // Kiểm tra code trùng khi thêm mới
            if (dao.codeExists(x.getCode())) return false;
            return dao.insert(x) > 0;
        } else {
            // (Lưu ý: Bạn có thể thêm logic kiểm tra code trùng khi update nếu cần)
            return dao.update(x) > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }
    public List<FnbItem> getAllActive() throws SQLException {
        // Gọi hàm DAO mới
        return dao.findAllActive();
    }
}