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
        <%@ include file="/WEB-INF/views/layout/_header.jsp" %>

        <div class="container py-3">
            <!-- ===== Header tìm kiếm ===== -->
            <div class="route-header d-flex align-items-center gap-2 mb-3">
                <div class="flag">Chiều đi:</div>
                <div class="route-text">
                    ngày <c:out value="${sc != null ? sc.departDate : ''}"/>
                    từ <c:out value="${sc != null ? sc.originName  : ''}"/>
                    đến <c:out value="${sc != null ? sc.destName    : ''}"/>
                </div>
            </div>

            <!-- ===== Strip tàu ===== -->
            <div class="train-strip mb-3">
                <div id="trainCards" class="train-cards d-flex gap-3">
                    <div class="train-card active"
                         data-trip-id="${param.tripId}"
                         data-train-code="${not empty vm and not empty vm.trip and not empty vm.trip.trainCode ? vm.trip.trainCode : ''}"
                         data-depart="${departFmt}"
                         data-arrive="${arriveFmt}">
                        <div class="train-head">
                            <span>Tàu</span>
                            <span class="ms-1 fw-semibold">
                                <c:out value="${not empty vm and not empty vm.trip ? vm.trip.trainCode : ''}"/>
                            </span>
                        </div>
                        <div class="kv">TG đi</div>   <div class="vv" id="kDepart"><c:out value="${departFmt}"/></div>
                        <div class="kv">TG đến</div>  <div class="vv" id="kArrive"><c:out value="${arriveFmt}"/></div>
                        <div class="kv">SL đã đặt</div><div class="vv" id="kBooked">0</div>
                        <div class="kv">SL còn</div>  <div class="vv" id="kFree">0</div>
                        <div class="wheels"></div>
                    </div>
                </div>

                <div class="train-line my-2"></div>

                <!-- Mini-cars (JS sẽ vẽ danh sách toa) -->
                <div id="carMini" class="mini-cars d-flex align-items-center gap-2"></div>
            </div>

            <!-- ===== Khung Toa + Ghế (JS vẽ) ===== -->
            <div class="coach-wrapper">
                <div class="coach-header d-flex align-items-center justify-content-between">
                    <div class="coach-title" id="coachTitle">
                        Toa số <span class="fw-semibold">—</span> :
                        <span id="className">—</span>
                    </div>
                    <div class="pager">
                        <button class="nav-btn" id="btnPrev" type="button">〈</button>
                        <button class="nav-btn" id="btnNext" type="button">〉</button>
                    </div>
                </div>

                <div class="coach-body">
                    <div class="brace left"></div>
                    <div class="seat-area">
                        <!-- seat-grid: JS set --cols để căn giữa -->
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
                    <button id="btnBook" class="btn btn-primary px-4" type="button">Đặt vé</button>
                </div>
            </div>
        </div><!-- /.container -->

        <!-- ===== Form ẩn để POST sang /checkout (JS sẽ append seats) ===== -->
        <form id="checkoutPostForm" class="d-none" method="post" action="${ctx}/checkout">
            <input type="hidden" name="_csrf" id="csrfHidden" value="${sessionScope.csrfToken}">
            <input type="hidden" name="tripId"  value="${param.tripId}">
            <input type="hidden" name="trainCode"   value="${not empty vm && not empty vm.trip ? vm.trip.trainCode : ''}">
            <input type="hidden" name="originName"  value="${sc != null ? sc.originName : ''}">
            <input type="hidden" name="destName"    value="${sc != null ? sc.destName   : ''}">
            <input type="hidden" name="departTime"  value="${departFmt}">
            <input type="hidden" name="arriveTime"  value="${arriveFmt}">
            <div id="seatsContainer"></div>
        </form>

        <script>
            // fallback: nếu input _csrf chưa có value, lấy từ <meta>
            (function () {
                var h = document.getElementById('csrfHidden');
                if (h && (!h.value || h.value.trim() === '')) {
                    var m = document.querySelector('meta[name="csrf-token"]');
                    if (m && m.content)
                        h.value = m.content.trim();
                }
            })();
        </script>

        <script>
            const ctx = "${ctx}";
            const csrf = "${sessionScope.csrfToken}";
            let   tripId = "${param.tripId}";
            const trainCode = "${not empty vm && not empty vm.trip ? vm.trip.trainCode : ''}";
        </script>
        <script defer src="${ctx}/assets/js/seatmap.js"></script>
    </body>
</html>
