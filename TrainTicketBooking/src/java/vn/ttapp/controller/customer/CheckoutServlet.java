// src/java/vn/ttapp/controller/customer/CheckoutServlet.java
package vn.ttapp.controller.customer;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

import vn.ttapp.model.SeatSelection;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private static final BigDecimal SERVICE_FEE = new BigDecimal("10000");
    private static final BigDecimal INSURANCE_PER_PAX = new BigDecimal("1000");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        int tripId = Integer.parseInt(req.getParameter("tripId"));

        String[] seatIds = req.getParameterValues("seatId[]");
        String[] seatCodes = req.getParameterValues("seatCode[]");
        String[] prices = req.getParameterValues("price[]");
        String[] seatClassNm = req.getParameterValues("seatClassName[]"); // nếu có
        // nhận cả 2 tên: carriageId[] hoặc carriageNo[] (JS của bạn có thể gửi 1 trong 2)
        String[] carriageIds = Optional.ofNullable(req.getParameterValues("carriageId[]"))
                .orElse(req.getParameterValues("carriageNo[]"));

        if (seatIds == null || seatIds.length == 0) {
            res.sendRedirect(req.getContextPath() + "/seatmap?tripId=" + tripId);
            return;
        }

        List<SeatSelection> selected = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (int i = 0; i < seatIds.length; i++) {
            Integer seatId = Integer.valueOf(seatIds[i]);
            Integer carriageId = (carriageIds != null && carriageIds.length > i && carriageIds[i] != null && !carriageIds[i].isBlank())
                    ? Integer.valueOf(carriageIds[i])
                    : null;
            String code = (seatCodes != null && seatCodes.length > i) ? seatCodes[i] : "";
            String clsName = (seatClassNm != null && seatClassNm.length > i) ? seatClassNm[i] : "";

            // giá từ client (int theo model bạn)
            int priceInt = new BigDecimal(prices[i]).intValue();
            subtotal = subtotal.add(new BigDecimal(priceInt));

            SeatSelection s = new SeatSelection(seatId, carriageId, code, clsName, priceInt);
            selected.add(s);
        }

        BigDecimal insurance = INSURANCE_PER_PAX.multiply(BigDecimal.valueOf(selected.size()));
        BigDecimal service = SERVICE_FEE;
        BigDecimal total = subtotal.add(insurance).add(service);

        // Trip info (demo; thay bằng TripDao nếu có)
        Map<String, Object> trip = new HashMap<>();
        trip.put("originName", Optional.ofNullable(req.getParameter("originName")).orElse("Hà Nội"));
        trip.put("destName", Optional.ofNullable(req.getParameter("destName")).orElse("Đà Nẵng"));
        trip.put("trainCode", Optional.ofNullable(req.getParameter("trainCode")).orElse("SE7"));
        trip.put("departTimeStr", Optional.ofNullable(req.getParameter("departTime")).orElse("06:00"));
        trip.put("arriveTimeStr", Optional.ofNullable(req.getParameter("arriveTime")).orElse("22:43"));
        trip.put("departDateStr", "Thứ 4, 05/11");

        req.setAttribute("tripId", tripId);
        req.setAttribute("trip", trip);
        req.setAttribute("selectedSeats", selected);
        req.setAttribute("amounts", Map.of(
                "subtotal", subtotal,
                "insurance", insurance,
                "serviceFee", service,
                "total", total
        ));
        req.setAttribute("countdownSec", 600);

        req.getRequestDispatcher("/WEB-INF/views/customer/checkout.jsp").forward(req, res);
    }
}
