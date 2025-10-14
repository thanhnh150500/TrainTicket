package vn.ttapp.service;

import vn.ttapp.dao.SeatClassDao;
import vn.ttapp.model.SeatClass;

import java.sql.SQLException;
import java.util.List;

public class SeatClassService {

    private final SeatClassDao dao = new SeatClassDao();

    public List<SeatClass> findAll() throws SQLException {
        return dao.findAll();
    }

    public SeatClass findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public Integer create(String code, String name) throws SQLException {
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            return null;
        }
        if (dao.codeExists(code)) {
            return null;
        }
        return dao.create(code.trim(), name.trim());
    }

    public boolean update(SeatClass sc) throws SQLException {
        SeatClass existed = dao.findByCode(sc.getCode());
        if (existed != null && !existed.getSeatClassId().equals(sc.getSeatClassId())) {
            return false;
        }
        return dao.update(sc) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }
}
