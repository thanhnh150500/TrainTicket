<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>
            <c:choose>
                <c:when test="${t != null && t.tripId != null}">Sửa chuyến #${t.tripId}</c:when>
                <c:otherwise>Thêm chuyến</c:otherwise>
            </c:choose>
        </title>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet" />
    </head>
    <body class="p-4">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <h3 class="mb-3">
            <c:choose>
                <c:when test="${t != null && t.tripId != null}">Sửa chuyến #${t.tripId}</c:when>
                <c:otherwise>Thêm chuyến</c:otherwise>
            </c:choose>
        </h3>

        <c:if test="${not empty error}">
            <div class="alert alert-danger"><c:out value="${error}"/></div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/manager/trips" accept-charset="UTF-8">
            <input type="hidden" name="op" value="save"/>
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
            <c:if test="${t != null && t.tripId != null}">
                <input type="hidden" name="trip_id" value="${t.tripId}"/>
            </c:if>

            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Tuyến</label>
                    <select id="route_id" name="route_id" class="form-select" required>
                        <option value="">-- chọn tuyến --</option>
                        <c:forEach items="${routes}" var="r">
                            <option value="${r.routeId}" ${t.routeId != null && t.routeId == r.routeId ? 'selected' : ''}>
                                <c:out value="${r.code}"/> — <c:out value="${r.originName}"/> → <c:out value="${r.destName}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-6">
                    <label class="form-label">Đoàn tàu</label>
                    <select id="train_id" name="train_id" class="form-select" required>
                        <option value="">-- chọn tàu --</option>
                        <c:forEach items="${trains}" var="tr">
                            <option value="${tr.trainId}" ${t.trainId != null && t.trainId == tr.trainId ? 'selected' : ''}>
                                <c:out value="${tr.code}"/> — <c:out value="${tr.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <hr class="my-4"/>

            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Khởi hành</label>
                    <input type="datetime-local" name="depart_at" class="form-control"
                           value="${departAtInput}" required />
                    <div class="form-text">Định dạng: yyyy-MM-ddTHH:mm</div>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Đến nơi</label>
                    <input type="datetime-local" name="arrive_at" class="form-control"
                           value="${arriveAtInput}" required />
                </div>
            </div>

            <div class="mt-3">
                <label class="form-label">Trạng thái</label>
                <c:set var="st" value="${empty t.status ? 'SCHEDULED' : t.status}" />
                <select name="status" class="form-select" required>
                    <option value="SCHEDULED" ${st=='SCHEDULED'?'selected':''}>SCHEDULED</option>
                    <option value="RUNNING"   ${st=='RUNNING'  ?'selected':''}>RUNNING</option>
                    <option value="CANCELED"  ${st=='CANCELED' ?'selected':''}>CANCELED</option>
                    <option value="FINISHED"  ${st=='FINISHED' ?'selected':''}>FINISHED</option>
                </select>
            </div>

            <div class="d-flex gap-2 mt-4">
                <button class="btn btn-primary" type="submit">Lưu</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/trips">Hủy</a>
            </div>
        </form>
    </body>
</html>
