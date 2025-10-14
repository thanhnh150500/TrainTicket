package vn.ttapp.service;

import vn.ttapp.dao.SeatDao;
import vn.ttapp.model.Seat;

import java.sql.SQLException;
import java.util.List;

public class SeatService {

    private final SeatDao dao = new SeatDao();

    public List<Seat> findAll() throws SQLException {
        return dao.findAll();
    }

    public Seat findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public Integer create(int carriageId, String code, int seatClassId, String positionInfo) throws SQLException {
        if (dao.codeExists(carriageId, code)) {
            return null;
        }
        return dao.create(carriageId, code, seatClassId, positionInfo);
    }

    public boolean update(Seat s) throws SQLException {
        if (dao.codeExists(s.getCarriageId(), s.getCode())) {
            Seat existed = dao.findById(s.getSeatId());
            if (existed == null || !(existed.getCarriageId().equals(s.getCarriageId()) && existed.getCode().equals(s.getCode()))) {
                return false;
            }
        }
        return dao.update(s) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }
}
