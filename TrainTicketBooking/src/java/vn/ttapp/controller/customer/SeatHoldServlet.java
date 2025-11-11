package vn.ttapp.controller.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import vn.ttapp.service.SeatService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet(name="SeatHoldServlet", urlPatterns={"/seat/hold"})
public class SeatHoldServlet extends HttpServlet {
    private final SeatService seatService = new SeatService();
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    static class HoldRequest {
        public Integer tripId;
        public List<Integer> seatIds;
        public Integer ttlMinutes;   // optional (default 15)
        public Long bookingId;       // optional: nếu đã có booking draft
    }
    static class HoldResult {
        public int seatId;
        public Integer seatLockId; // null nếu fail
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        HoldRequest body = mapper.readValue(req.getInputStream(), HoldRequest.class);
        
        // validate fl tripid and seatid
        if (body.tripId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "tripId required");
            return;
        }
        if (body.seatIds == null || body.seatIds.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "seatIds required");
            return;
        }
        
        int ttl = (body.ttlMinutes != null ? Math.max(1, Math.min(60, body.ttlMinutes)) : 15);
        
        try {
            List<HoldResult> results = new ArrayList<>(body.seatIds.size());
            for (Integer seatId : body.seatIds) {
                if (seatId == null) continue;
                HoldResult r = new HoldResult();
                r.seatId = seatId;
                r.seatLockId = seatService.holdOneSeat(body.tripId, seatId, body.bookingId, ttl);
                results.add(r);
            }

            Map<String,Object> payload = new HashMap<>();
            payload.put("results", results);
            // thời điểm hết hạn (UTC millis) để FE hẹn giờ tự refresh
            payload.put("expiresAtUtc", System.currentTimeMillis() + ttl * 60_000L);

            resp.setHeader("Cache-Control", "no-store");
            resp.setContentType("application/json; charset=UTF-8");
            mapper.writeValue(resp.getWriter(), payload);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }
}
