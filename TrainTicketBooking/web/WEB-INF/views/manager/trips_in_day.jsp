<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%
  String ctx = request.getContextPath();
  java.time.LocalDate day = (java.time.LocalDate) request.getAttribute("day");
%>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Lịch chạy trong ngày</title>
        <link rel="stylesheet" href="<%=ctx%>/assets/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="<%=ctx%>/assets/icons/bootstrap-icons.min.css">
    </head>
    <body class="bg-light">
        <%@ include file="/WEB-INF/views/layout/_header.jsp" %>

        <div class="container py-3">
            <h3 class="mb-3">Tất cả chuyến trong ngày <span class="text-primary"><%= day %></span></h3>

            <form class="row g-2 mb-3" method="get">
                <div class="col-auto">
                    <label class="form-label">Ngày</label>
                    <input type="date" class="form-control" name="date" value="<%= day %>" onchange="this.form.submit()">
                </div>
                <div class="col-auto">
                    <label class="form-label">Vùng</label>
                    <select class="form-select" name="regionId" onchange="this.form.submit()">
                        <option value="">Tất cả</option>
                        <c:forEach var="r" items="${regions}">
                            <option value="${r.regionId}" ${param.regionId == r.regionId ? 'selected' : ''}>${r.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-auto">
                    <label class="form-label">Thành phố</label>
                    <select class="form-select" name="cityId" onchange="this.form.submit()">
                        <option value="">Chọn thành phố</option>
                        <c:forEach var="c" items="${cities}">
                            <option value="${c.cityId}" ${param.cityId == c.cityId ? 'selected' : ''}>${c.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-auto">
                    <label class="form-label">Ga đi (ID)</label>
                    <input class="form-control" name="originStationId" value="${param.originStationId}" placeholder="VD: 1">
                </div>
                <div class="col-auto">
                    <label class="form-label">Ga đến (ID)</label>
                    <input class="form-control" name="destStationId" value="${param.destStationId}" placeholder="VD: 2">
                </div>
                <div class="col-auto align-self-end">
                    <button class="btn btn-primary"><i class="bi bi-search"></i> Lọc</button>
                    <a class="btn btn-outline-secondary" href="<%=ctx%>/customer/trips/day">Reset</a>
                </div>
            </form>

            <c:choose>
                <c:when test="${empty trips}">
                    <div class="alert alert-info">Không có chuyến nào trong ngày này theo điều kiện lọc.</div>
                </c:when>
                <c:otherwise>
                    <div class="list-group">
                        <c:forEach var="t" items="${trips}">
                            <div class="list-group-item">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <div class="fw-semibold">
                                            <i class="bi bi-train-front"></i>
                                            ${t.trainCode}
                                            <span class="text-muted">(${t.trainName})</span>
                                        </div>
                                        <div>
                                            <span class="me-2">${t.originName}</span>
                                            <i class="bi bi-arrow-right"></i>
                                            <span class="ms-2">${t.destName}</span>
                                        </div>
                                        <div class="small text-muted">
                                            Khởi hành:
                                            <c:out value="${t.departAt}"/>
                                            &nbsp; | &nbsp;
                                            Đến nơi:
                                            <c:out value="${t.arriveAt}"/>
                                            &nbsp; | &nbsp;
                                            Trạng thái: <span class="badge bg-secondary">${t.status}</span>
                                        </div>
                                    </div>
                                    <div>
                                        <a href="<%=ctx%>/seatmap?tripId=${t.tripId}" class="btn btn-outline-primary">
                                            Chọn ghế
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <script src="<%=ctx%>/assets/bootstrap/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
