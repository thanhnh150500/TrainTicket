package vn.ttapp.service;

import vn.ttapp.dao.SeatDao;
import vn.ttapp.model.Seat;
import vn.ttapp.model.SeatView;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SeatService {

    private final SeatDao dao = new SeatDao();
    /* ========= Admin/CRUD ========= */
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
        if (dao.codeExistsExceptId(s.getCarriageId(), s.getCode(), s.getSeatId())) {
            return false;
        }
        return dao.update(s) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id) > 0;
    }

    /* ========= Customer / Seat Map ========= */
    public List<Seat> findByTripId(int tripId) throws SQLException {
        return dao.findByTripId(tripId);
    }

    /**
     * Lấy seat map cho toàn bộ toa của train theo trip. - Nếu DB đã có
     * row_no/col_no => giữ nguyên. - Nếu thiếu => tự "ren" row/col theo layout
     * mặc định theo seatClassId.
     */
    public List<SeatView> getSeatMapWithAvailabilityForTrain(int tripId, int trainId) throws SQLException {
        List<SeatView> list = dao.getSeatMapWithAvailabilityForTrain(tripId, trainId);
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        // Group theo toa
        Map<Integer, List<SeatView>> byCar = list.stream()
                .collect(Collectors.groupingBy(v -> v.carriageId, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<SeatView>> e : byCar.entrySet()) {
            List<SeatView> seats = e.getValue();

            // sort "tự nhiên": ưu tiên số trong seatCode; fallback seatId
            seats.sort((a, b) -> {
                int na = codeNumber(a.seatCode);
                int nb = codeNumber(b.seatCode);
                if (na != nb) {
                    return Integer.compare(na, nb);
                }
                // nếu cùng số -> so sánh chuỗi để ổn định
                String sa = a.seatCode != null ? a.seatCode : "";
                String sb = b.seatCode != null ? b.seatCode : "";
                int c = sa.compareToIgnoreCase(sb);
                return (c != 0) ? c : Integer.compare(a.seatId, b.seatId);
            });

            // ren row/col cho GHẾ THIẾU row/col; ghế đã có thì giữ nguyên
            int rn = 0;
            for (SeatView v : seats) {
                boolean missing = (v.rowNo == null || v.colNo == null);
                if (!missing) {
                    continue;
                }

                rn += 1; // stt trong toa (chỉ đếm ghế thiếu để không đụng ghế đã có)
                int seatClassId = (v.seatClassId != null) ? v.seatClassId : 0;

                LayoutRule rule = layoutForClass(seatClassId);
                int row = ((rn - 1) / rule.seatsPerRow) + 1;
                int pos = ((rn - 1) % rule.seatsPerRow) + 1;
                int col = mapPosToCol(rule, pos);

                v.rowNo = row;
                v.colNo = col;
            }
        }

        // Trả list theo order ổn định: carriageId, row, col, seatId
        list.sort(Comparator
                .comparingInt((SeatView v) -> v.carriageId)
                .thenComparingInt(v -> nullSafe(v.rowNo))
                .thenComparingInt(v -> nullSafe(v.colNo))
                .thenComparingInt(v -> v.seatId));
        return list;
    }

    public boolean seatBelongsToTrip(int seatId, int tripId) throws SQLException {
        if (seatId <= 0 || tripId <= 0) {
            return false;
        }
        return dao.seatBelongsToTrip(seatId, tripId);
    }

    public Integer holdOneSeat(int tripId, int seatId, Long bookingIdOrNull, int ttlMinutes) throws SQLException {
        if (!seatBelongsToTrip(seatId, tripId)) {
            return null;
        }
    List<Integer> lockIds = dao.lockSeats(tripId, Collections.singletonList(seatId), bookingIdOrNull, ttlMinutes);
    return (lockIds != null && !lockIds.isEmpty()) ? lockIds.get(0) : null;
    }

    public List<Integer> holdSeats(int tripId, List<Integer> seatIds, Long bookingIdOrNull, int ttlMinutes) throws SQLException {
        if (seatIds == null || seatIds.isEmpty()) {
            return List.of();
        }
        for (Integer sid : seatIds) {
            if (sid == null || sid <= 0 || !seatBelongsToTrip(sid, tripId)) {
                return List.of();
            }
        }
        return dao.lockSeats(tripId, seatIds, bookingIdOrNull, ttlMinutes);
    }

    public boolean releaseLock(int seatLockId) throws SQLException {
        return dao.releaseLock(seatLockId) > 0;
    }

    public List<Seat> getSeatMap(int tripId) throws SQLException {
        return findByTripId(tripId);
    }

    /* ======================== helpers ======================== */
    private static int nullSafe(Integer x) {
        return (x != null) ? x : Integer.MAX_VALUE;
    }

    private static int codeNumber(String seatCode) {
        if (seatCode == null) {
            return Integer.MAX_VALUE / 2;
        }
        String s = seatCode;
        // lấy số đầu tiên trong mã ghế
        StringBuilder num = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isDigit(ch)) {
                num.append(ch);
            } else if (num.length() > 0) {
                break;
            }
        }
        if (num.length() == 0) {
            return Integer.MAX_VALUE / 2;
        }
        try {
            return Integer.parseInt(num.toString());
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE / 2;
        }
    }

    /**
     * Quy tắc layout theo seatClassId
     */
    private static LayoutRule layoutForClass(int seatClassId) {
        // Mặc định: 2–2 (không aisle)
        if (seatClassId == 1) {
            // 2–aisle–2
            return LayoutRule.TWO_AISLE_TWO;
        } else if (seatClassId == 2) {
            // 3–aisle–3
            return LayoutRule.THREE_AISLE_THREE;
        } else {
            // 2–2
            return LayoutRule.TWO_TWO;
        }
    }

    private static int mapPosToCol(LayoutRule rule, int pos) {
        // pos: 1..seatsPerRow
        if (rule == LayoutRule.TWO_AISLE_TWO) {
            // 4 ghế/hàng; cột 3 là aisle → 1,2,4,5
            switch (pos) {
                case 1:
                    return 1;
                case 2:
                    return 2;
                case 3:
                    return 4;
                case 4:
                    return 5;
                default:
                    return pos;
            }
        } else if (rule == LayoutRule.THREE_AISLE_THREE) {
            // 6 ghế/hàng; cột 4 là aisle → 1,2,3,5,6,7
            switch (pos) {
                case 1:
                    return 1;
                case 2:
                    return 2;
                case 3:
                    return 3;
                case 4:
                    return 5;
                case 5:
                    return 6;
                case 6:
                    return 7;
                default:
                    return pos;
            }
        } else {
            // 2–2: 1,2,3,4 (không aisle)
            return pos;
        }
    }

    private enum LayoutRule {
        TWO_AISLE_TWO(4),
        THREE_AISLE_THREE(6),
        TWO_TWO(4);

        final int seatsPerRow;

        LayoutRule(int seatsPerRow) {
            this.seatsPerRow = seatsPerRow;
        }
    }
    
    public List<SeatView> getSeatMapWithAvailability(int tripId) throws SQLException {
        // ✅ YÊU CẦU: SeatDao.getSeatsWithAvailability cũng trả List<SeatView>
        return dao.getSeatsWithAvailability(tripId);
    }

    /**
     * Tìm ghế theo tripId và mã ghế (code). Trả về Seat hoặc null.
     */
    public vn.ttapp.model.Seat findByTripAndCode(int tripId, String code) throws SQLException {
        if (code == null || code.isBlank()) return null;
        return dao.findByTripAndCode(tripId, code.trim());
    }
}
