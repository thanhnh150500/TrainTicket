package vn.ttapp.service;

import vn.ttapp.dao.CarriageDao;
import vn.ttapp.model.Carriage;

import java.sql.SQLException;
import java.util.List;

public class CarriageService {

    private final CarriageDao dao = new CarriageDao();

    /** Dành cho admin: xem toàn bộ các toa trên hệ thống */
    public List<Carriage> findAll() throws SQLException {
        return dao.findAll();
    }

    /** Lấy thông tin toa theo ID */
    public Carriage findById(int id) throws SQLException {
        return dao.findById(id);
    }

    /** Tạo toa mới (Admin) */
    public Integer create(int trainId, String code, int seatClassId, int sortOrder) throws SQLException {
        if (dao.codeExists(trainId, code)) {
            return null; // đã trùng mã toa trong cùng train
        }
        return dao.create(trainId, code, seatClassId, sortOrder);
    }

    /** Cập nhật toa (Admin) */
    public boolean update(Carriage c) throws SQLException {
        // kiểm tra trùng code trong cùng train, ngoại trừ chính nó
        if (dao.codeExistsExceptId(c.getTrainId(), c.getCode(), c.getCarriageId())) {
            return false;
        }
        return dao.update(c) > 0;
    }

    /** Xoá toa (Admin) */
    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }

    /** ✅ Dành cho Customer SeatMap: lấy danh sách toa theo train_id */
    public List<Carriage> findByTrain(int trainId) throws SQLException {
        return dao.findByTrain(trainId);
    }
}
