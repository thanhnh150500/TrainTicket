<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib prefix="c"  uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="uri" value="${pageContext.request.requestURI}" />

<link rel="stylesheet" href="${ctx}/assets/css/main.css">

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

                <!-- Trips: link thẳng -->
                <li class="nav-item">
                    <a class="nav-link ${ fn:contains(uri,'/manager/trips') ? 'active' : '' }"
                       href="${ctx}/manager/trips">Lịch trình tàu</a>
                </li>

                <!-- Pricing: link thẳng -->
                <li class="nav-item">
                    <a class="nav-link ${ fn:contains(uri,'/manager/fare-rules') ? 'active' : '' }"
                       href="${ctx}/manager/fare-rules">Giá vé</a>
                </li>

                <!-- Catalog (giữ dropdown) -->
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
                    </ul>
                </li>

                <!-- Reports -->
                <li class="nav-item">
                    <a class="nav-link ${ fn:contains(uri,'/manager/reports') ? 'active' : '' }"
                       href="${ctx}/manager/reports">Báo cáo</a>
                </li>
            </ul>

            <!-- Right (user) -->
            <ul class="navbar-nav align-items-lg-center mb-2 mb-lg-0">
                <c:choose>
                    <c:when test="${not empty sessionScope.authUser}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle d-flex align-items-center" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-person-circle me-1"></i>${sessionScope.authUser.fullName}
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="${ctx}/profile">Tài khoản</a></li>
                                <li><a class="dropdown-item" href="${ctx}/settings">Thiết lập</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li>
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

<!-- chừa khoảng cho sticky-top -->
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
