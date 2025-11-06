<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng nhập | TrainTicket</title>

        <!-- Bootstrap & Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

        <!-- Custom CSS -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
    </head>
    <body class="bg-light">

        <div class="login-card shadow-sm border rounded-4 bg-white p-4 mx-auto mt-5" style="max-width: 420px;">
            <h2 class="text-center fw-bold mb-2">Đăng nhập</h2>
            <p class="text-center text-muted mb-4">Nhập thông tin tài khoản để tiếp tục</p>

            <c:if test="${param.msg == 'must-login'}">
                <div class="alert alert-warning text-center fw-semibold py-2 mb-3">
                    <i class="bi bi-exclamation-triangle me-1"></i>
                    Vui lòng đăng nhập để đặt vé.
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

            <c:if test="${not empty error}">
                <div class="alert alert-danger text-center fw-semibold py-2 mb-3">
                    <i class="bi bi-x-circle me-1"></i>
                    ${error}
                </div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/auth/login">
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                <c:if test="${not empty param.next}">
                    <input type="hidden" name="next" value="${param.next}">
                </c:if>

                <div class="mb-3">
                    <label class="form-label">Email</label>
                    <input type="email"
                           name="email"
                           class="form-control"
                           value="${param.email}"
                           placeholder="you@example.com"
                           required autofocus>
                </div>

                <div class="mb-3">
                    <label class="form-label">Mật khẩu</label>
                    <div class="input-group">
                        <input type="password"
                               name="password"
                               class="form-control"
                               id="passwordField"
                               placeholder="••••••••"
                               required>
                        <button type="button" class="btn btn-outline-secondary" id="togglePassword" tabindex="-1">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>
                </div>

                <div class="d-flex justify-content-between align-items-center mb-3">
                    <div class="form-check">
                        <input type="checkbox" class="form-check-input" id="rememberMe">
                        <label class="form-check-label" for="rememberMe">Ghi nhớ tôi</label>
                    </div>
                    <a href="${pageContext.request.contextPath}/auth/forgot" class="small">Quên mật khẩu?</a>
                </div>

                <button class="btn btn-primary w-100 py-2 fw-semibold" type="submit">
                    <i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập
                </button>

                <p class="text-center mt-3 mb-0">
                    Chưa có tài khoản?
                    <a href="${pageContext.request.contextPath}/auth/register" class="fw-semibold">Đăng ký ngay</a>
                </p>
            </form>
        </div>

        <!-- Bootstrap JS -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

        <!-- Toggle mật khẩu -->
        <script>
            const toggle = document.getElementById("togglePassword");
            const passField = document.getElementById("passwordField");
            toggle.addEventListener("click", () => {
                const type = passField.getAttribute("type") === "password" ? "text" : "password";
                passField.setAttribute("type", type);
                toggle.querySelector("i").classList.toggle("bi-eye");
                toggle.querySelector("i").classList.toggle("bi-eye-slash");
            });
        </script>
    </body>
</html>
