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
        if (dao.codeExists(code)) return null;
        return dao.create(code, name);
    }
    public boolean update(Train t) throws SQLException {
        Train existed = dao.findByCode(t.getCode());
        if (existed != null && existed.getTrainId() != t.getTrainId()) {
            return false;
        }
        return dao.update(t) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }
}
