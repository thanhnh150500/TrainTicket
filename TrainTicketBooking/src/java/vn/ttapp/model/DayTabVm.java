package vn.ttapp.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class DayTabVm {

    public LocalDate date;   // Ngày hiển thị
    public boolean active;   // Ngày đang chọn
    public String url;       // Link chuyển ngày

    // ---- Constructor ----
    public DayTabVm() {
    }

    public DayTabVm(LocalDate date, boolean active, String url) {
        this.date = date;
        this.active = active;
        this.url = url;
    }

    // ---- Getter / Setter cơ bản ----
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // ---- Các getter tính toán cho JSP ----
    /**
     * Trả về chuỗi định dạng dd-MM-yyyy (VD: 04-11-2025)
     */
    public String getDdMMyyyy() {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    /**
     * Trả về thứ ngắn bằng tiếng Việt (VD: "Th 2", "Th 7", "CN")
     */
    public String getWeekday() {
        if (date == null) {
            return "";
        }
        Locale vi = new Locale("vi");
        String full = date.getDayOfWeek().getDisplayName(TextStyle.FULL, vi);
        // Viết ngắn gọn hơn chút để giao diện gọn
        if (full.equalsIgnoreCase("chủ nhật")) {
            return "CN";
        }
        if (full.startsWith("thứ")) {
            return full.substring(0, 3).replace("ứ", "");
        }
        return full;
    }
}
