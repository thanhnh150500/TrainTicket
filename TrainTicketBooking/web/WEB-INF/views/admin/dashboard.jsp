<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <title>Trang Tổng Quan Quản Trị</title>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet" />
</head>
<body class="bg-light">

    <%-- (Dùng header của Admin) --%>
    <%@ include file="/WEB-INF/views/layout/_header_admin.jsp" %>

    <div class="container mt-4 tt-admin-dashboard">
    
        <%-- Hiển thị Flash Message (nếu có) --%>
        <c:if test="${not empty sessionScope.flash_success}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${sessionScope.flash_success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            <c:remove var="flash_success" scope="session"/>
        </c:if>
        
<!--        <div class="card mt-4 shadow-sm">
            <div class="card-body">
                <h5 class="card-title">Doanh thu dịch vụ (F&B) — Đã bán</h5>

                <c:choose>
                    <c:when test="${not empty fnbRevenues}">
                        <div class="mb-3 d-flex align-items-center justify-content-between">
                            <div>
                            </div>
                        </div>
                        <div class="mb-2">
                            <form method="get" class="row g-2 align-items-center" id="rangeForm">
                                <div class="col-auto">
                                    <label class="form-label small mb-0">Từ</label>
                                    <input type="date" name="from" class="form-control form-control-sm" value="${fromSelected}">
                                </div>
                                <div class="col-auto">
                                    <label class="form-label small mb-0">Đến</label>
                                    <input type="date" name="to" class="form-control form-control-sm" value="${toSelected}">
                                </div>
                                <div class="col-auto">
                                    <button class="btn btn-sm btn-primary mt-2">Áp dụng</button>
                                </div>
                            </form>
                        </div>
                        <div class="mb-4">
                            <canvas id="fnbRevenueChart" height="140"></canvas>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-striped table-hover">
                                <thead>
                                    <tr>
                                        <th style="width:48px">#</th>
                                        <th>Món / Dịch vụ</th>
                                        <th style="width:140px">Số lượng</th>
                                        <th style="width:160px">Doanh thu</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="r" items="${fnbRevenues}" varStatus="st">
                                        <tr>
                                            <td>${st.index + 1}</td>
                                            <td>${r.itemName}</td>
                                            <td>${r.totalQuantity}</td>
                                            <td>
                                                <fmt:formatNumber value="${r.totalAmount}" pattern="#,##0" /> đ
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
                        <script>
                            (function(){
                                // If server provided daily revenue, draw daily chart instead of per-item
                                var daily = <c:choose><c:when test="${not empty fnbDailyRevenues}">true</c:when><c:otherwise>false</c:otherwise></c:choose>;
                                if (daily) {
                                    var labels = [
                                        <c:forEach var="d" items="${fnbDailyRevenues}" varStatus="st">
                                            '<c:out value="${d.day}" />'<c:if test="${not st.last}">,</c:if>
                                        </c:forEach>
                                    ];
                                    var data = [
                                        <c:forEach var="d" items="${fnbDailyRevenues}" varStatus="st">
                                            ${d.totalAmount}<c:if test="${not st.last}">,</c:if>
                                        </c:forEach>
                                    ];

                                    var ctx = document.getElementById('fnbRevenueChart').getContext('2d');
                                    if (window._fnbRevenueChart instanceof Chart) window._fnbRevenueChart.destroy();
                                    window._fnbRevenueChart = new Chart(ctx, {
                                        type: 'line',
                                        data: { labels: labels, datasets: [{ label: 'Doanh thu (đ)', data: data, borderColor: '#0d6efd', backgroundColor: 'rgba(13,110,253,0.08)', tension: 0.3, fill: true, pointRadius: 3 }] },
                                        options: { responsive:true, maintainAspectRatio:false, scales:{ y:{ beginAtZero:true, suggestedMin:0, ticks:{ callback: function(v){ return v.toLocaleString() + ' đ'; } } } }, plugins:{ legend:{ display:false } } }
                                    });
                                } else {
                                    // fallback to per-item chart (previous behavior)
                                    var labels = [
                                        <c:forEach var="r" items="${fnbRevenues}" varStatus="st">
                                            '<c:out value="${r.itemName}" />'<c:if test="${not st.last}">,</c:if>
                                        </c:forEach>
                                    ];
                                    var data = [
                                        <c:forEach var="r" items="${fnbRevenues}" varStatus="st">
                                            ${r.totalAmount}<c:if test="${not st.last}">,</c:if>
                                        </c:forEach>
                                    ];
                                    var ctx = document.getElementById('fnbRevenueChart').getContext('2d');
                                    if (window._fnbRevenueChart instanceof Chart) window._fnbRevenueChart.destroy();
                                    window._fnbRevenueChart = new Chart(ctx, {
                                        type: 'line', data: { labels: labels, datasets:[{ label:'Doanh thu (đ)', data:data, borderColor:'#0d6efd', backgroundColor:'rgba(13,110,253,0.08)', tension:0.3, fill:true }] },
                                        options: { responsive:true, maintainAspectRatio:false, scales:{ y:{ beginAtZero:true, suggestedMin:0, ticks:{ callback:function(v){ return v.toLocaleString() + ' đ'; } } } }, plugins:{ legend:{ display:false } } }
                                    });
                                }
                            })();
                        </script>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-info">Chưa có dữ liệu doanh thu dịch vụ.</div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>-->
        
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>