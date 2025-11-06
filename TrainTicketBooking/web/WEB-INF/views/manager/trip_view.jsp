<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <title>Xem chi tiết chuyến #${t.tripId}</title>
        <meta name="viewport" content="width=device-width, initial-scale=1"/>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
        <style>
            .ul-tight {
                margin: .25rem 0 .5rem 1.25rem;
            }
            .mono {
                font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace;
            }
        </style>
    </head>
    <body class="pb-5">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <div class="container mt-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h3>Chi tiết chuyến #${t.tripId}</h3>
                <div class="d-flex gap-2">
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/trips">Quay lại</a>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/manager/trips?op=edit&id=${t.tripId}">Sửa</a>
                </div>
            </div>

            <!-- Tổng quan -->
            <div class="card mb-3">
                <div class="card-header fw-semibold">Tổng quan</div>
                <div class="card-body">
                    <div><b>Mã tuyến:</b> <span class="mono">${t.routeCode}</span></div>
                    <div><b>Hành trình:</b> ${t.originName} → ${t.destName}</div>
                    <div class="mt-2"><b>Mã tàu:</b> <span class="mono">${t.trainCode}</span></div>
                    <div class="row mt-2">
                        <div class="col-md-6"><b>Khởi hành:</b> <c:out value="${t.departAt}"/></div>
                        <div class="col-md-6"><b>Đến nơi:</b> <c:out value="${t.arriveAt}"/></div>
                    </div>
                    <div class="mt-2">
                        <b>Trạng thái:</b>
                        <span class="badge bg-secondary">${t.status}</span>
                    </div>
                </div>
            </div>

            <!-- Thông tin tuyến -->
            <div class="card mb-3">
                <div class="card-header fw-semibold">Thông tin tuyến</div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${routeMeta != null}">
                            <div><b>Mã tuyến:</b> <span class="mono">${routeMeta.code}</span></div>
                            <div><b>Hành trình:</b> ${routeMeta.originName} → ${routeMeta.destName}</div>
                        </c:when>
                        <c:otherwise>
                            <div class="text-muted">Không tìm thấy thông tin tuyến.</div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Thông tin đoàn tàu -->
            <div class="card mb-4">
                <div class="card-header fw-semibold d-flex justify-content-between align-items-center">
                    <span>Thông tin đoàn tàu</span>
                    <c:if test="${trainMeta != null}">
                        <span class="text-muted small">
                            Toa: ${trainMeta.totalCarriages} • Ghế: ${trainMeta.totalSeats}
                        </span>
                    </c:if>
                </div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${trainMeta != null}">
                            <div><b>Mã tàu:</b> <span class="mono">${trainMeta.code}</span></div>
                            <div><b>Tên tàu:</b> ${trainMeta.name}</div>

                            <c:if test="${not empty trainMeta.carriages}">
                                <hr/>
                                <div class="fw-semibold mb-2">Chi tiết toa & ghế</div>
                                <c:forEach items="${trainMeta.carriages}" var="c" varStatus="st">
                                    <div class="mb-3">
                                        <div class="mb-1">
                                            <b>${st.index + 1}. Toa:</b> <span class="mono">${c.carriageCode}</span>
                                            — Hạng: ${c.seatClassName} <span class="mono">(${c.seatClassCode})</span>
                                            — Thứ tự: ${c.sortOrder} — Tổng ghế: ${c.seatCount}
                                        </div>
                                        <c:if test="${not empty c.seats}">
                                            <ul class="ul-tight">
                                                <c:forEach items="${c.seats}" var="s">
                                                    ${s.code} — ${s.seatClassName} (${s.seatClassCode})
                                                </c:forEach>
                                            </ul>
                                        </c:if>
                                    </div>
                                </c:forEach>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <div class="text-muted">Không tìm thấy thông tin đoàn tàu.</div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
