/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.service;

import vn.ttapp.config.Db;
import vn.ttapp.dao.FnbOrderDao;
import vn.ttapp.dao.FnbOrderItemDao;
import vn.ttapp.model.FnbOrder;
import vn.ttapp.model.FnbOrderItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp; // (Thêm)
import java.util.List;
import java.util.UUID;
// import vn.ttapp.model.FnbItem; // (Không cần thiết)

public class FnbOrderService {

    private final FnbOrderDao orderDao = new FnbOrderDao();
    private final FnbOrderItemDao orderItemDao = new FnbOrderItemDao();
    
    /**
     * Lấy doanh thu theo món (tổng quantity và tổng amount) cho tất cả thời gian (đã thanh toán).
     */
    public java.util.List<vn.ttapp.model.FnbItemRevenue> getFnbRevenueAllTime() throws SQLException {
        return orderItemDao.findRevenueAllTime();
    }

    public java.util.List<vn.ttapp.model.FnbDailyRevenue> getFnbRevenueByDays(int days) throws SQLException {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate fromDate = today.minusDays(days - 1);
        java.sql.Timestamp fromTs = java.sql.Timestamp.valueOf(fromDate.atStartOfDay());
        java.sql.Timestamp toTs = java.sql.Timestamp.valueOf(today.plusDays(1).atStartOfDay().minusSeconds(1));
        java.util.List<vn.ttapp.model.FnbDailyRevenue> raw = orderItemDao.findDailyRevenue(fromTs, toTs);
        // Normalize: make sure we have an entry per day in the range (fill zeros)
        java.util.Map<java.time.LocalDate, Double> map = new java.util.HashMap<>();
        for (vn.ttapp.model.FnbDailyRevenue r : raw) {
            if (r.getDay() != null) map.put(r.getDay(), r.getTotalAmount());
        }
        java.util.List<vn.ttapp.model.FnbDailyRevenue> out = new java.util.ArrayList<>();
        for (int i = 0; i < days; i++) {
            java.time.LocalDate d = fromDate.plusDays(i);
            vn.ttapp.model.FnbDailyRevenue r = new vn.ttapp.model.FnbDailyRevenue();
            r.setDay(d);
            r.setTotalAmount(map.getOrDefault(d, 0.0));
            out.add(r);
        }
        return out;
    }
    public java.util.List<vn.ttapp.model.FnbDailyRevenue> getFnbRevenueBetween(java.time.LocalDate from, java.time.LocalDate to) throws SQLException {
        if (from == null || to == null) return java.util.Collections.emptyList();
        java.sql.Timestamp fromTs = java.sql.Timestamp.valueOf(from.atStartOfDay());
        java.sql.Timestamp toTs = java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay().minusSeconds(1));
        java.util.List<vn.ttapp.model.FnbDailyRevenue> raw = orderItemDao.findDailyRevenue(fromTs, toTs);
        java.util.Map<java.time.LocalDate, Double> map = new java.util.HashMap<>();
        for (vn.ttapp.model.FnbDailyRevenue r : raw) {
            if (r.getDay() != null) map.put(r.getDay(), r.getTotalAmount());
        }
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
        java.util.List<vn.ttapp.model.FnbDailyRevenue> out = new java.util.ArrayList<>();
        for (int i = 0; i < daysBetween; i++) {
            java.time.LocalDate d = from.plusDays(i);
            vn.ttapp.model.FnbDailyRevenue r = new vn.ttapp.model.FnbDailyRevenue();
            r.setDay(d);
            r.setTotalAmount(map.getOrDefault(d, 0.0));
            out.add(r);
        }
        return out;
    }

    /**
     * (Hàm cũ)
     * Tạo một đơn hàng mới (bao gồm order header và các order items)
     */
    public boolean createOrder(FnbOrder order, List<FnbOrderItem> items) {
        
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            try {
                long newOrderId = orderDao.insert(order, conn);
                order.setOrderId(newOrderId); 

                for (FnbOrderItem item : items) {
                    item.setOrderId(newOrderId); 
                    orderItemDao.insert(item, conn); 
                }
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace(); 
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
            return false;
        }
    }
    public List<FnbOrder> getAllFnbOrdersForManager(String fromDate, String toDate) throws SQLException {
        
        Timestamp fromTimestamp = Timestamp.valueOf(fromDate + " 00:00:00");
        Timestamp toTimestamp = Timestamp.valueOf(toDate + " 23:59:59");

        return orderDao.findAllDetailedByDateRange(fromTimestamp, toTimestamp);
    }
    
    
    public List<FnbOrder> getDetailedOrdersByTripAndStaff(int tripId, UUID staffId) throws SQLException {
        return orderDao.findDetailedOrdersByTripAndStaff(tripId, staffId);
    }
    
    public FnbOrder getOrderDetails(long orderId) throws SQLException {
        FnbOrder order = orderDao.findDetailedById(orderId);
        if (order == null) {
            return null;
        }
        
        List<FnbOrderItem> items = orderItemDao.findDetailedByOrderId(orderId);
        order.setItems(items); 
        
        return order;
    }

    public boolean updateOrderStatus(long orderId, String orderStatus, String paymentStatus) throws SQLException {
        if (!("CREATED".equals(orderStatus) || "DELIVERED".equals(orderStatus) || "CANCELED".equals(orderStatus))) {
            return false; 
        }
         if (!("PENDING".equals(paymentStatus) || "SUCCESS".equals(paymentStatus) || "FAILED".equals(paymentStatus))) {
            return false; 
        }
        
        return orderDao.updateStatus(orderId, orderStatus, paymentStatus);
    }
}