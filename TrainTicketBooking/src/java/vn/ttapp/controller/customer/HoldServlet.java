package vn.ttapp.controller.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import vn.ttapp.service.SeatService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/api/hold")
public class HoldServlet extends HttpServlet {

    private final SeatService seatService = new SeatService();
    private final ObjectMapper mapper = new ObjectMapper();

    public static class HoldRequest {

        public int tripId;
        public List<Integer> seatIds;
    }

    public static class HoldResponse {

        public boolean ok;
        public List<Integer> lockIds;
        public String error;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        HoldResponse out = new HoldResponse();

        try {
            HoldRequest body = mapper.readValue(req.getReader(), HoldRequest.class);
            if (body == null || body.seatIds == null || body.seatIds.isEmpty()) {
                out.ok = false;
                out.error = "Missing seats";
                mapper.writeValue(resp.getWriter(), out);
                return;
            }

            List<Integer> locks = seatService.holdSeats(body.tripId, body.seatIds, null, 5);
            if (locks.isEmpty()) {
                out.ok = false;
                out.error = "Some seats are locked or booked.";
            } else {
                out.ok = true;
                out.lockIds = locks;
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.ok = false;
            out.error = e.getMessage();
        }
        mapper.writeValue(resp.getWriter(), out);
    }
}
