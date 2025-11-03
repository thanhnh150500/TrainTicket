// assets/js/seatmap.js — BẢN CHUẨN (dựng toa từ payload.coaches + fix layout)
const MAX_SELECT = 4;
let raw = [];
let seatsByCarId = new Map();
let cars = [];
let carIndex = 0;
let selected = new Set();
let busy = false;

const $ = (s) => document.querySelector(s);
const $$ = (s) => document.querySelectorAll(s);
const CSRF = () => ($('meta[name="csrf-token"]')?.content || '').trim();

/* ---------- helpers ---------- */
function coerceBool(v) {
    if (typeof v === 'boolean')
        return v;
    if (typeof v === 'number')
        return v === 1;
    if (typeof v === 'string') {
        const t = v.trim().toLowerCase();
        if (['true', '1', 'yes'].includes(t))
            return true;
        if (['false', '0', 'no'].includes(t))
            return false;
    }
    return null;
}
function statusToAvailable(st) {
    if (st == null)
        return null;
    const s = String(st).toUpperCase();
    if (s === 'FREE' || s === 'AVAILABLE')
        return true;
    if (['SOLD', 'BOOKED', 'LOCKED', 'HOLD', 'HELD'].includes(s))
        return false;
    return null;
}
function makeSeatId(carriageId, seatCode) {
    const cid = Number.isFinite(carriageId) ? carriageId : 0;
    const sc = String(seatCode ?? '').padStart(3, '0').slice(-3);
    return Number(`${cid}${sc}`);
}
function numberFromCode(code) {
    const m = String(code ?? '').match(/\d+/);
    return m ? Number(m[0]) : NaN;
}
function ensureCarriageId(seat, coachNo) {
    let id = Number(seat?.carriageId);
    if (!Number.isFinite(id) && Number.isFinite(coachNo))
        id = coachNo;
    if (!Number.isFinite(id)) {
        const n = numberFromCode(seat?.carriageCode);
        if (Number.isFinite(n))
            id = n;
    }
    return id;
}
function uniqBySeatId(list) {
    const seen = new Set();
    const out = [];
    for (const s of list) {
        const id = Number(s.seatId);
        if (!seen.has(id)) {
            seen.add(id);
            out.push(s);
        }
    }
    return out;
}
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
            const da = parseInt(xa, 10), db = parseInt(xb, 10);
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

/* ---------- normalize ---------- */
function normalizeSeat(s, forcedCarriageId = null) {
    let carriageId = Number(forcedCarriageId ?? s.carriageId ?? s.carriage_id ?? s.carId ?? s.carriageID);
    const carriageCode = s.carriageCode ?? s.carriage_code ?? s.car_code ?? '';
    if (!Number.isFinite(carriageId)) {
        const n = numberFromCode(carriageCode);
        if (Number.isFinite(n))
            carriageId = n;
    }

    const seatCode = s.seatCode ?? s.seat_code ?? s.code ?? '';
    const seatId = Number(s.seatId ?? s.seat_id ?? s.id ?? makeSeatId(carriageId, seatCode));
    const seatClassId = Number(s.seatClassId ?? s.seat_class_id);
    const seatClassCode = s.seatClassCode ?? s.seat_class_code ?? '';
    const seatClassName = s.seatClassName ?? s.seat_class_name ?? '';

    let row = Number(s.row ?? s.rowNo ?? s.row_no ?? s.r);
    let col = Number(s.col ?? s.colNo ?? s.col_no ?? s.c);
    row = Number.isFinite(row) ? row : null;
    col = Number.isFinite(col) ? col : null;

    let price = s.price ?? s.base_price ?? s.amount ?? 0;
    price = Number(price);

    const lockExpiresAt = s.lockExpiresAt ?? s.lock_expires_at ?? s.expires_at ?? null;

    let available = coerceBool(s.available);
    if (available == null)
        available = statusToAvailable(s.status);
    if (available == null)
        available = !lockExpiresAt;

    return {
        seatId, carriageId, seatCode, carriageCode,
        seatClassId, seatClassCode, seatClassName,
        row, col, price, available, lockExpiresAt,
        departTime: s.departTime ?? s.depart_time ?? null,
        arriveTime: s.arriveTime ?? s.arrive_time ?? null
    };
}

