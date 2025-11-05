<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="uri" value="${pageContext.request.requestURI}" />

<%-- (Giữ style của Manager) --%>
<link rel="stylesheet" href="${ctx}/assets/css/main.css">

<%-- (Giữ style của Manager: bg-white, navbar-light) --%>
<nav class="navbar navbar-expand-lg navbar-light bg-white sticky-top tt-nav border-bottom">
    <div class="container">

        <a class="navbar-brand d-flex align-items-center fw-semibold" href="${ctx}/admin">
            <i class="bi bi-train-front-fill me-2"></i> TrainTicket <span class="ms-1 text-muted">Admin</span>
        </a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#ttAdminNavbar"
                aria-controls="ttAdminNavbar" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="ttAdminNavbar">

            <ul class="navbar-nav me-auto mb-2 mb-lg-0">

                <li class="nav-item">
                    <a class="nav-link ${ (uri == ctx.concat('/admin')) || (uri == ctx.concat('/admin/')) ? 'active' : '' }"
                       href="${ctx}/admin">Tổng quan</a>
                </li>

                <li class="nav-item">
                    <a class="nav-link ${ fn:contains(uri,'/admin/users') ? 'active' : '' }"
                       href="${ctx}/admin/users">Quản lý Người dùng</a>
                </li>

                <%-- (Bạn có thể thêm các link khác của Admin ở đây) --%>

            </ul>

            <ul class="navbar-nav align-items-lg-center mb-2 mb-lg-0">
                <c:choose>
                    <%-- (SỬA) Đảm bảo key là AUTH_USER (khớp với LoginServlet) --%>
                    <c:when test="${not empty sessionScope.AUTH_USER}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle d-flex align-items-center" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-person-circle me-1"></i>${sessionScope.AUTH_USER.fullName}
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="${ctx}/profile">Tài khoản</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <%-- (Giữ nguyên form logout của bạn) --%>
                                    <form action="${ctx}/auth/logout" method="post" class="px-3 py-1">
                                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                        <button class="btn btn-outline-danger w-100">
                                            <i class="bi bi-box-arrow-right me-1"></i> Đăng xuất
                                        </button>
                                    </form>
                                </li>
                            </ul>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item me-2">
                            <a class="btn btn-outline-primary" href="${ctx}/auth/login">
                                <i class="bi bi-person me-1"></i> Đăng nhập
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="btn btn-primary" href="${ctx}/auth/register">Đăng ký</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>

<div style="height:56px;"></div>

<style>
    .tt-nav.navbar {
        z-index: 1050;
    }
    .navbar .nav-link.active {
        font-weight: 600;
    }
    .navbar .nav-link:hover {
        color: #0d6efd;
    }
</style>