<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Trạng thái thanh toán</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="${ctx}/assets/css/payment_status.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="container-xxl my-4">
            <div class="row g-3">
                <!-- Cột 1: Trạng thái -->
                <div class="col-lg-5">
                    <div class="card shadow-sm border-0 rounded-3">
                        <div class="card-body p-4">
                            <div class="d-flex align-items-center gap-3">
                                <div class="circle-ok"></div>
                                <div>
                                    <h5 class="mb-1">Thanh toán ${status == 'PAID' ? 'thành công' : status}</h5>
                                    <div class="text-muted">Mã đặt chỗ: <b>#${bookingId}</b></div>
                                </div>
                            </div>

                            <hr class="my-4"/>

                            <div class="d-flex justify-content-between">
                                <span>Tạm tính</span>
                                <b><c:out value="${subtotal}" /> đ</b>
                            </div>
                            <div class="d-flex justify-content-between mt-1">
                                <span>Giảm giá</span>
                                <b>- <c:out value="${discount}" /> đ</b>
                            </div>
                            <div class="d-flex justify-content-between mt-2 fs-5">
                                <span>Tổng thanh toán</span>
                                <b class="text-success"><c:out value="${total}" /> đ</b>
                            </div>

                            <c:if test="${not empty payment}">
                                <div class="alert alert-success mt-4 mb-0">
                                    <div class="fw-semibold mb-1">Phương thức: <c:out value="${payment.method}" /></div>
                                    <div class="mb-0">Trạng thái giao dịch: <b><c:out value="${payment.status}" /></b></div>
                                </div>
                            </c:if>

                            <div class="d-flex gap-2 mt-4">
                                <a class="btn btn-primary" href="${ctx}/booking-history">Xem lịch sử đặt vé</a>
                                <a class="btn btn-outline-secondary" href="${ctx}/">Về trang chủ</a>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Cột 2: Thông tin vé -->
                <div class="col-lg-7">
                    <div class="card shadow-sm border-0 rounded-3">
                        <div class="card-body p-4">
                            <h5 class="mb-3">Thông tin vé</h5>
                            <c:choose>
                                <c:when test="${empty items}">
                                    <div class="text-muted">Không có vé nào trong đơn này.</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="table-responsive">
                                        <table class="table align-middle">
                                            <thead>
                                                <tr>
                                                    <th>#</th>
                                                    <th>Toa</th>
                                                    <th>Ghế</th>
                                                    <th>Chuyến (trip_id)</th>
                                                    <th class="text-end">Giá</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="it" items="${items}" varStatus="st">
                                                    <tr>
                                                        <td>${st.index + 1}</td>
                                                        <td><c:out value="${it.carriageCode}" /></td>
                                                        <td><span class="badge text-bg-primary"><c:out value="${it.seatCode}" /></span></td>
                                                        <td><c:out value="${it.tripId}" /></td>
                                                        <td class="text-end"><c:out value="${it.amount}" /> đ</td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <div class="alert alert-info mt-3 mb-0">
                                Vé đã được ghi nhận **ĐÃ THANH TOÁN**. Bạn có thể xem/ tải vé điện tử trong mục lịch sử đặt vé.
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </body>
</html>
