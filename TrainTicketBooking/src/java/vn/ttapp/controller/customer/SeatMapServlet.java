package vn.ttapp.controller.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import vn.ttapp.service.SeatService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "SeatMapServlet", urlPatterns = {"/seatmap"})
public class SeatMapServlet extends HttpServlet {

    private final SeatService seatService = new SeatService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String tripIdStr = req.getParameter("tripId");
        if (tripIdStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing tripId");
            return;
        }

        try {
            int tripId = Integer.parseInt(tripIdStr);

            // Dành cho customer → có cả trạng thái ghế
            var seats = seatService.getSeatMapWithAvailability(tripId);

            resp.setContentType("application/json; charset=UTF-8");
            mapper.writeValue(resp.getWriter(), seats);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tripId format");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }
}
