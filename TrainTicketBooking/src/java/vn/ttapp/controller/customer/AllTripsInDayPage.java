package vn.ttapp.controller.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.dao.CityDao;
import vn.ttapp.dao.RegionDao;
import vn.ttapp.dao.TripDao;
import vn.ttapp.model.TripView;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@WebServlet(name = "AllTripsInDayPage", urlPatterns = {"/customer/trips/day"})
public class AllTripsInDayPage extends HttpServlet {

    private final TripDao tripDao = new TripDao();
    private final RegionDao regionDao = new RegionDao();
    private final CityDao cityDao = new CityDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String dateStr = req.getParameter("date");
        LocalDate day = (dateStr == null || dateStr.isBlank())
                ? LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                : LocalDate.parse(dateStr);

        Integer regionId = parseInt(req.getParameter("regionId"));
        Integer cityId = parseInt(req.getParameter("cityId"));
        Integer originId = parseInt(req.getParameter("originStationId"));
        Integer destId = parseInt(req.getParameter("destStationId"));

        try {
            List<TripView> trips = tripDao.findTripsInDay(day, regionId, cityId, originId, destId);
            req.setAttribute("regions", regionDao.findAll());
            req.setAttribute("cities", cityDao.findByRegion(regionId));
            req.setAttribute("trips", trips);
            req.setAttribute("day", day);
            req.setAttribute("regionId", regionId);
            req.setAttribute("cityId", cityId);
            req.setAttribute("originStationId", originId);
            req.setAttribute("destStationId", destId);

            req.getRequestDispatcher("/WEB-INF/views/customer/trips_in_day.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private static Integer parseInt(String s) {
        try {
            return (s == null || s.isBlank()) ? null : Integer.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }
}
