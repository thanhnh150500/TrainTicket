
package vn.ttapp.controller.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.ttapp.model.User;
import vn.ttapp.model.BookingSummary;
import vn.ttapp.service.BookingService;
import vn.ttapp.service.SessionUtils;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/booking-history")
public class BookingHistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Đổi nếu login của bạn là /login
    private static final String LOGIN_PATH = "/auth/login";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final BookingService bookingService = new BookingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Lọc & phân trang
        final String status = trimOrNull(req.getParameter("status"));
        final int page = parseInt(req.getParameter("page"), 1);
        final int size = parseInt(req.getParameter("size"), 10);

        final HttpSession ss = req.getSession(false);
        final Object principal = SessionUtils.getUserPrincipal(ss);

        if (principal == null) {
            // Chưa đăng nhập -> vẫn vào trang lịch sử, hiển thị CTA đăng nhập
            req.setAttribute("needsLogin", true);
            req.setAttribute("loginHref", req.getContextPath() + LOGIN_PATH);
            req.setAttribute("list", Collections.emptyList());
            req.setAttribute("total", 0L);
            req.setAttribute("page", page);
            req.setAttribute("size", size);
            req.setAttribute("lastPage", 0L);
            req.setAttribute("status", status);

            req.getRequestDispatcher("/WEB-INF/views/customer/booking_history.jsp")
                    .forward(req, resp);
            return;
        }

        // Đã đăng nhập
        final User u = (User) principal; // đảm bảo User.getUserId() là UUID
        try {
            final long total = bookingService.countUserBookings(u.getUserId());
            final List<BookingSummary> raw
                    = bookingService.listUserBookings(u.getUserId(), status, page, size);

            // Map sang view model để hiển thị chuỗi thời gian
            final List<Map<String, Object>> list = new ArrayList<>(raw.size());
            for (BookingSummary b : raw) {
                Map<String, Object> m = new HashMap<>();
                m.put("bookingId", b.getBookingId());
                m.put("tripCode", b.getTripCode());
                m.put("trainName", b.getTrainName());
                m.put("seatCodes", b.getSeatCodes());
                m.put("itemCount", b.getItemCount());
                m.put("totalAmount", b.getTotalAmount());
                m.put("status", b.getStatus());
                m.put("createdAtStr", b.getCreatedAt() != null ? DTF.format(b.getCreatedAt()) : "");
                m.put("paidAtStr", b.getPaidAt() != null ? DTF.format(b.getPaidAt()) : null);
                list.add(m);
            }

            final long lastPage = (total + size - 1) / size;

            req.setAttribute("needsLogin", false);
            req.setAttribute("list", list);
            req.setAttribute("total", total);
            req.setAttribute("page", page);
            req.setAttribute("size", size);
            req.setAttribute("lastPage", lastPage);
            req.setAttribute("status", status);

            req.getRequestDispatcher("/WEB-INF/views/customer/booking_history.jsp")
                    .forward(req, resp);

        } catch (Exception e) {
            throw new ServletException("Load booking history failed", e);
        }
    }

    private static String trimOrNull(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }

    private static int parseInt(String s, int dft) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ignore) {
            return dft;
        }
    }
}
