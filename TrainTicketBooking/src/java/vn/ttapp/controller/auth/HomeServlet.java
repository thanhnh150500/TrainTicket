package vn.ttapp.controller.auth;

import vn.ttapp.dao.StationDao;
import vn.ttapp.model.Station;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "HomeServlet", urlPatterns = {"/home", "/api/stations/suggest"})
public class HomeServlet extends HttpServlet {

    private final StationDao stationDao = new StationDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Nếu là API gợi ý -> trả JSON rồi thoát
        String path = request.getServletPath();
        if ("/api/stations/suggest".equals(path)) {
            handleSuggest(request, response);
            return;
        }

        // Trang home bình thường
        try {
            List<Station> stations = stationDao.findAll(); // để fallback nếu muốn
            request.setAttribute("stations", stations);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("stations", java.util.List.of());
        }
        request.setAttribute("today", java.time.LocalDate.now().toString());
        request.setAttribute("ctx", request.getContextPath());
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }

    private void handleSuggest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String q = req.getParameter("q");
        int n = 8;
        try {
            String nStr = req.getParameter("n");
            if (nStr != null) {
                n = Math.max(1, Math.min(20, Integer.parseInt(nStr)));
            }
        } catch (NumberFormatException ignore) {
        }

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        // Cho frontend dễ debug, và GET không cần CSRF
        resp.setHeader("Cache-Control", "no-store");

        try {
            List<Station> list = stationDao.suggestByNameOrCode(q, n);
            String json = list.stream().map(s -> String.format(
                    "{\"id\":%d,\"code\":%s,\"name\":%s,\"city\":%s}",
                    s.getStationId(),
                    js(s.getCode()),
                    js(s.getName()),
                    js(s.getCityName())
            )).collect(Collectors.joining(",", "[", "]"));
            resp.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("[]");
        }
    }

    private static String js(String s) {
        if (s == null) {
            return "null";
        }
        String esc = s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
        return "\"" + esc + "\"";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Không xử lý POST ở đây
        resp.sendRedirect(req.getContextPath() + "/tripsearch");
    }
}
