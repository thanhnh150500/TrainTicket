package vn.ttapp.controller.auth;

import vn.ttapp.dao.StationDao;
import vn.ttapp.model.Station;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    private final StationDao stationDao = new StationDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Lấy danh sách tất cả các ga trong DB để hiển thị datalist
            List<Station> stations = stationDao.findAll();
            request.setAttribute("stations", stations);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("stations", java.util.List.of());
        }
        request.setAttribute("today", java.time.LocalDate.now().toString());
        request.setAttribute("ctx", request.getContextPath());
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Không xử lý POST ở đây, form submit tới /tripsearch
        resp.sendRedirect(req.getContextPath() + "/tripsearch");
    }
}
