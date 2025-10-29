package vn.ttapp.service;

import vn.ttapp.dao.SeatDao;
import vn.ttapp.dao.CarriageDao;
import vn.ttapp.dao.SeatClassDao;
import vn.ttapp.model.Seat;

import java.sql.SQLException;
import java.util.List;

public class SeatService {

    private final SeatDao dao = new SeatDao();
    private final CarriageDao carriageDao = new CarriageDao();
    private final SeatClassDao seatClassDao = new SeatClassDao();

    public List<Seat> findAll() throws SQLException {
        return dao.findAll();
    }

    public Seat findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public Integer create(int carriageId, String code, int seatClassId, String positionInfo) throws SQLException {
        if (!carriageDao.existsById(carriageId) || !seatClassDao.existsById(seatClassId)) {
            return null;
        }

        String normCode = normalizeCode(code);
        String normPos = normalizePos(positionInfo);
        if (normCode == null) {
            return null;
        }

        if (dao.codeExists(carriageId, normCode)) {
            return null;
        }
        return dao.create(carriageId, normCode, seatClassId, normPos);
    }

    public boolean update(Seat s) throws SQLException {
        if (s.getSeatId() == null || s.getCarriageId() == null || s.getSeatClassId() == null) {
            return false;
        }
        if (!carriageDao.existsById(s.getCarriageId()) || !seatClassDao.existsById(s.getSeatClassId())) {
            return false;
        }

        String normCode = normalizeCode(s.getCode());
        String normPos = normalizePos(s.getPositionInfo());
        if (normCode == null) {
            return false;
        }

        Seat conflict = dao.findByCarriageAndCode(s.getCarriageId(), normCode);
        if (conflict != null && !conflict.getSeatId().equals(s.getSeatId())) {
            return false; // trùng với ghế khác trong cùng toa
        }

        s.setCode(normCode);
        s.setPositionInfo(normPos);
        return dao.update(s) > 0;
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
            return null; // ví dụ: 01A, 12-B, 07_A
        }
        return s;
    }

    private String normalizePos(String pos) {
        if (pos == null) {
            return null;
        }
        String s = pos.trim();
        return s.isEmpty() ? null : (s.length() > 50 ? s.substring(0, 50) : s);
    }
}
