<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c"  uri="jakarta.tags.core"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Ga theo khu vực</title>
        <link rel="stylesheet" href="${ctx}/assets/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="${ctx}/assets/icons/bootstrap-icons.min.css">
        <link rel="stylesheet" href="${ctx}/assets/css/stations_browse.css"><!-- (tùy chọn) -->
    </head>
    <body class="bg-light">
        <%@ include file="/WEB-INF/views/layout/_header.jsp" %>

        <div class="container py-4">
            <div class="page-header">
                <h3 class="mb-0">
                    <i class="bi bi-geo-alt"></i> Ga tàu theo khu vực
                </h3>
                <a class="btn btn-outline-secondary" href="${ctx}/customer/trips/day">
                    <i class="bi bi-calendar3"></i> Xem tất cả chuyến hôm nay
                </a>
            </div>

            <!-- Bộ lọc -->
            <form class="row g-3 align-items-end mb-4" method="get" action="${ctx}/customer/stations">
                <div class="col-md-4">
                    <label for="regionId" class="form-label">Vùng</label>
                    <select class="form-select" id="regionId" name="regionId" onchange="this.form.submit()">
                        <option value="">-- Tất cả vùng --</option>
                        <c:forEach var="r" items="${regions}">
                            <option value="${r.regionId}" ${r.regionId == regionId ? 'selected' : ''}>${r.name}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-4">
                    <label for="cityId" class="form-label">Thành phố</label>
                    <select class="form-select" id="cityId" name="cityId" onchange="this.form.submit()">
                        <option value="">-- Chọn thành phố --</option>
                        <c:forEach var="c0" items="${cities}">
                            <option value="${c0.cityId}" ${c0.cityId == cityId ? 'selected' : ''}>${c0.name}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-4 text-md-end">
                    <a class="btn btn-outline-secondary mt-4" href="${ctx}/customer/stations">
                        <i class="bi bi-x-circle"></i> Xóa lọc
                    </a>
                </div>
            </form>

            <!-- Kết quả -->
            <c:choose>
                <c:when test="${empty stations}">
                    <div class="alert alert-info">
                        <i class="bi bi-info-circle"></i>
                        Hãy chọn <b>Vùng</b> rồi <b>Thành phố</b> để xem danh sách ga.
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive shadow-sm bg-white rounded">
                        <table class="table table-hover mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th style="width:120px">Mã ga</th>
                                    <th>Tên ga</th>
                                    <th>Địa chỉ</th>
                                    <th style="width:220px"></th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="s" items="${stations}">
                                    <tr>
                                        <td class="fw-semibold"><c:out value="${s.code}"/></td>
                                        <td>
                                            <div class="fw-semibold"><c:out value="${s.name}"/></div>
                                            <div class="small text-muted"><c:out value="${s.cityName}"/></div>
                                        </td>
                                        <td class="text-muted">
                                            <c:out value="${empty s.address ? '—' : s.address}"/>
                                        </td>
                                        <td class="text-end">
                                            <a class="btn btn-sm btn-outline-primary"
                                               href="${ctx}/customer/trips/day?originStationId=${s.stationId}">
                                                <i class="bi bi-train-front"></i> Chuyến đi từ đây
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
        <script src="${ctx}/assets/js/stations_browse.js"></script>                
        <script src="${ctx}/assets/bootstrap/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
