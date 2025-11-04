// assets/js/seatmap.js — FINAL (API-match V2: status FREE/LOCKED/LOCKED_BY_ME/BOOKED + preselect my holds)
const MAX_SELECT = 4;
let raw = [];                 // toàn bộ seat (mọi toa) từ API
let seatsByCarId = new Map(); // group theo carriageId
let cars = [];                // [{id, no, name, sort}]
let carIndex = 0;
let busy = false;

// Map seatId -> seatObj
const selected = new Map();

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
const S_FREE = 'FREE';
const S_LOCKED = 'LOCKED';
const S_LOCKED_ME = 'LOCKED_BY_ME';
const S_BOOKED = 'BOOKED';

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

    const statusRaw = String(s.status ?? '').toUpperCase();
    const status = [S_FREE, S_LOCKED, S_LOCKED_ME, S_BOOKED].includes(statusRaw) ? statusRaw : S_FREE;

    const lockedByMe = !!(s.lockedByMe === true || status === S_LOCKED_ME);
    const remainSec = Number.isFinite(+s.remainSec) ? +s.remainSec : null;
    const holdExpiresAt = s.holdExpiresAt ?? null;

    // available: chỉ FREE mới chọn; LOCKED_BY_ME sẽ tự đánh dấu chọn nhưng không cho bỏ chọn
    const selectable = (status === S_FREE);
    const myHold = (status === S_LOCKED_ME);

    return {
        seatId, seatCode, carriageId, carriageCode,
        seatClassId, seatClassCode, seatClassName,
        row, col, price,
        status, lockedByMe, remainSec, holdExpiresAt,
        selectable, myHold
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
            console.error('[seatmap] GET /api/seatmap failed', r.status, await r.text().catch(() => ''));
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

        // Preselect các ghế do tôi đang giữ (LOCKED_BY_ME)
        selected.clear();
        for (const s of raw) {
            if (s.myHold)
                selected.set(s.seatId, s);
        }

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
        console.log('[seatmap] coaches:', cars.length, 'seats:', raw.length, 'groups:', dbg, 'selected(myhold):', selected.size);
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

        // map status -> class
        let klass = 'free';
        if (s.status === S_BOOKED)
            klass = 'sold';
        else if (s.status === S_LOCKED)
            klass = 'hold';
        else if (s.status === S_LOCKED_ME)
            klass = 'myhold';

        const chosen = selected.has(s.seatId);
        btn.className = `seat ${klass}${chosen ? ' chosen' : ''}`;
        btn.dataset.seatId = String(s.seatId);
        btn.dataset.carriageId = String(s.carriageId);
        btn.dataset.seatCode = s.seatCode || '';
        btn.style.gridColumn = String(Number(s.col));
        btn.style.gridRow = String(Number(s.row));

        const priceHtml = (Number.isFinite(s.price) && s.price > 0)
                ? `<div class="seat-price">${formatVND(s.price)}</div>`
                : `<div class="seat-price">-</div>`;

        // countdown nhỏ nếu đang lock
        let badge = '';
        if (s.status === S_LOCKED || s.status === S_LOCKED_ME) {
            const secs = Number.isFinite(s.remainSec) ? s.remainSec : null;
            if (secs != null)
                badge = `<div class="seat-ttl">${Math.max(0, secs)}s</div>`;
        }

        btn.innerHTML = `<span class="seat-label">${s.seatCode || s.seatId}</span>${priceHtml}${badge}`;
        const human = s.status === S_FREE ? 'Còn'
                : (s.status === S_LOCKED_ME ? 'Bạn đang giữ'
                        : (s.status === S_LOCKED ? 'Đang giữ'
                                : 'Đã bán'));
        btn.title = `${s.seatClassName || ''} • ${human}`;

        // Chặn click nếu không phải FREE; ghế tôi đang giữ không cho bỏ chọn ở đây
        if (!s.selectable)
            btn.disabled = (s.status !== S_FREE);

        btn.addEventListener('click', () => {
            if (!s.selectable)
                return; // chỉ FREE được chọn/bỏ
            const id = s.seatId;
            if (selected.has(id)) {
                selected.delete(id);
                btn.classList.remove('chosen');
            } else {
                if (selected.size >= MAX_SELECT) {
                    alert(`Bạn chỉ chọn tối đa ${MAX_SELECT} ghế`);
                    return;
                }
                selected.set(id, s);
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
    if (!el)
        return;
    const myHoldCount = Array.from(selected.values()).filter(s => s.myHold).length;
    const justSelected = selected.size - myHoldCount;
    el.textContent = selected.size
            ? `Đang chọn: ${justSelected} mới + ${myHoldCount} ghế bạn đang giữ`
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
    const total = raw.length;
    const free = raw.filter(s => s.status === S_FREE).length;
    const myhold = raw.filter(s => s.status === S_LOCKED_ME).length;
    const locked = raw.filter(s => s.status === S_LOCKED).length;
    const booked = raw.filter(s => s.status === S_BOOKED).length;

    $('#kFree')?.replaceChildren(String(free));
    $('#kBooked')?.replaceChildren(String(booked));
    $('#kHold')?.replaceChildren(String(locked));
    $('#kMyHold')?.replaceChildren(String(myhold));

    // nếu bạn chỉ có kFree/kBooked trong DOM thì vẫn ok
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

    // chỉ gửi những ghế FREE vừa chọn thêm (không gửi ghế myHold vì đã giữ)
    const newSeatIds = Array.from(selected.values())
            .filter(s => s.status === S_FREE)
            .map(s => s.seatId);

    if (newSeatIds.length === 0) {
        alert('Không có ghế mới để giữ.');
        return;
    }

    const body = {tripId: Number(tripId), seatIds: newSeatIds};
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
        await fetchSeatMap(); // sẽ tự preselect lại myHold
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
        const t = form.querySelector('input[name="_csrf"]');
        if (t && (!t.value || !t.value.trim()))
            t.value = CSRF() || '';
    }

    const seatsDiv = document.getElementById('seatsContainer') || form;
    if (seatsDiv !== form)
        seatsDiv.innerHTML = '';

    // gồm cả ghế myHold (để đi tiếp thanh toán)
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
        if (document.getElementById('checkoutPostForm')) {
            postCheckout();
            return;
        }
        await postHold();
    });
})();

/* ================= boot ================= */
document.addEventListener('DOMContentLoaded', () => {
    fetchSeatMap();
    // có thể bổ sung wireTrainCards() nếu cần
});
