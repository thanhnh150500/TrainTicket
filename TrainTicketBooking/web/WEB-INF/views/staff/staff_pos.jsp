<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>

<html lang="vi">
<head>
<meta charset="UTF-8" />
<title>Tạo Đơn Hàng F&B - Chuyến ${tripId}</title>
<meta name="viewport" content="width=device-width, initial-scale=1" />
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet" />

<%-- (CSS Tùy chỉnh cho giao diện mới) --%>

<style>
body {
background-color: #f8f9fa;
}

/* (Sửa card) */
.item-card {
    transition: all 0.2s ease;
}
.item-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.08) !important;
}

/* =======================================================
  ==&gt; (SỬA) THU NHỎ GIAO DIỆN
  =======================================================
*/

/* (SỬA ẢNH) - Thu nhỏ ảnh */
.card-img-top {
    width: 100%;
    height: 130px; /* (Giảm từ 180px) */
    object-fit: cover;
    background-color: #e9ecef;
}

/* (SỬA) - Thu nhỏ text */
.item-card .card-body {
    padding: 0.75rem; /* (Giảm padding một chút) */
}
.item-card .item-name {
    font-size: 0.95rem; /* (Thu nhỏ tên) */
    font-weight: 500;
    margin-bottom: 0.25rem; /* (Giảm khoảng cách) */
    
    /* (Chống tràn text nếu tên quá dài) */
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
 .item-card .item-price {
    font-size: 0.9rem; /* (Thu nhỏ giá) */
    color: #d9534f;
    font-weight: bold;
}
.item-card .card-footer {
    padding: 0 0.75rem 0.75rem; /* (Giảm padding) */
}

/* (SỬA WIDGET) - Thu nhỏ nút */
.quantity-widget {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.quantity-widget .form-control {
    text-align: center;
    border-left: 0;
    border-right: 0;
    border-radius: 0;
    padding: 0.25rem 0.25rem; /* (Giảm padding) */
    height: 34px; /* (Giảm từ 38px) */
    font-size: 0.9rem;
    pointer-events: none; 
}
.quantity-widget .btn {
    border-radius: 0.375rem;
    z-index: 1;
    padding: 0.25rem 0.75rem; /* (Thu nhỏ nút) */
    height: 34px; /* (Giữ chiều cao) */
}
 .quantity-widget .btn-dec {
    border-top-right-radius: 0;
    border-bottom-right-radius: 0;
}
 .quantity-widget .btn-inc {
    border-top-left-radius: 0;
    border-bottom-left-radius: 0;
}

/* (Cột order bên phải - Giữ nguyên) */
.order-summary-item {
    font-size: 0.9rem;
    padding-bottom: 0.5rem;
    margin-bottom: 0.5rem;
    border-bottom: 1px solid #eee;
}
.order-summary-item:last-child {
    border-bottom: 0;
}


</style>

</head>
<body class="bg-light">

<%@ include file="/WEB-INF/views/layout/_header_staff.jsp" %>

<div class="container mt-4">

    <%-- (Hiển thị Flash Message - Giữ nguyên) --%>
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

    <h3 class="mb-3">Tạo Đơn Hàng (Chuyến ${tripId})</h3>

    <c:choose>
        <c:when test="${trip == null}">
            <div class="alert alert-warning">Không tìm thấy thông tin chuyến. Vui lòng kiểm tra lại.</div>
        </c:when>
        <c:when test="${tripNotStarted}">
            <div class="alert alert-warning">Chuyến chưa đến giờ khởi hành (${trip.departAt}). Không thể tạo đơn trước khi khởi hành.</div>
        </c:when>
        <c:when test="${tripFinished}">
            <div class="alert alert-danger">Chuyến đã kết thúc (${trip.arriveAt}). Không thể tạo đơn.</div>
        </c:when>
        <c:when test="${tripStatus != 'RUNNING'}">
            <div class="alert alert-danger">Chuyến này hiện có trạng thái <strong>${tripStatus}</strong>. Nếu chuyến chưa chạy hoặc đã kết thúc thì không thể tạo đơn.</div>
        </c:when>
    </c:choose>

    <form method="POST" action="${pageContext.request.contextPath}/staff/pos" id="orderForm">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}" />
        <input type="hidden" name="trip_id" value="${tripId}" />

        <div class="row">
            <!-- ============================================ -->
            <!-- CỘT TRÁI: MENU (DẠNG LƯỚI)           -->
            <!-- ============================================ -->
            <div class="col-lg-8">
                <%-- (SỬA) Bỏ card-body để padding 0 --%>
                <div class="p-0" style="max-height: 80vh; overflow-y: auto;">
                    
                    <%-- Lặp qua các danh mục --%>
                    <c:forEach items="${categories}" var="category">
                        <h4 class="mt-3">${category.name}</h4>
                        <hr class="mt-1 mb-3">
                        
                        <%-- Dùng layout lưới (Grid) --%>
                        <div class="row g-3 row-cols-1 row-cols-md-2 row-cols-lg-3">
                            
                            <c:set var="items" value="${itemsByCategory[category.categoryId]}" />
                            <c:if test="${not empty items}">
                                <c:forEach items="${items}" var="item">
                                    <div class="col">
                                        <div class="card h-100 shadow-sm item-card" data-item-id="${item.itemId}" data-item-name="${item.name}" data-item-price="${item.price}">
                                            
                                            <%-- (Giải quyết lỗi nháy ảnh) --%>
                                            <c:choose>
                                                <c:when test="${not empty item.imageUrl}">
                                                    <img src="${pageContext.request.contextPath}/${item.imageUrl}" 
                                                         class="card-img-top" alt="${item.name}">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="https://placehold.co/400x300/e9ecef/6c757d?text=No%5CnImage" 
                                                         class="card-img-top" alt="Chưa có ảnh">
                                                </c:otherwise>
                                            </c:choose>

                                            <%-- (SỬA) Giảm padding ở đây --%>
                                            <div class="card-body pb-2">
                                                <h6 class="card-title item-name">${item.name}</h6>
                                                <p class="card-text text-danger fw-bold item-price">
                                                    <fmt:formatNumber value="${item.price}" type="currency" currencySymbol="đ" minFractionDigits="0"/>
                                                </p>
                                            </div>
                                            
                                            <%-- (SỬA) Giảm padding ở đây --%>
                                            <div class="card-footer bg-white border-0 pt-0 pb-3">
                                                <div class="quantity-widget">
                                                    <button type="button" class="btn btn-outline-secondary btn-dec" aria-label="Decrease quantity">-</button>
                                                    <input type="number" 
                                                           class="form-control mx-0 item-quantity" 
                                                           name="quantity_${item.itemId}" 
                                                           value="0" min="0" 
                                                           readonly>
                                                    <button type="button" class="btn btn-outline-secondary btn-inc" aria-label="Increase quantity">+</button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:if>
                        </div> <%-- Hết .row (lưới) --%>
                    </c:forEach>
                    
                    <%-- Hiển thị món 'Khác' (tương tự) --%>
                    <c:set var="itemsOther" value="${itemsByCategory[0]}" />
                     <c:if test="${not empty itemsOther}">
                        <h4 class="mt-3">Khác</h4>
                        <hr class="mt-1 mb-3">
                        <div class="row g-3 row-cols-1 row-cols-md-2 row-cols-lg-3">
                            <c:forEach items="${itemsOther}" var="item">
                                <div class="col">
                                    <div class="card h-100 shadow-sm item-card" data-item-id="${item.itemId}" data-item-name="${item.name}" data-item-price="${item.price}">
                                        <c:choose>
                                            <c:when test="${not empty item.imageUrl}">
                                                <img src="${pageContext.request.contextPath}/${item.imageUrl}" class="card-img-top" alt="${item.name}">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="https://placehold.co/400x300/e9ecef/6c757d?text=No%5CnImage" class="card-img-top" alt="Chưa có ảnh">
                                            </c:otherwise>
                                        </c:choose>
                                        <%-- (SỬA) Giảm padding --%>
                                        <div class="card-body pb-2">
                                            <h6 class="card-title item-name">${item.name}</h6>
                                            <p class="card-text text-danger fw-bold item-price">
                                                <fmt:formatNumber value="${item.price}" type="currency" currencySymbol="đ" minFractionDigits="0"/>
                                            </p>
                                        </div>
                                        <%-- (SỬA) Giảm padding --%>
                                        <div class="card-footer bg-white border-0 pt-0 pb-3">
                                            <div class="quantity-widget">
                                                <button type="button" class="btn btn-outline-secondary btn-dec" aria-label="Decrease quantity">-</button>
                                                <input type="number" class="form-control mx-0 item-quantity" name="quantity_${item.itemId}" value="0" min="0" readonly>
                                                <button type="button" class="btn btn-outline-secondary btn-inc" aria-label="Increase quantity">+</button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                         </div>
                     </c:if>

                </div>
            </div>

            <!-- ============================================ -->
            <!-- CỘT PHẢI: ĐƠN HÀNG                   -->
            <!-- ============================================ -->
            <div class="col-lg-4">
                <div class="card shadow-sm sticky-top" style="top: 20px;">
                    <div class="card-header">
                        <h5 class="mb-0">Thông Tin Đơn Hàng</h5>
                    </div>
                    <div class="card-body">
                        <div class="mb-3">
                            <label for="seat_label" class="form-label">Số ghế/Vị trí</label>
                            <input type="text" class="form-control" id="seat_label" name="seat_label" placeholder="Ví dụ: A01, B12..." required>
                            <div id="seatError" class="form-text text-danger d-none">Số ghế không hợp lệ cho chuyến này.</div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Thanh toán</label>
                            <select class="form-select" name="payment_method" required>
                                <option value="CASH">Tiền mặt (CASH)</option>
                                <option value="QR">Quét QR (QR)</option>
                            </select>
                        </div>

                        <hr>
                        
                        <h6 class="border-bottom pb-2 mb-2">Tóm tắt</h6>
                        <div id="order-summary-items" style="max-height: 200px; overflow-y: auto; font-size: 0.9rem;">
                            <p class="text-muted text-center" id="empty-cart-message">Vui lòng chọn món...</p>
                            <%-- JS sẽ điền các món vào đây --%>
                        </div>
                        <hr>

                        <h4 class="d-flex justify-content-between align-items-center">
                            <span>Tổng cộng:</span>
                            <strong id="total-display" class="text-danger">0 đ</strong>
                        </h4>

                        <div class="d-grid mt-3">
                            <button type="submit" class="btn btn-success btn-lg">
                                <i class="bi bi-plus-circle"></i> Tạo Đơn
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </form>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<%-- (JAVASCRIPT - Giữ nguyên, không cần sửa) --%>
<script>
document.addEventListener('DOMContentLoaded', function() {
    // Định dạng tiền tệ (giữ nguyên)
    const formatter = new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    });

    // Lấy các element DOM
    const allItemCards = document.querySelectorAll('.item-card');
    const totalDisplay = document.getElementById('total-display');
    const summaryContainer = document.getElementById('order-summary-items');
    const emptyCartMessage = document.getElementById('empty-cart-message');

    // Hàm (MỚI) Xử lý nút [+] và [-]
    function handleQuantityButtons() {
        allItemCards.forEach(card => {
            const incButton = card.querySelector('.btn-inc');
            const decButton = card.querySelector('.btn-dec');
            const quantityInput = card.querySelector('.item-quantity');

            incButton.setAttribute('type', 'button');
            decButton.setAttribute('type', 'button');

            incButton.addEventListener('click', () => {
                let currentValue = parseInt(quantityInput.value) || 0;
                quantityInput.value = currentValue + 1;
                updateOrder();
            });

            decButton.addEventListener('click', () => {
                let currentValue = parseInt(quantityInput.value) || 0;
                if (currentValue > 0) {
                    quantityInput.value = currentValue - 1;
                    updateOrder();
                }
            });
        });
    }

    // Hàm (MỚI) Cập nhật toàn bộ (Tóm tắt + Tổng tiền)
    function updateOrder() {
        let total = 0;
        let summaryHtml = '';
        
        allItemCards.forEach(card => {
            const quantityInput = card.querySelector('.item-quantity');
            const quantity = parseInt(quantityInput.value) || 0;
            
            if (quantity > 0) {
                const price = parseFloat(card.dataset.itemPrice) || 0;
                const name = card.dataset.itemName;
                
                total += (quantity * price);
                
                summaryHtml += `
                    <div class="order-summary-item d-flex justify-content-between">
                        <div>
                            <strong>\${quantity}x</strong> \${name}
                        </div>
                        <div class="fw-bold">
                            \${formatter.format(quantity * price)}
                        </div>
                    </div>
                `;
            }
        });

        if (summaryHtml === '') {
            emptyCartMessage.style.display = 'block';
            summaryContainer.innerHTML = '';
        } else {
            emptyCartMessage.style.display = 'none';
            summaryContainer.innerHTML = summaryHtml;
        }
        
        totalDisplay.textContent = formatter.format(total);
    }

    handleQuantityButtons();
    updateOrder();
});
</script>

