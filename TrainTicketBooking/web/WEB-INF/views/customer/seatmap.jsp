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

            <!-- ===== Tính tổng FREE/SOLD an toàn ===== -->
            <c:set var="totalFree" value="0"/>
            <c:set var="totalSold" value="0"/>
            <c:if test="${not empty vm and not empty vm.carriages}">
                <c:forEach var="car" items="${vm.carriages}">
                    <c:if test="${not empty vm.seatsByCarriage[car.carriageId]}">
                        <c:forEach var="sv" items="${vm.seatsByCarriage[car.carriageId]}">
                            <c:choose>
                                <c:when test="${sv.available}">
                                    <c:set var="totalFree" value="${totalFree + 1}"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="totalSold" value="${totalSold + 1}"/>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </c:if>
                </c:forEach>
            </c:if>

            <!-- ===== Chọn toa đầu tiên có ghế (không dùng c:break) ===== -->
            <c:set var="firstCarId" value="${null}"/>
            <c:set var="firstCar"   value="${null}"/>
            <c:if test="${not empty vm and not empty vm.carriages}">
                <!-- ưu tiên toa có dữ liệu ghế -->
                <c:forEach var="car" items="${vm.carriages}">
                    <c:if test="${empty firstCarId and not empty vm.seatsByCarriage[car.carriageId]}">
                        <c:set var="firstCarId" value="${car.carriageId}"/>
                        <c:set var="firstCar"   value="${car}"/>
                    </c:if>
                </c:forEach>
                <!-- nếu không toa nào có ghế -> fallback toa đầu -->
                <c:if test="${empty firstCarId}">
                    <c:set var="firstCarId" value="${vm.carriages[0].carriageId}"/>
                    <c:set var="firstCar"   value="${vm.carriages[0]}"/>
                </c:if>
            </c:if>

            <!-- ===== Strip tàu ===== -->
            <div class="train-strip mb-3">
                <div id="trainCards" class="train-cards d-flex gap-3">
                    <div class="train-card active"
                         data-trip-id="${param.tripId}"
                         data-train-code="${not empty vm and not empty vm.trip and not empty vm.trip.trainCode ? vm.trip.trainCode : ''}"
                         data-depart="${departFmt}"
                         data-arrive="${arriveFmt}"
                         data-free="${totalFree}"
                         data-booked="${totalSold}">
                        <div class="train-head">
                            <span>Tàu</span>
                            <span class="ms-1 fw-semibold">
                                <c:out value="${not empty vm and not empty vm.trip ? vm.trip.trainCode : ''}"/>
                            </span>
                        </div>
                        <div class="kv">TG đi</div>   <div class="vv" id="kDepart"><c:out value="${departFmt}"/></div>
                        <div class="kv">TG đến</div>  <div class="vv" id="kArrive"><c:out value="${arriveFmt}"/></div>
                        <div class="kv">SL đã đặt</div><div class="vv" id="kBooked"><c:out value="${totalSold}"/></div>
                        <div class="kv">SL còn</div>  <div class="vv" id="kFree"><c:out value="${totalFree}"/></div>
                        <div class="wheels"></div>
                    </div>
                </div>

                <div class="train-line my-2"></div>

                <!-- Mini-cars -->
                <div id="carMini" class="mini-cars d-flex align-items-center gap-2">
                    <c:if test="${not empty vm and not empty vm.carriages}">
                        <c:forEach var="car" items="${vm.carriages}" varStatus="st">
                            <button type="button"
                                    class="mini-car ${car.carriageId == firstCarId ? 'active' : ''}"
                                    data-carriage-id="${car.carriageId}"
                                    data-carriage-code="${car.code}"
                                    data-sort="${car.sortOrder != null ? car.sortOrder : (st.index + 1)}">
                                Toa <c:out value="${car.sortOrder != null ? car.sortOrder : (st.index + 1)}"/>
                            </button>
                        </c:forEach>
                    </c:if>
                </div>
            </div>

            <!-- ===== Khung Toa + Ghế ===== -->
            <div class="coach-wrapper">
                <div class="coach-header d-flex align-items-center justify-content-between">
                    <div class="coach-title" id="coachTitle">
                        <c:choose>
                            <c:when test="${not empty vm and not empty vm.carriages}">
                                Toa số
                                <c:out value="${firstCar != null && firstCar.sortOrder != null ? firstCar.sortOrder : 1}"/> :
                                <span id="className">
                                    <c:choose>
                                        <c:when test="${not empty vm.seatsByCarriage[firstCarId]}">
                                            <c:out value="${vm.seatsByCarriage[firstCarId][0].seatClassName}"/>
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </span>
                            </c:when>
                            <c:otherwise>
                                Toa : <span id="className">—</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="pager">
                        <button class="nav-btn" id="btnPrev" type="button">〈</button>
                        <button class="nav-btn" id="btnNext" type="button">〉</button>
                    </div>
                </div>

                <div class="coach-body">
                    <div class="brace left"></div>
                    <div class="seat-area">
                        <div id="seatGrid" class="seat-grid"
                             <c:if test="${not empty vm and not empty vm.carriages}">
                                 data-current-carriage-id="${firstCarId}"
                             </c:if>>
                            <!-- Render ghế của toa đầu đã chọn -->
                            <c:if test="${not empty vm and not empty vm.carriages}">
                                <c:choose>
                                    <c:when test="${not empty vm.seatsByCarriage[firstCarId]}">
                                        <c:forEach var="sv" items="${vm.seatsByCarriage[firstCarId]}">
                                            <c:set var="status" value="${vm.seatStatus[sv.seatId]}"/>
                                            <c:set var="cls">
                                                <c:choose>
                                                    <c:when test="${status == 'FREE'}">free</c:when>
                                                    <c:when test="${status == 'LOCKED'}">hold</c:when>
                                                    <c:otherwise>sold</c:otherwise>
                                                </c:choose>
                                            </c:set>

                                            <button type="button"
                                                    class="seat ${cls}"
                                                    data-seat-id="${sv.seatId}"
                                                    data-seat-code="${sv.seatCode}"
                                                    data-carriage-id="${sv.carriageId}"
                                                    data-carriage-code="${sv.carriageCode}"
                                                    data-seat-class-id="${sv.seatClassId}"
                                                    data-seat-class-code="${sv.seatClassCode}"
                                                    data-seat-class-name="${sv.seatClassName}"
                                                    data-available="${sv.available ? 'true' : 'false'}"
                                                    data-price="${sv.price}"
                                                    ${sv.available ? '' : 'disabled'}>
                                                <span class="seat-label"><c:out value="${sv.seatCode}"/></span>
                                                <span class="seat-price"><c:out value="${sv.price}"/></span>
                                            </button>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="text-muted p-3">Toa này chưa có dữ liệu ghế.</div>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </div>
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

        <script>
    const ctx = "${ctx}";
    const csrf = "${sessionScope.csrfToken}";
    let   tripId = "${param.tripId}";
    const trainCode = "${not empty vm && not empty vm.trip ? vm.trip.trainCode : ''}";
        </script>
        <script defer src="${ctx}/assets/js/seatmap.js"></script>
    </body>
</html>
