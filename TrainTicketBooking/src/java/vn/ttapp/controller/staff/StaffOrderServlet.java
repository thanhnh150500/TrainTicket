/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package vn.ttapp.controller.staff; 

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.model.*; 
import vn.ttapp.service.FnbItemService;
import vn.ttapp.service.FnbOrderService;
import vn.ttapp.dao.FnbCategoryDao;
import vn.ttapp.dao.TripStaffDao; 

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@WebServlet("/staff/pos") 
public class StaffOrderServlet extends HttpServlet {

    private final FnbItemService itemService = new FnbItemService();
    private final FnbCategoryDao categoryDao = new FnbCategoryDao();
    private final FnbOrderService orderService = new FnbOrderService();
    private final TripStaffDao tripStaffDao = new TripStaffDao(); 
    private final vn.ttapp.service.SeatService seatService = new vn.ttapp.service.SeatService();
    private final vn.ttapp.service.TripService tripService = new vn.ttapp.service.TripService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession();

        // AJAX helper: checkSeat
        String op = req.getParameter("op");
        if ("checkSeat".equals(op)) {
            res.setContentType("application/json;charset=UTF-8");
                User staffUser = (User) session.getAttribute("AUTH_USER");
            if (staffUser == null) {
                res.getWriter().write("{\"available\":false,\"message\":\"Unauthenticated\"}");
                return;
            }
            String tripRaw = req.getParameter("tripId");
            String seatLabel = req.getParameter("seat_label");
            try {
                if (tripRaw == null || tripRaw.isBlank() || seatLabel == null || seatLabel.isBlank()) {
                    res.getWriter().write("{\"available\":false,\"message\":\"Thiếu tham số\"}");
                    return;
                }
                int tripId = Integer.parseInt(tripRaw);
                    // User.getUserId() is UUID; convert to String when passing to DAOs that expect String
                    String staffId = staffUser.getUserId() == null ? null : staffUser.getUserId().toString();
                // staff must be assigned to this trip
                if (!tripStaffDao.isStaffAssignedToTrip(staffId, tripId)) {
                    res.getWriter().write("{\"available\":false,\"message\":\"No permission\"}");
                    return;
                }
                var trip = tripService.findById(tripId);
                if (trip == null) {
                    res.getWriter().write("{\"available\":false,\"message\":\"Không tìm thấy chuyến\"}");
                    return;
                }
                // kiểm tra thời gian chạy: chỉ cho phép order khi hiện tại nằm trong [departAt, arriveAt]
                try {
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    java.time.LocalDateTime depart = trip.getDepartAt();
                    java.time.LocalDateTime arrive = trip.getArriveAt();
                    if (depart != null && now.isBefore(depart)) {
                        res.getWriter().write("{\"available\":false,\"message\":\"Chuyến chưa đến giờ khởi hành\"}");
                        return;
                    }
                    if (arrive != null && now.isAfter(arrive)) {
                        res.getWriter().write("{\"available\":false,\"message\":\"Chuyến đã kết thúc\"}");
                        return;
                    }
                } catch (Exception ignore) {
                    // nếu có lỗi thời gian, fallback sang kiểm tra trạng thái
                }
                if (!"RUNNING".equalsIgnoreCase(trip.getStatus())) {
                    res.getWriter().write("{\"available\":false,\"message\":\"Chuyến không ở trạng thái đang chạy\"}");
                    return;
                }
                var seat = seatService.findByTripAndCode(tripId, seatLabel.trim());
                if (seat == null) {
                    res.getWriter().write("{\"available\":false,\"message\":\"Không tìm thấy ghế\"}");
                    return;
                }
                res.getWriter().write("{\"available\":true,\"seatId\":" + seat.getSeatId() + "}");
                return;
            } catch (Exception e) {
                res.getWriter().write("{\"available\":false,\"message\":\"Lỗi hệ thống\"}");
                return;
            }
        }

