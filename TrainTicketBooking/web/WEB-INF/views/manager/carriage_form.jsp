<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>
            <c:choose>
                <c:when test="${c != null && c.carriageId != null}">Sửa toa #${c.carriageId}</c:when>
                <c:otherwise>Thêm toa</c:otherwise>
            </c:choose>
        </title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    </head>
    <body class="p-4">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <h3 class="mb-3">
            <c:choose>
                <c:when test="${c != null && c.carriageId != null}">Sửa toa #${c.carriageId}</c:when>
                <c:otherwise>Thêm toa</c:otherwise>
            </c:choose>
        </h3>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/manager/carriages" accept-charset="UTF-8">
        <input type="hidden" name="op" value="save"/>
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
        <c:if test="${c != null && c.carriageId != null}">
            <input type="hidden" name="carriage_id" value="${c.carriageId}"/>
        </c:if>

        <div class="row g-3">
            <div class="col-md-6">
                <label class="form-label">Tàu</label>
                <select name="train_id" class="form-select" required>
                    <option value="">-- chọn tàu --</option>
                    <c:forEach items="${trains}" var="t">
                        <option value="${t.trainId}" ${c.trainId != null && c.trainId == t.trainId ? 'selected' : ''}>
                            ${t.code} - ${t.name}
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-6">
                <label class="form-label">Hạng ghế mặc định</label>
                <select name="seat_class_id" class="form-select" required>
                    <option value="">-- chọn hạng ghế --</option>
                    <c:forEach items="${seatClasses}" var="sc">
                        <option value="${sc.seatClassId}" ${c.seatClassId != null && c.seatClassId == sc.seatClassId ? 'selected' : ''}>
                            ${sc.code} - ${sc.name}
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="mt-3">
            <label class="form-label">Mã toa (code)</label>
            <input type="text" name="code" class="form-control" value="${c.code}" required maxlength="20" placeholder="VD: C01"/>
            <div class="form-text">Code phải là duy nhất trong **cùng** một tàu.</div>
        </div>

        <div class="mt-3">
            <label class="form-label">Thứ tự (sort order)</label>
            <input type="number" name="sort_order" class="form-control" value="${c.sortOrder}" min="0" step="1"/>
        </div>

        <div class="d-flex gap-2 mt-3">
            <button class="btn btn-primary" type="submit">Lưu</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/carriages">Hủy</a>
        </div>
    </form>
</body>
</html>
