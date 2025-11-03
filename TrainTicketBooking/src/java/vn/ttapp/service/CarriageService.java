package vn.ttapp.service;

import vn.ttapp.dao.CarriageDao;
import vn.ttapp.model.Carriage;

import java.sql.SQLException;
import java.util.List;
import vn.ttapp.dao.SeatDao;
import vn.ttapp.model.SeatView;

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
            return null; // đã trùng mã toa trong cùng train
        }
        return dao.create(trainId, code, seatClassId, sortOrder);
    }

    public boolean update(Carriage c) throws SQLException {
        // kiểm tra trùng code trong cùng train, ngoại trừ chính nó
        if (dao.codeExistsExceptId(c.getTrainId(), c.getCode(), c.getCarriageId())) {
            return false;
        }
        return dao.update(c) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }

    public List<Carriage> findByTrain(int trainId) throws SQLException {
        return dao.findByTrain(trainId);
    }

}
