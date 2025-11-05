package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.dao.RouteDao;
import vn.ttapp.dao.TrainDao;
import vn.ttapp.model.Route;
import vn.ttapp.model.Trip;
import vn.ttapp.model.Train;
import vn.ttapp.model.User;
import vn.ttapp.service.TripService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "TripManagerServlet", urlPatterns = {"/manager/trips"})
public class TripManagerServlet extends HttpServlet {

    private final TripService service = new TripService();
    private final RouteDao routeDao = new RouteDao();
    private final TrainDao trainDao = new TrainDao();

    /**
     * Tải các đối tượng tham chiếu cần thiết (Routes, Trains, Staff) để đổ vào form.
     */
    private void loadRefs(HttpServletRequest req) throws SQLException {
        List<Route> routes = routeDao.findAll();
        List<Train> trains = trainDao.findAll();
        List<User> allStaff = service.getAllStaffFNB(); // Lấy danh sách Staff cho Food & Beverage

        req.setAttribute("routes", routes);
        req.setAttribute("trains", trains);
        req.setAttribute("allStaff", allStaff); // Thêm Staff vào Attribute
    }

    /**
     * Chuyển đổi LocalDateTime sang định dạng chuẩn HTML5 input (yyyy-MM-ddTHH:mm).
     * @param dt Thời gian LocalDateTime.
     * @return Chuỗi định dạng cho input datetime-local.
     */
    private static String toInputValue(LocalDateTime dt) {
        if (dt == null) {
            return "";
        }
        String s = dt.toString();
        // Cắt bỏ phần giây (.ss) nếu có, giữ lại yyyy-MM-ddTHH:mm
        int secondSep = s.lastIndexOf(':');
        if (secondSep > 0) {
            return s.substring(0, secondSep);
        }
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }
    
    // Hàm esc từ code 2 được giữ lại (nếu cần dùng trong JSP, tuy nhiên thường không cần trong Servlet)
    /*
    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    */

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String op = req.getParameter("op");
        if (op == null) {
            op = "list";
        }

