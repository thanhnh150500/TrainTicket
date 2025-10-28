/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */
const MAX_SELECT = 4;
let raw = [];                 // toàn bộ ghế từ API
let byCar = new Map();        // carriageCode -> seats[]
let carKeys = [];
let carIndex = 0;
let selected = new Set();     // seatId đang chọn

function fetchSeatMap() {
    fetch(`${ctx}/api/seatmap?tripId=${encodeURIComponent(tripId)}&with=availability`)
            .then(r => r.json())
            .then(data => {
                raw = data || [];
                groupByCarriage();
                renderMiniCars();
                openCar(0);
                // (tuỳ chọn) cập nhật card đầu tàu
                updateSummary();
            })
            .catch(err => console.error(err));
}

function groupByCarriage() {
    byCar.clear();
    for (const s of raw) {
        const key = s.carriageCode || `#${s.carriageId}`;
        if (!byCar.has(key))
            byCar.set(key, []);
        byCar.get(key).push(s);
    }
    // sắp xếp ghế theo seatCode (A1,A2,… hoặc số)
    for (const arr of byCar.values()) {
        arr.sort((a, b) => (a.seatCode || a.code).localeCompare((b.seatCode || b.code), 'vi', {numeric: true}));
    }
    carKeys = Array.from(byCar.keys());
}

function renderMiniCars() {
    const wrap = document.getElementById('carMini');
    wrap.innerHTML = '';
    carKeys.forEach((k, i) => {
        const el = document.createElement('div');
        el.className = 'car ' + colorByIndex(i) + (i === carIndex ? ' active' : '');
        el.innerHTML = `<div class="num">${k.replace(/\D+/g, '') || (i + 1)}</div>`;
        el.onclick = () => openCar(i);
        wrap.appendChild(el);
    });

    // pager
    document.getElementById('btnPrev').onclick = () => openCar(Math.max(0, carIndex - 1));
    document.getElementById('btnNext').onclick = () => openCar(Math.min(carKeys.length - 1, carIndex + 1));
}

function openCar(i) {
    carIndex = i;
    document.querySelectorAll('#carMini .car').forEach((c, idx) => {
        c.classList.toggle('active', idx === i);
    });
    const code = carKeys[i];
    renderCoach(code);
}

function renderCoach(code) {
    const seats = byCar.get(code) || [];
    const grid = document.getElementById('seatGrid');
    grid.innerHTML = '';

    // Tiêu đề: Toa số ... + tên hạng ghế (lấy từ phần tử đầu nếu có)
    const any = seats[0] || {};
    document.getElementById('coachTitle').textContent = `Toa số ${code} :`;
    document.getElementById('className').textContent = any.seatClassName || '—';

    // Bố cục 2–aisle–2: cứ 2 ghế trái, 2 ghế phải theo thứ tự
    // Chèn “aisle” ở cột giữa
    let col = 0;
    for (const s of seats) {
        if (col % 4 === 2) {
            // chèn khoảng trống lối đi (1 lần mỗi hàng)
            const aisle = document.createElement('div');
            aisle.className = 'aisle';
            grid.appendChild(aisle);
            col++;
        }

        const div = document.createElement('div');
        div.className = 'seat ' + cssState(s);
        div.textContent = s.seatCode || s.code;
        div.title = (s.seatClassName || '') + (s.available ? ' • Còn' : ' • Không khả dụng');

        div.onclick = () => {
            if (!s.available)
                return;
            const id = s.seatId;
            if (selected.has(id)) {
                selected.delete(id);
                div.classList.remove('chosen');
            } else {
                if (selected.size >= MAX_SELECT)
                    return;
                selected.add(id);
                div.classList.add('chosen');
            }
            updateHint();
        };

        grid.appendChild(div);
        col++;
        if (col % 5 === 0)
            col = 0; // 2 ghế + aisle + 2 ghế = 5 cột
    }

    updateHint();
}

function cssState(s) {
    // API của bạn có “available”, và có lock/occupied trong CTE.
    // Ở đây quy ước: available=true -> free, ngược lại nếu lock_expires_at có -> hold, còn lại -> sold
    if (s.available)
        return 'free';
    if (s.lockExpiresAt)
        return 'hold';
    return 'sold';
}

function updateHint() {
    const el = document.getElementById('hint');
    el.textContent = `Đang chọn: ${selected.size}/${MAX_SELECT} ghế`;
}

document.getElementById('btnHold').onclick = () => {
    if (selected.size === 0)
        return alert('Bạn chưa chọn ghế nào.');
    const body = {
        tripId: parseInt(tripId, 10),
        seatIds: Array.from(selected),
        ttlMinutes: 15
    };
    fetch(`${ctx}/api/seat/hold`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body)
    }).then(r => r.json())
            .then(res => {
                const fail = res.results.filter(x => x.seatLockId == null).map(x => x.seatId);
                if (fail.length)
                    alert('Một số ghế đã bị giữ trước: ' + fail.join(', '));
                else
                    alert('Giữ ghế thành công. Hết hạn: ' + new Date(res.expiresAtUtc).toLocaleTimeString());
                selected.clear();
                fetchSeatMap(); // refresh trạng thái
            })
            .catch(e => alert('Lỗi giữ ghế'));
};

function updateSummary() {
    // Nếu bạn có API khác trả giờ đi/đến, số chỗ,… hãy set ở đây.
    // Tạm tính từ dữ liệu ghế:
    const free = raw.filter(s => s.available).length;
    const total = raw.length;
    const booked = total - free;
    document.getElementById('kFree').textContent = free;
    document.getElementById('kBooked').textContent = booked;
}

function colorByIndex(i) {
    const arr = ['blue', 'green', 'orange', 'blue', 'green', 'orange'];
    return arr[i % arr.length];
}

fetchSeatMap();