<script>
// Seat validation: check seat exists in trip and trip is RUNNING
(function(){
    var seatInput = document.getElementById('seat_label');
    var seatError = document.getElementById('seatError');
    var orderForm = document.getElementById('orderForm');
    var tripId = '${tripId}';

    function validateSeat(cb){
        var val = seatInput.value.trim();
        if (!val) { seatError.classList.add('d-none'); if(cb) cb(false); return; }
        var ctx = '${pageContext.request.contextPath}';
        fetch(ctx + '/staff/pos?op=checkSeat&tripId=' + encodeURIComponent(tripId) + '&seat_label=' + encodeURIComponent(val), {credentials:'same-origin'})
            .then(r => r.json())
            .then(function(data){
                if (data && data.available === true) {
                    seatError.classList.add('d-none');
                    if (cb) cb(true);
                } else {
                    seatError.textContent = data && data.message ? data.message : 'Số ghế không hợp lệ cho chuyến này.';
                    seatError.classList.remove('d-none');
                    if (cb) cb(false);
                }
            }).catch(function(){
                seatError.textContent = 'Lỗi kiểm tra ghế.';
                seatError.classList.remove('d-none');
                if (cb) cb(false);
            });
    }

    if (seatInput) {
        seatInput.addEventListener('blur', function(){ validateSeat(); });
        seatInput.addEventListener('input', function(){ seatError.classList.add('d-none'); });
    }

    if (orderForm) {
        orderForm.addEventListener('submit', function(e){
            e.preventDefault();
            validateSeat(function(ok){
                if (ok) {
                    orderForm.submit();
                } else {
                    seatInput.focus();
                }
            });
        });
    }
})();
</script>

<script>
// Nếu chuyến không chạy thì disable UI chọn món và nút tạo đơn
(function(){
    var tripStatus = '${tripStatus}';
    if (tripStatus && tripStatus !== 'RUNNING') {
        // disable quantity buttons and submit
        document.querySelectorAll('.btn-inc, .btn-dec').forEach(function(b){ b.disabled = true; });
        document.querySelectorAll('.item-quantity').forEach(function(i){ i.value = 0; });
        var submit = document.querySelector('button[type="submit"]');
        if (submit) { submit.disabled = true; submit.classList.add('btn-secondary'); }
    }
})();
</script>


</body>
</html>