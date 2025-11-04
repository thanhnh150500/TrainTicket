// assets/js/seatmap.js — FINAL (API-match + price + centered grid + checkout post fallback hold)
const MAX_SELECT = 4;
let raw = [];                 // toàn bộ seat (mọi toa) từ API
let seatsByCarId = new Map(); // group theo carriageId
let cars = [];                // [{id, no, name, sort}]
let carIndex = 0;
let busy = false;

// Thay Set -> Map để giữ đủ thông tin ghế
const selected = new Map();   // key: seatId, val: seatObj (chuẩn normalizeSeat)

const $ = (s) => document.querySelector(s);
const $$ = (s) => document.querySelectorAll(s);
const CSRF = () => ($('meta[name="csrf-token"]')?.content || '').trim();

/* ================= helpers ================= */
function naturalCompare(a, b) {
    const rx = /(\d+)|(\D+)/g;
    const A = String(a ?? '').match(rx) || [];
    const B = String(b ?? '').match(rx) || [];
    const len = Math.min(A.length, B.length);
    for (let i = 0; i < len; i++) {
        const xa = A[i], xb = B[i];
        if (xa === xb)
            continue;
        const na = /^\d+$/.test(xa), nb = /^\d+$/.test(xb);
        if (na && nb) {
            const da = +xa, db = +xb;
            if (da !== db)
                return da - db;
        } else {
            const c = xa.localeCompare(xb, undefined, {sensitivity: 'base'});
            if (c !== 0)
                return c;
        }
    }
    return A.length - B.length;
}
function toBoolFromStatus(st) {
    if (st == null)
        return null;
    const s = String(st).toUpperCase();
    if (s === 'FREE' || s === 'AVAILABLE')
        return true;
    if (['SOLD', 'BOOKED', 'LOCKED', 'HOLD', 'HELD'].includes(s))
        return false;
    return null;
}
function formatVND(n) {
    try {
        return Number(n).toLocaleString('vi-VN') + ' đ';
    } catch {
        return n;
    }
}

function normalizeSeat(s) {
    const seatId = Number(s.id ?? s.seatId ?? s.seat_id);
    const seatCode = String(s.code ?? s.seatCode ?? '').trim();
    const carriageId = Number(s.carriageId ?? s.carriage_id);
    const carriageCode = String(s.carriageCode ?? s.carriage_code ?? '').trim();
    const seatClassId = (s.seatClassId == null ? 0 : Number(s.seatClassId));
    const seatClassCode = s.seatClassCode ?? '';
    const seatClassName = s.seatClassName ?? '';
    const row = Number.isFinite(+s.row) ? +s.row : null;
    const col = Number.isFinite(+s.col) ? +s.col : null;
    const price = Number.isFinite(+s.price) ? +s.price : null;
    const available = toBoolFromStatus(s.status);
    const lockExpiresAt = s.holdExpiresAt ?? null;

    return {
        seatId, seatCode, carriageId, carriageCode,
        seatClassId, seatClassCode, seatClassName,
        row, col, price, available, lockExpiresAt
    };
}

/* =============== giá fallback theo seatClass =============== */
function fillMissingPrices(seats) {
    const byClass = new Map(); // seatClassId -> price đầu tiên > 0
    for (const s of seats) {
        if (s.seatClassId && Number.isFinite(s.price) && s.price > 0 && !byClass.has(s.seatClassId)) {
            byClass.set(s.seatClassId, s.price);
        }
    }
    for (const s of seats) {
        if (!(Number.isFinite(s.price) && s.price > 0) && s.seatClassId) {
            const fb = byClass.get(s.seatClassId);
            if (Number.isFinite(fb))
                s.price = fb;
        }
    }
}

