<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>Danh sách tuyến</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet" />
    </head>
    <body class="p-4">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <div class="d-flex justify-content-between align-items-center mb-3">
            <h3>Danh sách tuyến</h3>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/manager/routes?op=new">
                <i class="bi bi-plus"></i> Thêm tuyến
            </a>
        </div>

        <table class="table table-striped align-middle">
            <thead>
                <tr>
                    <th style="width:90px;">ID</th>
                    <th style="width:180px;">Code</th>
                    <th>Ga đi</th>
                    <th>Ga đến</th>
                    <th style="width:220px;">Hành động</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${list}" var="r">
                    <tr>
                        <td><c:out value="${r.routeId}"/></td>
                        <td><c:out value="${r.code}"/></td>
                        <td><c:out value="${r.originName}"/></td>
                        <td><c:out value="${r.destName}"/></td>
                        <td class="d-flex gap-2">
                            <a class="btn btn-sm btn-outline-secondary"
                               href="${pageContext.request.contextPath}/manager/routes?op=edit&id=${r.routeId}">Sửa</a>

                            <form method="post" action="${pageContext.request.contextPath}/manager/routes" class="d-inline">
                                <input type="hidden" name="op" value="delete"/>
                                <input type="hidden" name="id" value="${r.routeId}"/>
                                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                                <button type="submit" class="btn btn-sm btn-outline-danger"
                                        onclick="return confirm('Xóa tuyến này?');">Xóa</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty list}">
                    <tr><td colspan="5" class="text-center text-muted">Chưa có tuyến.</td></tr>
                </c:if>
            </tbody>
        </table>

        <!-- Toast -->
        <div aria-live="polite" aria-atomic="true" class="position-fixed top-0 end-0 p-3" style="z-index:1080">
            <c:if test="${not empty sessionScope.flash_success}">
                <div id="toast-success" class="toast align-items-center text-bg-success border-0" role="alert">
                    <div class="d-flex">
                        <div class="toast-body">${sessionScope.flash_success}</div>
                        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                    </div>
                </div>
            </c:if>
            <c:if test="${not empty sessionScope.flash_error}">
                <div id="toast-error" class="toast align-items-center text-bg-danger border-0" role="alert">
                    <div class="d-flex">
                        <div class="toast-body">${sessionScope.flash_error}</div>
                        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                    </div>
                </div>
            </c:if>
        </div>
        <c:if test="${not empty sessionScope.flash_success}"><c:remove var="flash_success" scope="session"/></c:if>
        <c:if test="${not empty sessionScope.flash_error}"><c:remove var="flash_error" scope="session"/></c:if>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script>
                                            [document.getElementById('toast-success'), document.getElementById('toast-error')]
                                                    .forEach(el => {
                                                        if (el)
                                                            new bootstrap.Toast(el, {delay: 2500, autohide: true}).show();
                                                    });
        </script>
    </body>
</html>
