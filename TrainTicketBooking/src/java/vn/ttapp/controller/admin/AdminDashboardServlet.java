package vn.ttapp.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // <<== THIẾU IMPORT NÀY
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

import vn.ttapp.model.FnbDailyRevenue;
import vn.ttapp.model.FnbItemRevenue;
import vn.ttapp.model.Role;
import vn.ttapp.model.User;
import vn.ttapp.service.FnbOrderService;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin"})
public class AdminDashboardServlet extends HttpServlet {

    // ====== Helpers ======
    private static boolean isAdmin(User u) {
        if (u == null || u.getRoles() == null) {
            return false;
        }
        for (Role r : u.getRoles()) {
            if (r != null && "ADMIN".equalsIgnoreCase(r.getCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parse LocalDate yyyy-MM-dd an toàn (nullable).
     */
    private static LocalDate parseISODate(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException ignore) {
            return null;
        }
    }

    /**
     * Ép days về [1..365], mặc định 30.
     */
    private static int normalizeDays(String daysParam) {
        int days = 30;
        if (daysParam != null) {
            try {
                days = Integer.parseInt(daysParam);
            } catch (NumberFormatException ignore) {
            }
        }
        if (days < 1) {
            days = 1;
        }
        if (days > 365) {
            days = 365;
        }
        return days;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) Kiểm tra đăng nhập + quyền ADMIN
        HttpSession ss = req.getSession(false);
        User authUser = (ss == null) ? null : (User) ss.getAttribute("authUser");
        if (authUser == null || !isAdmin(authUser)) {
            // Chuyển sang login và mang next=/admin
            String ctx = req.getContextPath();
            resp.sendRedirect(ctx + "/auth/login?next=/admin");
            return;
        }

        // (khuyến nghị) Ngăn cache trang admin
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");

        FnbOrderService fnbService = new FnbOrderService();

        try {
            // 2) Dữ liệu tổng hợp F&B (all-time)
            List<FnbItemRevenue> revenues = fnbService.getFnbRevenueAllTime();
            req.setAttribute("fnbRevenues", revenues);

            // 3) Quyết định khoảng ngày cho phần daily
            String fromParam = req.getParameter("from"); // yyyy-MM-dd
            String toParam = req.getParameter("to");
            String daysParam = req.getParameter("days");

            LocalDate today = LocalDate.now();
            LocalDate fromDate;
            LocalDate toDate;

            LocalDate parsedFrom = parseISODate(fromParam);
            LocalDate parsedTo = parseISODate(toParam);

            if (parsedFrom != null || parsedTo != null) {
                fromDate = (parsedFrom != null) ? parsedFrom : today;
                toDate = (parsedTo != null) ? parsedTo : today;

                if (fromDate.isAfter(toDate)) { // swap
                    LocalDate tmp = fromDate;
                    fromDate = toDate;
                    toDate = tmp;
                }

                long span = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
                if (span > 365) {
                    fromDate = toDate.minusDays(364);
                }
            } else if (daysParam != null) {
                int days = normalizeDays(daysParam);
                toDate = today;
                fromDate = toDate.minusDays(days - 1);
            } else {
                // mặc định: từ ngày 1 tháng này đến hôm nay
                toDate = today;
                fromDate = today.withDayOfMonth(1);
            }

            // 4) Lấy dữ liệu daily theo range
            List<FnbDailyRevenue> daily = fnbService.getFnbRevenueBetween(fromDate, toDate);
            req.setAttribute("fnbDailyRevenues", daily);

            // 5) Gán lại các tham số đã chọn để hiện lên UI
            req.setAttribute("fromSelected", fromDate.toString());
            req.setAttribute("toSelected", toDate.toString());
            req.setAttribute("daysSelected", (int) (ChronoUnit.DAYS.between(fromDate, toDate) + 1));

        } catch (SQLException ex) {
            ex.printStackTrace();
            req.setAttribute("error", "Không tải được dữ liệu doanh thu F&B: " + ex.getMessage());
        }

        // 6) Forward tới view
        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}
