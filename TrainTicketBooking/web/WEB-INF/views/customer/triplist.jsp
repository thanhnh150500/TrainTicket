<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c"  uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="sc"  value="${sessionScope.searchCtx}" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="csrf-token" content="${sessionScope.csrfToken}">
        <title>Chọn chiều đi</title>

        <link rel="stylesheet" href="${ctx}/assets/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="${ctx}/assets/icons/bootstrap-icons.min.css">
        <link rel="stylesheet" href="${ctx}/assets/css/trip.css">
    </head>
    <body class="bg-light">
        <%@ include file="/WEB-INF/views/layout/_header.jsp" %>

        <div class="container py-3">

            <!-- Header route -->
            <div class="d-flex flex-wrap align-items-center justify-content-between mb-3 gap-2">
                <div class="d-flex align-items-center gap-2">
                    <div class="text-secondary">Một chiều</div>
                    <div class="vr"></div>
                    <div class="text-secondary">${empty sc.pax ? 1 : sc.pax} khách</div>
                </div>

                <div class="d-flex flex-wrap gap-2">
                    <div class="trip-input">
                        <i class="bi bi-geo-alt"></i>
                        <span>${routeOriginCode}</span>
                    </div>
                    <div class="trip-swap"><i class="bi bi-arrow-left-right"></i></div>
                    <div class="trip-input">
                        <i class="bi bi-geo"></i>
                        <span>${routeDestCode}</span>
                    </div>
                    <div class="trip-input">
                        <i class="bi bi-calendar3"></i>
                        <span>${activeDateLabel}</span>
                    </div>

                    <!-- Sửa: quay lại home để chỉnh tìm kiếm -->
                    <a class="btn btn-primary px-4" href="${ctx}/home">Tìm</a>
                </div>
            </div>

            <!-- Dải ngày -->
            <div class="card-body pt-2 pb-0">
                <div class="d-flex justify-content-between align-items-center text-muted mb-2">
                    <div></div>
                    <div>${routeTitle}</div>
                    <div class="d-flex gap-2">
                        <a class="btn btn-outline-secondary btn-sm" href="${prevDateUrl}">
                            <i class="bi bi-chevron-left"></i>
                        </a>
                        <a class="btn btn-outline-secondary btn-sm" href="${nextDateUrl}">
                            <i class="bi bi-chevron-right"></i>
                        </a>
                    </div>
                </div>

                <div class="days-strip d-flex flex-nowrap gap-3 overflow-auto pb-2">
                    <%
                      java.time.LocalDate base = (java.time.LocalDate) request.getAttribute("searchDate");
                      if (base == null) base = java.time.LocalDate.now();

                      String prev = (String) request.getAttribute("prevDateUrl");
                      String next = (String) request.getAttribute("nextDateUrl");
                      String urlPrefix = null;
                      if (prev != null && prev.contains("date=")) {
                        urlPrefix = prev.substring(0, prev.indexOf("date=") + "date=".length());
                      } else if (next != null && next.contains("date=")) {
                        urlPrefix = next.substring(0, next.indexOf("date=") + "date=".length());
                      } else {
                        urlPrefix = request.getContextPath() + "/trips?date="; // fallback
                      }

                      java.time.format.DateTimeFormatter DMY = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
                      java.util.Locale VI = new java.util.Locale("vi");

                      for (int offset = -1; offset <= 7; offset++) {
                        java.time.LocalDate d = base.plusDays(offset);
                        String activeClass = d.equals(base) ? " active" : "";
                        String dateLabel = d.format(DMY);
                        String weekLabel = d.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, VI);
                    %>
                    <a class="day-tab<%= activeClass %>" href="<%= urlPrefix + d.toString() %>">
                        <div class="dd"><%= dateLabel %></div>
                        <div class="wk"><%= weekLabel %></div>
                    </a>
                    <%
                      }
                    %>
                </div>
            </div>

            <c:if test="${not empty message}">
                <div class="alert alert-success mt-3">${message}</div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger mt-3">${error}</div>
            </c:if>

            <c:choose>
                <c:when test="${empty trips}">
                    <div class="alert alert-warning mt-3">Không có chuyến cho ngày này.</div>
                </c:when>
                <c:otherwise>
                    <div class="vstack gap-3 mt-3">
                        <c:forEach var="t" items="${trips}">
                            <%
                              vn.ttapp.model.TripCardVm tvm =
                                  (vn.ttapp.model.TripCardVm) pageContext.findAttribute("t");
                              java.time.format.DateTimeFormatter HHMM =
                                  java.time.format.DateTimeFormatter.ofPattern("HH:mm");
                              String hhmmDep = (tvm.getDepartTime() == null) ? "--:--" : HHMM.format(tvm.getDepartTime());
                              String hhmmArr = (tvm.getArriveTime() == null) ? "--:--" : HHMM.format(tvm.getArriveTime());
                            %>

                            <div class="card trip-card border-0 shadow-sm">
                                <div class="card-body">
                                    <div class="row g-3 align-items-center">

                                        <div class="col-md-3">
                                            <div class="small text-muted">Tàu Tốc Hành</div>
                                            <div class="h5 m-0">${t.trainCode}</div>
                                        </div>

                                        <div class="col-md-5">
                                            <div class="row text-center align-items-center">
                                                <div class="col-5">
                                                    <div class="time-lg"><%= hhmmDep %></div>
                                                    <div class="small text-muted">${t.originName}</div>
                                                </div>
                                                <div class="col-2">
                                                    <div class="duration">
                                                        <i class="bi bi-arrow-right-short"></i>
                                                        ${t.durationMin div 60}h
                                                        <c:choose>
                                                            <c:when test="${(t.durationMin mod 60) lt 10}">0${t.durationMin mod 60}p</c:when>
                                                            <c:otherwise>${t.durationMin mod 60}p</c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </div>
                                                <div class="col-5">
                                                    <div class="time-lg"><%= hhmmArr %></div>
                                                    <div class="small text-muted">${t.destName}</div>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="col-md-2 text-md-center">
                                            <div class="badge bg-warning-subtle text-warning-emphasis px-3 py-2">
                                                Còn <strong>${t.availableSeats}</strong> chỗ
                                            </div>
                                        </div>

                                        <div class="col-md-2 text-md-end">
                                            <div class="text-muted small mb-1">Từ</div>
                                            <div class="h5 fw-bold mb-2">
                                                <fmt:formatNumber value="${t.minPrice}" type="number" groupingUsed="true"/>
                                                <span class="text-muted small">đ</span>
                                            </div>
                                            <a class="btn btn-warning fw-semibold px-4" href="${ctx}/seatmap?tripId=${t.tripId}">
                                                Chọn chỗ
                                            </a>
                                        </div>

                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>

        <script src="${ctx}/assets/bootstrap/js/bootstrap.bundle.min.js"></script>
        <script src="${ctx}/assets/js/trip.js"></script>
    </body>
</html>
