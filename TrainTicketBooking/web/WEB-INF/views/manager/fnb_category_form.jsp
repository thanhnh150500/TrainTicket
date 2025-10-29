<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>
            <c:choose>
                <c:when test="${c != null && c.categoryId != null}">Sửa danh mục #${c.categoryId}</c:when>
                <c:otherwise>Thêm danh mục món (F&B)</c:otherwise>
            </c:choose>
        </title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    </head>
    <body class="p-4">
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <h3 class="mb-3">
            <c:choose>
                <c:when test="${c != null && c.categoryId != null}">Sửa danh mục #${c.categoryId}</c:when>
                <c:otherwise>Thêm danh mục món (F&B)</c:otherwise>
            </c:choose>
        </h3>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/manager/fnb-categories">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <input type="hidden" name="op" value="save"/>
            <c:if test="${c != null && c.categoryId != null}">
                <input type="hidden" name="category_id" value="${c.categoryId}"/>
            </c:if>

            <div class="mb-3">
                <label class="form-label">Tên danh mục</label>
                <input type="text" name="name" class="form-control"
                       value="<c:out value='${c.name}'/>"
                       maxlength="100" required />
            </div>

            <div class="form-check mb-3">
                <input class="form-check-input" type="checkbox" id="is_active" name="is_active"
                       ${c.active ? 'checked' : ''}/>
                <label class="form-check-label" for="is_active">Hoạt động</label>
            </div>

            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary">Lưu</button>
                <a href="${pageContext.request.contextPath}/manager/fnb-categories" class="btn btn-secondary">Hủy</a>
            </div>
        </form>
    </body>
</html>
