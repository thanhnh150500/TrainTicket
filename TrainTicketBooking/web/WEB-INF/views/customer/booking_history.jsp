<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"  %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Lịch sử đặt vé</title>

        <link rel="stylesheet" href="${ctx}/assets/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="${ctx}/assets/icons/bootstrap-icons.min.css">
        <link rel="stylesheet" href="${ctx}/assets/css/booking-history.css">
    </head>
    <body class="bg-light">

        <%@ include file="/WEB-INF/views/layout/_header.jsp" %>

        <div class="container py-4">

            <div class="d-flex align-items-center justify-content-between mb-3">
                <div class="d-flex align-items-center gap-2">
                    <h3 class="m-0">🎟️ Lịch sử đặt vé</h3>
                </div>

                <form class="d-flex" method="get" action="${ctx}/booking-history">
                    <select name="status" class="form-select form-select-sm me-2" style="width:200px">
                        <option value="" ${empty status ? 'selected' : ''}>Tất cả trạng thái</option>
                        <option value="PAID"     ${status=='PAID' ? 'selected' : ''}>Đã thanh toán</option>
                        <option value="HOLD"     ${status=='HOLD' ? 'selected' : ''}>Đang giữ chỗ</option>
                        <option value="DRAFT"    ${status=='DRAFT' ? 'selected' : ''}>Nháp</option>
                        <option value="CANCELED" ${status=='CANCELED' ? 'selected' : ''}>Đã hủy</option>
                        <option value="EXPIRED"  ${status=='EXPIRED' ? 'selected' : ''}>Hết hạn</option>
                    </select>
                    <input type="hidden" name="size" value="${size != null ? size : 10}"/>
                    <button class="btn btn-primary btn-sm"><i class="bi bi-funnel"></i> Lọc</button>
                </form>
            </div>

            <c:choose>
                <c:when test="${needsLogin}">
                    <div class="alert alert-warning shadow-sm">
                        Bạn cần <a class="alert-link" href="${loginHref}">đăng nhập</a> để xem lịch sử đặt vé của mình.
                    </div>
                    <div class="mt-3">
                        <a href="${loginHref}" class="btn btn-primary me-2">
                            <i class="bi bi-box-arrow-in-right"></i> Đăng nhập
                        </a>
                        <a href="${ctx}/" class="btn btn-secondary">
                            <i class="bi bi-arrow-left"></i> Quay về Trang chủ
                        </a>
                    </div>
                </c:when>

                <c:otherwise>
                    <c:choose>
                        <c:when test="${empty list}">
                            <div class="alert alert-info shadow-sm">
                                Bạn chưa có đơn đặt vé nào.
                                <a href="${ctx}/" class="alert-link">Về trang chủ</a> để đặt vé nhé.
                            </div>
                        </c:when>

                        <c:otherwise>
                            <div class="card shadow-sm">
                                <div class="table-responsive">
                                    <table class="table align-middle table-hover mb-0">
                                        <thead class="table-light sticky-top">
                                            <tr>
                                                <th>Mã</th>
                                                <th>Chuyến</th>
                                                <th>Tàu</th>
                                                <th>Ghế</th>
                                                <th class="text-center">SL</th>
                                                <th class="text-end">Tổng tiền</th>
                                                <th>Trạng thái</th>
                                                <th>Tạo lúc</th>
                                                <th>Thanh toán lúc</th>
                                                <th></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="b" items="${list}">
                                                <tr>
                                                    <td class="fw-semibold">BK-${b.bookingId}</td>
                                                    <td>
                                                        <span class="badge bg-primary-subtle text-primary border">
                                                            ${b.tripCode}
                                                        </span>
                                                    </td>
                                                    <td>${b.trainName}</td>
                                                    <td class="text-truncate" style="max-width:220px" title="${b.seatCodes}">
                                                        ${b.seatCodes}
                                                    </td>
                                                    <td class="text-center">${b.itemCount}</td>
                                                    <td class="text-end">
                                                        <fmt:formatNumber value="${b.totalAmount}" type="currency"/>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${b.status=='PAID'}">
                                                                <span class="badge rounded-pill bg-success">
                                                                    <i class="bi bi-check2-circle"></i> ĐÃ THANH TOÁN
                                                                </span>
                                                            </c:when>
                                                            <c:when test="${b.status=='HOLD'}">
                                                                <span class="badge rounded-pill bg-warning text-dark">
                                                                    <i class="bi bi-hourglass-split"></i> GIỮ CHỖ
                                                                </span>
                                                            </c:when>
                                                            <c:when test="${b.status=='DRAFT'}">
                                                                <span class="badge rounded-pill bg-secondary">NHÁP</span>
                                                            </c:when>
                                                            <c:when test="${b.status=='CANCELED'}">
                                                                <span class="badge rounded-pill bg-danger">ĐÃ HỦY</span>
                                                            </c:when>
                                                            <c:when test="${b.status=='EXPIRED'}">
                                                                <span class="badge rounded-pill bg-dark">HẾT HẠN</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge rounded-pill bg-light text-dark">${b.status}</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>${b.createdAtStr}</td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty b.paidAtStr}">
                                                                ${b.paidAtStr}
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-muted">—</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="text-end">
                                                        <a class="btn btn-outline-primary btn-sm"
                                                           href="${ctx}/booking/detail?id=${b.bookingId}">
                                                            Chi tiết
                                                        </a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>

                                <c:if test="${lastPage > 1}">
                                    <div class="card-footer d-flex justify-content-between align-items-center">
                                        <small class="text-muted">Tổng: ${total} đơn</small>
                                        <nav>
                                            <ul class="pagination pagination-sm mb-0">
                                                <c:forEach var="p" begin="1" end="${lastPage}">
                                                    <li class="page-item ${p==page?'active':''}">
                                                        <a class="page-link"
                                                           href="${ctx}/booking-history?page=${p}&size=${size}&status=${status}">
                                                            ${p}
                                                        </a>
                                                    </li>
                                                </c:forEach>
                                            </ul>
                                        </nav>
                                    </div>
                                </c:if>
                            </div>

                            <div class="mt-3">
                                <a href="${ctx}/home" class="btn btn-secondary">
                                    <i class="bi bi-arrow-left"></i> Quay về Trang chủ
                                </a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>

        </div>

        <script src="${ctx}/assets/bootstrap/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
