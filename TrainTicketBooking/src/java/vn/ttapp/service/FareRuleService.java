package vn.ttapp.service;

import vn.ttapp.dao.FareRuleDao;
import vn.ttapp.model.FareRule;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class FareRuleService {

    private final FareRuleDao dao = new FareRuleDao();

    public List<FareRule> findAll() throws SQLException {
        return dao.findAll();
    }

    public FareRule findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public Integer create(int routeId, int seatClassId, BigDecimal price, LocalDate from, LocalDate to) throws SQLException {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (from == null) {
            return null;
        }
        if (to != null && to.isBefore(from)) {
            return null;
        }
        return dao.create(routeId, seatClassId, price, from, to);
    }

    public boolean update(FareRule f) throws SQLException {
        if (f.getBasePrice() == null || f.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (f.getEffectiveFrom() == null) {
            return false;
        }
        if (f.getEffectiveTo() != null && f.getEffectiveTo().isBefore(f.getEffectiveFrom())) {
            return false;
        }
        return dao.update(f) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }
}
