// src/java/vn/ttapp/controller/customer/BookingDetailServlet.java
package vn.ttapp.controller.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import vn.ttapp.dao.BookingDao;
import vn.ttapp.dao.BookingItemDao;
import vn.ttapp.model.Booking;
import vn.ttapp.model.BookingItem;
import vn.ttapp.model.TripInfo;

@WebServlet("/booking/detail")
public class BookingDetailServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final BookingDao bookingDao = new BookingDao.Impl();
    private final BookingItemDao itemDao = new BookingItemDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Long bookingId = parseLong(req.getParameter("id"));
        if (bookingId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id");
            return;
        }

        try {
            Booking b = bookingDao.findById(bookingId);
            if (b == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found");
                return;
            }

            // ✅ Dùng TripInfo từ model (không phải BookingItemDao.TripInfo)
            TripInfo ti = itemDao.getTripInfo(bookingId);

            // ✅ listDetails trả về List<BookingItem>
            List<BookingItem> items = itemDao.listDetails(bookingId);

            // map chuỗi thời gian để render
            String createdAtStr = (b.getCreatedAt() != null) ? DTF.format(b.getCreatedAt()) : "";
            String paidAtStr = (b.getPaidAt() != null) ? DTF.format(b.getPaidAt()) : null;
            String departStr = (ti != null && ti.getDepartAt() != null)
                    ? DTF.format(ti.getDepartAt().toInstant().atOffset(ZoneOffset.UTC)) : null;
            String arriveStr = (ti != null && ti.getArriveAt() != null)
                    ? DTF.format(ti.getArriveAt().toInstant().atOffset(ZoneOffset.UTC)) : null;

            req.setAttribute("booking", b);
            req.setAttribute("tripId", ti != null ? ti.getTripId() : null);
            req.setAttribute("trainCode", ti != null ? ti.getTrainCode() : null);
            req.setAttribute("trainName", ti != null ? ti.getTrainName() : null);
            req.setAttribute("departAtStr", departStr);
            req.setAttribute("arriveAtStr", arriveStr);
            req.setAttribute("createdAtStr", createdAtStr);
            req.setAttribute("paidAtStr", paidAtStr);
            req.setAttribute("items", items);

            req.getRequestDispatcher("/WEB-INF/views/customer/booking_detail.jsp")
                    .forward(req, resp);

        } catch (Exception e) {
            throw new ServletException("Load booking detail failed", e);
        }
    }

    private static Long parseLong(String s) {
        try {
            return Long.valueOf(s);
        } catch (Exception ignore) {
            return null;
        }
    }
}