/* ================= tải seatmap ================= */
async function fetchSeatMap() {
    const url = `${ctx}/api/seatmap?tripId=${encodeURIComponent(tripId)}&_=${Date.now()}`;
    setLoading(true);
    try {
        const r = await fetch(url, {credentials: 'same-origin', headers: {Accept: 'application/json'}});
        if (r.status === 403) {
            alert('Phiên làm việc đã hết hạn (403). Vui lòng tải lại trang.');
            return;
        }
        if (!r.ok) {
            let text = '';
            try {
                text = await r.text();
            } catch {
            }
            console.error('[seatmap] GET /api/seatmap failed', r.status, text);
            throw new Error(`HTTP ${r.status}`);
        }
        const payload = await r.json();

        // 1) Cars (coaches)
        cars = Array.isArray(payload?.coaches) ? payload.coaches.map(co => ({
                id: Number(co.id),
                no: Number(co.no),
                name: String(co.name ?? '').trim(),
                sort: Number(co.no) || Number(co.id)
            })) : [];

        const seen = new Set();
        cars = cars.filter(c => (c.id && !seen.has(c.sort) && seen.add(c.sort)))
                .sort((a, b) => a.sort - b.sort || a.id - b.id);

        // 2) Seats
        const seatsRaw = Array.isArray(payload?.seats) ? payload.seats : [];
        raw = seatsRaw.map(normalizeSeat);

        // 3) Giá fallback
        fillMissingPrices(raw);

        // 4) Group seats theo carriageId
        buildSeatsByCarId();

        // 5) Render
        renderMiniCars();
        if (cars.length > 0)
            openCar(Math.min(carIndex, cars.length - 1));
        updateSummary();

        // debug
        const dbg = {};
        for (const [cid, arr] of seatsByCarId.entries())
            dbg[cid] = arr.length;
        console.log('[seatmap] coaches:', cars.length, 'seats:', raw.length, 'groups:', dbg);
    } catch (e) {
        console.error('fetchSeatMap error:', e);
        alert('Không tải được sơ đồ ghế.');
    } finally {
        setLoading(false);
    }
}

function buildSeatsByCarId() {
    seatsByCarId.clear();
    for (const s0 of raw) {
        if (!Number.isFinite(s0.carriageId))
            continue;
        const cid = s0.carriageId;
        if (!seatsByCarId.has(cid))
            seatsByCarId.set(cid, []);
        seatsByCarId.get(cid).push({...s0});
    }
    for (const arr of seatsByCarId.values()) {
        arr.sort((a, b) => naturalCompare(a.seatCode, b.seatCode));
    }
}

/* ================= mini-cars ================= */
function renderMiniCars() {
    const wrap = $('#carMini');
    if (!wrap)
        return;
    wrap.innerHTML = '';
    cars.forEach((car, i) => {
        const el = document.createElement('button');
        el.type = 'button';
        el.className = 'mini-car' + (i === carIndex ? ' active' : '');
        el.dataset.carriageId = String(car.id);
        el.dataset.sort = String(car.sort);
        el.textContent = `Toa ${car.sort}`;
        el.addEventListener('click', () => openCar(i));
        wrap.appendChild(el);
    });

    const prev = $('#btnPrev'), next = $('#btnNext');
    if (prev)
        prev.onclick = () => openCar(Math.max(0, carIndex - 1));
    if (next)
        next.onclick = () => openCar(Math.min(cars.length - 1, carIndex + 1));
}

function openCar(i) {
    if (!cars.length)
        return;
    carIndex = Math.max(0, Math.min(i, cars.length - 1));
    renderMiniCars();               // cập nhật active
    renderCoach(cars[carIndex]);    // vẽ ghế
}

