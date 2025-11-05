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

        <!-- Bootstrap -->
        <link rel="stylesheet" href="<%=ctx%>/assets/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="<%=ctx%>/assets/icons/bootstrap-icons.min.css">

        <!-- Custom -->
        <link rel="stylesheet" href="<%=ctx%>/assets/css/home.css">
    </head>
    <body>
        <%@ include file="/WEB-INF/views/layout/_header.jsp" %>

        <!-- ===== SECTION 1: SEARCH ===== -->
        <section class="voy-hero py-5 bg-light">
            <div class="container">
                <div class="row g-4 align-items-center">
                    <div class="col-lg-6">
                        <h1 class="fw-bold mb-2">Tìm & đặt vé tàu nhanh chóng</h1>
                        <p class="text-muted mb-4">Chọn chuyến, chọn ghế, thanh toán an toàn — tất cả trong vài bước.</p>

                        <div class="card shadow-sm border-0 p-3 rounded-4">
                            <ul class="nav nav-pills gap-2 mb-3">
                                <li class="nav-item">
                                    <button class="btn btn-outline-primary active" id="tab-oneway" type="button">Một chiều</button>
                                </li>
                                <li class="nav-item">
                                    <button class="btn btn-outline-primary" id="tab-round" type="button">Khứ hồi</button>
                                </li>
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
                                            <input list="stationList" class="form-control" name="originStation" id="originStation"
                                                   placeholder="Từ: Hà Nội" required>
                                            <button type="button" class="btn btn-light border" id="swapBtn" title="Đổi chiều">
                                                <i class="bi bi-arrow-left-right"></i>
                                            </button>
                                        </div>
                                    </div>

                                    <!-- Ga đến -->
                                    <div class="col-md-6">
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="bi bi-geo"></i></span>
                                            <input list="stationList" class="form-control" name="destStation" id="destStation"
                                                   placeholder="Đến: Đà Nẵng" required>
                                        </div>
                                    </div>

                                    <!-- Ngày đi -->
                                    <div class="col-md-6">
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="bi bi-calendar3"></i></span>
                                            <input class="form-control" type="date" name="departDate" id="departDate"
                                                   value="<%=today%>" min="<%=today%>" required>
                                        </div>
                                    </div>

                                    <!-- Ngày về -->
                                    <div class="col-md-6">
                                        <div class="input-group">
                                            <span class="input-group-text"><i class="bi bi-calendar3"></i></span>
                                            <input class="form-control" type="date" name="returnDate" id="returnDate" disabled>
                                            <button type="button" class="btn btn-light border ms-2" id="clearReturnBtn" title="Xoá ngày về">
                                                <i class="bi bi-x-lg"></i>
                                            </button>
                                        </div>
                                    </div>

                                    <!-- Submit -->
                                    <div class="col-12 text-end">
                                        <button class="btn btn-primary px-4" type="submit">Tìm chuyến</button>
                                    </div>
                                </div>

                                <!-- Datalist -->
                                <datalist id="stationList">
                                    <c:forEach var="s" items="${stations}">
                                        <option value="${s.name}" data-id="${s.stationId}" data-code="${s.code}">
                                            ${s.name} (${s.code})
                                        </option>
                                    </c:forEach>
                                </datalist>

                                <!-- Messages -->
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

        <!-- ===== SECTION 2: DEALS ===== -->
        <section class="sec py-5">
            <div class="container">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h2 class="h5 fw-bold mb-0">Deal tàu giá rẻ</h2>
                    <a class="link-primary text-decoration-none" href="<%=ctx%>/deals">Xem tất cả →</a>
                </div>

                <div class="d-flex flex-row flex-nowrap gap-3 overflow-auto pb-2">
                    <c:forEach var="d" items="${cheapDeals}">
                        <div class="card border-0 shadow-sm" style="width:240px;">
                            <img class="card-img-top" src="${empty d.imageUrl ? (pageContext.request.contextPath+'/static/img/deal-fallback.jpg') : d.imageUrl}" alt="${d.title}">
                            <div class="card-body">
                                <div class="fw-semibold text-truncate">${d.title}</div>
                                <div class="text-muted small text-truncate">${d.routeLabel}</div>
                                <div class="fw-bold mt-1"><span class="text-muted small">VND</span> ${d.priceFmt}</div>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty cheapDeals}">
                        <c:forEach begin="1" end="6">
                            <div class="card border-0 bg-light" style="width:240px;height:210px;"></div>
                        </c:forEach>
                    </c:if>
                </div>
            </div>
        </section>

        <!-- ===== SECTION 3: BLOG ===== -->
        <section class="sec py-5 bg-light">
            <div class="container">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h2 class="h5 fw-bold mb-0">Bài viết nổi bật</h2>
                    <a class="link-primary text-decoration-none" href="<%=ctx%>/blog">Xem blog →</a>
                </div>

                <div class="row g-4">
                    <c:forEach var="p" items="${featuredPosts}">
                        <div class="col-md-4">
                            <div class="card border-0 shadow-sm h-100">
                                <img class="card-img-top" src="${empty p.coverUrl ? (pageContext.request.contextPath+'/static/img/post-fallback.jpg') : p.coverUrl}" alt="${p.title}">
                                <div class="card-body">
                                    <h5 class="fw-semibold">${p.title}</h5>
                                    <p class="text-muted small mb-2">${p.excerpt}</p>
                                    <a class="link-primary text-decoration-none" href="<%=ctx%>/blog/${p.slug}">Đọc tiếp</a>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </section>

        <!-- ===== SECTION 4: PARTNERS ===== -->
        <section class="sec py-5">
            <div class="container">
                <h2 class="h5 fw-bold mb-3">Đối tác vận hành</h2>
                <div class="row g-3">
                    <c:forEach var="pt" items="${partners}">
                        <div class="col-6 col-md-3 col-lg-2">
                            <div class="border rounded-3 bg-white d-flex align-items-center justify-content-center p-3" style="height:76px;">
                                <img src="${empty pt.logoUrl ? (pageContext.request.contextPath+'/static/img/logo-fallback.svg') : pt.logoUrl}" class="img-fluid" alt="${pt.name}">
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </section>

        <!-- JS -->
        <script src="<%=ctx%>/assets/bootstrap/js/bootstrap.bundle.min.js"></script>
        <script src="<%=ctx%>/assets/js/home.js"></script>
    </body>
</html>
