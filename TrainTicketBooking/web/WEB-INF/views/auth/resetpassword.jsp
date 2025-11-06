<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Đặt lại mật khẩu | TrainTicket</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
        <link rel="stylesheet" href="<%=ctx%>/assets/css/auth.css">
    </head>
    <body class="bg-light">
        <div class="login-card shadow-sm border rounded-4 bg-white p-4 mx-auto mt-5" style="max-width: 460px;">
            <h2 class="text-center fw-bold mb-2">Đặt lại mật khẩu</h2>
            <p class="text-center text-muted mb-4">Nhập mật khẩu mới cho tài khoản của bạn</p>

            <!-- Ưu tiên token từ request attribute (được servlet set), fallback param -->
            <c:set var="tokenVal" value="${not empty token ? token : param.token}" />

            <c:if test="${not empty error}">
                <div class="alert alert-danger text-center fw-semibold">${error}</div>
            </c:if>

            <c:if test="${not empty message}">
                <div class="alert alert-success text-center fw-semibold">${message}</div>
            </c:if>

            <!-- Nếu thiếu token thì báo ngay và không render form -->
            <c:if test="${empty tokenVal}">
                <div class="alert alert-warning text-center">
                    Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.
                </div>
                <div class="text-center">
                    <a class="btn btn-outline-primary" href="<%=ctx%>/auth/forgot">Gửi lại liên kết</a>
                </div>
            </c:if>

            <c:if test="${not empty tokenVal}">
                <form method="post" action="<%=ctx%>/auth/reset" novalidate>
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="token" value="${tokenVal}">

                    <div class="mb-3">
                        <label class="form-label">Mật khẩu mới</label>
                        <div class="input-group">
                            <input id="password" name="password" type="password" class="form-control" minlength="8" required>
                            <button class="btn btn-outline-secondary" type="button" id="togglePw">
                                <i class="bi bi-eye"></i>
                            </button>
                        </div>
                        <div class="form-text">Tối thiểu 8 ký tự.</div>
                    </div>

                    <div class="mb-2">
                        <label class="form-label">Xác nhận mật khẩu</label>
                        <div class="input-group">
                            <input id="confirmPassword" name="confirmPassword" type="password" class="form-control" minlength="8" required>
                            <button class="btn btn-outline-secondary" type="button" id="togglePw2">
                                <i class="bi bi-eye"></i>
                            </button>
                        </div>
                        <div id="mismatchMsg" class="form-text text-danger d-none" aria-live="polite">
                            Xác nhận mật khẩu chưa khớp.
                        </div>
                    </div>

                    <button class="btn btn-primary w-100 fw-semibold mt-2" type="submit">
                        Cập nhật mật khẩu
                    </button>
                </form>
            </c:if>
        </div>

        <script>
            (function () {
                const pw = document.getElementById('password');
                const cf = document.getElementById('confirmPassword');
                const msg = document.getElementById('mismatchMsg');
                const t1 = document.getElementById('togglePw');
                const t2 = document.getElementById('togglePw2');
                let dirty = false;

                function validate() {
                    if (!dirty || !pw || !cf)
                        return;
                    if (!pw.value || !cf.value) {
                        msg?.classList.add('d-none');
                        cf?.classList.remove('is-invalid');
                        cf?.setCustomValidity('');
                        return;
                    }
                    if (pw.value !== cf.value) {
                        msg?.classList.remove('d-none');
                        cf?.classList.add('is-invalid');
                        cf?.setCustomValidity('Passwords do not match');
                    } else {
                        msg?.classList.add('d-none');
                        cf?.classList.remove('is-invalid');
                        cf?.setCustomValidity('');
                    }
                }
                cf?.addEventListener('input', () => {
                    dirty = true;
                    validate();
                });
                pw?.addEventListener('input', validate);

                document.querySelector('form')?.addEventListener('submit', (e) => {
                    dirty = true;
                    validate();
                    if (!e.target.checkValidity())
                        e.preventDefault();
                });

                function toggle(input, btn) {
                    if (!input || !btn)
                        return;
                    btn.addEventListener('click', () => {
                        const type = input.type === 'password' ? 'text' : 'password';
                        input.type = type;
                        const icon = btn.querySelector('i');
                        if (icon)
                            icon.className = (type === 'password') ? 'bi bi-eye' : 'bi bi-eye-slash';
                    });
                }
                toggle(pw, t1);
                toggle(cf, t2);
            })();
        </script>
    </body>
</html>
