(() => {
    const $ = (s, r = document) => r.querySelector(s);
    // ---------- Helpers ----------
    function guessCtx() {
        if (window.APP_CTX && typeof window.APP_CTX === 'string')
            return window.APP_CTX.replace(/\/+$/, '');
        const m = location.pathname.match(/^\/[^/]+/);
        return m ? m[0] : '';
    }
    const CTX = guessCtx();
    const API_SUGGEST = CTX + '/api/stations/suggest';

    // Chuẩn hóa không dấu để so khớp ổn định
    const norm = (s) => (s || '')
                .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
                .toLowerCase().trim();

    // ---------- Refs ----------
    const tabOne = $('#tab-oneway');
    const tabRound = $('#tab-round');
    const tripType = $('#tripType');
    const departDate = $('#departDate');
    const returnDate = $('#returnDate');
    const clearReturn = $('#clearReturnBtn');

    const originInput = $('#originStation');
    const destInput = $('#destStation');
    const originIdEl = $('#originId');
    const destIdEl = $('#destId');
    const swapBtn = $('#swapBtn');
    const datalist = $('#stationList');

    // Dùng chung 1 map cho datalist hiện tại
    let nameToId = new Map();
    let activeInput = null; // input đang focus

    // ---------- One-way / Round-trip ----------
    function setRound(isRound) {
        tripType.value = isRound ? 'ROUNDTRIP' : 'ONEWAY';
        tabOne?.classList.toggle('active', !isRound);
        tabRound?.classList.toggle('active', isRound);
        if (isRound) {
            returnDate.removeAttribute('disabled');
            if (departDate?.value) {
                returnDate.min = departDate.value;
                if (!returnDate.value || returnDate.value < departDate.value)
                    returnDate.value = departDate.value;
            }
        } else {
            returnDate.value = '';
            returnDate.setAttribute('disabled', 'disabled');
            returnDate.removeAttribute('min');
        }
    }
    function syncMinReturn() {
        if (tripType.value === 'ROUNDTRIP' && departDate?.value) {
            returnDate.min = departDate.value;
            if (returnDate.value && returnDate.value < departDate.value)
                returnDate.value = departDate.value;
        }
    }
    tabOne?.addEventListener('click', () => setRound(false));
    tabRound?.addEventListener('click', () => setRound(true));
    departDate?.addEventListener('change', syncMinReturn);
    clearReturn?.addEventListener('click', (e) => {
        e.preventDefault();
        if (!returnDate.disabled)
            returnDate.value = '';
    });

    // ---------- Swap ----------
    swapBtn?.addEventListener('click', () => {
        const a = originInput.value, b = destInput.value;
        originInput.value = b;
        destInput.value = a;
        const ai = originIdEl.value, di = destIdEl.value;
        originIdEl.value = di;
        destIdEl.value = ai;
        originInput.focus();
    });

    // ---------- Datalist logic ----------
    function rebuildMapFromDatalist() {
        nameToId = new Map();
        for (const opt of datalist.options) {
            const id = opt.getAttribute('data-id') || '';
            const v = opt.value || '';
            const code = opt.getAttribute('data-code') || '';
            if (id) {
                nameToId.set(norm(v), id);
                if (code)
                    nameToId.set(norm(code), id);
            }
        }
    }

    function fillDatalist(items) {
        datalist.innerHTML = '';
        (items || []).forEach(s => {
            const opt = document.createElement('option');
            opt.value = s.name;
            if (s.id)
                opt.setAttribute('data-id', s.id);
            if (s.code)
                opt.setAttribute('data-code', s.code);
            datalist.appendChild(opt);
        });
        rebuildMapFromDatalist();
    }

    // Đặt hidden id nếu text khớp 1 option (không dấu)
    function syncHiddenFor(inputEl, hiddenEl) {
        const id = nameToId.get(norm(inputEl.value)) || '';
        hiddenEl.value = id;
    }

    // Debounce fetch
    function attach(inputEl, hiddenEl) {
        let timer = null;
        let lastQ = '';

        inputEl.addEventListener('focus', () => {
            activeInput = inputEl;
            // Khi vừa focus, nếu có text hiện tại -> nạp gợi ý theo text đó
            const q = inputEl.value.trim();
            if (q) {
                lastQ = q;
                fetch(API_SUGGEST + '?q=' + encodeURIComponent(q) + '&n=8', {credentials: 'same-origin'})
                        .then(r => r.ok ? r.json() : [])
                        .then(fillDatalist)
                        .catch(() => {
                        });
            }
        });

        inputEl.addEventListener('input', () => {
            hiddenEl.value = ''; // khi gõ lại thì reset id
            const q = inputEl.value.trim();
            lastQ = q;
            clearTimeout(timer);
            if (!q) {
                fillDatalist([]);
                return;
            }
            timer = setTimeout(() => {
                // Chỉ nạp nếu ô này vẫn đang active
                if (activeInput !== inputEl)
                    return;
                fetch(API_SUGGEST + '?q=' + encodeURIComponent(q) + '&n=8', {credentials: 'same-origin'})
                        .then(r => r.ok ? r.json() : [])
                        .then(items => {
                            if (inputEl.value.trim() === lastQ)
                                fillDatalist(items);
                        })
                        .catch(() => {
                        });
            }, 160);
        });

        // Enter chọn ngay; blur/change chốt id
        inputEl.addEventListener('keydown', (e) => {
            if (e.key === 'Enter')
                setTimeout(() => syncHiddenFor(inputEl, hiddenEl), 0);
        });
        inputEl.addEventListener('change', () => syncHiddenFor(inputEl, hiddenEl));
        inputEl.addEventListener('blur', () => {
            syncHiddenFor(inputEl, hiddenEl);
            if (activeInput === inputEl)
                activeInput = null;
        });
    }

    // Map sẵn từ danh sách preload trong JSP (nếu có)
    rebuildMapFromDatalist();

    if (originInput && originIdEl)
        attach(originInput, originIdEl);
    if (destInput && destIdEl)
        attach(destInput, destIdEl);

    // Init
    setRound(!!tabRound?.classList.contains('active'));
    syncMinReturn();
})();
