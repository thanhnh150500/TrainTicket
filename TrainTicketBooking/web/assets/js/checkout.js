(function () {
    // ==== Cấu hình ====
    // Trang checkout.jsp set:
    // window.TT_CHECKOUT = { ctx: "<%=ctx%>", countdownSec: 300, bookingId: 123, tripId: 25 };
    const cfg = window.TT_CHECKOUT || {};
    const COUNTDOWN_SEC = Number.isFinite(+cfg.countdownSec) ? +cfg.countdownSec : 300; // 5 phút
    const KEY = `tt_checkout_exp_${cfg.bookingId || cfg.tripId || "default"}`;
    const HOME_URL = (cfg.expireRedirect) || ((cfg.ctx || "") + "/"); // nơi sẽ quay lại khi hết giờ

    const btn = document.getElementById("btnPay");
    const span = document.getElementById("btnPayCountdown");
    const form = document.getElementById("checkoutForm") || document.querySelector("form.checkout-form");

    // ==== Khởi tạo/khôi phục thời điểm hết hạn từ localStorage ====
    let expiresAt = parseInt(localStorage.getItem(KEY) || "0", 10);
    const now = Date.now();
    if (!expiresAt || expiresAt < now - 1000) {
        expiresAt = now + COUNTDOWN_SEC * 1000;
        localStorage.setItem(KEY, String(expiresAt));
    }

    function secondsLeft() {
        return Math.max(0, Math.floor((expiresAt - Date.now()) / 1000));
    }
    function fmt(sec) {
        const m = Math.floor(sec / 60).toString().padStart(2, "0");
        const s = (sec % 60).toString().padStart(2, "0");
        return `${m}:${s}`;
    }

    let timedOut = false;

    function onTimeout() {
        if (timedOut)
            return;
        timedOut = true;

        // khoá nút & hiển thị 00:00
        btn?.setAttribute("disabled", "disabled");
        if (span)
            span.textContent = "(00:00)";
        // xoá mốc hết hạn để lần vào lại không “hồi sinh”
        localStorage.removeItem(KEY);

        // Thông báo & điều hướng
        alert("Hết thời gian giữ vé. Vui lòng đặt lại vé.");
        // Tránh double-submit/back-forward: điều hướng về trang chủ
        window.location.href = HOME_URL;
    }

    function renderCountdown() {
        const left = secondsLeft();
        if (span) {
            span.textContent = `(${fmt(left)})`;
            if (left <= 60)
                span.classList.add("text-danger");
        }
        if (left <= 0) {
            clearInterval(timer);
            onTimeout();
        }
    }

    // Lần đầu & interval
    renderCountdown();
    const timer = setInterval(renderCountdown, 1000);

    // Nếu submit khi đã hết giờ → chặn & báo
    form?.addEventListener("submit", (e) => {
        if (secondsLeft() <= 0) {
            e.preventDefault();
            onTimeout();
            return false;
        }
        // submit thành công thì xoá KEY (không mang sang trang sau)
        localStorage.removeItem(KEY);
        return true;
    });

    // Nếu trang phục hồi từ bfcache (back/forward) → cập nhật ngay
    window.addEventListener("pageshow", renderCountdown);

    // ==== Chuẩn hoá input ngày sinh dd/mm/yyyy ====
    document.querySelectorAll(".dob").forEach(inp => {
        inp.addEventListener("blur", () => {
            const v = (inp.value || "").trim();
            if (!v)
                return;
            const ok = /^\d{2}\/\d{2}\/\d{4}$/.test(v);
            inp.classList.toggle("is-invalid", !ok);
        });
    });

    // ==== Nút “Nhập dạng bảng” (demo) ====
    const bulk = document.getElementById("btnBulkFill");
    bulk?.addEventListener("click", () => {
        document.querySelector("input[name='fullname[]']")?.focus();
    });
})();
