package vn.ttapp.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import vn.ttapp.service.FnbOrderService;
import vn.ttapp.model.FnbItemRevenue;
import vn.ttapp.model.FnbDailyRevenue;

/**
 * Simple servlet that serves the Admin Dashboard view.
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin"})
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // You can add authorization checks here if needed (e.g., ensure user has ADMIN role)
        // Load F&B revenue data (all-time) to display services that have been sold
        FnbOrderService fnbService = new FnbOrderService();
        try {
            List<FnbItemRevenue> revenues = fnbService.getFnbRevenueAllTime();
            req.setAttribute("fnbRevenues", revenues);

            // Allow optional ?days= on the dashboard to control how many days of daily revenue to show.
            String daysParam = req.getParameter("days");
            int days = 30; // default
            if (daysParam != null) {
                try {
                    days = Integer.parseInt(daysParam);
                } catch (NumberFormatException e) {
                    days = 30;
                }
            }
            // clamp days to reasonable range
            if (days < 1) days = 1;
            if (days > 365) days = 365;

            // Check for explicit from/to parameters. If none provided, default to first day of current month.
            String fromParam = req.getParameter("from");
            String toParam = req.getParameter("to");
            java.util.List<FnbDailyRevenue> daily;
            if (fromParam != null) {
                try {
                    java.time.LocalDate fromDate = java.time.LocalDate.parse(fromParam);
                    java.time.LocalDate toDate = (toParam != null) ? java.time.LocalDate.parse(toParam) : java.time.LocalDate.now();
                    // clamp range
                    if (fromDate.isAfter(toDate)) { java.time.LocalDate tmp = fromDate; fromDate = toDate; toDate = tmp; }
                    long span = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1;
                    if (span > 365) { fromDate = toDate.minusDays(364); }
                    daily = fnbService.getFnbRevenueBetween(fromDate, toDate);
                    req.setAttribute("fromSelected", fromDate.toString());
                    req.setAttribute("toSelected", toDate.toString());
                    req.setAttribute("daysSelected", (int)java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1);
                } catch (Exception ex) {
                    // parse error -> fallback to first day of current month
                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.time.LocalDate fromDate = today.withDayOfMonth(1);
                    java.time.LocalDate toDate = today;
                    daily = fnbService.getFnbRevenueBetween(fromDate, toDate);
                    req.setAttribute("fromSelected", fromDate.toString());
                    req.setAttribute("toSelected", toDate.toString());
                    req.setAttribute("daysSelected", (int)java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1);
                }
            } else {
                // Default: start from first day of current month
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDate fromDate = today.withDayOfMonth(1);
                java.time.LocalDate toDate = today;
                daily = fnbService.getFnbRevenueBetween(fromDate, toDate);
                req.setAttribute("fromSelected", fromDate.toString());
                req.setAttribute("toSelected", toDate.toString());
                req.setAttribute("daysSelected", (int)java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate) + 1);
            }
            req.setAttribute("fnbDailyRevenues", daily);
        } catch (SQLException ex) {
            // If DB error occurs, forward without revenues and log
            ex.printStackTrace();
        }

        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}