/* ================= render coach ================= */
function renderCoach(car) {
    const grid = $('#seatGrid');
    if (!grid)
        return;

    const seats = (seatsByCarId.get(Number(car.id)) || []).map(s => ({...s}));
    grid.innerHTML = '';

    $('#coachTitle')?.replaceChildren(`Toa số ${car.sort} :`);
    $('#className')?.replaceChildren((seats[0]?.seatClassName) || '—');

    if (!seats.length) {
        grid.innerHTML = '<div class="text-muted p-3">Toa này chưa có dữ liệu ghế.</div>';
        updateHint();
        return;
    }

    // Tính row/col; nếu thiếu -> auto layout
    let maxRow = 0, maxCol = 0, missing = 0;
    for (const s of seats) {
        if (s.row != null && s.col != null) {
            maxRow = Math.max(maxRow, Number(s.row));
            maxCol = Math.max(maxCol, Number(s.col));
        } else
            missing++;
    }
    if (missing > 0) {
        const n = seats.length, cols = Math.max(6, Math.round(Math.sqrt(n)));
        let r = 1, c = 1;
        for (const s of seats) {
            if (s.row == null || s.col == null) {
                s.row = r; s.col = c;
                c++;
                if (c > cols) {
                    c = 1;
                    r++;
                }
            }
            maxRow = Math.max(maxRow, Number(s.row));
            maxCol = Math.max(maxCol, Number(s.col));
        }
    }

    // set số cột cho CSS Grid (để seat layout canh giữa)
    grid.style.setProperty('--cols', String(maxCol));

    seats.sort((a, b) => naturalCompare(a.seatCode, b.seatCode));

    const frag = document.createDocumentFragment();
    for (const s of seats) {
        const btn = document.createElement('button');
        btn.type = 'button';
        const chosen = selected.has(s.seatId);
        const klass = s.available === false ? (s.lockExpiresAt ? 'hold' : 'sold') : 'free';
        btn.className = `seat ${klass}${chosen ? ' chosen' : ''}`;
        btn.dataset.seatId = String(s.seatId);
        btn.dataset.carriageId = String(s.carriageId);
        btn.dataset.seatCode = s.seatCode || '';
        btn.style.gridColumn = String(Number(s.col));
        btn.style.gridRow = String(Number(s.row));

        const priceHtml = (Number.isFinite(s.price) && s.price > 0)
                ? `<div class="seat-price">${formatVND(s.price)}</div>`
                : `<div class="seat-price">-</div>`;
        btn.innerHTML = `<span class="seat-label">${s.seatCode || s.seatId}</span>${priceHtml}`;
        btn.title = `${s.seatClassName || ''}${(s.available ?? true) ? ' • Còn' : ' • Không khả dụng'}`;

        if (s.available === false)
            btn.disabled = true;

        btn.addEventListener('click', () => {
            if (s.available === false)
                return;
            const id = s.seatId;
            if (selected.has(id)) {
                selected.delete(id);
                btn.classList.remove('chosen');
            } else {
                if (selected.size >= MAX_SELECT) {
                    alert(`Bạn chỉ chọn tối đa ${MAX_SELECT} ghế`);
                    return;
                }
                selected.set(id, s); // lưu full seat object
                btn.classList.add('chosen');
            }
            updateHint();
        });

        frag.appendChild(btn);
    }
    grid.appendChild(frag);
    updateHint();
}

/* ================= state / actions ================= */
function updateHint() {
    const el = $('#hint');
    if (el)
        el.textContent = selected.size
                ? `Đang chọn: ${selected.size}/${MAX_SELECT} ghế`
                : `Chọn tối đa ${MAX_SELECT} ghế`;
}
function getBookBtn() {
    return document.getElementById('btnBook') || document.getElementById('btnHold');
}
function setLoading(on) {
    const b = getBookBtn();
    if (b)
        b.toggleAttribute('disabled', !!on || busy);
}
function updateSummary() {
    const free = raw.filter(s => s.available === true).length;
    const total = raw.length;
    const booked = Math.max(0, total - free);
    $('#kFree')?.replaceChildren(String(free));
    $('#kBooked')?.replaceChildren(String(booked));
}

/* ================== BOOK/HOLD or CHECKOUT ================== */
const BOOK_ENDPOINT = `${ctx}/api/hold`;

function addHidden(root, name, val) {
    const i = document.createElement('input');
    i.type = 'hidden';
    i.name = name;
    i.value = (val ?? '').toString();
    root.appendChild(i);
}

