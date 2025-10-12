<%-- 
    Document   : login
    Created on : Oct 2, 2025, 12:36:25 AM
    Author     : New User
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Sign In</title>
        
        <!-- Bootstrap & Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

        <!-- CSS riêng -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
    </head>
    <body>
        <div class="login-card">
            <h2 class="text-center">Sign In</h2>
            <p class="text-center">Enter your credentials to access your account</p>
            
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>
                
            <form method="post" action="${pageContext.request.contextPath}/auth/login">
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><!-- comment -->

                <div class="mb-3"><!-- comment -->
                    <label class="form-label">Email Address</label>
                    <input type="email" name="email" class="form-control" placeholder="you@example.com" required>
                </div>

                <div class="mb-3">
                    <label class="form-label">Password</label>
                    <div class="input-group">
                        <input type="password" name="password" class="form-control" id="passwordField" placeholder="••••••••" required>
                        <button type="button" class="btn btn-outline-secondary" id="togglePassword" tabindex="-1">
                            <i class="bi bi-eye" ></i>
                        </button>
                    </div>
                </div>

                <div class="d-flex justify-content-between algin-items-center mb-3">
                    <div class="form-check">
                        <input type="checkbox" class="form-check-input" id="rememberMe"><!-- comment -->
                        <label class="form-check-label" for="rememberMe">Remember me</label>
                    </div>
                    <a href="${pageContext.request.contextPath}/auth/forgot" class="small">Forgot Password?</a>
                </div>

                <button class="btn btn-primary w-100" type="submit">Sign In</button>

                <p class="text-center mt-3 mb-0">
                    Don't have an account?
                    <a href="${pageContext.request.contextPath}/auth/register">Create one</a>
                </p>
            </form>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
    </body>
</html>
