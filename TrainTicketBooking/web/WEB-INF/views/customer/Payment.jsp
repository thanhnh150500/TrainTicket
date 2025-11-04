<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Thanh toán</title>
        <meta name="csrf-token" content="${sessionScope.csrfToken}">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="${ctx}/assets/css/payment.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <%@ include file="/WEB-INF/views/layout/_header.jsp" %>

        <div class="container-xxl my-3">
            <!-- Banner đếm ngược -->
            <div class="alert alert-warning tt-countdown" role="alert">
                <i class="bi bi-clock me-2"></i>
                Thời gian chờ thanh toán <span id="payRemain">--:--</span>
                <small class="text-muted ms-2">Hoàn tất thanh toán trong thời gian trên</small>
            </div>

            <div class="row g-3">
                <!-- LEFT: chỉ 1 phương thức chuyển khoản QR -->
                <div class="col-lg-8">
                    <div class="tt-card">
                        <div class="d-flex align-items-center mb-2">
                            <i class="bi bi-shield-check me-2 text-primary"></i>
                            <div class="fw-semibold">Chọn hình thức thanh toán</div>
                        </div>

                        <form id="payForm" method="post" action="${ctx}/pay/qr" target="_blank" class="m-0">
                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="bookingId" value="${bookingId}">
                            <input type="hidden" name="amount" value="${amounts.total}">
                            <!-- memo/nội dung CK gợi ý: mã đơn hoặc code -->
                            <input type="hidden" name="memo" value="${not empty bookingCode ? bookingCode : ('BOOK-' += bookingId)}">
                            <!-- bank info bạn cấp từ server (hoặc để mặc định rồi sửa ở servlet) -->
                            <input type="hidden" name="bankCode" value="${bankCode}">
                            <input type="hidden" name="accountNo" value="${accountNo}">
                            <input type="hidden" name="accountName" value="${accountName}">

                            <label class="pay-option d-flex align-items-center">
                                <input type="radio" class="form-check-input me-3" checked>
                                <img src="https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/svg/1f4b3.svg" width="28" class="me-2" alt="">
                                <div class="flex-grow-1">
                                    <div class="fw-semibold">Chuyển khoản QR</div>
                                    <div class="text-muted small">Quét mã QR để chuyển khoản • Miễn phí</div>
                                </div>
                            </label>

                            <div class="text-end mt-3">
                                <button id="btnConfirmPay" type="submit" class="btn btn-warning fw-semibold px-4">
                                    Xác nhận thanh toán <span class="ms-1"><c:out value="${amounts.total}"/> đ</span>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- RIGHT: tổng tiền + tóm tắt chuyến -->
                <div class="col-lg-4">
                    <div class="tt-card mb-3">
                        <div class="tt-right-head">
                            <div class="fw-semibold">Tổng thanh toán</div>
                            <div class="tt-total"><c:out value="${amounts.total}"/> đ</div>
                            <div class="small text-muted">1 chiều • <c:out value="${passengerCount != null ? passengerCount : 1}"/> khách</div>
                        </div>
                        <hr>
                        <div class="small fw-semibold mb-2">Thông tin chuyến tàu</div>
                        <div class="d-flex justify-content-between mb-1 small">
                            <div><c:out value="${trip.originName}"/> → <c:out value="${trip.destName}"/></div>
                            <div class="text-muted"><c:out value="${trip.trainCode}"/></div>
                        </div>
                        <div class="d-flex justify-content-between small text-muted">
                            <div>Đi: <c:out value="${trip.departTimeStr}"/></div>
                            <div>Đến: <c:out value="${trip.arriveTimeStr}"/></div>
                        </div>
                        <hr>
                        <div class="small fw-semibold mb-2">Chi tiết giá</div>
                        <div class="row small"><div class="col">Giá vé</div><div class="col text-end"><c:out value="${amounts.subtotal}"/></div></div>
                        <div class="row small"><div class="col">Phí bảo hiểm</div><div class="col text-end">+ <c:out value="${amounts.insurance}"/></div></div>
                        <div class="row small"><div class="col">Phí dịch vụ</div><div class="col text-end">+ <c:out value="${amounts.serviceFee}"/></div></div>
                        <hr>
                        <div class="d-flex justify-content-between">
                            <div class="fw-semibold">Tổng cộng</div>
                            <div class="tt-total"><c:out value="${amounts.total}"/> đ</div>
                        </div>
                    </div>

                    <div class="tt-card">
                        <div class="fw-semibold mb-2">Lưu ý</div>
                        <ul class="small mb-0">
                            <li>Vé điện tử gửi qua email/SMS sau khi hệ thống xác nhận thanh toán.</li>
                            <li>Giữ chỗ chỉ trong thời gian đếm ngược.</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>

        <script>window.TT_PAYMENT = {countdownSec:${empty countdownSec ? 300 : countdownSec}, bookingId: '${bookingId}'};</script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.js"></script>
        <script src="${ctx}/assets/js/payment.js"></script>
    </body>
</html>
