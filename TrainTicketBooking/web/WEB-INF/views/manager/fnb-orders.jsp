<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>Quản lý Đơn hàng F&B</title>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet" />
    </head>
    <body class="bg-light">

        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <div class="container mt-4">

            <%-- (Hiển thị Flash Message) --%>
            <c:if test="${not empty sessionScope.flash_success}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    ${sessionScope.flash_success}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <c:remove var="flash_success" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.flash_error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    ${sessionScope.flash_error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <c:remove var="flash_error" scope="session"/>
            </c:if>

            <h3 class="mb-3">Quản lý Đơn hàng F&B</h3>

            <%-- Form Lọc (Giống trang Báo cáo) --%>
            <div class="card shadow-sm mb-4">
                <div class="card-body">
                    <form method="GET" action="${pageContext.request.contextPath}/manager/fnb-orders" class="row g-3 align-items-end">
                        <div class="col-md-5">
                            <label for="from" class="form-label">Từ ngày</label>
                            <input type="date" class="form-control" id="from" name="from" value="${fromDate}" required>
                        </div>
                        <div class="col-md-5">
                            <label for="to" class="form-label">Đến ngày</label>
                            <input type="date" class="form-control" id="to" name="to" value="${toDate}" required>
                        </div>
                        <div class="col-md-2 d-grid">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-search"></i> Lọc
                            </button>
                        </div>
                    </form>
                </div>
                <%-- (Hiển thị Doanh thu) --%>
                <div class="card-footer d-flex justify-content-around">
                    <div class="text-center">
                        <h6 class="text-muted mb-0">Doanh thu (Đã trả)</h6>
                        <h4 class="text-success mb-0">
                            <fmt:formatNumber value="${totalRevenue}" type="currency" currencySymbol="đ" minFractionDigits="0"/>
                        </h4>
                    </div>
                    <div class="text-center">
                        <h6 class="text-muted mb-0">Chờ thanh toán</h6>
                        <h4 class="text-warning mb-0">
                            <fmt:formatNumber value="${totalPending}" type="currency" currencySymbol="đ" minFractionDigits="0"/>
                        </h4>
                    </div>
                </div>
            </div>

            <div class="card shadow-sm">
                <div class="card-body">
                    
                    <div class="table-responsive">
                        <table class="table table-striped table-hover align-middle" style="font-size: 0.9em;">
                            <thead class="table-dark">
                                <tr>
                                    <th>ID</th>
                                    <th>Chuyến tàu</th>
                                    <th>Ghế</th>
                                    <th>Tổng tiền</th>
                                    <th>Trạng thái</th>
                                    <th>Thanh toán</th>
                                    <th>Giờ tạo</th>
                                    <th>Staff tạo</th>
                                    <th style="width: 150px;">Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${orders}" var="order">
                                    <tr>
                                        <td><strong>#${order.orderId}</strong></td>
                                        <td>
                                            <%-- (SỬA LỖI) Thêm kiểm tra 'not empty order.trip' --%>
                                            <c:if test="${not empty order.trip}">
                                                <div><strong>${order.trip.trainCode}</strong> (${order.trip.originName} → ${order.trip.destName})</div>
                                                <small class="text-muted">${order.trip.departAtStr}</small>
                                            </c:if>
                                        </td>
                                        <td>${order.seatLabel}</td>
                                        <td>
                                            <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="đ" minFractionDigits="0"/>
                                        </td>
                                        <td>
                                            <%-- (SỬA) Chỉ dùng các giá trị phù hợp với enum trong model: CREATED/DELIVERED/CANCELED --%>
                                            <c:if test="${not empty order.orderStatus}">
                                                <select name="orderStatus" class="form-select form-select-sm" form="form_${order.orderId}" <c:if test="${order.orderStatus == 'CANCELED'}">disabled</c:if>>
                                                    <option value="CREATED" <c:if test="${order.orderStatus == 'CREATED'}">selected</c:if>>Mới tạo</option>
                                                    <option value="DELIVERED" <c:if test="${order.orderStatus == 'DELIVERED'}">selected</c:if>>Đã giao</option>
                                                    <option value="CANCELED" <c:if test="${order.orderStatus == 'CANCELED'}">selected</c:if>>Đã hủy</option>
                                                </select>
                                            </c:if>
                                        </td>
                                        <td>
                                            <%-- (SỬA) PaymentStatus: PENDING/SUCCESS/FAILED --%>
                                            <c:if test="${not empty order.paymentStatus}">
                                                <select name="paymentStatus" class="form-select form-select-sm" form="form_${order.orderId}" <c:if test="${order.orderStatus == 'CANCELED'}">disabled</c:if>>
                                                    <option value="PENDING" <c:if test="${order.paymentStatus == 'PENDING'}">selected</c:if>>Chờ thanh toán</option>
                                                    <option value="SUCCESS" <c:if test="${order.paymentStatus == 'SUCCESS'}">selected</c:if>>Đã thanh toán</option>
                                                    <option value="FAILED" <c:if test="${order.paymentStatus == 'FAILED'}">selected</c:if>>Thất bại</option>
                                                </select>
                                            </c:if>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${order.createdAt}" pattern="HH:mm dd/MM" timeZone="Asia/Ho_Chi_Minh"/>
                                        </td>
                                        <td>${order.createdByStaffName}</td>
                                        <td>
                                            <form id="form_${order.orderId}" method="POST" action="${pageContext.request.contextPath}/manager/fnb-orders" style="display:inline">
                                                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}" />
                                                <input type="hidden" name="op" value="update_status" />
                                                <input type="hidden" name="orderId" value="${order.orderId}" />
                                                <%-- (Gửi lại param lọc) --%>
                                                <input type="hidden" name="from" value="${fromDate}" />
                                                <input type="hidden" name="to" value="${toDate}" />
                                                <c:if test="${not empty order.orderStatus && order.orderStatus != 'CANCELED'}">
                                                    <button type="submit" class="btn btn-success btn-sm">
                                                        <i class="bi bi-check-lg"></i> Cập nhật
                                                    </button>
                                                </c:if>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            <c:if test="${empty orders}">
                                <tr>
                                    <td colspan="9" class="text-center">Không tìm thấy đơn hàng nào trong khoảng thời gian này.</td>
                                </tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>