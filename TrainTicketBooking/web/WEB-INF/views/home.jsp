<%-- 
    Document   : home
    Created on : Oct 12, 2025, 7:31:02 PM
    Author     : New User
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/bootstrap/css/bootstrap.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/icons/fonts/bootstrap-icons.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/navbar.css">
        <title>Home - TrainTicket</title>
        <%@include file="/WEB-INF/views/layout/_header.jsp" %>
    </head>
    <body>
        <main class="container py-4">
            <h1 class="h4">Body</h1>
        </main>
        <%@ include file="/WEB-INF/views/layout/_footer.jsp" %>
        <script src="${pageContext.request.contextPath}/assets/bootstrap/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
