<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <title>Thông Báo</title>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
</head>
<body class="bg-light">
    <%-- <%@ include file="/WEB-INF/views/layout/_header_staff.jsp" %> --%>
    <div class="container mt-4" style="max-width: 600px;">
        <div class="alert alert-warning" role="alert">
            <h4 class="alert-heading">Không thể truy cập!</h4>
            <p>
                <c:choose>
                    <c:when test="${not empty error}">
                        ${error}
                    </c:when>
                    <c:otherwise>
                        Bạn không có quyền truy cập hoặc không tìm thấy chuyến tàu.
                    </c:otherwise>
                </c:choose>
            </p>
            <hr>
            <p class="mb-0">Vui lòng liên hệ quản lý nếu bạn cho rằng đây là lỗi.</p>
        </div>
        <a href="${pageContext.request.contextPath}/auth/logout">Đăng xuất</a>
    </div>
</body>
</html>