<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Thông tin chuyển khoản</title>
        <meta name="csrf-token" content="${sessionScope.csrfToken}">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="${ctx}/assets/css/payment.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="container-xxl my-3">
            <div class="tt-card">
                <div class="d-flex justify-content-between align-items-center">
                    <div class="fw-bold fs-5">Thông tin chuyển khoản</div>
                    <div class="badge text-bg-primary">
                        Thời gian giữ vé còn lại <span id="qrRemain">--:--</span>
                    </div>
                </div>

                <div class="row g-3 mt-2">
                    <div class="col-md-5">
                        <div class="qr-box text-center">
                            <!-- 1) dataStr: ưu tiên qrData, nếu không có thì ghép bankCode|accountNo|amount|memo -->
                            <c:choose>
                                <c:when test="${not empty qrData}">
                                    <c:set var="dataStr" value="${qrData}" />
                                </c:when>
                                <c:otherwise>
                                    <c:set var="dataStr" value="${bankCode}|${accountNo}|${amount}|${memo}" />
                                </c:otherwise>
                            </c:choose>

                            <!-- 2) Ảnh QR: dùng ảnh server nếu có; nếu không tạo URL động -->
                            <c:choose>
                                <c:when test="${not empty qrImageUrl}">
                                    <img class="img-fluid" src="${qrImageUrl}" alt="QR chuyển khoản">
                                </c:when>
                                <c:otherwise>
                                    <c:url var="qrUrl" value="https://api.qrserver.com/v1/create-qr-code/">
                                        <c:param name="size" value="260x260" />
                                        <c:param name="data" value="${dataStr}" />
                                    </c:url>
                                    <img class="img-fluid" src="${qrUrl}" alt="QR chuyển khoản">
                                </c:otherwise>
                            </c:choose>

                            <div class="small text-muted mt-2">Quét mã QR để thanh toán nhanh</div>
                        </div>
                    </div>

                    <div class="col-md-7">
                        <div class="row gy-2">
                            <div class="col-12">
                                <div class="kv">
                                    <span class="k">Ngân hàng</span>
                                    <span class="v"><c:out value="${bankName}" /></span>
                                </div>
                            </div>

                            <div class="col-12 d-flex align-items-center">
                                <div class="kv me-2 flex-grow-1">
                                    <span class="k">Số tài khoản</span>
                                    <span class="v" id="accNo"><c:out value="${accountNo}" /></span>
                                </div>
                                <button class="btn btn-sm btn-outline-secondary" data-copy="#accNo">Sao chép</button>
                            </div>

                            <div class="col-12">
                                <div class="kv">
                                    <span class="k">Người thụ hưởng</span>
                                    <span class="v"><c:out value="${accountName}" /></span>
                                </div>
                            </div>

                            <div class="col-12 d-flex align-items-center">
                                <div class="kv me-2 flex-grow-1">
                                    <span class="k">Số tiền</span>
                                    <span class="v" id="amt"><c:out value="${amount}" /> đ</span>
                                </div>
                                <button class="btn btn-sm btn-outline-secondary" data-copy="#amt">Sao chép</button>
                            </div>

                            <div class="col-12 d-flex align-items-center">
                                <div class="kv me-2 flex-grow-1">
                                    <span class="k">Nội dung</span>
                                    <span class="v" id="memo"><c:out value="${memo}" /></span>
                                </div>
                                <button class="btn btn-sm btn-outline-secondary" data-copy="#memo">Sao chép</button>
                            </div>
                        </div>

                        <div class="alert alert-info small mt-3 mb-0">
                            * Giao dịch sẽ tự động xác nhận nếu chuyển đúng <b>số tiền</b> và <b>nội dung</b>.
                        </div>

                        <div class="d-flex gap-2 mt-3">
                            <a class="btn btn-success" href="${ctx}/payment/status?bookingId=${bookingId}">Tôi đã chuyển khoản</a>
                            <a class="btn btn-outline-secondary" href="${ctx}/payment">Đổi hình thức (trở lại)</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <script>
            window.TT_QR = {
                countdownSec: ${empty countdownSec ? 300 : countdownSec},
                bookingId: '${bookingId}'
            };
        </script>
        <script src="${ctx}/assets/js/payment_qr.js"></script>
    </body>
</html>
