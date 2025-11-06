
package vn.ttapp.service;

import vn.ttapp.dao.CarriageDao;
import vn.ttapp.dao.SeatClassDao;
import vn.ttapp.dao.TrainDao;
import vn.ttapp.model.Carriage;

import java.sql.SQLException;
import java.util.List;

public class CarriageService {

    private final CarriageDao dao = new CarriageDao();
    private final TrainDao trainDao = new TrainDao();
    private final SeatClassDao seatClassDao = new SeatClassDao();

    public List<Carriage> findAll() throws SQLException {
        return dao.findAll();
    }

    public Carriage findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public Integer create(Integer trainId, String code, Integer seatClassId, Integer sortOrder) throws SQLException {
        // basic validate
        if (trainId == null || seatClassId == null || code == null || code.isBlank()) {
            return null;
        }
        if (!trainDao.existsById(trainId)) {
            return null;
        }
        if (!seatClassDao.existsById(seatClassId)) {
            return null;
        }

        code = code.trim(); // hoặc code = code.trim().toUpperCase();
        if (sortOrder == null) {
            sortOrder = 0;
        }

        if (dao.findByTrainAndCode(trainId, code) != null) {
            return null;
        }
        return dao.create(trainId, code, seatClassId, sortOrder);
    }

    public boolean update(Carriage c) throws SQLException {
        if (c.getCarriageId() == null || c.getTrainId() == null
                || c.getSeatClassId() == null || c.getCode() == null || c.getCode().isBlank()) {
            return false;
        }
        if (!trainDao.existsById(c.getTrainId())) {
            return false;
        }
        if (!seatClassDao.existsById(c.getSeatClassId())) {
            return false;
        }

        String code = c.getCode().trim(); // hoặc .toUpperCase()
        c.setCode(code);
        if (c.getSortOrder() == null) {
            c.setSortOrder(0);
        }

        Carriage dup = dao.findByTrainAndCode(c.getTrainId(), code);
        if (dup != null && !dup.getCarriageId().equals(c.getCarriageId())) {
            return false; // trùng code trong cùng train
        }
        return dao.update(c) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }
}
