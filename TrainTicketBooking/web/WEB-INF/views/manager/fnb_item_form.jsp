<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>
            <c:choose>
                <c:when test="${x != null && x.itemId != null}">Sửa món #${x.itemId}</c:when>
                <c:otherwise>Thêm món ăn / đồ uống</c:otherwise>
            </c:choose>
        </title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    </head>
    <body class="p-4">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <h3 class="mb-3">
            <c:choose>
                <c:when test="${x != null && x.itemId != null}">Sửa món #${x.itemId}</c:when>
                <c:otherwise>Thêm món ăn / đồ uống</c:otherwise>
            </c:choose>
        </h3>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/manager/fnb-items">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <input type="hidden" name="op" value="save" />
        <c:if test="${x != null && x.itemId != null}">
            <input type="hidden" name="item_id" value="${x.itemId}" />
        </c:if>

        <div class="row g-3">
            <div class="col-md-6">
                <label class="form-label">Danh mục</label>
                <select name="category_id" class="form-select">
                    <option value="">-- Chọn danh mục --</option>
                    <c:forEach items="${categories}" var="c">
                        <option value="${c.categoryId}"
                                <c:if test="${x.categoryId != null && x.categoryId == c.categoryId}">selected</c:if>>
                            ${c.name}
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-6">
                <label class="form-label">Mã món</label>
                <input type="text" name="code" class="form-control"
                       value="<c:out value='${x.code}'/>"
                       maxlength="40" required pattern="[A-Za-z0-9\-_]+"
                       title="Chỉ chữ, số, '-' hoặc '_'"/>
            </div>
        </div>

        <div class="mt-3">
            <label class="form-label">Tên món</label>
            <input type="text" name="name" class="form-control" value="<c:out value='${x.name}'/>" required maxlength="150" />
        </div>

        <div class="mt-3">
            <label class="form-label">Giá (VND)</label>
            <input type="number" step="100" min="0" name="price" class="form-control"
                   value="<c:out value='${x.price}'/>" required />
        </div>

        <div class="form-check mt-3">
            <input class="form-check-input" type="checkbox" id="is_active" name="is_active" ${x.active ? 'checked' : ''}>
            <label class="form-check-label" for="is_active">Đang kinh doanh</label>
        </div>

        <div class="d-flex gap-2 mt-3">
            <button class="btn btn-primary" type="submit">Lưu</button>
            <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/fnb-items">Hủy</a>
        </div>
    </form>
</body>
</html>
