<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="c"  uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="uri" value="${pageContext.request.requestURI}" />

<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${ctx}/assets/css/main.css">
<link rel="stylesheet" href="${ctx}/assets/css/theme.css">
<link rel="stylesheet" href="${ctx}/assets/icons/bootstrap-icons.min.css"><!-- nếu bạn dùng local icons -->

<nav class="navbar navbar-expand-lg navbar-light bg-white sticky-top tt-nav border-bottom">
    <div class="container">

        <!-- Brand -->
        <a class="navbar-brand d-flex align-items-center fw-semibold" href="${ctx}/manager">
            <i class="bi bi-train-front-fill me-2"></i> TrainTicket <span class="ms-1 text-muted">Manager</span>
        </a>

        <!-- Toggler -->
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#ttMgrNavbar"
                aria-controls="ttMgrNavbar" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <!-- Nav -->
        <div class="collapse navbar-collapse" id="ttMgrNavbar">

            <!-- Left -->
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">

                <!-- Tổng quan -->
                <li class="nav-item">
                    <a class="nav-link ${ (uri == ctx.concat('/manager')) || (uri == ctx.concat('/manager/')) ? 'active' : '' }"
                       href="${ctx}/manager">Tổng quan</a>
                </li>

                <!-- Trips -->
                <li class="nav-item">
                    <a class="nav-link ${ fn:contains(uri,'/manager/trips') ? 'active' : '' }"
                       href="${ctx}/manager/trips">Lịch trình tàu</a>
                </li>

                <!-- Pricing -->
                <li class="nav-item">
                    <a class="nav-link ${ fn:contains(uri,'/manager/fare-rules') ? 'active' : '' }"
                       href="${ctx}/manager/fare-rules">Giá vé</a>
                </li>

                <!-- Catalog -->
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle ${ fn:contains(uri,'/manager/cities')
                                                          || fn:contains(uri,'/manager/stations')
                                                          || fn:contains(uri,'/manager/routes')
                                                          || fn:contains(uri,'/manager/trains')
                                                          || fn:contains(uri,'/manager/carriages')
                                                          || fn:contains(uri,'/manager/seat-classes')
                                                          || fn:contains(uri,'/manager/seats') ? 'active' : '' }"
                       href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        Danh mục
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="${ctx}/manager/stations">Ga tàu</a></li>
                        <li><a class="dropdown-item" href="${ctx}/manager/routes">Tuyến</a></li>
                        <li><a class="dropdown-item" href="${ctx}/manager/trains">Đoàn tàu</a></li>
                        <li><a class="dropdown-item" href="${ctx}/manager/carriages">Toa</a></li>
                        <li><a class="dropdown-item" href="${ctx}/manager/seat-classes">Hạng ghế</a></li>
                        <li><a class="dropdown-item" href="${ctx}/manager/seats">Sơ đồ ghế</a></li>
                        <li><a class="dropdown-item" href="${ctx}/manager/fnb-items">Đồ Order</a></li>
                        <li><a class="dropdown-item" href="${ctx}/manager/fnb-categories">Loại dịch vụ</a></li>
                    </ul>
                </li>

                <!-- Reports -->
                <li class="nav-item">
                    <a class="nav-link ${ fn:contains(uri,'/manager/fnb-orders') ? 'active' : '' }"
                       href="${ctx}/manager/fnb-orders">Đơn F&B</a>
                </li>
            </ul>

            <!-- Right (user) -->
            <ul class="navbar-nav align-items-lg-center mb-2 mb-lg-0">
                <c:choose>
                    <%-- ĐÃ ĐĂNG NHẬP --%>
                    <c:when test="${not empty sessionScope.authUser}">
                        <c:set var="__fullName" value="${sessionScope.authUser.fullName}" />
                        <c:set var="__email"    value="${sessionScope.authUser.email}" />
                        <c:set var="displayName"
                               value="${
                               not empty fn:trim(__fullName)
                                   ? fn:trim(__fullName)
                                   : (not empty fn:trim(__email) ? fn:trim(__email) : 'Tài khoản')
                               }" />
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle d-flex align-items-center" href="#" role="button"
                               data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-person-circle me-1"></i>
                                <span><c:out value="${displayName}" /></span>

                                <c:if test="${sessionScope.isAdmin}">
                                    <span class="badge bg-danger ms-2">ADMIN</span>
                                </c:if>
                                <c:if test="${sessionScope.isManager and not sessionScope.isAdmin}">
                                    <span class="badge bg-primary ms-2">MANAGER</span>
                                </c:if>
                                <c:if test="${sessionScope.isStaff and (not sessionScope.isAdmin) and (not sessionScope.isManager)}">
                                    <span class="badge bg-secondary ms-2">STAFF</span>
                                </c:if>
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="${ctx}/profile">Tài khoản</a></li>
                                    <c:if test="${sessionScope.isAdmin}">
                                    <li><a class="dropdown-item" href="${ctx}/admin">Bảng điều khiển Admin</a></li>
                                    </c:if>
                                    <c:if test="${sessionScope.isManager}">
                                    <li><a class="dropdown-item" href="${ctx}/manager/trips">Quản lý chuyến</a></li>
                                    </c:if>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <form action="${ctx}/auth/logout" method="post" class="px-3 py-1">
                                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                                        <button class="btn btn-outline-danger w-100">
                                            <i class="bi bi-box-arrow-right me-1"></i> Đăng xuất
                                        </button>
                                    </form>
                                </li>
                            </ul>
                        </li>
                    </c:when>

                    <%-- CHƯA ĐĂNG NHẬP --%>
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
