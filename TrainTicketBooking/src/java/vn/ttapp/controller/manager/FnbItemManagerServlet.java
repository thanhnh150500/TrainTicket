/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package vn.ttapp.controller.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig; // <-- THÊM (1)
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.ttapp.model.FnbItem;
import vn.ttapp.model.FnbCategory;
import vn.ttapp.service.FnbItemService;
import vn.ttapp.dao.FnbCategoryDao;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID; // <-- THÊM (2)

@WebServlet("/manager/fnb-items")
@MultipartConfig // <-- THÊM (1)
public class FnbItemManagerServlet extends HttpServlet {

    private final FnbItemService service = new FnbItemService();
    private final FnbCategoryDao categoryDao = new FnbCategoryDao();

    // Tên thư mục để lưu ảnh (sẽ nằm trong thư mục webapp của bạn)
    private static final String UPLOAD_DIR = "uploads" + File.separator + "fnb";

    private void loadRefs(HttpServletRequest req) throws SQLException {
        List<FnbCategory> categories = categoryDao.findAll();
        req.setAttribute("categories", categories);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        // =======================================================
        // ==> (2) BẮT ĐẦU SỬA LỖI CSRF
        // =======================================================
        HttpSession session = req.getSession();
        String token = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", token);
        // =======================================================
        
        String op = req.getParameter("op");
        if (op == null) op = "list";
        
        try {
            switch (op) {
                case "new" -> {
                    loadRefs(req);
                    req.setAttribute("x", new FnbItem());
                    req.getRequestDispatcher("/WEB-INF/views/manager/fnb_item_form.jsp").forward(req, res);
                }
                case "edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    FnbItem item = service.getById(id);
                    if (item == null) {
                        req.getSession().setAttribute("flash_error", "Không tìm thấy món.");
                        res.sendRedirect(req.getContextPath() + "/manager/fnb-items");
                        return;
                    }
                    loadRefs(req);
                    req.setAttribute("x", item);
                    req.getRequestDispatcher("/WEB-INF/views/manager/fnb_item_form.jsp").forward(req, res);
                }
                default -> { // "list"
                    req.setAttribute("list", service.getAll());
                    req.getRequestDispatcher("/WEB-INF/views/manager/fnb_item_list.jsp").forward(req, res);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        
        // =======================================================
        // ==> (3) XÁC THỰC CSRF TOKEN
        // =======================================================
        HttpSession session = req.getSession();
        String sessionToken = (String) session.getAttribute("csrfToken");
        String formToken = req.getParameter("_csrf");

        if (sessionToken == null || formToken == null || !sessionToken.equals(formToken)) {
            // Lỗi CSRF
            req.getSession().setAttribute("flash_error", "Lỗi: Thao tác không hợp lệ. Vui lòng thử lại.");
            res.sendRedirect(req.getContextPath() + "/manager/fnb-items");
            return;
        }
        // Xóa token sau khi dùng
        session.removeAttribute("csrfToken");
        // =======================================================

        String op = req.getParameter("op");
        if (op == null) op = "save";
        
        try {
            switch (op) {
                case "save" -> {
                    // (BƯỚC 1: Lấy các trường text)
                    String idRaw = req.getParameter("item_id");
                    String categoryRaw = req.getParameter("category_id");
                    String code = req.getParameter("code");
                    String name = req.getParameter("name");
                    String priceRaw = req.getParameter("price");
                    boolean active = "on".equals(req.getParameter("is_active"));
                    String existingImageUrl = req.getParameter("existing_image_url");

                    FnbItem x = new FnbItem();
                    x.setItemId((idRaw == null || idRaw.isBlank()) ? null : Integer.parseInt(idRaw));
                    x.setCategoryId((categoryRaw == null || categoryRaw.isBlank()) ? null : Integer.parseInt(categoryRaw));
                    x.setCode(code);
                    x.setName(name);
                    x.setPrice(Double.parseDouble(priceRaw));
                    x.setActive(active);
                    x.setImageUrl(existingImageUrl); // <-- Tạm gán ảnh cũ

                    // =======================================================
                    // ==> (4) BẮT ĐẦU XỬ LÝ FILE UPLOAD
                    // =======================================================
                    Part filePart = req.getPart("image_file");
                    String newImageUrl = null;

                    if (filePart != null && filePart.getSize() > 0 && filePart.getSubmittedFileName() != null && !filePart.getSubmittedFileName().isBlank()) {
                        String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
                        
                        String applicationPath = req.getServletContext().getRealPath("");
                        String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
                        
                        File uploadDir = new File(uploadFilePath);
                        if (!uploadDir.exists()) uploadDir.mkdirs();
                        
                        filePart.write(uploadFilePath + File.separator + uniqueFileName);
                        
                        newImageUrl = UPLOAD_DIR + File.separator + uniqueFileName;
                        newImageUrl = newImageUrl.replace(File.separator, "/"); // Chuẩn hóa cho URL
                        
                        x.setImageUrl(newImageUrl); // Cập nhật ảnh mới
                        
                        // Xóa file ảnh cũ (nếu đang update và có ảnh cũ)
                        if (existingImageUrl != null && !existingImageUrl.isBlank()) {
                             File oldFile = new File(applicationPath + File.separator + existingImageUrl);
                             if (oldFile.exists()) oldFile.delete();
                        }
                    }
                    // =======================================================

                    if (service.save(x)) {
                        req.getSession().setAttribute("flash_success", "Đã lưu món.");
                        res.sendRedirect(req.getContextPath() + "/manager/fnb-items");
                    } else {
                        req.setAttribute("error", "Dữ liệu không hợp lệ hoặc mã đã tồn tại.");
                        loadRefs(req);
                        req.setAttribute("x", x);
                        req.getRequestDispatcher("/WEB-INF/views/manager/fnb_item_form.jsp").forward(req, res);
                    }
                }
                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    
                    // (4) Xóa file ảnh vật lý khi xóa món
                    FnbItem item = service.getById(id);
                    if (item != null && item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
                         String applicationPath = req.getServletContext().getRealPath("");
                         File oldFile = new File(applicationPath + File.separator + item.getImageUrl());
                         if (oldFile.exists()) oldFile.delete();
                    }
                    
                    service.delete(id);
                    req.getSession().setAttribute("flash_success", "Đã xóa món.");
                    res.sendRedirect(req.getContextPath() + "/manager/fnb-items");
                }
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("flash_error", "Lỗi hệ thống: " + e.getMessage());
            res.sendRedirect(req.getContextPath() + "/manager/fnb-items");
        } catch (Exception e) {
            // Bắt các lỗi khác (như ParseDouble, ParseInt...)
            req.getSession().setAttribute("flash_error", "Dữ liệu không hợp lệ: " + e.getMessage());
            res.sendRedirect(req.getContextPath() + "/manager/fnb-items");
        }
    }
}