/* ---------- extractors ---------- */
function extractFlatSeats(p) {
    if (Array.isArray(p))
        return p;
    const c = [p?.data?.seats, p?.result?.seats, p?.seats, p?.items, p?.data];
    for (const a of c)
        if (Array.isArray(a))
            return a;
    return null;
}
function extractGroupedSeats(p) {
    const m = [p?.seatsByCarriage, p?.byCarriage, p?.carSeats, p?.seats_grouped, p?.seats_map];
    for (const mp of m) {
        if (mp && typeof mp === 'object') {
            const out = [];
            for (const k of Object.keys(mp)) {
                const list = mp[k];
                if (!Array.isArray(list))
                    continue;
                const cid = Number(k);
                for (const s of list)
                    out.push(normalizeSeat(s, Number.isFinite(cid) ? cid : null));
            }
            return out;
        }
    }
    return null;
}

/* ---------- DOM fallback ---------- */
function readSeatsFromDOM() {
    const btns = $$('#seatGrid .seat');
    const out = [];
    btns.forEach((b, i) => {
        const cid = Number(b.dataset.carriageId);
        const code = b.dataset.seatCode ?? b.textContent?.trim() ?? String(i + 1);
        const sid = Number(b.dataset.seatId) || makeSeatId(cid, code);
        const available = !/sold/.test(b.className) && !/hold/.test(b.className) && !b.disabled;
        out.push({
            seatId: sid,
            carriageId: ensureCarriageId({carriageId: cid, carriageCode: b.dataset.carriageCode}, undefined),
            seatCode: code,
            carriageCode: b.dataset.carriageCode ?? '',
            seatClassId: Number(b.dataset.seatClassId ?? 0),
            seatClassCode: b.dataset.seatClassCode ?? '',
            seatClassName: b.dataset.seatClassName ?? '',
            row: Number.isFinite(Number(b.dataset.row)) ? Number(b.dataset.row) : null,
            col: Number.isFinite(Number(b.dataset.col)) ? Number(b.dataset.col) : null,
            price: Number(b.dataset.price ?? 0),
            available, lockExpiresAt: null, departTime: null, arriveTime: null
        });
    });
    return out;
}

/* ---------- fetch per coach ---------- */
function getCoachNosFromDOM() {
    const nos = [];
    $$('#carMini .mini-car').forEach(b => {
        const no = Number(b.dataset.sort ?? b.textContent?.replace(/\D+/g, '') ?? NaN);
        if (Number.isFinite(no))
            nos.push(no);
    });
    return [...new Set(nos)].sort((a, b) => a - b).slice(0, 20);
}
async function fetchSeatsPerCoach(tripId, coachNos) {
    const base = `${ctx}/api/seatmap`;
    const tasks = coachNos.map(async (no, idx) => {
        const url = `${base}?tripId=${encodeURIComponent(tripId)}&coachNo=${encodeURIComponent(no)}&_=${Date.now() + idx}`;
        const r = await fetch(url, {credentials: 'same-origin', headers: {Accept: 'application/json'}});
        if (!r.ok)
            return [];
        const payload = await r.json();
        let seats = extractFlatSeats(payload);
        if (seats)
            seats = seats.map(normalizeSeat);
        if (!seats || !seats.length)
            seats = extractGroupedSeats(payload) || [];
        return seats.map(s => {
            const n = normalizeSeat(s, no);
            n.carriageId = ensureCarriageId(n, no);
            return n;
        });
    });
    return (await Promise.all(tasks)).flat().filter(s => Number.isFinite(s.carriageId));
}

/* ---------- giá fallback ---------- */
function buildPriceByClass(seats) {
    const m = new Map();
    for (const s of seats) {
        const p = Number(s.price);
        if (Number.isFinite(p) && p > 0 && Number.isFinite(s.seatClassId) && !m.has(s.seatClassId)) {
            m.set(s.seatClassId, p);
        }
    }
    return m;
}
function fillMissingPrices(seats) {
    const map = buildPriceByClass(seats);
    for (const s of seats) {
        const p = Number(s.price);
        if (!(Number.isFinite(p) && p > 0) && Number.isFinite(s.seatClassId)) {
            const fb = map.get(s.seatClassId);
            if (Number.isFinite(fb))
                s.price = fb;
        }
    }
}

/* ---------- build cars from payload ---------- */
function buildCarsFromPayload(payload) {
    cars = [];
    const list = Array.isArray(payload?.coaches) ? payload.coaches : [];
    if (!list.length)
        return;

    cars = list.map(co => {
        const id = Number(co.id ?? co.carriageId);
        const no = Number(co.no ?? co.sort);
        const name = String(co.name ?? `Toa ${Number.isFinite(no) ? no : ''}`).trim();
        if (!Number.isFinite(id))
            return null;
        return {
            id,
            code: name || `Toa ${Number.isFinite(no) ? no : id}`,
            sort: Number.isFinite(no) ? no : id
        };
    }).filter(Boolean)
            .sort((a, b) => a.sort - b.sort || a.id - b.id);
}

