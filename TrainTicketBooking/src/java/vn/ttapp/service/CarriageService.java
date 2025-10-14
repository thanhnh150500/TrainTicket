package vn.ttapp.service;

import vn.ttapp.dao.CarriageDao;
import vn.ttapp.model.Carriage;

import java.sql.SQLException;
import java.util.List;

public class CarriageService {

    private final CarriageDao dao = new CarriageDao();

    public List<Carriage> findAll() throws SQLException {
        return dao.findAll();
    }

    public Carriage findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public Integer create(int trainId, String code, int seatClassId, int sortOrder) throws SQLException {
        if (dao.codeExists(trainId, code)) {
            return null;
        }
        return dao.create(trainId, code, seatClassId, sortOrder);
    }

    public boolean update(Carriage c) throws SQLException {
        // đảm bảo không trùng code trong cùng train
        if (dao.codeExists(c.getTrainId(), c.getCode())) {
            Carriage existed = dao.findById(c.getCarriageId());
            if (existed == null || !(existed.getTrainId().equals(c.getTrainId()) && existed.getCode().equals(c.getCode()))) {
                return false;
            }
        }
        return dao.update(c) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }
}
