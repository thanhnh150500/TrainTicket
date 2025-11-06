<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Thông tin khách hàng</title>
        <meta name="csrf-token" content="${sessionScope.csrfToken}">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="${ctx}/assets/css/checkout.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <%@ include file="/WEB-INF/views/layout/_header.jsp" %>

        <div class="container-xxl my-3">
            <div class="row g-3">

                <!-- ===== LEFT: FORM (bọc tất cả input vào 1 form) ===== -->
                <div class="col-lg-8">
                    <form id="checkoutForm" action="${ctx}/confirm-booking" method="post" class="m-0">
                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="tripId" value="${param.tripId}"/>

                        <!-- Thông tin liên hệ -->
                        <div class="tt-card mb-3">
                            <div class="tt-card-title">Thông tin liên hệ</div>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Số điện thoại <span class="text-danger">*</span></label>
                                    <input type="tel" class="form-control" name="phone" placeholder="Nhập số điện thoại để liên lạc" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Email <span class="text-danger">*</span></label>
                                    <input type="email" class="form-control" name="email" placeholder="Nhập email để nhận vé điện tử" required>
                                </div>
                                <div class="col-12">
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" id="needInvoice" name="needInvoice">
                                        <label class="form-check-label" for="needInvoice">Xuất hóa đơn điện tử</label>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Thông tin hành khách + GHẾ đã chọn -->
                        <div class="tt-card mb-3">
                            <div class="tt-card-title d-flex justify-content-between align-items-center">
                                <span>Thông tin hành khách</span>
                            </div>

                            <c:forEach var="s" items="${selectedSeats}">
                                <!-- Chip ghế -->
                                <div class="seat-chip">
                                    <div class="chip-line">
                                        <div>
                                            <div class="chip-label">Chiều đi</div>
                                            <div class="chip-sub">
                                                Toa ${s.carriageId} - Ghế ${s.seatCode}
                                                <c:if test="${not empty s.seatClassName}">
                                                    · <span class="text-muted">${s.seatClassName}</span>
                                                </c:if>
                                            </div>
                                        </div>
                                        <div class="chip-price">
                                            <c:out value="${s.price}"/> đ
                                        </div>
                                    </div>
                                </div>

                                <!-- Hành khách tương ứng -->
                                <div class="passenger-card">
                                    <div class="row g-3">
                                        <div class="col-md-4">
                                            <label class="form-label">Họ và tên <span class="text-danger">*</span></label>
                                            <input type="text" class="form-control" name="fullname[]" placeholder="VD: Nguyễn Văn Nam" required>
                                        </div>
                                        <div class="col-md-4">
                                            <label class="form-label">Ngày sinh</label>
                                            <input type="text" class="form-control dob" name="dob[]" placeholder="dd/mm/yyyy">
                                        </div>
                                        <div class="col-md-4">
                                            <label class="form-label">CCCD / Passport</label>
                                            <input type="text" class="form-control" name="idNumber[]" placeholder="Nhập CCCD hoặc Passport">
                                        </div>
                                    </div>
                                </div>

                                <input type="hidden" name="seatId"      value="${s.seatId}">
                                <input type="hidden" name="seatClassId" value="${s.seatClassId}">
                                <input type="hidden" name="price"       value="${s.price}">
                            </c:forEach>
                        </div>

                        <!-- Actions -->
                        <div class="d-flex justify-content-between align-items-center mt-3">
                            <a class="btn btn-outline-secondary px-4" href="${ctx}/seatmap?tripId=${param.tripId}">
                                <span class="me-1">〈</span> Quay lại
                            </a>
                            <button id="btnPay" type="submit" class="btn-pay">
                                Thanh toán <span id="btnPayCountdown">(05:00)</span> 〉
                            </button>
                        </div>
                    </form>
                </div>

                <!-- ===== RIGHT: Tóm tắt ===== -->
                <div class="col-lg-4">
                    <div class="tt-card mb-3">
                        <div class="trip-head d-flex justify-content-between align-items-start">
                            <div>
                                <div class="trip-route fw-semibold">
                                    <c:out value="${trip.originName}"/> → <c:out value="${trip.destName}"/>
                                </div>
                                <div class="small text-muted"><c:out value="${trip.departDateStr}"/></div>
                            </div>
                            <div class="fw-semibold"><c:out value="${trip.trainCode}"/></div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center my-2">
                            <div>
                                <div class="big-hour"><c:out value="${trip.departTimeStr}"/></div>
                                <div class="small text-muted">Đi</div>
                            </div>
                            <i class="bi bi-train-front fs-4 opacity-50"></i>
                            <div class="text-end">
                                <div class="big-hour"><c:out value="${trip.arriveTimeStr}"/></div>
                                <div class="small text-muted">Đến</div>
                            </div>
                        </div>

                        <hr>
                        <div class="row small">
                            <div class="col">Tổng tiền vé</div>
                            <div class="col text-end"><c:out value="${amounts.subtotal}"/></div>
                        </div>
                        <div class="row small">
                            <div class="col">Phí bảo hiểm</div>
                            <div class="col text-end">+ <c:out value="${amounts.insurance}"/></div>
                        </div>
                        <div class="row small">
                            <div class="col">Phí dịch vụ</div>
                            <div class="col text-end">+ <c:out value="${amounts.serviceFee}"/></div>
                        </div>
                        <hr>
                        <div class="d-flex justify-content-between align-items-center">
                            <div class="fw-semibold">Tổng tiền</div>
                            <div class="total-strong"><c:out value="${amounts.total}"/> đ</div>
                        </div>
                    </div>

                    <div class="tt-card">
                        <div class="fw-semibold mb-2">Bước tiếp theo:</div>
                        <ul class="small mb-0">
                            <li>Vé điện tử sẽ gửi qua email và điện thoại sau khi thanh toán.</li>
                            <li>Thanh toán qua mã QR, chuyển khoản, thẻ nội địa/quốc tế hoặc MoMo.</li>
                            <li>Hỗ trợ: Gọi <strong>1900 2087</strong>.</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>

        <script>
            window.TT_CHECKOUT = {
                ctx: "${ctx}",
                countdownSec: ${empty countdownSec ? 300 : countdownSec} // 5 phút mặc định
            };
        </script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.js"></script>
        <script src="${ctx}/assets/js/checkout.js"></script>
    </body>
</html>
