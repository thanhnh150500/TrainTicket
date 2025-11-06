package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.dao.CityDao;
import vn.ttapp.model.Station;
import vn.ttapp.service.StationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "StationManagerServlet", urlPatterns = {"/manager/stations"})
public class StationManagerServlet extends HttpServlet {

    private final StationService service = new StationService();
    private final CityDao cityDao = new CityDao();

    private void loadCities(HttpServletRequest req) throws SQLException {
        req.setAttribute("cities", cityDao.findAll());
    }

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
                    loadCities(req);
                    req.setAttribute("s", new Station());
                    req.getRequestDispatcher("/WEB-INF/views/manager/station_form.jsp").forward(req, res);
                }
                case "edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    Station s = service.findById(id);
                    if (s == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy ga.");
                        res.sendRedirect(req.getContextPath() + "/manager/stations");
                        return;
                    }
                    loadCities(req);
                    req.setAttribute("s", s);
                    req.getRequestDispatcher("/WEB-INF/views/manager/station_form.jsp").forward(req, res);
                }
                default -> {
                    List<Station> list = service.findAll();
                    req.setAttribute("list", list);
                    req.getRequestDispatcher("/WEB-INF/views/manager/station_list.jsp").forward(req, res);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
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
                    String idRaw = req.getParameter("station_id");
                    String cityIdRaw = req.getParameter("city_id");
                    String code = req.getParameter("code");
                    String name = req.getParameter("name");
                    String address = req.getParameter("address");

                    Integer cityId = (cityIdRaw == null || cityIdRaw.isBlank()) ? null : Integer.parseInt(cityIdRaw);
                    if (cityId == null || code == null || code.isBlank() || name == null || name.isBlank()) {
                        req.setAttribute("error", "Vui lòng chọn Thành phố và nhập Code/Name.");
                        Station s = new Station();
                        if (idRaw != null && !idRaw.isBlank()) {
                            s.setStationId(Integer.parseInt(idRaw));
                        }
                        s.setCityId(cityId);
                        s.setCode(code);
                        s.setName(name);
                        s.setAddress(address);
                        loadCities(req);
                        req.setAttribute("s", s);
                        req.getRequestDispatcher("/WEB-INF/views/manager/station_form.jsp").forward(req, res);
                        return;
                    }

                    if (idRaw == null || idRaw.isBlank()) {
                        Integer newId = service.create(cityId, code.trim(), name.trim(), address == null ? null : address.trim());
                        if (newId == null) {
                            req.setAttribute("error", "Thành phố không hợp lệ hoặc Code đã tồn tại.");
                            Station s = new Station();
                            s.setCityId(cityId);
                            s.setCode(code);
                            s.setName(name);
                            s.setAddress(address);
                            loadCities(req);
                            req.setAttribute("s", s);
                            req.getRequestDispatcher("/WEB-INF/views/manager/station_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã tạo ga mới.");
                        res.sendRedirect(req.getContextPath() + "/manager/stations");
                    } else {
                        Station s = new Station();
                        s.setStationId(Integer.parseInt(idRaw));
                        s.setCityId(cityId);
                        s.setCode(code.trim());
                        s.setName(name.trim());
                        s.setAddress(address == null ? null : address.trim());
                        boolean ok = service.update(s);
                        if (!ok) {
                            req.setAttribute("error", "Thành phố không hợp lệ hoặc Code đã tồn tại ở bản ghi khác.");
                            loadCities(req);
                            req.setAttribute("s", s);
                            req.getRequestDispatcher("/WEB-INF/views/manager/station_form.jsp").forward(req, res);
                            return;
                        }
                        req.getSession().setAttribute("flash_success", "Đã cập nhật ga.");
                        res.sendRedirect(req.getContextPath() + "/manager/stations");
                    }
                }
                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    service.delete(id);
                    req.getSession().setAttribute("flash_success", "Đã xóa ga.");
                    res.sendRedirect(req.getContextPath() + "/manager/stations");
                }
                default ->
                    res.sendRedirect(req.getContextPath() + "/manager/stations");
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("flash_error", "Có lỗi hệ thống. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/stations");
        }
    }
}
