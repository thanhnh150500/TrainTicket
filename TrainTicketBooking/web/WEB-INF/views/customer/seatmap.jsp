<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="sc"  value="${sessionScope.searchCtx}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="csrf-token" content="${sessionScope.csrfToken}">
        <title>Chọn ghế</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="${ctx}/assets/css/seatmap.css" rel="stylesheet">
    </head>
    <body class="bg-white">

        <div class="container py-3">

            <!-- ===== Header: Chiều đi ... ===== -->
            <div class="route-header d-flex align-items-center gap-2 mb-3">
                <div class="flag">Chiều đi:</div>
                <div class="route-text">
                    <span>ngày
                        <c:out value="${sc != null ? sc.departDate : ''}"/>
                        từ
                        <c:out value="${sc != null ? sc.originName : ''}"/>
                        đến
                        <c:out value="${sc != null ? sc.destName : ''}"/>
                    </span>
                </div>
            </div>

            <!-- ===== Strip tàu: thẻ “đầu tàu” to + dải toa nhỏ (1..n) ===== -->
            <div class="train-strip mb-3">
                <div class="train-cards d-flex gap-3">
                    <!-- Thẻ đầu tàu đang chọn -->
                    <div class="train-card active">
                        <div class="train-head">SE7</div>
                        <div class="kv">TG đi</div><div class="vv" id="kDepart">--:--</div>
                        <div class="kv">TG đến</div><div class="vv" id="kArrive">--:--</div>
                        <div class="kv">SL chỗ đặt</div><div class="vv" id="kBooked">--</div>
                        <div class="kv">SL chỗ trống</div><div class="vv" id="kFree">--</div>
                        <div class="wheels"></div>
                    </div>
                    <!-- Một vài card “tàu khác” để giống mẫu (placeholder, không bắt buộc) -->
                    <div class="train-card ghost"><div class="train-head">SE9</div><div class="wheels"></div></div>
                    <div class="train-card ghost"><div class="train-head">SE3</div><div class="wheels"></div></div>
                    <div class="train-card ghost"><div class="train-head">SE19</div><div class="wheels"></div></div>
                </div>

                <div class="train-line my-2"></div>

                <div id="carMini" class="mini-cars d-flex align-items-center gap-2">
                    <!-- sẽ render các “toa nhỏ” 1..n bằng JS -->
                </div>
            </div>

            <!-- ===== Khung Toa + Ghế ===== -->
            <div class="coach-wrapper">
                <div class="coach-header d-flex align-items-center justify-content-between">
                    <div class="coach-title" id="coachTitle">Toa số … : <span id="className">…</span></div>
                    <div class="pager">
                        <button class="nav-btn" id="btnPrev">〈</button>
                        <button class="nav-btn" id="btnNext">〉</button>
                    </div>
                </div>

                <div class="coach-body">
                    <div class="brace left"></div>
                    <div class="seat-area">
                        <!-- Cụm 2–AISLE–2 theo hàng, sẽ render bằng JS -->
                        <div id="seatGrid" class="seat-grid"></div>
                    </div>
                    <div class="brace right"></div>
                </div>

                <div class="legend mt-2">
                    <span class="dot free"></span> Còn
                    <span class="dot hold"></span> Giữ
                    <span class="dot sold"></span> Đã đặt
                    <span class="dot chosen"></span> Đang chọn
                </div>

                <div class="actions mt-3 d-flex justify-content-between align-items-center">
                    <div class="muted" id="hint">Chọn tối đa 4 ghế</div>
                    <button id="btnHold" class="btn btn-primary px-4">Giữ ghế</button>
                </div>
            </div>

            <!-- ===== Dịch vụ đi kèm ===== -->
            <div class="addon mt-4">
                <div class="addon-title">Chúng tôi cung cấp các dịch vụ để quý khách chọn mua kèm dưới đây</div>
                <div class="addon-row">
                    <label class="form-check-label d-flex align-items-start gap-2">
                        <input class="form-check-input mt-1" type="checkbox" id="vipLounge">
                        <div>
                            <div class="fw-semibold">Phòng chờ VIP ga Hà Nội</div>
                            <div class="text-muted small">
                                Sử dụng phòng chờ VIP ở ga Hà Nội trước 02h tàu xuất phát hoặc sau 02h tàu đến ga – giá dịch vụ 20.000&nbsp;VND
                            </div>
                        </div>
                    </label>
                </div>
            </div>
        </div>

        <script>
            const ctx = "${ctx}";
            const tripId = "${param.tripId}";
        </script>
        <script src="${ctx}/assets/js/seatmap.js"></script>
        <script>console.log('csrf=', '${sessionScope.csrfToken}');</script>
    </body>
</html>
