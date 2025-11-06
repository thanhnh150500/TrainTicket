/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.model.FnbOrder;
import vn.ttapp.service.FnbOrderService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Servlet này xử lý trang Quản lý Đơn F&B cho Manager
 */
@WebServlet("/manager/fnb-orders")
public class ManagerFnbOrderServlet extends HttpServlet {
    
    private final FnbOrderService orderService = new FnbOrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        // (Mặc định lọc theo ngày hôm nay)
        String fromDate = req.getParameter("from");
        String toDate = req.getParameter("to");
        
        if (fromDate == null || fromDate.isBlank()) {
            fromDate = LocalDate.now().toString();
        }
        if (toDate == null || toDate.isBlank()) {
            toDate = LocalDate.now().toString();
        }

        try {
            // (1) Lấy dữ liệu (gọi hàm mới)
            List<FnbOrder> orders = orderService.getAllFnbOrdersForManager(fromDate, toDate);
            
            // (2) Tính tổng
            double totalRevenue = 0;
            double totalPending = 0;
            for (FnbOrder order : orders) {
                if (order.getPaymentStatus() == FnbOrder.PaymentStatus.SUCCESS) {
                    totalRevenue += order.getTotalAmount();
                } else if (order.getPaymentStatus() == FnbOrder.PaymentStatus.PENDING) {
                    totalPending += order.getTotalAmount();
                }
            }

            req.setAttribute("orders", orders);
            req.setAttribute("totalRevenue", totalRevenue);
            req.setAttribute("totalPending", totalPending);
            req.setAttribute("fromDate", fromDate);
            req.setAttribute("toDate", toDate);
            
            // (Tạo CSRF token cho các form)
            String token = UUID.randomUUID().toString();
            req.getSession().setAttribute("csrfToken", token);
            
            req.getRequestDispatcher("/WEB-INF/views/manager/fnb-orders.jsp").forward(req, res);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
    
    /**
     * Xử lý Cập nhật trạng thái (Manager có thể cập nhật)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        
        String sessionToken = (String) session.getAttribute("csrfToken");
        String formToken = req.getParameter("_csrf");
        if (sessionToken == null || formToken == null || !sessionToken.equals(formToken)) {
            session.setAttribute("flash_error", "Lỗi thao tác. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/fnb-orders");
            return;
        }
        
        // (Lấy param để redirect)
        String fromDate = req.getParameter("from");
        String toDate = req.getParameter("to");
        String returnUrl = req.getContextPath() + "/manager/fnb-orders?from=" + fromDate + "&to=" + toDate;

        try {
            String op = req.getParameter("op");
            if (!"update_status".equals(op)) {
                res.sendRedirect(returnUrl);
                return;
            }
            
            // (Manager đã đăng nhập)
            if (session.getAttribute("AUTH_USER") == null) {
                 session.setAttribute("flash_error", "Phiên đăng nhập hết hạn.");
                 res.sendRedirect(req.getContextPath() + "/login");
                 return;
            }

            long orderId = Long.parseLong(req.getParameter("orderId"));
            String orderStatus = req.getParameter("orderStatus");
            String paymentStatus = req.getParameter("paymentStatus");
            
            // (Bảo mật: Manager có quyền cập nhật mọi đơn)
            boolean success = orderService.updateOrderStatus(orderId, orderStatus, paymentStatus);
            
            if (success) {
                session.setAttribute("flash_success", "Cập nhật đơn hàng #" + orderId + " thành công.");
            } else {
                session.setAttribute("flash_error", "Cập nhật thất bại.");
            }
            res.sendRedirect(returnUrl);
            
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (Exception e) {
             session.setAttribute("flash_error", "Dữ liệu không hợp lệ.");
             res.sendRedirect(returnUrl);
        }
    }
}