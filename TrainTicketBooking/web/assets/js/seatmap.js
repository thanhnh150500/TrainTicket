// assets/js/seatmap.js
const MAX_SELECT = 4;
let raw = [];                 // toàn bộ ghế từ API
let byCar = new Map();        // carriageCode -> seats[]
let carKeys = [];
let carIndex = 0;
let selected = new Set();     // seatId đang chọn
let holding = false;          // chặn spam nút "Giữ ghế"

const $  = (sel)=>document.querySelector(sel);
const $$ = (sel)=>document.querySelectorAll(sel);

const CSRF = () => ($('meta[name="csrf-token"]')?.content || '').trim();

async function fetchSeatMap() {
  const url = `${ctx}/api/seatmap?tripId=${encodeURIComponent(tripId)}&with=availability&_=${Date.now()}`;
  setLoading(true);
  try {
    const r = await fetch(url, { credentials: 'same-origin' });
    if (r.status === 403) {
      alert('Phiên làm việc đã hết hạn (403). Vui lòng tải lại trang.');
      return;
    }
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    const data = await r.json();

    raw = Array.isArray(data) ? data : [];
    groupByCarriage();
    renderMiniCars();
    if (carKeys.length > 0) openCar( Math.min(carIndex, carKeys.length - 1) );
    updateSummary();
    // nếu API có giờ đi/đến -> cập nhật
    const any = raw[0];
    if (any?.departTime) $('#kDepart').textContent = any.departTime;
    if (any?.arriveTime) $('#kArrive').textContent = any.arriveTime;

  } catch (err) {
    console.error('fetchSeatMap error:', err);
    alert('Không tải được sơ đồ ghế.');
  } finally {
    setLoading(false);
  }
}

function setLoading(on) {
  $('#btnHold')?.toggleAttribute('disabled', on || holding);
}

function groupByCarriage() {
  byCar.clear();
  for (const s of raw) {
    const key = s.carriageCode || `#${s.carriageId}`;
    if (!byCar.has(key)) byCar.set(key, []);
    byCar.get(key).push(s);
  }
  for (const arr of byCar.values()) {
    arr.sort((a, b) => (a.seatCode || a.code).localeCompare((b.seatCode || b.code), 'vi', { numeric: true }));
  }
  carKeys = Array.from(byCar.keys());
}

function renderMiniCars() {
  const wrap = $('#carMini');
  if (!wrap) return;
  wrap.innerHTML = '';
  if (carKeys.length === 0) {
    wrap.innerHTML = '<div class="text-muted small">Chưa có toa nào.</div>';
    $('#coachTitle').textContent = 'Toa số … :'; $('#className').textContent = '—';
    $('#seatGrid').innerHTML = '';
    return;
  }
  carKeys.forEach((k, i) => {
    const el = document.createElement('div');
    el.className = 'car ' + colorByIndex(i) + (i === carIndex ? ' active' : '');
    el.innerHTML = `<div class="num">${k.replace(/\D+/g, '') || (i + 1)}</div>`;
    el.onclick = () => openCar(i);
    wrap.appendChild(el);
  });
  $('#btnPrev').onclick = () => openCar(Math.max(0, carIndex - 1));
  $('#btnNext').onclick = () => openCar(Math.min(carKeys.length - 1, carIndex + 1));
}

function openCar(i) {
  if (carKeys.length === 0) return;
  carIndex = Math.max(0, Math.min(i, carKeys.length - 1));
  $$('#carMini .car').forEach((c, idx) => c.classList.toggle('active', idx === carIndex));
  const code = carKeys[carIndex];
  renderCoach(code);
}

function renderCoach(code) {
  const seats = byCar.get(code) || [];
  const grid = $('#seatGrid');
  if (!grid) return;
  grid.innerHTML = '';

  const any = seats[0] || {};
  $('#coachTitle').textContent = `Toa số ${code} :`;
  $('#className').textContent  = any.seatClassName || '—';

  // grid 2–aisle–2 (5 cột)
  let col = 0;
  for (const s of seats) {
    if (col % 4 === 2) {
      const aisle = document.createElement('div');
      aisle.className = 'aisle';
      grid.appendChild(aisle);
      col++;
    }

    const div = document.createElement('div');
    const isChosen = selected.has(s.seatId);
    div.className = 'seat ' + cssState(s) + (isChosen ? ' chosen' : '');
    div.textContent = s.seatCode || s.code;
    div.title = `${s.seatClassName || ''}${s.available ? ' • Còn' : ' • Không khả dụng'}`;

    div.onclick = () => {
      if (!s.available) return;
      const id = s.seatId;
      if (selected.has(id)) {
        selected.delete(id);
        div.classList.remove('chosen');
      } else {
        if (selected.size >= MAX_SELECT) return;
        selected.add(id);
        div.classList.add('chosen');
      }
      updateHint();
    };

    grid.appendChild(div);
    col++;
    if (col % 5 === 0) col = 0;
  }
  updateHint();
}

function cssState(s) {
  if (s.available) return 'free';
  if (s.lockExpiresAt) return 'hold';
  return 'sold';
}

function updateHint() {
  const el = $('#hint');
  if (el) el.textContent = `Đang chọn: ${selected.size}/${MAX_SELECT} ghế`;
}

$('#btnHold')?.addEventListener('click', async () => {
  if (selected.size === 0) return alert('Bạn chưa chọn ghế nào.');

  const csrf = CSRF();
  if (!csrf) return alert('Thiếu CSRF token. Vui lòng tải lại trang.');

  const body = {
    tripId: parseInt(tripId, 10),
    seatIds: Array.from(selected)
  };

  try {
    holding = true;
    setLoading(true);

    const r = await fetch(`${ctx}/api/hold`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-Token': csrf
      },
      credentials: 'same-origin',
      body: JSON.stringify(body)
    });

    if (r.status === 403) {
      alert('Phiên làm việc đã hết hạn hoặc token không hợp lệ (403). Vui lòng tải lại trang.');
      return;
    }
    if (!r.ok) throw new Error(`HTTP ${r.status}: ${await r.text()}`);

    const res = await r.json();
    if (!res.ok) {
      alert(res.error || 'Giữ ghế thất bại');
      return;
    }
    alert('Giữ ghế thành công!');
    selected.clear();
    await fetchSeatMap(); // refresh trạng thái

  } catch (e) {
    console.error('Hold error', e);
    alert('Giữ ghế thất bại.');
  } finally {
    holding = false;
    setLoading(false);
  }
});

function updateSummary() {
  const free = raw.filter(s => s.available).length;
  const total = raw.length;
  const booked = total - free;
  $('#kFree').textContent = free;
  $('#kBooked').textContent = booked;
}

function colorByIndex(i) {
  const arr = ['blue', 'green', 'orange', 'blue', 'green', 'orange'];
  return arr[i % arr.length];
}

// Khởi động
fetchSeatMap();
