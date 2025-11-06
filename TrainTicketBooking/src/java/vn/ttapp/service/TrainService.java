package vn.ttapp.service;

import vn.ttapp.dao.TrainDao;
import vn.ttapp.model.Train;

import java.sql.SQLException;
import java.util.List;

public class TrainService {

    private final TrainDao dao = new TrainDao();

    public Train findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public List<Train> findAll() throws SQLException {
        return dao.findAll();
    }

    public Integer create(String code, String name) throws SQLException {
        String normCode = normalizeCode(code);
        String normName = normalizeName(name);
        if (normCode == null || normName == null) {
            return null;
        }
        if (dao.codeExists(normCode)) {
            return null; // đã tồn tại code
        }
        return dao.create(normCode, normName);
    }

    public boolean update(Train t) throws SQLException {
        if (t == null || t.getTrainId() == null) {
            return false;
        }
        String normCode = normalizeCode(t.getCode());
        String normName = normalizeName(t.getName());
        if (normCode == null || normName == null) {
            return false;
        }

        // code không được trùng với train khác
        Train existed = dao.findByCode(normCode);
        if (existed != null && !existed.getTrainId().equals(t.getTrainId())) {
            return false;
        }

        t.setCode(normCode);
        t.setName(normName);
        return dao.update(t) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }

    // --- helpers ---
    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String s = code.trim().toUpperCase();
        if (s.isEmpty() || s.length() > 40) {
            return null;
        }
        // Optional: kiểm tra pattern: s.matches("[A-Z0-9\\-]+")
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
