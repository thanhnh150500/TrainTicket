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
        String normCode = normalizeCode(code);
        String normName = normalizeName(name);
        if (normCode == null || normName == null) {
            return null;
        }

        if (dao.codeExists(normCode)) {
            return null;
        }
        return dao.create(normCode, normName);
    }

    public boolean update(SeatClass sc) throws SQLException {
        if (sc.getSeatClassId() == null) {
            return false;
        }
        String normCode = normalizeCode(sc.getCode());
        String normName = normalizeName(sc.getName());
        if (normCode == null || normName == null) {
            return false;
        }

        SeatClass existed = dao.findByCode(normCode);
        if (existed != null && !existed.getSeatClassId().equals(sc.getSeatClassId())) {
            return false; // trùng code ở bản ghi khác
        }
        sc.setCode(normCode);
        sc.setName(normName);
        return dao.update(sc) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }

    // ---- helpers ----
    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String s = code.trim().toUpperCase();
        if (s.isEmpty() || s.length() > 20) {
            return null;
        }
        if (!s.matches("[A-Z0-9\\-_]+")) {
            return null; // ví dụ: VIP, STD_A
        }
        return s;
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String s = name.trim();
        if (s.isEmpty() || s.length() > 100) {
            return null;
        }
        return s;
    }
}
