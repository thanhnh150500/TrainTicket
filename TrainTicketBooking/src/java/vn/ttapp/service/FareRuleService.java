package vn.ttapp.service;

import vn.ttapp.dao.FareRuleDao;
import vn.ttapp.dao.RouteDao;
import vn.ttapp.dao.SeatClassDao;
import vn.ttapp.model.FareRule;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class FareRuleService {

    private final FareRuleDao dao = new FareRuleDao();
    private final RouteDao routeDao = new RouteDao();
    private final SeatClassDao seatClassDao = new SeatClassDao();

    public List<FareRule> findAll() throws SQLException {
        return dao.findAll();
    }

    public FareRule findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public Integer create(int routeId, int seatClassId, BigDecimal price, LocalDate from, LocalDate to) throws SQLException {
        if (!basicValidate(routeId, seatClassId, price, from, to)) {
            return null;
        }
        price = price.setScale(2, BigDecimal.ROUND_HALF_UP);
        // chồng lấn?
        if (dao.hasOverlappingPeriod(null, routeId, seatClassId, from, to)) {
            return null;
        }
        return dao.create(routeId, seatClassId, price, from, to);
    }

    public boolean update(FareRule f) throws SQLException {
        if (f.getFareRuleId() == null) {
            return false;
        }
        if (!basicValidate(f.getRouteId(), f.getSeatClassId(), f.getBasePrice(), f.getEffectiveFrom(), f.getEffectiveTo())) {
            return false;
        }
        f.setBasePrice(f.getBasePrice().setScale(2, BigDecimal.ROUND_HALF_UP));
        if (dao.hasOverlappingPeriod(f.getFareRuleId(), f.getRouteId(), f.getSeatClassId(), f.getEffectiveFrom(), f.getEffectiveTo())) {
            return false;
        }
        return dao.update(f) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }

    private boolean basicValidate(Integer routeId, Integer seatClassId, BigDecimal price, LocalDate from, LocalDate to) throws SQLException {
        if (routeId == null || seatClassId == null || price == null || from == null) {
            return false;
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (to != null && to.isBefore(from)) {
            return false;
        }
        // validate FK
        if (!routeDao.existsById(routeId)) {
            return false;
        }
        if (!seatClassDao.existsById(seatClassId)) {
            return false;
        }
        return true;
    }
}
