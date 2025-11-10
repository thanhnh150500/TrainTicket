<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>

<%
  String ctx = request.getContextPath();
  String today = java.time.LocalDate.now().toString();
%>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Trang chủ | Đặt vé tàu</title>
        <link rel="stylesheet" href="<%=ctx%>/assets/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="<%=ctx%>/assets/icons/bootstrap-icons.min.css">
        <link rel="stylesheet" href="<%=ctx%>/assets/css/home.css">
    </head>
    <body>
        <%@ include file="/WEB-INF/views/layout/_header.jsp" %>

        <section class="voy-hero py-5 bg-light">
            <div class="container">
                <div class="row g-4 align-items-center">
                    <div class="col-lg-6">
                        <h1 class="fw-bold mb-2">Tìm & đặt vé tàu nhanh chóng</h1>
                        <p class="text-muted mb-4">Chọn chuyến, chọn ghế, thanh toán an toàn — tất cả trong vài bước.</p>

                        <div class="card shadow-sm border-0 p-3 rounded-4">
                            <ul class="nav nav-pills gap-2 mb-3">
                                <li class="nav-item"><button class="btn btn-outline-primary active" id="tab-oneway" type="button">Một chiều</button></li>
                                <li class="nav-item"><button class="btn btn-outline-primary" id="tab-round" type="button">Khứ hồi</button></li>
                            </ul>

                            <form id="tripSearchForm" action="<%=ctx%>/tripsearch" method="post" novalidate autocomplete="off">
                                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                                <input type="hidden" name="tripType" id="tripType" value="ONEWAY"/>
                                <input type="hidden" name="originId" id="originId"/>
                                <input type="hidden" name="destId" id="destId"/>

                                <div class="row g-3">
                                    <!-- Ga đi -->
                                    <div class="col-md-6">
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="bi bi-train-front"></i></span>
                                            <input class="form-control"
                                                   id="originStation" name="originStation"
                                                   placeholder="Từ: Hà Nội" required
                                                   list="stationList" autocomplete="off" autocapitalize="off" autocorrect="off" spellcheck="false">
                                            <button type="button" class="btn btn-light border" id="swapBtn" title="Đổi chiều">
                                                <i class="bi bi-arrow-left-right"></i>
                                            </button>
                                        </div>
                                    </div>

                                    <!-- Ga đến -->
                                    <div class="col-md-6">
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="bi bi-geo"></i></span>
                                            <input class="form-control"
                                                   id="destStation" name="destStation"
                                                   placeholder="Đến: Đà Nẵng" required
                                                   list="stationList" autocomplete="off" autocapitalize="off" autocorrect="off" spellcheck="false">
                                        </div>
                                    </div>

                                    <!-- Ngày đi -->
                                    <div class="col-md-6">
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="bi bi-calendar3"></i></span>
                                            <input class="form-control" type="date" id="departDate" name="departDate"
                                                   value="<%=today%>" min="<%=today%>" required>
                                        </div>
                                    </div>

                                    <!-- Ngày về -->
                                    <div class="col-md-6">
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="bi bi-calendar3"></i></span>
                                            <input class="form-control" type="date" id="returnDate" name="returnDate" disabled>
                                            <button type="button" class="btn btn-light border ms-2" id="clearReturnBtn" title="Xoá ngày về">
                                                <i class="bi bi-x-lg"></i>
                                            </button>
                                        </div>
                                    </div>

                                    <div class="col-12 text-end">
                                        <button class="btn btn-primary px-4" type="submit">Tìm chuyến</button>
                                    </div>
                                </div>

                                <!-- Datalist native -->
                                <datalist id="stationList">
                                    <c:forEach var="s" items="${stations}">
                                        <option value="${s.name}" data-id="${s.stationId}" data-code="${s.code}">${s.name}</option>
                                    </c:forEach>
                                </datalist>

                                <c:if test="${not empty sessionScope.error}">
                                    <div class="alert alert-danger mt-3">${sessionScope.error}</div>
                                    <c:remove var="error" scope="session"/>
                                </c:if>
                                <c:if test="${not empty sessionScope.message}">
                                    <div class="alert alert-success mt-3">${sessionScope.message}</div>
                                    <c:remove var="message" scope="session"/>
                                </c:if>
                            </form>
                        </div>
                    </div>

                    <div class="col-lg-6 d-none d-lg-block">
                        <img class="img-fluid" src="<%=ctx%>/static/voyage/assets/img/gallery/hero-section-1.png" alt="hero">
                    </div>
                </div>
            </div>
        </section>

        <!-- … các section khác giữ nguyên … -->

        <script src="<%=ctx%>/assets/bootstrap/js/bootstrap.bundle.min.js"></script>
        <script src="<%=ctx%>/assets/js/home.js?v=7"></script>
    </body>
</html>
