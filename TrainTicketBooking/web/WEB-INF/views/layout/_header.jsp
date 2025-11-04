<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c"  uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!-- Lấy URI gốc: ưu tiên giá trị forward nếu có (Tomcat 10 dùng 'jakarta.*'; fallback 'javax.*') -->
<c:choose>
    <c:when test="${not empty requestScope['jakarta.servlet.forward.request_uri']}">
        <c:set var="rawUri" value="${requestScope['jakarta.servlet.forward.request_uri']}" />
        <c:set var="rawQs"  value="${requestScope['jakarta.servlet.forward.query_string']}" />
    </c:when>
    <c:when test="${not empty requestScope['javax.servlet.forward.request_uri']}">
        <c:set var="rawUri" value="${requestScope['javax.servlet.forward.request_uri']}" />
        <c:set var="rawQs"  value="${requestScope['javax.servlet.forward.query_string']}" />
    </c:when>
    <c:otherwise>
        <c:set var="rawUri" value="${pageContext.request.requestURI}" />
        <c:set var="rawQs"  value="${pageContext.request.queryString}" />
    </c:otherwise>
</c:choose>

<!-- path = rawUri bỏ contextPath -->
<c:set var="path" value="${fn:replace(rawUri, ctx, '')}" />
<!-- Nếu đang trỏ vào /WEB-INF hoặc .jsp hoặc rỗng → ép về /home -->
<c:if test="${fn:startsWith(path, '/WEB-INF') or fn:endsWith(path, '.jsp') or path == '' or path == '/'}">
    <c:set var="path" value="/home" />
</c:if>
<!-- here = path + ?query (nếu có) -->
<c:set var="here" value="${path}" />
<c:if test="${not empty rawQs}">
    <c:set var="here" value="${here}?${rawQs}" />
</c:if>

<!-- Build URL đúng (c:url tự thêm contextPath) -->
<c:url var="homeUrl"   value="/home" />
<c:url var="loginUrl"  value="/auth/login"><c:param name="next" value="${here}" /></c:url>
<c:url var="logoutUrl" value="/auth/logout" />
<c:url var="registerUrl" value="/auth/register" />

<!-- (Giữ nếu bạn cần CSS này, nhưng tốt nhất include ở <head> của layout) -->
<link rel="stylesheet" href="${ctx}/assets/css/main.css">

<nav class="navbar navbar-expand-lg navbar-light bg-white sticky-top tt-nav">
    <div class="container">

        <a class="navbar-brand d-flex align-items-center fw-semibold" href="${homeUrl}">
            <i class="bi bi-train-front-fill me-2"></i> TrainTicket
        </a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#ttNavbar"
                aria-controls="ttNavbar" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="ttNavbar">

            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <!-- Active khi path bắt đầu bằng /home -->
                    <a class="nav-link ${ fn:startsWith(path, '/home') ? 'active' : '' }" href="${homeUrl}">Tìm vé</a>
                </li>
                <li class="nav-item"><a class="nav-link" href="#">Thông tin đặt chỗ</a></li>
                <li class="nav-item"><a class="nav-link" href="#">Kiểm tra vé</a></li>
                <li class="nav-item"><a class="nav-link" href="#">Thông tin ga tàu</a></li>
                <li class="nav-item"><a class="nav-link" href="#">Khuyến mãi</a></li>
                <li class="nav-item"><a class="nav-link" href="#">Quy Định</a></li>
                <li class="nav-item"><a class="nav-link" href="#">Menu trên tàu</a></li>
                <li class="nav-item"><a class="nav-link" href="#">Liên hệ</a></li>
            </ul>

            <ul class="navbar-nav align-items-lg-center mb-2 mb-lg-0">
                <c:choose>
                    <c:when test="${not empty sessionScope.authUser}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle d-flex align-items-center" href="#" data-bs-toggle="dropdown">
                                <i class="bi bi-person-circle me-1"></i>${sessionScope.authUser.fullName}
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="#">Tài khoản</a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <form action="${logoutUrl}" method="post" class="px-3 py-1">
                                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                        <button class="btn btn-outline-danger w-100">
                                            <i class="bi bi-box-arrow-right me-1"></i>Đăng xuất
                                        </button>
                                    </form>
                                </li>
                            </ul>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item me-2">
                            <a class="btn btn-outline-primary" href="${loginUrl}">Đăng nhập</a>
                        </li>
                        <li class="nav-item">
                            <a class="btn btn-primary ${ fn:startsWith(path, '/auth/register') ? 'active' : '' }"
                               href="${registerUrl}">Đăng ký</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>

        </div>
    </div>
</nav>
