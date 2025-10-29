<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>
            <c:choose>
                <c:when test="${s != null && s.stationId != null}">Sửa ga #${s.stationId}</c:when>
                <c:otherwise>Thêm ga</c:otherwise>
            </c:choose>
        </title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet" />
    </head>
    <body class="p-4">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <h3 class="mb-3">
            <c:choose>
                <c:when test="${s != null && s.stationId != null}">Sửa ga #${s.stationId}</c:when>
                <c:otherwise>Thêm ga</c:otherwise>
            </c:choose>
        </h3>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/manager/stations" accept-charset="UTF-8">
            <input type="hidden" name="op" value="save" />
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}" />
            <c:if test="${s != null && s.stationId != null}">
                <input type="hidden" name="station_id" value="${s.stationId}" />
            </c:if>

            <div class="mb-3">
                <label class="form-label">Thành phố</label>
                <select name="city_id" class="form-select" required>
                    <option value="">-- chọn thành phố --</option>
                    <c:forEach items="${cities}" var="c">
                        <option value="${c.cityId}"
                                <c:if test="${s.cityId != null && s.cityId == c.cityId}">selected</c:if>>
                            <c:out value="${c.name}"/> (<c:out value="${c.code}"/>)
                        </option>

                    </c:forEach>
                </select>
            </div>

            <div class="mb-3">
                <label class="form-label">Mã ga (code)</label>
                <input type="text" name="code" class="form-control"
                       value="<c:out value='${s.code}'/>"
                       required maxlength="20"
                       pattern="[A-Za-z0-9\-_]+"
                       title="Chỉ chữ, số, dấu '-' hoặc '_'"/>

                <div class="form-text">Ví dụ: GAHN, GASA (duy nhất).</div>
            </div>

            <div class="mb-3">
                <label class="form-label">Tên ga</label>
                <input type="text" name="name" class="form-control"
                       value="<c:out value='${s.name}'/>"
                       required maxlength="150"/>
            </div>

            <div class="mb-3">
                <label class="form-label">Địa chỉ</label>
                <input type="text" name="address" class="form-control"
                       value="<c:out value='${s.address}'/>"
                       maxlength="255"/><input type="text" name="address" class="form-control" value="${s.address}" maxlength="255" />
            </div>

            <div class="d-flex gap-2">
                <button class="btn btn-primary" type="submit"><i class="bi bi-save"></i> Lưu</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/stations">Hủy</a>
            </div>
        </form>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
