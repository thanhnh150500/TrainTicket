<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <title>Quản lý Người dùng</title>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet" />
</head>
<body class="bg-light">

    <%@ include file="/WEB-INF/views/layout/_header_admin.jsp" %>

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
        
        <div class="d-flex justify-content-between align-items-center mb-3">
             <h3 class="mb-0">Quản lý Người dùng</h3>
             <a href="${pageContext.request.contextPath}/admin/users?op=new" class="btn btn-primary">
                 <i class="bi bi-plus-lg"></i> Thêm Nhân viên
             </a>
        </div>
        
        <%-- (Chia 2 Quản lý) --%>
        <ul class="nav nav-tabs mb-3">
            <li class="nav-item">
                <a class="nav-link ${currentTab == 'INTERNAL' ? 'active' : ''}" 
                   href="${pageContext.request.contextPath}/admin/users?tab=INTERNAL">Nhân viên & Quản lý</a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${currentTab == 'CUSTOMER' ? 'active' : ''}" 
                   href="${pageContext.request.contextPath}/admin/users?tab=CUSTOMER">Khách hàng</a>
            </li>
        </ul>

        <div class="card shadow-sm">
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-striped table-hover align-middle">
                        <thead class="table-dark">
                            <tr>
                                <th>ID</th>
                                <th>Họ tên</th>
                                <th>Email</th>
                                <th>Vai trò</th>
                                <th>Trạng thái</th>
                                <th style="width: 100px;">Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${userList}" var="user">
                                <tr>
                                    <td><small>${user.userId}</small></td>
                                    <td>${user.fullName}</td>
                                    <td>${user.email}</td>
                                    <td>
                                        <c:forEach items="${user.roles}" var="role" varStatus="loop">
                                            <span class="badge bg-secondary">${role.name}</span>
                                        </c:forEach>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${user.active}">
                                                <span class="badge bg-success">Hoạt động</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-danger">Đã khóa</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/users?op=edit&id=${user.userId}" 
                                           class="btn btn-sm btn-outline-primary">
                                            <i class="bi bi-pencil"></i> Sửa
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>