        User staffUser = (User) session.getAttribute("AUTH_USER");
        if (staffUser == null) {
            session.setAttribute("flash_error", "Bạn phải đăng nhập để truy cập.");
            String targetUrl = req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
            session.setAttribute("targetUrl", targetUrl);
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String explicitTripIdRaw = req.getParameter("tripId");

        try {
            // Lấy staffId (String) từ UUID trong User model
            String staffId = staffUser.getUserId() == null ? null : staffUser.getUserId().toString();

            if (explicitTripIdRaw != null && !explicitTripIdRaw.isBlank()) {
                int tripId = Integer.parseInt(explicitTripIdRaw);
                
                // (SỬA) Truyền staffId (String) vào DAO
                if (tripStaffDao.isStaffAssignedToTrip(staffId, tripId)) {
                    loadPosMenu(req, res, tripId); 
                    return;
                } else {
                    req.setAttribute("error", "Bạn không được phân công vào chuyến tàu này.");
                    req.getRequestDispatcher("/WEB-INF/views/staff/staff_error.jsp").forward(req, res);
                    return;
                }
            }

            // (SỬA) Truyền staffId (String) vào DAO
            List<Trip> activeTrips = tripStaffDao.findActiveTripsByStaff(staffId);

            if (activeTrips.isEmpty()) {
                req.setAttribute("error", "Bạn không được phân công vào chuyến tàu nào đang chạy (status: RUNNING).");
                req.getRequestDispatcher("/WEB-INF/views/staff/staff_error.jsp").forward(req, res);
            } else if (activeTrips.size() == 1) {
                int tripId = activeTrips.get(0).getTripId();
                res.sendRedirect(req.getContextPath() + "/staff/pos?tripId=" + tripId);
            } else {
                req.setAttribute("trips", activeTrips);
                req.getRequestDispatcher("/WEB-INF/views/staff/staff_select_trip.jsp").forward(req, res);
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (IllegalArgumentException e) {
             // Lỗi này giờ chỉ xảy ra bên trong DAO (nếu ID không phải UUID)
             req.setAttribute("error", "ID nhân viên không hợp lệ (Không phải UUID).");
             req.getRequestDispatcher("/WEB-INF/views/staff/staff_error.jsp").forward(req, res);
        }
    }

    // (Hàm loadPosMenu không đổi)
    private void loadPosMenu(HttpServletRequest req, HttpServletResponse res, int tripId) 
            throws SQLException, ServletException, IOException {
        
        List<FnbItem> allItems = itemService.getAllActive();
        List<FnbCategory> categories = categoryDao.findAllActive();

        Map<Integer, List<FnbItem>> itemsByCategory = new HashMap<>();
        for (FnbItem item : allItems) {
            Integer catId = (item.getCategoryId() == null) ? 0 : item.getCategoryId();
            itemsByCategory.computeIfAbsent(catId, k -> new ArrayList<>()).add(item);
        }

        req.setAttribute("categories", categories);
        req.setAttribute("itemsByCategory", itemsByCategory);
        req.setAttribute("tripId", tripId); 

    // Thêm thông tin chuyến để JSP hiển thị trạng thái (RUNNING/ SCHEDULED / FINISHED ...)
    var trip = tripService.findById(tripId);
    req.setAttribute("trip", trip);
    req.setAttribute("tripStatus", trip == null ? null : trip.getStatus());
        // Thời gian: xem chuyến đã đến giờ chạy hay đã kết thúc
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            boolean notStarted = trip == null || trip.getDepartAt() == null ? false : now.isBefore(trip.getDepartAt());
            boolean finished = trip == null || trip.getArriveAt() == null ? false : now.isAfter(trip.getArriveAt());
            req.setAttribute("tripNotStarted", notStarted);
            req.setAttribute("tripFinished", finished);
        } catch (Exception ignore) {
            req.setAttribute("tripNotStarted", false);
            req.setAttribute("tripFinished", false);
        }

        HttpSession session = req.getSession();
        String token = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", token);

        req.getRequestDispatcher("/WEB-INF/views/staff/staff_pos.jsp").forward(req, res);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        HttpSession session = req.getSession();
        
        String sessionToken = (String) session.getAttribute("csrfToken");
        String formToken = req.getParameter("_csrf");
        if (sessionToken == null || formToken == null || !sessionToken.equals(formToken)) {
            session.setAttribute("flash_error", "Lỗi thao tác. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/staff/pos?tripId=" + req.getParameter("trip_id"));
            return;
        }
        session.removeAttribute("csrfToken");

        String tripIdParam = req.getParameter("trip_id");

        try {
            int tripId = Integer.parseInt(tripIdParam);
            String seatLabel = req.getParameter("seat_label");
            String paymentMethod = req.getParameter("payment_method");
            
            User staffUser = (User) session.getAttribute("AUTH_USER");
            if (staffUser == null) {
                 session.setAttribute("flash_error", "Phiên đăng nhập hết hạn.");
                 res.sendRedirect(req.getContextPath() + "/login");
                 return;
            }
            
            // Lấy staffId (String)
            String staffId = staffUser.getUserId() == null ? null : staffUser.getUserId().toString();

            // (SỬA) Truyền staffId (String) vào DAO
            if (!tripStaffDao.isStaffAssignedToTrip(staffId, tripId)) {
                 session.setAttribute("flash_error", "Bạn không có quyền tạo đơn cho chuyến tàu này.");
                 res.sendRedirect(req.getContextPath() + "/staff/pos"); 
                 return;
            }

            // ---- VALIDATE: trip must be RUNNING ----
            var trip = tripService.findById(tripId);
            if (trip == null) {
                session.setAttribute("flash_error", "Chuyến tàu không tồn tại.");
                res.sendRedirect(req.getContextPath() + "/staff/pos");
                return;
            }
            // kiểm tra thời gian: chỉ cho phép order khi hiện tại nằm trong [departAt, arriveAt]
            try {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                java.time.LocalDateTime depart = trip.getDepartAt();
                java.time.LocalDateTime arrive = trip.getArriveAt();
                if (depart != null && now.isBefore(depart)) {
                    session.setAttribute("flash_error", "Không thể tạo đơn: chuyến chưa đến giờ khởi hành.");
                    res.sendRedirect(req.getContextPath() + "/staff/pos?tripId=" + tripId);
                    return;
                }
                if (arrive != null && now.isAfter(arrive)) {
                    session.setAttribute("flash_error", "Không thể tạo đơn: chuyến đã kết thúc.");
                    res.sendRedirect(req.getContextPath() + "/staff/pos?tripId=" + tripId);
                    return;
                }
            } catch (Exception ignore) {
                // fallback to status check
            }
            if (!"RUNNING".equalsIgnoreCase(trip.getStatus())) {
                session.setAttribute("flash_error", "Không thể tạo đơn: chuyến hiện không chạy (status=" + trip.getStatus() + ").");
                res.sendRedirect(req.getContextPath() + "/staff/pos?tripId=" + tripId);
                return;
            }

            // ---- VALIDATE: seat label belongs to trip ----
            vn.ttapp.model.Seat seat = seatService.findByTripAndCode(tripId, seatLabel == null ? null : seatLabel.trim());
            if (seat == null) {
                session.setAttribute("flash_error", "Số ghế không hợp lệ cho chuyến này.");
                res.sendRedirect(req.getContextPath() + "/staff/pos?tripId=" + tripId);
                return;
            }
            
            // (Xử lý giỏ hàng - không đổi)
            List<FnbOrderItem> orderItems = new ArrayList<>();
            Map<String, String[]> parameterMap = req.getParameterMap();
            double totalAmount = 0;
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                if (entry.getKey().startsWith("quantity_")) {
                    int itemId = Integer.parseInt(entry.getKey().substring("quantity_".length()));
                    int quantity = Integer.parseInt(entry.getValue()[0]);
                    if (quantity > 0) {
                        FnbItem itemDetails = itemService.getById(itemId);
                        if (itemDetails != null && itemDetails.isActive()) {
                            FnbOrderItem lineItem = new FnbOrderItem();
                            lineItem.setItemId(itemId);
                            lineItem.setQuantity(quantity);
                            lineItem.setUnitPrice(itemDetails.getPrice());
                            lineItem.setAmount(itemDetails.getPrice() * quantity);
                            orderItems.add(lineItem);
                            totalAmount += lineItem.getAmount();
                        }
                    }
                }
            }

            if (orderItems.isEmpty()) {
                session.setAttribute("flash_error", "Đơn hàng rỗng. Vui lòng chọn ít nhất 1 món.");
                res.sendRedirect(req.getContextPath() + "/staff/pos?tripId=" + tripId);
                return;
            }

            FnbOrder order = new FnbOrder();
            order.setTripId(tripId);
            order.setSeatLabel(seatLabel);
            order.setOrderStatus(FnbOrder.OrderStatus.CREATED);
            order.setTotalAmount(totalAmount);
            order.setPaymentMethod(paymentMethod);
            order.setPaymentStatus(FnbOrder.PaymentStatus.SUCCESS);
            
            // setCreatedByUserId expects a UUID — we already have it on the model so use it directly
            order.setCreatedByUserId(staffUser.getUserId());
            
            boolean success = orderService.createOrder(order, orderItems);

            if (success) {
                session.setAttribute("flash_success", "Tạo đơn hàng #" + order.getOrderId() + " thành công.");
                res.sendRedirect(req.getContextPath() + "/staff/pos?tripId=" + tripId); 
            } else {
                session.setAttribute("flash_error", "Không thể lưu đơn hàng. Vui lòng thử lại.");
                res.sendRedirect(req.getContextPath() + "/staff/pos?tripId=" + tripId);
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (Exception e) {
             session.setAttribute("flash_error", "Dữ liệu không hợp lệ: " + e.getMessage());
             res.sendRedirect(req.getContextPath() + "/staff/pos?tripId=" + (tripIdParam != null ? tripIdParam : ""));
        }
    }

    
}