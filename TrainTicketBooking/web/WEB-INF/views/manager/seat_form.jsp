<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>
            <c:choose>
                <c:when test="${s != null && s.seatId != null}">Sửa ghế #${s.seatId}</c:when>
                <c:otherwise>Thêm ghế</c:otherwise>
            </c:choose>
        </title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    </head>
    <body class="p-4">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <h3 class="mb-3">
            <c:choose>
                <c:when test="${s != null && s.seatId != null}">Sửa ghế #${s.seatId}</c:when>
                <c:otherwise>Thêm ghế</c:otherwise>
            </c:choose>
        </h3>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/manager/seats" accept-charset="UTF-8">
            <input type="hidden" name="op" value="save"/>
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
            <c:if test="${s != null && s.seatId != null}">
                <input type="hidden" name="seat_id" value="${s.seatId}"/>
            </c:if>

            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Toa</label>
                    <select name="carriage_id" class="form-select" required>
                        <option value="">-- chọn toa --</option>
                        <c:forEach items="${carriages}" var="c">
                            <option value="${c.carriageId}"
                                    <c:if test="${s.carriageId != null && s.carriageId == c.carriageId}">selected</c:if>>
                                <c:out value="${c.trainCode}"/> - <c:out value="${c.code}"/>
                                <c:if test="${not empty c.trainName}">(<c:out value='${c.trainName}'/>)</c:if>
                                </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Hạng ghế</label>
                    <select name="seat_class_id" class="form-select" required>
                        <option value="">-- chọn hạng ghế --</option>
                        <c:forEach items="${seatClasses}" var="sc">
                            <option value="${sc.seatClassId}" ${s.seatClassId != null && s.seatClassId == sc.seatClassId ? 'selected' : ''}>
                                ${sc.code} - ${sc.name}
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="mt-3">
                <label class="form-label">Mã ghế (code)</label>
                <input type="text" name="code" class="form-control"
                       value="<c:out value='${s.code}'/>"
                       required maxlength="20" placeholder="VD: 01A"
                       pattern="[A-Za-z0-9\-_]+"
                       title="Chỉ chữ, số, dấu '-' hoặc '_'"/>

                <div class="form-text">Code phải là duy nhất trong **cùng** một toa.</div>
            </div>

            <div class="mt-3">
                <label class="form-label">Vị trí (tùy chọn)</label>
                <input type="text" name="position_info" class="form-control"
                       value="<c:out value='${s.positionInfo}'/>"
                       maxlength="50" placeholder="VD: Tầng 1 / Dãy B"/>

            </div>

            <div class="d-flex gap-2 mt-3">
                <button class="btn btn-primary" type="submit">Lưu</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/seats">Hủy</a>
            </div>
        </form>
    </body>
</html>