/* ---------- load seatmap ---------- */
async function fetchSeatMap() {
    const url = `${ctx}/api/seatmap?tripId=${encodeURIComponent(tripId)}&with=availability&_=${Date.now()}`;
    setLoading(true);
    try {
        if (!$('#seatGrid'))
            console.warn('[seatmap] Không tìm thấy #seatGrid');

        const r = await fetch(url, {credentials: 'same-origin', headers: {Accept: 'application/json'}});
        if (r.status === 403) {
            alert('Phiên làm việc đã hết hạn (403). Vui lòng tải lại trang.');
            return;
        }
        if (!r.ok)
            throw new Error(`HTTP ${r.status}`);

        const payload = await r.json();

        // ƯU TIÊN: dựng cars từ payload.coaches
        buildCarsFromPayload(payload);

        // 1) seats từ payload
        let seatsRaw = extractFlatSeats(payload);
        if (seatsRaw)
            raw = seatsRaw.map(normalizeSeat);
        else {
            const grouped = extractGroupedSeats(payload);
            raw = grouped ? grouped : [];
        }

        // 2) enrich nếu có nhiều coach nhưng raw chỉ chứa 1 carriage
        try {
            const coachList = Array.isArray(payload?.coaches) ? payload.coaches : [];
            if (coachList.length > 1) {
                const rawCarIds = new Set(raw.map(s => ensureCarriageId(s)));
                if (rawCarIds.size <= 1) {
                    const nos = coachList.map(c => Number(c.no)).filter(Number.isFinite);
                    const all = await fetchSeatsPerCoach(tripId, nos);
                    if (all.length > raw.length)
                        raw = uniqBySeatId(all);
                }
            }
        } catch (e) {
            console.warn('[seatmap] enrich by coaches failed:', e);
        }

        // 3) Fallbacks
        if (!raw.length) {
            const coachNos = getCoachNosFromDOM();
            raw = await fetchSeatsPerCoach(tripId, coachNos.length ? coachNos : [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]);
        }
        if (!raw.length) {
            raw = readSeatsFromDOM();
            console.warn('[seatmap] Fallback DOM seats:', raw.length);
        }

        fillMissingPrices(raw);
        buildSeatsByCarId();
        if (!cars.length)
            buildCarsFromDOMOrData(); // chỉ fallback khi API không có coaches
        wireMiniCars();

        // Vẽ mini-cars nếu DOM ít hơn dữ liệu
        const domCount = $$('#carMini .mini-car').length;
        if (cars.length > 0 && domCount < cars.length)
            renderMiniCarsFallback();

        if (cars.length > 0)
            openCar(Math.min(carIndex, cars.length - 1));
        updateSummary();

        const any = raw[0];
        if (any?.departTime)
            $('#kDepart')?.replaceChildren(any.departTime);
        if (any?.arriveTime)
            $('#kArrive')?.replaceChildren(any.arriveTime);

        wireTrainCards();

        // debug nhóm
        const dbg = {};
        for (const [cid, arr] of seatsByCarId.entries())
            dbg[cid] = arr.length;
        console.log('[seatmap] payload coaches:', payload?.coaches?.length || 0,
                'seats:', raw.length, 'cars:', cars.length,
                'groups:', dbg);
    } catch (err) {
        console.error('fetchSeatMap error:', err);
        alert('Không tải được sơ đồ ghế.');
    } finally {
        setLoading(false);
    }
}

