package vn.ttapp.controller.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.dao.CityDao;
import vn.ttapp.dao.RegionDao;
import vn.ttapp.dao.StationDao;
import vn.ttapp.model.Station;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "StationBrowsePage", urlPatterns = {"/customer/stations"})
public class StationBrowsePage extends HttpServlet {

    private final RegionDao regionDao = new RegionDao();
    private final CityDao cityDao = new CityDao();
    private final StationDao stationDao = new StationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer regionId = parseInt(req.getParameter("regionId"));
        Integer cityId = parseInt(req.getParameter("cityId"));

        try {
            var regions = regionDao.findAll();
            var cities = cityId == null ? cityDao.findByRegion(regionId) : cityDao.findByRegion(regionId);
            List<Station> stations = (cityId != null) ? stationDao.findByCityId(cityId) : Collections.emptyList();

            req.setAttribute("regions", regions);
            req.setAttribute("cities", cities);
            req.setAttribute("stations", stations);
            req.setAttribute("regionId", regionId);
            req.setAttribute("cityId", cityId);

            req.getRequestDispatcher("/WEB-INF/views/customer/stations_browse.jsp").forward(req, resp);
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
