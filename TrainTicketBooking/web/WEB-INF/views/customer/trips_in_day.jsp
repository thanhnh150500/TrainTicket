<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Chuyến tàu trong ngày</title>
        <link rel="stylesheet" href="${ctx}/assets/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="${ctx}/assets/icons/bootstrap-icons.min.css">
        <link rel="stylesheet" href="${ctx}/assets/css/trips_in_day.css"><!-- (tùy chọn) -->
        <style>
            .badge-status {
                font-weight: 600;
            }
            .table thead th {
                white-space: nowrap;
            }
        </style>
    </head>
    <body class="bg-light">
        <%@ include file="/WEB-INF/views/layout/_header.jsp" %>

        <div class="container py-4">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <h3 class="mb-0">
                    <i class="bi bi-calendar3 me-1"></i>
                    Tất cả chuyến tàu ngày <span class="text-primary">${day}</span>
                </h3>
                <a class="btn btn-outline-secondary" href="${ctx}/customer/stations">
                    <i class="bi bi-geo-alt"></i> Xem danh sách ga theo khu vực
                </a>
            </div>

            <!-- Bộ lọc -->
            <form class="row g-3 align-items-end mb-4" method="get" action="${ctx}/customer/trips/day">
                <div class="col-md-3">
                    <label class="form-label">Ngày</label>
                    <input type="date" name="date" class="form-control" value="${day}">
                </div>

                <div class="col-md-3">
                    <label class="form-label">Vùng</label>
                    <select name="regionId" class="form-select" onchange="this.form.submit()">
                        <option value="">-- Tất cả vùng --</option>
                        <c:forEach var="r" items="${regions}">
                            <option value="${r.regionId}" ${r.regionId eq regionId ? 'selected' : ''}>
                                <c:out value="${r.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-3">
                    <label class="form-label">Thành phố (theo vùng)</label>
                    <select name="cityId" class="form-select" onchange="this.form.submit()">
                        <option value="">-- Tất cả thành phố --</option>
                        <c:forEach var="c0" items="${cities}">
                            <option value="${c0.cityId}" ${c0.cityId eq cityId ? 'selected' : ''}>
                                <c:out value="${c0.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-3 d-none d-md-block"></div>

                <div class="col-md-3">
                    <label class="form-label">Ga đi (ID tuỳ chọn)</label>
                    <input type="number" name="originStationId" class="form-control"
                           value="${originStationId}" placeholder="VD: 101">
                </div>

                <div class="col-md-3">
                    <label class="form-label">Ga đến (ID tuỳ chọn)</label>
                    <input type="number" name="destStationId" class="form-control"
                           value="${destStationId}" placeholder="VD: 205">
                </div>

                <div class="col-md-3">
                    <button class="btn btn-primary w-100">
                        <i class="bi bi-search"></i> Lọc chuyến
                    </button>
                </div>

                <div class="col-md-3">
                    <a class="btn btn-outline-secondary w-100"
                       href="${ctx}/customer/trips/day?date=${day}">
                        <i class="bi bi-x-circle"></i> Xoá lọc
                    </a>
                </div>
            </form>

            <!-- Danh sách chuyến -->
            <c:choose>
                <c:when test="${empty trips}">
                    <div class="alert alert-info">
                        <i class="bi bi-info-circle"></i>
                        Không có chuyến nào phù hợp trong ngày này. Hãy đổi ngày hoặc thay đổi bộ lọc.
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive shadow-sm bg-white rounded">
                        <table class="table table-hover mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th>#</th>
                                    <th>Tuyến</th>
                                    <th>Tàu</th>
                                    <th>Khởi hành</th>
                                    <th>Đến nơi</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="t" items="${trips}">
                                    <tr>
                                        <td class="text-muted">#${t.tripId}</td>
                                        <td>
                                            <div class="fw-semibold">
                                                <c:out value="${t.originName}"/> →
                                                <c:out value="${t.destName}"/>
                                            </div>
                                            <div class="small text-muted">
                                                <c:out value="${t.originCityName}"/> • Vùng:
                                                <c:out value="${t.originRegionName}"/>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="fw-semibold"><c:out value="${t.trainCode}"/></div>
                                            <div class="small text-muted"><c:out value="${t.trainName}"/></div>
                                        </td>
                                        <td><span class="fw-semibold"><c:out value="${t.departAt}"/></span></td>
                                        <td><c:out value="${t.arriveAt}"/></td>
                                        <td>
                                            <span class="badge badge-status
                                                  ${t.status eq 'SCHEDULED' ? 'bg-primary' :
                                                    t.status eq 'RUNNING'   ? 'bg-success' :
                                                    t.status eq 'FINISHED'  ? 'bg-secondary' : 'bg-danger'}">
                                                  <c:out value="${t.status}"/>
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <script src="${ctx}/assets/bootstrap/js/bootstrap.bundle.min.js"></script>
        <script src="${ctx}/assets/js/trips_in_day.js"></script>
    </body>
</html>
