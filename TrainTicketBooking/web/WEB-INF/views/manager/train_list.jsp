<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>Danh sách tàu</title>

        <!-- Bootstrap CSS + Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet" />
    </head>
    <body class="p-4">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <div class="d-flex justify-content-between align-items-center mb-3">
            <h3>Danh sách tàu</h3>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/manager/trains?op=new">
                <i class="bi bi-plus"></i> Thêm tàu
            </a>
        </div>

        <table class="table table-striped align-middle">
            <thead>
                <tr>
                    <th style="width:80px;">ID</th>
                    <th style="width:180px;">Code</th>
                    <th>Tên tàu</th>
                    <th style="width:220px;">Hành động</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${list}" var="t">
                    <tr>
                        <td><c:out value="${t.trainId}"/></td>
                        <td><c:out value="${t.code}"/></td>
                        <td><c:out value="${t.name}"/></td>

                        <td class="d-flex gap-2">
                            <a class="btn btn-sm btn-outline-secondary"
                               href="${pageContext.request.contextPath}/manager/trains?op=edit&id=${t.trainId}">Sửa</a>

                            <!-- XÓA = POST + CSRF -->
                            <form method="post" action="${pageContext.request.contextPath}/manager/trains" class="d-inline">
                                <input type="hidden" name="op" value="delete"/>
                                <input type="hidden" name="id" value="${t.trainId}"/>
                                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                                <button type="submit" class="btn btn-sm btn-outline-danger"
                                        onclick="return confirm('Xóa tàu này?');">Xóa</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty list}">
                    <tr><td colspan="4" class="text-center text-muted">Chưa có tàu nào.</td></tr>
                </c:if>
            </tbody>
        </table>

        <!-- Toast container (góc phải trên) -->
        <div aria-live="polite" aria-atomic="true" class="position-fixed top-0 end-0 p-3" style="z-index: 1080">
            <c:if test="${not empty sessionScope.flash_success}">
                <div id="toast-success" class="toast align-items-center text-bg-success border-0" role="alert" aria-live="assertive" aria-atomic="true">
                    <div class="d-flex">
                        <div class="toast-body">
                            ${sessionScope.flash_success}
                        </div>
                        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Đóng"></button>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty sessionScope.flash_error}">
                <div id="toast-error" class="toast align-items-center text-bg-danger border-0" role="alert" aria-live="assertive" aria-atomic="true">
                    <div class="d-flex">
                        <div class="toast-body">
                            ${sessionScope.flash_error}
                        </div>
                        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Đóng"></button>
                    </div>
                </div>
            </c:if>
        </div>

        <!-- Xóa flash sau khi render để chỉ hiện 1 lần -->
        <c:if test="${not empty sessionScope.flash_success}">
            <c:remove var="flash_success" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.flash_error}">
            <c:remove var="flash_error" scope="session" />
        </c:if>

        <!-- Bootstrap JS -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

        <!-- Khởi chạy toast & auto-hide sau 2.5s -->
        <script>
                                            (function () {
                                                const shown = [];
                                                const successEl = document.getElementById('toast-success');
                                                const errorEl = document.getElementById('toast-error');

                                                [successEl, errorEl].forEach(function (el) {
                                                    if (!el)
                                                        return;
                                                    const t = new bootstrap.Toast(el, {delay: 2500, autohide: true});
                                                    t.show();
                                                    shown.push(t);
                                                });
                                            })();
        </script>
    </body>
</html>
