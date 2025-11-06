<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Tạo tài khoản | TrainTicket</title>

        <!-- Bootstrap & Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

        <!-- Custom CSS (dùng chung với login) -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
    </head>
    <body class="bg-light">

        <div class="login-card shadow-sm border rounded-4 bg-white p-4 mx-auto mt-5" style="max-width: 460px;">
            <h2 class="text-center fw-bold mb-2">Tạo tài khoản</h2>
            <p class="text-center text-muted mb-4">Nhập thông tin để đăng ký</p>

            <c:if test="${not empty error}">
                <div class="alert alert-danger text-center fw-semibold py-2 mb-3">
                    <i class="bi bi-x-circle me-1"></i>
                    ${error}
                </div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/auth/register" id="regForm">
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>

                <div class="mb-3">
                    <label class="form-label">Họ và tên</label>
                    <input name="fullName"
                           class="form-control"
                           placeholder="Nguyễn Văn A"
                           value="${param.fullName}"
                           required autofocus>
                </div>

                <div class="mb-3">
                    <label class="form-label">Email</label>
                    <input type="email"
                           name="email"
                           class="form-control"
                           placeholder="you@example.com"
                           value="${param.email}"
                           required>
                </div>

                <div class="mb-3">
                    <label class="form-label">Mật khẩu</label>
                    <div class="input-group">
                        <input type="password"
                               name="password"
                               id="regPassword"
                               class="form-control"
                               placeholder="••••••••"
                               minlength="8"
                               required>
                        <button type="button" class="btn btn-outline-secondary" id="toggleRegPassword" tabindex="-1">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>
                    <div class="form-text">Mật khẩu tối thiểu 8 ký tự.</div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Xác nhận mật khẩu</label>
                    <div class="input-group">
                        <input type="password"
                               id="regConfirm"
                               class="form-control"
                               placeholder="••••••••"
                               minlength="8"
                               required>
                        <button type="button" class="btn btn-outline-secondary" id="toggleRegConfirm" tabindex="-1">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>
                    <div id="confirmHelp" class="form-text text-danger d-none" aria-live="polite">
                        Xác nhận mật khẩu chưa khớp.
                    </div>
                </div>

                <button class="btn btn-primary w-100 py-2 fw-semibold" type="submit">
                    <i class="bi bi-person-plus me-1"></i> Tạo tài khoản
                </button>

                <p class="text-center mt-3 mb-0">
                    Đã có tài khoản?
                    <a href="${pageContext.request.contextPath}/auth/login" class="fw-semibold">Đăng nhập</a>
                </p>
            </form>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

        <script>
            const pw = document.getElementById('password');
            const cf = document.getElementById('confirmPassword');
            const msg = document.getElementById('confirmHelp');
            let cfDirty = false; // chỉ báo lỗi sau khi user chạm vào ô xác nhận

            function validateConfirm() {
                // Ẩn lỗi nếu chưa "dirty" hoặc chưa có đủ 2 ô
                if (!cfDirty || !pw.value || !cf.value) {
                    msg.classList.add('d-none');
                    cf.classList.remove('is-invalid');
                    cf.setCustomValidity(''); // xoá trạng thái invalid của HTML5
                    return;
                }
                // Hiện lỗi khi lệch
                if (pw.value !== cf.value) {
                    msg.classList.remove('d-none');
                    cf.classList.add('is-invalid');
                    cf.setCustomValidity('Passwords do not match');
                } else {
                    msg.classList.add('d-none');
                    cf.classList.remove('is-invalid');
                    cf.setCustomValidity('');
                }
            }

            cf.addEventListener('input', () => {
                cfDirty = true;
                validateConfirm();
            });
            pw.addEventListener('input', validateConfirm);
            
            document.querySelector('form').addEventListener('submit', (e) => {
                cfDirty = true;
                validateConfirm();
                if (!e.target.checkValidity())
                    e.preventDefault();
            });
        </script>

    </body>
</html>
