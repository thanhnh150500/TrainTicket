<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"  %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Chi tiết đơn đặt vé</title>
        <link rel="stylesheet" href="${ctx}/assets/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="${ctx}/assets/icons/bootstrap-icons.min.css">
        <style>
            .kv {
                display:flex;
                gap:12px;
                margin-bottom:6px;
            }
            .kv .k {
                width:160px;
                color:#6c757d;
            }
            .pill {
                border-radius: 999px;
                padding: 2px 10px;
                font-size: .85rem;
                border:1px solid rgba(0,0,0,.1);
            }
        </style>
    </head>
    <body class="bg-light">
        <%@ include file="/WEB-INF/views/layout/_header.jsp" %>

        <div class="container py-4">

            <div class="d-flex align-items-center justify-content-between mb-3">
                <h3 class="m-0">🎫 Chi tiết đơn đặt vé</h3>
            </div>

            <c:if test="${empty booking}">
                <div class="alert alert-danger">Không tìm thấy đơn đặt vé.</div>
            </c:if>

            <c:if test="${not empty booking}">
                <!-- Thông tin đơn -->
                <div class="card shadow-sm mb-3">
                    <div class="card-header bg-white">
                        <span class="pill bg-light">Mã đơn: <b>BK-${booking.bookingId}</b></span>
                        <c:choose>
                            <c:when test="${booking.status=='PAID'}">
                                <span class="pill bg-success text-white ms-2"><i class="bi bi-check2-circle"></i> ĐÃ THANH TOÁN</span>
                            </c:when>
                            <c:when test="${booking.status=='HOLD'}">
                                <span class="pill bg-warning text-dark ms-2"><i class="bi bi-hourglass-split"></i> GIỮ CHỖ</span>
                            </c:when>
                            <c:when test="${booking.status=='DRAFT'}">
                                <span class="pill bg-secondary text-white ms-2">NHÁP</span>
                            </c:when>
                            <c:when test="${booking.status=='CANCELED'}">
                                <span class="pill bg-danger text-white ms-2">ĐÃ HỦY</span>
                            </c:when>
                            <c:when test="${booking.status=='EXPIRED'}">
                                <span class="pill bg-dark text-white ms-2">HẾT HẠN</span>
                            </c:when>
                            <c:otherwise>
                                <span class="pill bg-light text-dark ms-2">${booking.status}</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="card-body">
                        <div class="kv"><div class="k">Ngày tạo</div><div class="v">${createdAtStr}</div></div>
                        <div class="kv"><div class="k">Ngày thanh toán</div>
                            <div class="v"><c:out value="${empty paidAtStr ? '—' : paidAtStr}"/></div>
                        </div>
                        <div class="kv"><div class="k">Email liên hệ</div><div class="v"><c:out value="${booking.contactEmail}"/></div></div>
                        <div class="kv"><div class="k">SĐT liên hệ</div><div class="v"><c:out value="${booking.contactPhone}"/></div></div>
                        <div class="kv"><div class="k">Tổng tiền</div>
                            <div class="v"><b><fmt:formatNumber value="${booking.totalAmount}" type="currency"/></b></div>
                        </div>
                    </div>
                </div>

                <!-- Thông tin chuyến -->
                <div class="card shadow-sm mb-3">
                    <div class="card-header bg-white">🚆 Chuyến tàu</div>
                    <div class="card-body">
                        <div class="kv"><div class="k">Mã chuyến</div><div class="v"><c:out value="${tripId}"/></div></div>
                        <div class="kv"><div class="k">Tàu</div>
                            <div class="v">
                                <c:out value="${trainCode}"/>
                                <c:if test="${not empty trainName}"> — <span class="text-muted"><c:out value="${trainName}"/></span></c:if>
                                </div>
                            </div>
                            <div class="kv"><div class="k">Khởi hành</div><div class="v"><c:out value="${empty departAtStr ? '—' : departAtStr}"/></div></div>
                        <div class="kv"><div class="k">Đến nơi</div><div class="v"><c:out value="${empty arriveAtStr ? '—' : arriveAtStr}"/></div></div>
                    </div>
                </div>

                <!-- Danh sách ghế -->
                <div class="card shadow-sm">
                    <div class="card-header bg-white">💺 Ghế / Hạng / Giá</div>
                    <div class="table-responsive">
                        <table class="table align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th style="width:80px">#</th>
                                    <th>Ghế</th>
                                    <th>Toa</th>
                                    <th>Hạng ghế</th>
                                    <th class="text-end">Giá vé</th>
                                    <th class="text-end">Giảm giá</th>
                                    <th class="text-end">Thành tiền</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="it" items="${items}" varStatus="st">
                                    <tr>
                                        <td>${st.index + 1}</td>
                                        <td>${it.seatCode}</td>
                                        <td>${it.carriageCode}</td>
                                        <td>${it.seatClassName}</td>
                                        <td class="text-end"><fmt:formatNumber value="${it.basePrice}" type="currency"/></td>
                                        <td class="text-end"><fmt:formatNumber value="${it.discountAmount}" type="currency"/></td>
                                        <td class="text-end"><fmt:formatNumber value="${it.amount}" type="currency"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                            <tfoot class="table-light">
                                <tr>
                                    <th colspan="6" class="text-end">Tổng cộng</th>
                                    <th class="text-end"><fmt:formatNumber value="${booking.totalAmount}" type="currency"/></th>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>

                <div class="mt-3">
                    <a href="${ctx}/home" class="btn btn-secondary">
                        <i class="bi bi-house"></i> Về Trang chủ
                    </a>
                </div>
            </c:if>
        </div>

        <script src="${ctx}/assets/bootstrap/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
