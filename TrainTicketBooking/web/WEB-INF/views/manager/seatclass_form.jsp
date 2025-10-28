<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>
            <c:choose>
                <c:when test="${sc != null && sc.seatClassId != null}">Sửa hạng ghế #${sc.seatClassId}</c:when>
                <c:otherwise>Thêm hạng ghế</c:otherwise>
            </c:choose>
        </title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    </head>
    <body class="p-4">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <h3 class="mb-3">
            <c:choose>
                <c:when test="${sc != null && sc.seatClassId != null}">Sửa hạng ghế #${sc.seatClassId}</c:when>
                <c:otherwise>Thêm hạng ghế</c:otherwise>
            </c:choose>
        </h3>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/manager/seat-classes" accept-charset="UTF-8">
        <input type="hidden" name="op" value="save"/>
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
        <c:if test="${sc != null && sc.seatClassId != null}">
            <input type="hidden" name="seat_class_id" value="${sc.seatClassId}"/>
        </c:if>

        <div class="mb-3">
            <label class="form-label">Mã (code)</label>
            <input type="text" name="code" class="form-control" value="${sc.code}" required maxlength="20"/>
        </div>
        <div class="mb-3">
            <label class="form-label">Tên</label>
            <input type="text" name="name" class="form-control" value="${sc.name}" required maxlength="100"/>
        </div>

        <div class="d-flex gap-2">
            <button class="btn btn-primary" type="submit">Lưu</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/seat-classes">Hủy</a>
        </div>
    </form>
</body>
</html>
