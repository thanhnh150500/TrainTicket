<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="uri" value="${pageContext.request.requestURI}" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">

<nav class="navbar navbar-expand-lg navbar-light bg-white sticky-top tt-nav">
    <div class="container">

        <a class="navbar-brand d-flex align-items-center fw-semibold" href="${ctx}/">
            <i class="bi bi-train-front-fill me-2"></i> TrainTicket
        </a>


        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#ttNavbar"
                aria-controls="ttNavbar" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>


        <div class="collapse navbar-collapse" id="ttNavbar">


            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link ${ (uri == ctx || uri == ctx.concat('/')) ? 'active' : '' }" 
                       href="${ctx}/">Tìm vé</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#">Thông tin ??t ch?</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#">Kiểm tra vé</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#">Thông tin ga tàu</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#">Khuyến mãi</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#">Quy Định</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#">Menu trên tàu</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="#">Liên hệ</a>
                </li>
            </ul>

            <ul class="navbar-nav align-items-lg-center mb-2 mb-lg-0">
                <c:choose>
                    <c:when test="${not empty sessionScope.authUser}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle d-flex align-items-center" href="#" data-bs-toggle="dropdown">
                                <i class="bi bi-person-circle me-1"></i>${sessionScope.authUser.fullName}
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="#">Tài kho?n</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <form action="${ctx}/auth/logout" method="post" class="px-3 py-1">
                                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                        <button class="btn btn-outline-danger w-100">
                                            <i class="bi bi-box-arrow-right me-1"></i>??ng xu?t
                                        </button>
                                    </form>
                                </li>
                            </ul>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item me-2">
                            <a class="btn btn-outline-primary ${ uri.startsWith(ctx.concat('/auth/login')) ? 'active' : '' }"
                               href="${ctx}/auth/login">
                                <i class="bi bi-person me-1"></i>??ng Nh?p
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="btn btn-primary ${ uri.startsWith(ctx.concat('/auth/register')) ? 'active' : '' }"
                               href="${ctx}/auth/register">??ng ký</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>
