<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8" />
        <title>
            <c:choose>
                <c:when test="${t != null && t.tripId != null}">Sửa chuyến #${t.tripId}</c:when>
                <c:otherwise>Thêm chuyến</c:otherwise>
            </c:choose>
        </title>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet" />
        <style>
            .ul-tight {
                margin: .25rem 0 .75rem 1.25rem;
            }
            .muted {
                color:#6c757d;
            }
        </style>
    </head>
    <body class="pb-5">
        <%-- Navbar quản trị, nếu bạn đang dùng --%>
        <%@ include file="/WEB-INF/views/layout/_header_manager.jsp" %>

        <div class="container mt-4">
            <h3 class="mb-3">
                <c:choose>
                    <c:when test="${t != null && t.tripId != null}">Sửa chuyến #${t.tripId}</c:when>
                    <c:otherwise>Thêm chuyến</c:otherwise>
                </c:choose>
            </h3>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/manager/trips" accept-charset="UTF-8">
                <input type="hidden" name="op" value="save"/>
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                <c:if test="${t != null && t.tripId != null}">
                    <input type="hidden" name="trip_id" value="${t.tripId}"/>
                </c:if>

                <div class="row g-3">
                    <!-- Tuyến -->
                    <div class="col-md-6">
                        <label class="form-label">Tuyến</label>
                        <select id="route_id" name="route_id" class="form-select" required>
                            <option value="">-- chọn tuyến --</option>
                            <c:forEach items="${routes}" var="r">
                                <option value="${r.routeId}" ${t.routeId != null && t.routeId == r.routeId ? 'selected' : ''}>
                                    ${r.code} — ${r.originName} → ${r.destName}
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <!-- Đoàn tàu -->
                    <div class="col-md-6">
                        <label class="form-label">Đoàn tàu</label>
                        <select id="train_id" name="train_id" class="form-select" required>
                            <option value="">-- chọn tàu --</option>
                            <c:forEach items="${trains}" var="tr">
                                <!-- Lưu ý: value là trainId (số), KHÔNG phải code SE1/SE2 -->
                                <option value="${tr.trainId}" ${t.trainId != null && t.trainId == tr.trainId ? 'selected' : ''}>
                                    ${tr.code} — ${tr.name}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <!-- Panel meta TUYẾN -->
                <div id="routePane" class="mt-3">
                    <div class="alert alert-info">Chọn Tuyến để xem thông tin.</div>
                </div>

                <!-- Panel meta TÀU -->
                <div id="trainPane" class="mt-3">
                    <div class="alert alert-info">Chọn Đoàn tàu để xem thông tin.</div>
                </div>

                <hr class="my-4"/>

                <!-- Thời gian & Trạng thái -->
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label">Khởi hành</label>
                        <input type="datetime-local" name="depart_at" class="form-control"
                               value="<c:out value='${t.departAt}'/>" required />
                        <div class="form-text">Định dạng: yyyy-MM-ddTHH:mm</div>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Đến nơi</label>
                        <input type="datetime-local" name="arrive_at" class="form-control"
                               value="<c:out value='${t.arriveAt}'/>" required />
                    </div>
                </div>

                <div class="mt-3">
                    <label class="form-label">Trạng thái</label>
                    <select name="status" class="form-select" required>
                        <c:set var="st" value="${empty t.status ? 'SCHEDULED' : t.status}" />
                        <option value="SCHEDULED" ${st=='SCHEDULED'?'selected':''}>SCHEDULED</option>
                        <option value="RUNNING"   ${st=='RUNNING'  ?'selected':''}>RUNNING</option>
                        <option value="CANCELED"  ${st=='CANCELED' ?'selected':''}>CANCELED</option>
                        <option value="FINISHED"  ${st=='FINISHED' ?'selected':''}>FINISHED</option>
                    </select>
                </div>

                <div class="d-flex gap-2 mt-4">
                    <button class="btn btn-primary" type="submit">Lưu</button>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/manager/trips">Hủy</a>
                </div>
            </form>
        </div>

        <script>
      const ctx = '${pageContext.request.contextPath}';
      const routeSel = document.getElementById('route_id');
      const trainSel = document.getElementById('train_id');
      const routePane = document.getElementById('routePane');
      const trainPane = document.getElementById('trainPane');

      function escapeHtml(s) {
          return s ? s.replace(/[&<>"']/g, function (m) {
              return {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[m];
          }) : '';
      }

      // ===== Hiển thị meta TUYẾN =====
      function renderRoute(r) {
          if (!r || !r.routeId)
              return '<div class="alert alert-info">Chọn Tuyến để xem thông tin.</div>';
          var h = '';
          h += '<div class="card mb-2">'
                  + '<div class="card-header fw-semibold">Thông tin tuyến</div>'
                  + '<div class="card-body">'
                  + '<div><b>Mã tuyến:</b> ' + escapeHtml(r.code) + '</div>'
                  + '<div><b>Hành trình:</b> ' + escapeHtml(r.originName) + ' → ' + escapeHtml(r.destName) + '</div>'
                  + '</div>'
                  + '</div>';
          return h;
      }

      // Chuẩn hóa hiển thị position
      function labelPos(s) {
          if (!s)
              return '—';
          var m = s.trim().toUpperCase();
          if (m === 'A')
              return 'Dãy A';
          if (m === 'B')
              return 'Dãy B';
          if (m === 'UP' || m === 'U')
              return 'Tầng trên';
          if (m === 'DOWN' || m === 'L')
              return 'Tầng dưới';
          return s;
      }

      // ===== Hiển thị meta TÀU: thông tin + từng toa + toàn bộ ghế =====
      function renderTrain(t) {
          if (!t || !t.trainId)
              return '<div class="alert alert-info">Chọn Đoàn tàu để xem thông tin.</div>';

          var h = '';
          h += '<div class="card mb-2">'
                  + '<div class="card-header fw-semibold">Thông tin tàu</div>'
                  + '<div class="card-body">'
                  + '<div><b>Mã tàu:</b> ' + escapeHtml(t.code) + '</div>'
                  + '<div><b>Tên tàu:</b> ' + escapeHtml(t.name) + '</div>'
                  + '<div><b>Số lượng toa:</b> ' + (t.totalCarriages || 0) + '</div>'
                  + '<div class="mt-3"><b>Chi tiết toa &amp; ghế:</b></div>';

          if (t.carriages && t.carriages.length) {
              for (var i = 0; i < t.carriages.length; i++) {
                  var c = t.carriages[i];
                  h += '<div class="mt-2">'
                          + '<div><b>' + (i + 1) + '. Toa: ' + escapeHtml(c.code)
                          + ' (Hạng ghế: ' + escapeHtml(c.seatClassName || '—')
                          + (c.seatClassCode ? ' — ' + escapeHtml(c.seatClassCode) : '')
                          + ', thứ tự: ' + (c.sortOrder != null ? c.sortOrder : (i + 1)) + ')</b></div>'
                          + '<div class="ms-3"><b>Tổng ghế:</b> ' + (c.seatCount || 0) + '</div>';

                  if (c.seats && c.seats.length) {
                      h += '<ul class="ul-tight">';
                      for (var j = 0; j < c.seats.length; j++) {
                          var s = c.seats[j];
                          h += '<li>' + escapeHtml(s.code) + ' - '
                                  + escapeHtml(s.seatClassCode || '')
                                  + (s.seatClassName ? ' (' + escapeHtml(s.seatClassName) + ')' : '')
                                  + (s.positionInfo ? ' (' + escapeHtml(labelPos(s.positionInfo)) + ')' : '')
                                  + '</li>';
                      }
                      h += '</ul>';
                  }
                  h += '</div>';
              }
          }

          h += '</div>' /* card-body */
                  + '</div>';   /* card */
          return h;
      }

      // ===== Loaders =====
      async function loadRouteMeta() {
          var rid = routeSel.value;
          if (!rid) {
              routePane.innerHTML = '<div class="alert alert-info">Chọn Tuyến để xem thông tin.</div>';
              return;
          }
          var url = ctx + '/manager/trips?op=metaRoute&route_id=' + encodeURIComponent(rid);
          try {
              var res = await fetch(url, {headers: {'Accept': 'application/json'}, credentials: 'same-origin'});
              var txt = await res.text();
              var data = {};
              try {
                  data = JSON.parse(txt);
              } catch (e) {
                  data = {};
              }
              routePane.innerHTML = renderRoute(data);
          } catch (e) {
              routePane.innerHTML = '<div class="alert alert-warning">Không tải được thông tin tuyến.</div>';
          }
      }

      async function loadTrainMeta() {
          var tid = trainSel.value;
          if (!tid) {
              trainPane.innerHTML = '<div class="alert alert-info">Chọn Đoàn tàu để xem thông tin.</div>';
              return;
          }
          var url = ctx + '/manager/trips?op=metaTrain&train_id=' + encodeURIComponent(tid);
          try {
              var res = await fetch(url, {headers: {'Accept': 'application/json'}, credentials: 'same-origin'});
              var txt = await res.text();
              var data = {};
              try {
                  data = JSON.parse(txt);
              } catch (e) {
                  data = {};
              }
              trainPane.innerHTML = renderTrain(data);
          } catch (e) {
              trainPane.innerHTML = '<div class="alert alert-warning">Không tải được thông tin đoàn tàu.</div>';
          }
      }

      // Sự kiện change
      routeSel.addEventListener('change', loadRouteMeta);
      trainSel.addEventListener('change', loadTrainMeta);
      // Tự load khi vào trang (edit có giá trị sẵn)
      loadRouteMeta();
      loadTrainMeta();
        </script>
    </body>
</html>
