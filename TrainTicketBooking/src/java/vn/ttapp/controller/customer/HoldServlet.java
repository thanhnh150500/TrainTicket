package vn.ttapp.controller.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import vn.ttapp.service.SeatService;
import com.fasterxml.jackson.databind.SerializationFeature;

import vn.ttapp.model.HoldDtos.HoldRequest;
import vn.ttapp.model.HoldDtos.HoldResponse;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import vn.ttapp.model.HoldDtos;

@WebServlet(name = "HoldServlet", urlPatterns = {"/api/hold"})
public class HoldServlet extends HttpServlet {

    private final SeatService seatService = new SeatService();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Lưu tập lockId đã giữ trong session để tiện release hàng loạt
    @SuppressWarnings("unchecked")
    private static Set<Integer> sessionLockSet(HttpSession ss) {
        Object o = ss.getAttribute("seatLockIds");
        if (o instanceof Set) {
            return (Set<Integer>) o;
        }
        Set<Integer> s = new HashSet<>();
        ss.setAttribute("seatLockIds", s);
        return s;
    }

    /* ======================= POST /api/hold ======================= */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        HoldResponse out = new HoldResponse();
        out.setNow(Instant.now().toString());

        try {
            HoldRequest body = mapper.readValue(req.getReader(), HoldRequest.class);

            if (body == null || body.getTripId() == null || body.getTripId() <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.setOk(false);
                out.setError("Missing or invalid tripId");
                mapper.writeValue(resp.getWriter(), out);
                return;
            }
            if (body.getSeatIds() == null || body.getSeatIds().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.setOk(false);
                out.setError("Missing seats");
                mapper.writeValue(resp.getWriter(), out);
                return;
            }

            // Lọc trùng & giới hạn số ghế (ví dụ tối đa 6)
            LinkedHashSet<Integer> unique = new LinkedHashSet<>(body.getSeatIds());
            if (unique.size() > 6) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.setOk(false);
                out.setError("Too many seats (max 6)");
                mapper.writeValue(resp.getWriter(), out);
                return;
            }

            int ttl = (body.getTtlMinutes() == null || body.getTtlMinutes() < 1 || body.getTtlMinutes() > 30)
                    ? 5 : body.getTtlMinutes();
            out.setTtlMinutes(ttl);

            // Gọi service giữ ghế (DAO all-or-nothing: xung đột -> danh sách rỗng)
            List<Integer> seatIds = new ArrayList<>(unique);
            List<Integer> lockIds = seatService.holdSeats(body.getTripId(), seatIds, null, ttl);

            if (lockIds == null || lockIds.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT); // 409
                out.setOk(false);
                out.setConflicts(seatIds); // muốn chi tiết seat nào conflict thì có thể truy vấn riêng
                out.setError("Seats are already held or booked.");
            } else {
                Instant expires = Instant.now().plus(ttl, ChronoUnit.MINUTES);

                List<HoldDtos.HeldItem> heldList = new ArrayList<>(lockIds.size());
                for (int i = 0; i < lockIds.size(); i++) {
                    heldList.add(new HoldDtos.HeldItem(seatIds.get(i), lockIds.get(i), expires.toString()));
                }
                out.setOk(true);
                out.setHeld(heldList);

                // Lưu lockIds vào session
                HttpSession ss = req.getSession(true);
                sessionLockSet(ss).addAll(lockIds);
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.setOk(false);
            out.setError(e.getMessage());
        }

        mapper.writeValue(resp.getWriter(), out);
    }

    /* ====================== DELETE /api/hold ====================== */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        HoldDtos.ReleaseResponse out = new HoldDtos.ReleaseResponse();

        try {
            HoldDtos.ReleaseRequest body = null;
            if (req.getReader() != null) {
                try {
                    body = mapper.readValue(req.getReader(), HoldDtos.ReleaseRequest.class);
                } catch (Exception ignore) {
                    /* body có thể trống */ }
            }

            HttpSession ss = req.getSession(false);
            Set<Integer> sessionLocks = (ss != null) ? sessionLockSet(ss) : new HashSet<>();

            List<Integer> toRelease;
            if (body != null && body.getLockIds() != null && !body.getLockIds().isEmpty()) {
                toRelease = body.getLockIds();
            } else {
                toRelease = new ArrayList<>(sessionLocks);
            }

            if (toRelease.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.setOk(false);
                out.setError("No lockIds to release");
                mapper.writeValue(resp.getWriter(), out);
                return;
            }

            int released = 0;
            for (Integer lid : toRelease) {
                if (lid != null && seatService.releaseLock(lid)) {
                    released++;
                    sessionLocks.remove(lid);
                }
            }
            out.setOk(true);
            out.setReleased(released);

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.setOk(false);
            out.setError(e.getMessage());
        }

        mapper.writeValue(resp.getWriter(), out);
    }
}