/* ---------- group & mini-cars ---------- */
function buildSeatsByCarId() {
    seatsByCarId.clear();
    for (const s0 of raw) {
        const cid = ensureCarriageId(s0);
        if (!Number.isFinite(cid))
            continue;
        const s = {...s0, carriageId: cid};
        if (!seatsByCarId.has(cid))
            seatsByCarId.set(cid, []);
        seatsByCarId.get(cid).push(s);
    }
    for (const arr of seatsByCarId.values()) {
        arr.sort((a, b) => naturalCompare(a.seatCode, b.seatCode));
    }
}
function buildCarsFromDOMOrData() {
    const domCars = $$('#carMini .mini-car');
    const haveData = seatsByCarId.size > 0;
    const domCount = domCars.length;

    if (domCount >= 2 && (!haveData || domCount >= seatsByCarId.size)) {
        cars = Array.from(domCars).map((btn, idx) => {
            const id = Number(btn.dataset.carriageId || btn.dataset.coachNo);
            const code = btn.dataset.carriageCode || String(idx + 1);
            const sort = Number(btn.dataset.sort ?? (idx + 1));
            if (!Number.isFinite(id))
                return null;
            return {id, code, sort: Number.isFinite(sort) ? sort : (idx + 1)};
        }).filter(Boolean).sort((a, b) => a.sort - b.sort);

        const activeBtn = Array.from(domCars).find(x => x.classList.contains('active'));
        if (activeBtn) {
            const activeId = Number(activeBtn.dataset.carriageId || activeBtn.dataset.coachNo);
            const idx = cars.findIndex(c => c.id === activeId);
            carIndex = idx >= 0 ? idx : 0;
        } else {
            carIndex = 0;
        }
        return;
    }

    // build từ dữ liệu API seats (fallback)
    cars = Array.from(seatsByCarId.keys()).map(id => {
        const any = (seatsByCarId.get(id) || [])[0] || {};
        const code = any.carriageCode || '#' + id;
        const num = parseInt((String(code).match(/\d+/) || ['0'])[0], 10);
        return {id: Number(id), code, sort: Number.isFinite(num) ? num : Number(id)};
    }).sort((a, b) => a.sort - b.sort || String(a.code).localeCompare(String(b.code), 'vi', {numeric: true}));
    carIndex = 0;
}
function wireMiniCars() {
    const domCars = $$('#carMini .mini-car');
    domCars.forEach(btn => {
        const id = Number(btn.dataset.carriageId || btn.dataset.coachNo);
        const clone = btn.cloneNode(true);
        clone.addEventListener('click', () => openCarById(id));
        btn.replaceWith(clone);
    });
    $('#btnPrev')?.addEventListener('click', onPrevOnce, {once: true});
    $('#btnNext')?.addEventListener('click', onNextOnce, {once: true});
}
function colorByIndex(i) {
    return ['blue', 'green', 'orange', 'blue', 'green', 'orange'][i % 6];
}
function renderMiniCarsFallback() {
    const wrap = $('#carMini');
    if (!wrap)
        return;
    wrap.innerHTML = '';
    cars.forEach((car, i) => {
        const el = document.createElement('button');
        el.type = 'button';
        el.className = 'mini-car ' + colorByIndex(i) + (i === carIndex ? ' active' : '');
        el.dataset.carriageId = String(car.id);
        el.dataset.carriageCode = String(car.code);
        el.dataset.sort = String(car.sort);
        el.textContent = `Toa ${car.sort}`;
        el.addEventListener('click', () => openCarById(car.id));
        wrap.appendChild(el);
    });
    $('#btnPrev')?.addEventListener('click', onPrevOnce, {once: true});
    $('#btnNext')?.addEventListener('click', onNextOnce, {once: true});
}
function onPrevOnce() {
    openCar(Math.max(0, carIndex - 1));
    $('#btnPrev')?.addEventListener('click', onPrevOnce, {once: true});
}
function onNextOnce() {
    openCar(Math.min(cars.length - 1, carIndex + 1));
    $('#btnNext')?.addEventListener('click', onNextOnce, {once: true});
}
function openCarById(id) {
    const idx = cars.findIndex(c => c.id === id);
    if (idx >= 0)
        openCar(idx);
}

