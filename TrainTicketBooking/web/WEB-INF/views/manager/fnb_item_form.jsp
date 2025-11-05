<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>
            <c:choose>
                <c:when test="${x != null && x.itemId != null}">Sửa món #${x.itemId}</c:when>
                <c:otherwise>Thêm món</c:otherwise>
            </c:choose>
        </title>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    </head>
    <body class="pb-5">
        <%-- Giả sử bạn có header này --%>
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <div class="container mt-4">
            <h3 class="mb-3">
                <c:choose>
                    <c:when test="${x != null && x.itemId != null}">Sửa món #${x.itemId}</c:when>
                    <c:otherwise>Thêm món</c:otherwise>
                </c:choose>
            </h3>

            <%-- Hiển thị lỗi validation (nếu có) --%>
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <%-- (1) Thêm enctype cho form --%>
            <form method="post" action="${pageContext.request.contextPath}/manager/fnb-items" 
                  accept-charset="UTF-8" enctype="multipart/form-data">
                
                <input type="hidden" name="op" value="save"/>
                
                <%-- (2) Thêm trường CSRF --%>
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                
                <c:if test="${x != null && x.itemId != null}">
                    <input type="hidden" name="item_id" value="${x.itemId}"/>
                </c:if>

                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label">Tên món</label>
                        <input type="text" name="name" class="form-control" value="<c:out value='${x.name}'/>" required />
                    </div>

                    <div class="col-md-6">
                        <label class="form-label">Mã món</label>
                        <input type="text" name="code" class="form-control" value="<c:out value='${x.code}'/>" required 
                               ${x.itemId != null ? 'readonly' : ''} />
                        <c:if test="${x.itemId != null}">
                            <small class="form-text">Không thể sửa mã của món đã tạo.</small>
                        </c:if>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label">Danh mục</label>
                        <select name="category_id" class="form-select">
                            <option value="">-- không có --</option>
                            <c:forEach items="${categories}" var="c">
                                <option value="${c.categoryId}" ${x.categoryId != null && x.categoryId == c.categoryId ? 'selected' : ''}>
                                    ${c.name}
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label">Giá</label>
                        <input type="number" step="1000" name="price" class="form-control" 
                               value="${empty x.price ? '0' : x.price}" required />
                    </div>
                    
                    <div class="col-12">
                        <label class="form-label">Tải ảnh mới</label>
                        <input type="file" name="image_file" class="form-control" accept="image/png, image/jpeg, image/webp" />
                        <small class="form-text">Bỏ trống nếu không muốn đổi ảnh.</small>
                    </div>

                    <c:if test="${not empty x.imageUrl}">
                        <div class="col-12">
                             <label>Ảnh hiện tại:</label><br/>
                             <%-- ${pageContext.request.contextPath} là bắt buộc để hiển thị ảnh --%>
                             <img src="${pageContext.request.contextPath}/${x.imageUrl}" 
                                  alt="Preview" style="max-width: 150px; max-height: 150px; margin-top: 10px; border: 1px solid #ddd;" />
                             
                             <%-- Trường ẩn này để servlet biết ảnh cũ là gì --%>
                             <input type="hidden" name="existing_image_url" value="<c:out value='${x.imageUrl}'/>" />
                        </div>
                    </c:if>

                    <div class="col-12">
                        <div class="form-check form-switch mt-3">
                            <input class="form-check-input" type="checkbox" role="switch" id="is_active" name="is_active"
                                   ${(x.itemId == null or x.active) ? 'checked' : ''} />
                            <label class="form-check-label" for="is_active">Đang hoạt động (bán)</label>
                        </div>
                    </div>
                </div>

                <div class="d-flex gap-2 mt-4">
                    <button class="btn btn-primary" type="submit">Lưu</button>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/fnb-items">Hủy</a>
                </div>
            </form>
        </div>
        
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>