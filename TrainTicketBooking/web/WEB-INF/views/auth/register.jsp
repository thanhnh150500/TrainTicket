<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

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
        <link rel="stylesheet" href="${ctx}/assets/css/auth.css">
    </head>
    <body class="bg-light">

        <div class="login-card shadow-sm border rounded-4 bg-white p-4 mx-auto mt-5" style="max-width: 460px;">
            <h2 class="text-center fw-bold mb-2">Tạo tài khoản</h2>
            <p class="text-center text-muted mb-4">Nhập thông tin để đăng ký</p>

            <c:if test="${not empty error}">
                <div class="alert alert-danger text-center fw-semibold py-2 mb-3">
                    <i class="bi bi-x-circle me-1"></i>
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <form method="post" action="${ctx}/auth/register" id="regForm" novalidate autocomplete="on">
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>

                <!-- Giữ lại next nếu có (ưu tiên request attr, sau đó param) -->
                <c:choose>
                    <c:when test="${not empty next}">
                        <input type="hidden" name="next" value="${fn:escapeXml(next)}"/>
                    </c:when>
                    <c:when test="${not empty param.next}">
                        <input type="hidden" name="next" value="${fn:escapeXml(param.next)}"/>
                    </c:when>
                </c:choose>

                <div class="mb-3">
                    <label class="form-label" for="fullName">Họ và tên</label>
                    <input
                        id="fullName"
                        name="fullName"
                        class="form-control"
                        placeholder="Nguyễn Văn A"
                        value="${fn:escapeXml(fullName)}"
                        required
                        autofocus
                        autocomplete="name" />
                </div>

                <div class="mb-3">
                    <label class="form-label" for="email">Email</label>
                    <input
                        id="email"
                        type="email"
                        name="email"
                        class="form-control"
                        placeholder="you@example.com"
                        value="${fn:escapeXml(email)}"
                        required
                        autocomplete="username" />
                </div>

                <div class="mb-3">
                    <label class="form-label" for="regPassword">Mật khẩu</label>
                    <div class="input-group">
                        <input
                            type="password"
                            name="password"
                            id="regPassword"
                            class="form-control"
                            placeholder="••••••••"
                            minlength="8"
                            required
                            autocomplete="new-password" />
                        <button type="button" class="btn btn-outline-secondary" id="toggleRegPassword" tabindex="-1" aria-label="Hiện/ẩn mật khẩu">
                            <i class="bi bi-eye" aria-hidden="true"></i>
                        </button>
                    </div>
                    <div class="form-text">Mật khẩu tối thiểu 8 ký tự.</div>
                </div>

                <div class="mb-3">
                    <label class="form-label" for="confirmPassword">Xác nhận mật khẩu</label>
                    <div class="input-group">
                        <!-- name=confirmPassword để servlet đọc -->
                        <input
                            type="password"
                            id="confirmPassword"
                            name="confirmPassword"
                            class="form-control"
                            placeholder="••••••••"
                            minlength="8"
                            required
                            autocomplete="new-password" />
                        <button type="button" class="btn btn-outline-secondary" id="toggleRegConfirm" tabindex="-1" aria-label="Hiện/ẩn mật khẩu">
                            <i class="bi bi-eye" aria-hidden="true"></i>
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
                    <a href="${ctx}/auth/login" class="fw-semibold">Đăng nhập</a>
                </p>
            </form>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

        <script>
            document.addEventListener('DOMContentLoaded', function () {
                const pw = document.getElementById('regPassword');
                const cf = document.getElementById('confirmPassword');
                const tPw = document.getElementById('toggleRegPassword');
                const tCf = document.getElementById('toggleRegConfirm');
                const msg = document.getElementById('confirmHelp');
                const form = document.getElementById('regForm');
                let cfDirty = false;

                function bindToggle(btn, input) {
                    if (!btn || !input)
                        return;
                    btn.addEventListener('click', function () {
                        input.type = (input.type === 'password') ? 'text' : 'password';
                        const icon = btn.querySelector('i');
                        if (icon) {
                            icon.classList.toggle('bi-eye');
                            icon.classList.toggle('bi-eye-slash');
                        }
                        input.focus();
                    });
                }

                function validateConfirm() {
                    if (!pw || !cf || !msg)
                        return;
                    if (!cfDirty || !pw.value || !cf.value) {
                        msg.classList.add('d-none');
                        cf.classList.remove('is-invalid');
                        cf.setCustomValidity('');
                        return;
                    }
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

                if (cf)
                    cf.addEventListener('input', () => {
                        cfDirty = true;
                        validateConfirm();
                    });
                if (pw)
                    pw.addEventListener('input', validateConfirm);
                if (form) {
                    form.addEventListener('submit', (e) => {
                        cfDirty = true;
                        validateConfirm();
                        if (!form.checkValidity()) {
                            e.preventDefault();
                            e.stopPropagation();
                        }
                    });
                }

                bindToggle(tPw, pw);
                bindToggle(tCf, cf);
            });
        </script>

    </body>
</html>