/* ---------- render ---------- */
function openCar(i) {
    if (!cars.length)
        return;
    carIndex = Math.max(0, Math.min(i, cars.length - 1));
    $$('#carMini .mini-car').forEach((c, idx) => c.classList.toggle('active', idx === carIndex));
    renderCoach(cars[carIndex]);
}
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

    // Tính maxRow/maxCol nếu có row/col
    let maxRow = 0, maxCol = 0, missing = 0;
    for (const s of seats) {
        if (s.row != null && s.col != null) {
            maxRow = Math.max(maxRow, Number(s.row));
            maxCol = Math.max(maxCol, Number(s.col));
        } else {
            missing++;
        }
    }

    // Auto layout nếu thiếu row/col
    if (missing > 0) {
        const n = seats.length;
        const cols = Math.max(6, Math.round(Math.sqrt(n)));
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

    // Set số cột cho grid
    grid.style.setProperty('--cols', String(maxCol));
    grid.classList.remove('cols-5'); // bỏ layout cố định 5 cột (nếu có)

    // Sort nhãn đẹp
    seats.sort((a, b) => naturalCompare(a.seatCode, b.seatCode));

    const frag = document.createDocumentFragment();
    for (const s of seats) {
        const btn = document.createElement('button');
        btn.type = 'button';
        const isChosen = selected.has(s.seatId);
        btn.className = 'seat ' + cssState(s) + (isChosen ? ' chosen' : '');
        btn.dataset.seatId = String(s.seatId);
        btn.dataset.carriageId = String(s.carriageId);
        btn.dataset.seatCode = s.seatCode || '';
        btn.style.gridColumn = String(Number(s.col));
        btn.style.gridRow = String(Number(s.row));

        const seatLabel = String(s.seatCode || s.seatId);
        btn.innerHTML = `<span class="seat-label">${seatLabel}</span>` + (Number(s.price) > 0 ? `<div class="seat-price">${formatVND(s.price)}</div>` : '');
        btn.title = `${s.seatClassName || ''}${s.available ? ' • Còn' : ' • Không khả dụng'}`;

        if (!s.available)
            btn.disabled = true;

        btn.addEventListener('click', () => {
            if (!s.available)
                return;
            const id = s.seatId;
            if (selected.has(id)) {
                selected.delete(id);
                btn.classList.remove('chosen');
            } else {
                if (selected.size >= MAX_SELECT)
                    return;
                selected.add(id);
                btn.classList.add('chosen');
            }
            updateHint();
        });

        frag.appendChild(btn);
    }

    grid.appendChild(frag);
    console.log('[seatmap] render coach', {carId: car.id, seats: seats.length, maxCol, maxRow});
    updateHint();
}

/* ---------- state/actions ---------- */
function cssState(s) {
    if (s.available)
        return 'free';
    if (s.lockExpiresAt)
        return 'hold';
    return 'sold';
}
function updateHint() {
    const el = $('#hint');
    if (el)
        el.textContent = `Đang chọn: ${selected.size}/${MAX_SELECT} ghế`;
}
function getBookBtn() {
    return document.getElementById('btnBook') || document.getElementById('btnHold');
}
function setLoading(on) {
    const b = getBookBtn();
    if (b)
        b.toggleAttribute('disabled', !!(on || busy));
}
function formatVND(n) {
    try {
        return Number(n).toLocaleString('vi-VN') + ' đ';
    } catch {
        return n;
    }
}
function updateSummary() {
    const total = raw.length || 0;
    const free = raw.filter(s => !!s.available).length;
    const booked = Math.max(0, total - free);
    $('#kFree')?.replaceChildren(String(free));
    $('#kBooked')?.replaceChildren(String(booked));
}

/* ---------- booking ---------- */
const BOOK_ENDPOINT = `${ctx}/api/hold`;
(function bindBook() {
    const btn = getBookBtn();
    if (!btn)
        return;
    btn.addEventListener('click', async () => {
        if (selected.size === 0)
            return alert('Bạn chưa chọn ghế nào.');
        const csrf = CSRF();
        if (!csrf)
            return alert('Thiếu CSRF token. Vui lòng tải lại trang.');
        const body = {tripId: Number(tripId), seatIds: Array.from(selected)};
        try {
            busy = true;
            setLoading(true);
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
                alert(res.error || 'Đặt vé/giữ ghế thất bại');
                return;
            }
            alert('Đặt vé/giữ ghế thành công!');
            selected.clear();
            await fetchSeatMap();
        } catch (e) {
            console.error('Book error', e);
            alert('Đặt vé/giữ ghế thất bại.');
        } finally {
            busy = false;
            setLoading(false);
        }
    });
})();

/* ---------- train cards ---------- */
function wireTrainCards() {
    const cards = $$('#trainCards .train-card, .train-cards .train-card');
    cards.forEach(card => {
        const id = card.dataset.tripId;
        if (!id)
            return;
        const clone = card.cloneNode(true);
        clone.addEventListener('click', async () => {
            if (clone.classList.contains('active'))
                return;
            cards.forEach(c => c.classList.remove('active'));
            clone.classList.add('active');
            $('#kDepart')?.replaceChildren(clone.dataset.depart || '');
            $('#kArrive')?.replaceChildren(clone.dataset.arrive || '');
            $('#kFree')?.replaceChildren(clone.dataset.free || '—');
            $('#kBooked')?.replaceChildren(clone.dataset.booked || '—');
            window.tripId = String(id);
            selected.clear();
            carIndex = 0;
            await fetchSeatMap();
        });
        card.replaceWith(clone);
    });
}

/* ---------- boot ---------- */
document.addEventListener('DOMContentLoaded', fetchSeatMap);
