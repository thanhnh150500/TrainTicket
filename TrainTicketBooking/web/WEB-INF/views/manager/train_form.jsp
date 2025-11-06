<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>
            <c:choose>
                <c:when test="${t != null && t.trainId != null}">Sửa tàu #${t.trainId}</c:when>
                <c:otherwise>Thêm tàu</c:otherwise>
            </c:choose>
        </title>
        <meta name="csrf-token" content="${sessionScope.csrfToken}">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    </head>
    <body class="p-4">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <h3 class="mb-3">
            <c:choose>
                <c:when test="${t != null && t.trainId != null}">Sửa tàu #${t.trainId}</c:when>
                <c:otherwise>Thêm tàu</c:otherwise>
            </c:choose>
        </h3>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/manager/trains" accept-charset="UTF-8">
            <input type="hidden" name="op" value="save" />
            <!-- CSRF: bắt buộc -->
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}" />

            <c:if test="${t != null && t.trainId != null}">
                <input type="hidden" name="train_id" value="${t.trainId}" />
            </c:if>

            <div class="mb-3">
                <label class="form-label">Mã tàu (code)</label>
                <input type="text" name="code" class="form-control" value="${t.code}" required maxlength="40">
                <div class="form-text">Ví dụ: SE1, SE2… (duy nhất)</div>
            </div>

            <div class="mb-3">
                <label class="form-label">Tên tàu</label>
                <input type="text" name="name" class="form-control" value="${t.name}" required maxlength="100">
            </div>

            <div class="d-flex gap-2">
                <button class="btn btn-primary" type="submit"><i class="bi bi-save"></i> Lưu</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/trains">Hủy</a>
            </div>
        </form>

        <script>
            // Nếu sau này submit bằng fetch/axios: dùng header X-CSRF-Token = window.CSRF_TOKEN
            window.CSRF_TOKEN = document.querySelector('meta[name="csrf-token"]').content;
        </script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