        try {
            switch (op) {
                case "new" -> {
                    // Hiển thị form tạo mới
                    loadRefs(req);
                    Trip t = new Trip();
                    req.setAttribute("t", t);
                    // Cần thiết lập giá trị input rỗng khi tạo mới
                    req.setAttribute("departAtInput", "");
                    req.setAttribute("arriveAtInput", "");
                    req.setAttribute("assignedStaffIds", List.of()); 
                    req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                }
                case "edit" -> {
                    // Hiển thị form chỉnh sửa
                    int id = Integer.parseInt(req.getParameter("id"));
                    Trip t = service.findById(id);
                    if (t == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy chuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/trips");
                        return;
                    }
                    loadRefs(req);
                    req.setAttribute("t", t);
                    // Chuyển đổi thời gian sang định dạng HTML input
                    req.setAttribute("departAtInput", toInputValue(t.getDepartAt()));
                    req.setAttribute("arriveAtInput", toInputValue(t.getArriveAt()));

                    // Lấy danh sách staff đã được phân công cho chuyến này
            List<String> assignedStaffIds = service.getStaffByTrip(id)
                .stream()
                .map(u -> u.getUserId() == null ? null : u.getUserId().toString())
                .collect(Collectors.toList());
                    req.setAttribute("assignedStaffIds", assignedStaffIds);
                    req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                }
                case "view" -> {
                    // Hiển thị chi tiết chuyến đi
                    int id = Integer.parseInt(req.getParameter("id"));
                    Trip t = service.findById(id);
                    if (t == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy chuyến.");
                        res.sendRedirect(req.getContextPath() + "/manager/trips");
                        return;
                    }
                    var routeMeta = routeDao.findById(t.getRouteId());
                    var trainMeta = trainDao.findDetail(t.getTrainId());
                    List<User> staffList = service.getStaffByTrip(id);

                    req.setAttribute("t", t);
                    req.setAttribute("routeMeta", routeMeta);
                    req.setAttribute("trainMeta", trainMeta);
                    req.setAttribute("staffList", staffList);
                    req.getRequestDispatcher("/WEB-INF/views/manager/trip_view.jsp").forward(req, res);
                }
                default -> {
                    // Mặc định: Hiển thị danh sách
                    req.setAttribute("list", service.findAll());
                    req.getRequestDispatcher("/WEB-INF/views/manager/trip_list.jsp").forward(req, res);
                }
            }
        } catch (SQLException e) {
            // Lỗi DB: Xử lý ngoại lệ SQL
            throw new ServletException(e);
        } catch (NumberFormatException nfe) {
            // Lỗi tham số id không hợp lệ (Code 1 có xử lý này)
            req.getSession().setAttribute("flash_error", "Tham số ID chuyến không hợp lệ.");
            res.sendRedirect(req.getContextPath() + "/manager/trips");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String op = req.getParameter("op");
        if (op == null) {
            op = "save";
        }

        try {
            switch (op) {
                case "save" -> {
                    String idRaw = req.getParameter("trip_id");
                    String routeIdRaw = req.getParameter("route_id");
                    String trainIdRaw = req.getParameter("train_id");
                    String departRaw = req.getParameter("depart_at"); // yyyy-MM-ddTHH:mm
                    String arriveRaw = req.getParameter("arrive_at");
                    String status = req.getParameter("status");

                    Integer routeId = (routeIdRaw == null || routeIdRaw.isBlank()) ? null : Integer.parseInt(routeIdRaw);
                    Integer trainId = (trainIdRaw == null || trainIdRaw.isBlank()) ? null : Integer.parseInt(trainIdRaw);

                    // Xử lý chuyển đổi thời gian
                    LocalDateTime departAt = null, arriveAt = null;
                    try {
                        if (departRaw != null && !departRaw.isBlank()) {
                            departAt = LocalDateTime.parse(departRaw);
                        }
                    } catch (Exception ignored) {
                        // Bỏ qua lỗi format, sẽ được xử lý ở khối validation
                    }
                    try {
                        if (arriveRaw != null && !arriveRaw.isBlank()) {
                            arriveAt = LocalDateTime.parse(arriveRaw);
                        }
                    } catch (Exception ignored) {
                        // Bỏ qua lỗi format, sẽ được xử lý ở khối validation
                    }

                    // Lấy Staff IDs để bảo toàn khi validation thất bại
                    String[] staffIds = req.getParameterValues("staff_ids");
                    List<String> assignedStaffIds = (staffIds == null) ? List.of() : List.of(staffIds);

                    // VALIDATION (Kiểm tra dữ liệu bắt buộc)
                    if (routeId == null || trainId == null || departAt == null || arriveAt == null || status == null || status.isBlank()) {
                        req.setAttribute("error", "Vui lòng chọn Tuyến, Tàu, nhập thời gian đi/đến và trạng thái.");
                        Trip t = new Trip();
                        if (idRaw != null && !idRaw.isBlank()) {
                            t.setTripId(Integer.parseInt(idRaw));
                        }
                        t.setRouteId(routeId);
                        t.setTrainId(trainId);
                        t.setDepartAt(departAt);
                        t.setArriveAt(arriveAt);
                        t.setStatus(status);
                        loadRefs(req);
                        req.setAttribute("t", t);
                        req.setAttribute("departAtInput", departRaw); // Bảo toàn input thô
                        req.setAttribute("arriveAtInput", arriveRaw); // Bảo toàn input thô
                        req.setAttribute("assignedStaffIds", assignedStaffIds);
                        req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                        return;
                    }
                    
                    // VALIDATION (Kiểm tra logic thời gian)
                    if (!arriveAt.isAfter(departAt)) {
                        req.setAttribute("error", "Giờ đến phải sau giờ khởi hành.");
                        Trip t = new Trip(null, routeId, trainId, departAt, arriveAt, status);
                        loadRefs(req);
                        req.setAttribute("t", t);
                        req.setAttribute("departAtInput", departRaw);
                        req.setAttribute("arriveAtInput", arriveRaw);
                        req.setAttribute("assignedStaffIds", assignedStaffIds);
                        req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                        return;
                    }

                    // XỬ LÝ TẠO/CẬP NHẬT
                    Integer tripId;
                    if (idRaw == null || idRaw.isBlank()) {
                        // Tạo mới
                        tripId = service.create(routeId, trainId, departAt, arriveAt, status);
                        if (tripId == null) {
                            req.setAttribute("error", "Không thể tạo chuyến.");
                            Trip t = new Trip(null, routeId, trainId, departAt, arriveAt, status);
                            loadRefs(req);
                            req.setAttribute("t", t);
                            req.setAttribute("departAtInput", departRaw);
                            req.setAttribute("arriveAtInput", arriveRaw);
                            req.setAttribute("assignedStaffIds", assignedStaffIds);
                            req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã tạo chuyến.");
                    } else {
                        // Cập nhật
                        tripId = Integer.parseInt(idRaw);
                        Trip t = new Trip(tripId, routeId, trainId, departAt, arriveAt, status);
                        boolean ok = service.update(t);
                        if (!ok) {
                            req.setAttribute("error", "Không thể cập nhật chuyến.");
                            loadRefs(req);
                            req.setAttribute("t", t);
                            req.setAttribute("departAtInput", departRaw);
                            req.setAttribute("arriveAtInput", arriveRaw);
                            req.setAttribute("assignedStaffIds", assignedStaffIds);
                            req.getRequestDispatcher("/WEB-INF/views/manager/trip_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã cập nhật chuyến.");
                    }

                    // ===== Gán staff (chạy sau khi chuyến đi được tạo/cập nhật thành công) =====
                    service.assignStaff(tripId, assignedStaffIds, "STAFF_FNB");

                    res.sendRedirect(req.getContextPath() + "/manager/trips");
                }
                case "delete" -> {
                    // Xóa chuyến
                    int id = Integer.parseInt(req.getParameter("id"));
                    service.delete(id);
                    req.getSession().setAttribute("flash_success", "Đã xóa chuyến.");
                    res.sendRedirect(req.getContextPath() + "/manager/trips");
                }
                default ->
                    res.sendRedirect(req.getContextPath() + "/manager/trips");
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("flash_error", "Có lỗi hệ thống. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/trips");
        } catch (NumberFormatException nfe) {
            req.getSession().setAttribute("flash_error", "Tham số không hợp lệ.");
            res.sendRedirect(req.getContextPath() + "/manager/trips");
        }
    }
}