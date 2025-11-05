<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <title>Chọn Chuyến Tàu</title>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
</head>
<body class="bg-light">

    <%-- Giả sử bạn có header chung cho staff --%>
    <%-- <%@ include file="/WEB-INF/views/layout/_header_staff.jsp" %> --%>

    <div class="container mt-4" style="max-width: 800px;">
        <div class="card shadow-sm">
            <div class="card-header">
                <h4 class="mb-0">Chọn chuyến tàu để bán hàng</h4>
            </div>
            <div class="card-body">
                <p>Bạn được phân công vào nhiều chuyến tàu đang chạy. Vui lòng chọn một chuyến để tiếp tục:</p>
                
                <div class="list-group">
                    <c:forEach items="${trips}" var="trip">
                        <%-- Link này sẽ gọi lại StaffOrderServlet với tripId --%>
                        <a href="${pageContext.request.contextPath}/staff/pos?tripId=${trip.tripId}" 
                           class="list-group-item list-group-item-action">
                            
                            <div class="d-flex w-100 justify-content-between">
                                <h5 class="mb-1">${trip.trainCode} | ${trip.originName} → ${trip.destName}</h5>
                                <small>ID: ${trip.tripId}</small>
                            </div>
                            <p class="mb-1">
                                Khởi hành: 
                                <%-- Use helper Date property for formatting --%>
                                <fmt:formatDate value="${trip.departAtDate}" pattern="HH:mm 'ngày' dd/MM/yyyy" timeZone="Asia/Ho_Chi_Minh"/>
                            </p>
                            <small>Trạng thái: ${trip.status}</small>
                        </a>
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>
</body>
</html>