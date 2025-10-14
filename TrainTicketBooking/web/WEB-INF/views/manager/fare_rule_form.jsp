<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>
            <c:choose>
                <c:when test="${f != null && f.fareRuleId != null}">Sửa giá tuyến #${f.fareRuleId}</c:when>
                <c:otherwise>Thêm giá tuyến</c:otherwise>
            </c:choose>
        </title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    </head>
    <body class="p-4">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <h3 class="mb-3">
            <c:choose>
                <c:when test="${f != null && f.fareRuleId != null}">Sửa giá tuyến #${f.fareRuleId}</c:when>
                <c:otherwise>Thêm giá tuyến</c:otherwise>
            </c:choose>
        </h3>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/manager/fare-rules" accept-charset="UTF-8">
        <input type="hidden" name="op" value="save"/>
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
        <c:if test="${f != null && f.fareRuleId != null}">
            <input type="hidden" name="fare_rule_id" value="${f.fareRuleId}"/>
        </c:if>

        <div class="row g-3">
            <div class="col-md-6">
                <label class="form-label">Tuyến</label>
                <select name="route_id" class="form-select" required>
                    <option value="">-- chọn tuyến --</option>
                    <c:forEach items="${routes}" var="r">
                        <option value="${r.routeId}" ${f.routeId != null && f.routeId == r.routeId ? 'selected' : ''}>
                            ${r.code} — ${r.originName} → ${r.destName}
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-6">
                <label class="form-label">Hạng ghế</label>
                <select name="seat_class_id" class="form-select" required>
                    <option value="">-- chọn hạng ghế --</option>
                    <c:forEach items="${seatClasses}" var="sc">
                        <option value="${sc.seatClassId}" ${f.seatClassId != null && f.seatClassId == sc.seatClassId ? 'selected' : ''}>
                            ${sc.code} - ${sc.name}
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="row g-3 mt-1">
            <div class="col-md-4">
                <label class="form-label">Giá cơ bản (VND)</label>
                <input type="number" step="0.01" min="0.01" name="base_price" class="form-control"
                       value="${f.basePrice}" required />
            </div>
            <div class="col-md-4">
                <label class="form-label">Hiệu lực từ</label>
                <input type="date" name="effective_from" class="form-control"
                       value="${f.effectiveFrom}" required />
            </div>
            <div class="col-md-4">
                <label class="form-label">Đến (tuỳ chọn)</label>
                <input type="date" name="effective_to" class="form-control"
                       value="${f.effectiveTo}" />
            </div>
        </div>

        <div class="d-flex gap-2 mt-3">
            <button class="btn btn-primary" type="submit">Lưu</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/fare-rules">Hủy</a>
        </div>
    </form>
</body>
</html>
