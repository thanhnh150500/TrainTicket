<%-- 
    Document   : register
    Created on : Oct 2, 2025, 12:36:35 AM
    Author     : New User
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Create Account</title>
        
         <!-- Bootstrap & Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
    </head>
    <body>
        <div class="login-card">
            <h2 class="text-center">Create Account</h2>
            <p class="text-center">Enter your details to register</p>
            
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>
                
            <form method="post" action="${pageContext.request.contextPath}/auth/register" novalidate>
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"><!-- comment -->

                <div class="mb-3">
                    <label class="form-label">Full Name</label>
                    <input name="fullName" class="form-control" placeholder="Nguyen Van A" required>
                </div>

                <div class="mb-3">
                    <label class="form-label">Email Address</label>
                    <input name="email" class="form-control" placeholder="you@example.com" required>
                </div>

                <div class="mb-3">
                    <label class="form-label">Password</label>
                    <div class="input-group">
                        <input type="password" name="password" id="regPassword" class="form-control" placeholder="••••••••" minlength="8" required>
                        <button type="button" class="btn btn-outline-secondary" id="toggleRegPassword" tabindex="-1">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>
                    <div class="form-text" At least 8 characters. </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Confirm Password</label>
                    <div class="input-group">
                        <input type="password" id="regConfirm" class="form-control" placeholder="••••••••" minlength="8" required><!-- comment -->
                        <button type="button" class="btn btn-outline-secondary" id="toggleRegConfirm" tabindex="-1">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>
                </div>

                <button class="btn btn-primary w-100" type="submit">Create account</button>

                <p class="text-center mt-3 mb-0">
                    Already have an account?
                    <a href="${pageContext.request.contextPath}/auth/login">Sign in</a>
                </p>
            </form>
        </div>
    </body>
</html>
