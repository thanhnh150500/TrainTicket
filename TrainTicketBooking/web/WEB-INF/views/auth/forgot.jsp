<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Quên mật khẩu | TrainTicket</title>

        <!-- Bootstrap & Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
    </head>
    <body class="bg-light">
        <div class="login-card shadow-sm border rounded-4 bg-white p-4 mx-auto mt-5" style="max-width: 420px;">
            <h2 class="text-center fw-bold mb-2">Quên mật khẩu</h2>
            <p class="text-center text-muted mb-4">Nhập email của bạn để nhận liên kết đặt lại mật khẩu.</p>

            <!-- Thông báo -->
            <c:if test="${not empty message}">
                <div class="alert alert-success text-center fw-semibold">${message}</div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert alert-danger text-center fw-semibold">${error}</div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/auth/forgot">
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>

                <div class="mb-3">
                    <label class="form-label">Địa chỉ email</label>
                    <input type="email" name="email" class="form-control" placeholder="you@example.com" required autofocus>
                </div>

                <button class="btn btn-primary w-100 fw-semibold" type="submit">
                    <i class="bi bi-envelope me-1"></i> Gửi liên kết đặt lại
                </button>

                <p class="text-center mt-3 mb-0">
                    <a href="${pageContext.request.contextPath}/auth/login">Quay lại đăng nhập</a>
                </p>
            </form>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
