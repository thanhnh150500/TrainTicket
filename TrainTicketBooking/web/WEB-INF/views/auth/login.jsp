<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Đăng nhập | TrainTicket</title>

        <!-- Bootstrap & Icons (CDN) -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

        <!-- Custom CSS -->
        <link rel="stylesheet" href="${ctx}/assets/css/auth.css">
    </head>
    <body class="bg-light">

        <noscript>
        <div class="container mt-3">
            <div class="alert alert-warning">Trình duyệt của bạn đang tắt JavaScript. Một số tính năng có thể không hoạt động.</div>
        </div>
        </noscript>

        <div class="login-card shadow-sm border rounded-4 bg-white p-4 mx-auto mt-5" style="max-width: 420px;">
            <h2 class="text-center fw-bold mb-2">Đăng nhập</h2>
            <p class="text-center text-muted mb-4">Nhập thông tin tài khoản để tiếp tục</p>

            <!-- Thông điệp trạng thái đến từ query param -->
            <c:if test="${param.msg == 'must-login'}">
                <div class="alert alert-warning text-center fw-semibold py-2 mb-3">
                    <i class="bi bi-exclamation-triangle me-1"></i>
                    Vui lòng đăng nhập để tiếp tục.
                </div>
            </c:if>

            <c:if test="${param.msg == 'no-account'}">
                <div class="alert alert-info text-center fw-semibold py-2 mb-3">
                    <i class="bi bi-person-plus me-1"></i>
                    Bạn chưa có tài khoản. Vui lòng đăng ký để tiếp tục.
                </div>
            </c:if>

            <c:if test="${param.registered == '1'}">
                <div class="alert alert-success text-center fw-semibold py-2 mb-3">
                    <i class="bi bi-check-circle me-1"></i>
                    Đăng ký thành công! Vui lòng đăng nhập để tiếp tục.
                </div>
            </c:if>

            <!-- Lỗi do servlet đặt qua request.setAttribute("error", "...") -->
            <c:if test="${not empty error}">
                <div class="alert alert-danger text-center fw-semibold py-2 mb-3">
                    <i class="bi bi-x-circle me-1"></i>
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <!-- FORM ĐĂNG NHẬP
                 - action: /auth/login (khớp LoginServlet)
                 - _csrf: lấy từ sessionScope.csrfToken (LoginServlet.doGet đã cấp)
                 - next: nếu có ?next=... trên URL hoặc servlet đặt lại, sẽ đổ vào hidden để quay lại sau khi login
            -->
            <form method="post" action="${ctx}/auth/login" novalidate autocomplete="on">
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>

                <!-- Ưu tiên param.next; nếu cần bạn có thể đặt request attribute 'next' từ servlet -->
                <c:if test="${not empty param.next}">
                    <input type="hidden" name="next" value="${fn:escapeXml(param.next)}"/>
                </c:if>

                <div class="mb-3">
                    <label class="form-label" for="emailInput">Email</label>
                    <input
                        id="emailInput"
                        type="email"
                        name="email"
                        class="form-control"
                        value="${fn:escapeXml(param.email)}"
                        placeholder="you@example.com"
                        required
                        autofocus
                        autocomplete="username" />
                </div>

                <div class="mb-3">
                    <label class="form-label" for="passwordField">Mật khẩu</label>
                    <div class="input-group">
                        <input
                            id="passwordField"
                            type="password"
                            name="password"
                            class="form-control"
                            placeholder="••••••••"
                            required
                            autocomplete="current-password" />
                        <button type="button" class="btn btn-outline-secondary" id="togglePassword" tabindex="-1" aria-label="Hiện/ẩn mật khẩu">
                            <i class="bi bi-eye" aria-hidden="true"></i>
                        </button>
                    </div>
                </div>

                <div class="d-flex justify-content-between align-items-center mb-3">
                    <!-- Thêm name để server có thể đọc giá trị -->
                    <div class="form-check">
                        <input type="checkbox" class="form-check-input" id="rememberMe" name="remember" value="1">
                        <label class="form-check-label" for="rememberMe">Ghi nhớ tôi</label>
                    </div>
                    <a href="${ctx}/auth/forgot" class="small">Quên mật khẩu?</a>
                </div>

                <button class="btn btn-primary w-100 py-2 fw-semibold" type="submit">
                    <i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập
                </button>

                <p class="text-center mt-3 mb-0">
                    Chưa có tài khoản?
                    <a href="${ctx}/auth/register" class="fw-semibold">Đăng ký ngay</a>
                </p>
            </form>
        </div>

        <!-- Bootstrap JS -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

        <!-- Toggle hiển/ẩn mật khẩu -->
        <script>
            (function () {
                const toggle = document.getElementById("togglePassword");
                const passField = document.getElementById("passwordField");
                if (toggle && passField) {
                    toggle.addEventListener("click", () => {
                        const isPwd = passField.getAttribute("type") === "password";
                        passField.setAttribute("type", isPwd ? "text" : "password");
                        const icon = toggle.querySelector("i");
                        if (icon) {
                            icon.classList.toggle("bi-eye");
                            icon.classList.toggle("bi-eye-slash");
                        }
                    });
                }
            })();
        </script>
    </body>
</html>