async function postHold() {
    const csrf = CSRF();
    if (!csrf) {
        alert('Thiếu CSRF token. Vui lòng tải lại trang.');
        return;
    }
    const body = {tripId: Number(tripId), seatIds: Array.from(selected.keys())};
    busy = true;
    setLoading(true);
    try {
        const r = await fetch(BOOK_ENDPOINT, {
            method: 'POST',
            headers: {'Content-Type': 'application/json', 'X-CSRF-Token': csrf, Accept: 'application/json'},
            credentials: 'same-origin',
            body: JSON.stringify(body)
        });
        if (r.status === 403) {
            alert('Phiên làm việc đã hết hạn hoặc token không hợp lệ (403).');
            return;
        }
        if (!r.ok)
            throw new Error(`HTTP ${r.status}: ${await r.text()}`);
        const res = await r.json();
        if (!res.ok) {
            alert(res.error || 'Giữ ghế thất bại');
            return;
        }
        alert('Giữ ghế thành công!');
        selected.clear();
        await fetchSeatMap();
    } catch (e) {
        console.error('Book/Hold error', e);
        alert('Giữ ghế thất bại.');
    } finally {
        busy = false;
        setLoading(false);
    }
}

function postCheckout() {
    let form = document.getElementById('checkoutPostForm');
    if (!form) {
        // Tạo mới (fallback) → tự thêm _csrf từ <meta>
        form = document.createElement('form');
        form.method = 'POST';
        form.action = `${ctx}/checkout`;
        form.className = 'd-none';
        document.body.appendChild(form);
        addHidden(form, '_csrf', CSRF() || '');
        addHidden(form, 'tripId', tripId || '');
        addHidden(form, 'trainCode', (typeof trainCode !== 'undefined' ? trainCode : '') || '');
        addHidden(form, 'originName', ($('.route-text')?.textContent?.trim().split('từ')[1]?.split('đến')[0] || '').trim());
        addHidden(form, 'destName', ($('.route-text')?.textContent?.trim().split('đến')[1] || '').trim());
        addHidden(form, 'departTime', $('#kDepart')?.textContent?.trim() || '');
        addHidden(form, 'arriveTime', $('#kArrive')?.textContent?.trim() || '');
    } else {
        // Form có sẵn → nếu input _csrf rỗng thì châm từ <meta>
        const t = form.querySelector('input[name="_csrf"]');
        if (t && (!t.value || !t.value.trim()))
            t.value = CSRF() || '';
    }

    const seatsDiv = document.getElementById('seatsContainer') || form;
    if (seatsDiv !== form)
        seatsDiv.innerHTML = '';

    for (const s of selected.values()) {
        addHidden(seatsDiv, 'seatId[]', s.seatId);
        addHidden(seatsDiv, 'seatCode[]', s.seatCode);
        addHidden(seatsDiv, 'carriageId[]', s.carriageId);
        addHidden(seatsDiv, 'seatClassId[]', s.seatClassId);
        addHidden(form, 'price[]', Number(s.price ?? 0));
        addHidden(seatsDiv, 'seatClassName[]', s.seatClassName || '');
    }

    form.submit();
}


(function bindBook() {
    const btn = getBookBtn();
    if (!btn)
        return;

    btn.addEventListener('click', async () => {
        if (selected.size === 0) {
            alert('Vui lòng chọn ít nhất 1 ghế');
            return;
        }

        // Nếu có form checkout -> chuyển sang trang "Thông tin khách hàng"
        if (document.getElementById('checkoutPostForm')) {
            postCheckout();
            return;
        }
        // Nếu không có form (flow cũ) -> gọi API hold
        await postHold();
    });
})();

/* ================= boot ================= */
document.addEventListener('DOMContentLoaded', () => {
    fetchSeatMap();
    // nếu có strip chọn chuyến, có thể bổ sung wireTrainCards() ở đây
});
