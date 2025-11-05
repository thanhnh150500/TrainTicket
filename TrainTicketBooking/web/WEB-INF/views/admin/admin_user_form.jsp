<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <title>
        <c:choose>
            <c:when test="${not empty user.userId}">Sửa Người dùng</c:when>
            <c:otherwise>Thêm Người dùng</c:otherwise>
        </c:choose>
    </title>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
</head>
<body class="bg-light">

    <%@ include file="/WEB-INF/views/layout/_header_admin.jsp" %>

    <div class="container mt-4 mb-5" style="max-width: 800px;">
        
        <h3 class="mb-3">
             <c:choose>
                <c:when test="${not empty user.userId}">Sửa Người dùng: ${user.fullName}</c:when>
                <c:otherwise>Thêm Nhân viên/Quản lý mới</c:otherwise>
            </c:choose>
        </h3>
        
        <c:if test="${not empty flash_error}">
            <div class="alert alert-danger">${flash_error}</div>
            <c:remove var="flash_error" scope="session"/>
        </c:if>

        <div class="card shadow-sm">
            <div class="card-body p-4">
                 <form method="POST" action="${pageContext.request.contextPath}/admin/users">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}" />
                    <input type="hidden" name="op" value="save" />
                    
                    <c:if test="${not empty user.userId}">
                        <input type="hidden" name="userId" value="${user.userId}" />
                    </c:if>
                    
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label for="email" class="form-label">Email (Tên đăng nhập)</label>
                            <input type="email" class="form-control" id="email" name="email" value="${user.email}" required>
                        </div>
                         <div class="col-md-6">
                            <label for="fullName" class="form-label">Họ và tên</label>
                            <input type="text" class="form-control" id="fullName" name="fullName" value="${user.fullName}" required>
                        </div>
                        
                        <div class="col-12">
                            <label for="password" class="form-label">Mật khẩu mới</label>
                            <input type="password" class="form-control" id="password" name="password" 
                                   ${empty user.userId ? 'required' : ''}>
                            <c:choose>
                                <c:when test="${not empty user.userId}">
                                    <small class="form-text text-muted">Bỏ trống nếu không muốn thay đổi mật khẩu.</small>
                                </c:when>
                                <c:otherwise>
                                    <small class="form-text text-muted">Bắt buộc khi tạo mới.</small>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        
                        <hr class="my-3">
                        
                        <div class="col-12">
                            <label class="form-label">Vai trò (Roles)</label>
                            <div>
                                <c:forEach items="${allRoles}" var="role">
                                    <div class="form-check form-check-inline">
                                        <%-- (Kiểm tra xem role.roleId có trong list userRoleIds không) --%>
                                        <c:set var="isChecked" value="${userRoleIds.contains(role.roleId)}" />
                                        
                                        <input class="form-check-input" type="checkbox" name="roleIds" 
                                               id="role_${role.roleId}" value="${role.roleId}" 
                                               ${isChecked ? 'checked' : ''}>
                                        <label class="form-check-label" for="role_${role.roleId}">${role.name}</label>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                        
                        <div class="col-12">
                             <div class="form-check form-switch mt-3">
                                <input class="form-check-input" type="checkbox" role="switch" id="isActive" name="isActive"
                                       ${(empty user.userId or user.active) ? 'checked' : ''} />
                                <label class="form-check-label" for="isActive">Kích hoạt (Cho phép đăng nhập)</label>
                            </div>
                        </div>
                    </div>
                    
                    <div class="d-flex gap-2 mt-4">
                        <button type="submit" class="btn btn-primary">Lưu thay đổi</button>
                        <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary">Hủy</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>