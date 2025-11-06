<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>
            <c:choose>
                <c:when test="${r != null && r.routeId != null}">Sửa tuyến #${r.routeId}</c:when>
                <c:otherwise>Thêm tuyến</c:otherwise>
            </c:choose>
        </title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    </head>
    <body class="p-4">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <h3 class="mb-3">
            <c:choose>
                <c:when test="${r != null && r.routeId != null}">Sửa tuyến #${r.routeId}</c:when>
                <c:otherwise>Thêm tuyến</c:otherwise>
            </c:choose>
        </h3>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/manager/routes" accept-charset="UTF-8">
            <input type="hidden" name="op" value="save" />
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}" />
            <c:if test="${r != null && r.routeId != null}">
                <input type="hidden" name="route_id" value="${r.routeId}" />
            </c:if>

            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Ga đi (Origin)</label>
                    <select name="origin_station_id" class="form-select" required>
                        <option value="">-- chọn ga --</option>
                        <c:forEach items="${stations}" var="s">
                            <option value="${s.stationId}" ${r.originStationId != null && r.originStationId == s.stationId ? 'selected' : ''}>
                                ${s.name} (${s.code})
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Ga đến (Destination)</label>
                    <select name="dest_station_id" class="form-select" required>
                        <option value="">-- chọn ga --</option>
                        <c:forEach items="${stations}" var="s">
                            <option value="${s.stationId}" ${r.destStationId != null && r.destStationId == s.stationId ? 'selected' : ''}>
                                ${s.name} (${s.code})
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="mt-3">
                <label class="form-label">Mã tuyến (code)</label>
                <input type="text" name="code" class="form-control" value="${r.code}" required maxlength="40"
                       placeholder="Ví dụ: HNI-HCM"/>
                <div class="form-text">Gợi ý: &lt;OriginCode&gt;-&lt;DestCode&gt; (duy nhất).</div>
            </div>

            <div class="d-flex gap-2 mt-3">
                <button class="btn btn-primary" type="submit">Lưu</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/routes">Hủy</a>
            </div>
        </form>
    </body>
</html>
