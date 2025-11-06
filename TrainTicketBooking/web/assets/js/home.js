(() => {
    /* =========================
     Helpers
     ========================== */
    const $ = (s, r = document) => r.querySelector(s);

    // Lấy ctx: ưu tiên window.APP_CTX, fallback đoán theo URL
    function guessCtx() {
        if (window.APP_CTX && typeof window.APP_CTX === 'string') {
            return window.APP_CTX.replace(/\/+$/, '');
        }
        const m = location.pathname.match(/^\/[^/]+/);
        return m ? m[0] : '';
    }
    const CTX = guessCtx();
    const API_SUGGEST = CTX + '/api/stations/suggest';

    function escapeHtml(s) {
        return (s || '').replace(/[&<>"']/g, m => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[m]));
    }

    /* =========================
     DOM refs
     ========================== */
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

    /* =========================
     ONE WAY / ROUND TRIP
     ========================== */
    function setRound(isRound) {
        tripType.value = isRound ? 'ROUNDTRIP' : 'ONEWAY';
        tabOne?.classList.toggle('active', !isRound);
        tabRound?.classList.toggle('active', isRound);

        if (isRound) {
            returnDate.removeAttribute('disabled');
            if (departDate?.value) {
                returnDate.min = departDate.value;
                if (!returnDate.value || returnDate.value < departDate.value) {
                    returnDate.value = departDate.value;
                }
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
            if (returnDate.value && returnDate.value < departDate.value) {
                returnDate.value = departDate.value;
            }
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

    /* =========================
     Swap ga đi / ga đến (kèm ID)
     ========================== */
    swapBtn?.addEventListener('click', () => {
        const a = originInput.value, b = destInput.value;
        originInput.value = b;
        destInput.value = a;
        const ai = originIdEl.value, di = destIdEl.value;
        originIdEl.value = di;
        destIdEl.value = ai;
        originInput.focus();
    });

    /* =========================
     Autocomplete (API)
     ========================== */
    function makeAutocomplete(inputEl, hiddenIdEl) {
        // bọc để đặt dropdown
        const wrap = document.createElement('div');
        wrap.style.position = 'relative';
        // move input's parent (.input-group) vào wrap
        const group = inputEl.closest('.input-group') || inputEl.parentElement;
        const parent = group.parentElement;
        parent.insertBefore(wrap, group);
        wrap.appendChild(group);

        // dropdown
        const list = document.createElement('div');
        list.style.position = 'absolute';
        list.style.left = '0';
        list.style.right = '0';
        list.style.top = '100%';
        list.style.zIndex = '1000';
        list.style.background = '#fff';
        list.style.border = '1px solid #e5e7eb';
        list.style.borderTop = '0';
        list.style.borderRadius = '0 0 .75rem .75rem';
        list.style.boxShadow = '0 10px 18px rgba(0,0,0,.08)';
        list.style.display = 'none';
        list.style.maxHeight = '280px';
        list.style.overflowY = 'auto';
        wrap.appendChild(list);

        let timer = null;
        let lastQ = '';
        let idx = -1; // highlight index

        function hide() {
            list.style.display = 'none';
            list.innerHTML = '';
            idx = -1;
        }
        function show() {
            list.style.display = 'block';
        }

        function render(items) {
            list.innerHTML = '';
            if (!items || !items.length) {
                hide();
                return;
            }
            items.forEach((s, i) => {
                const row = document.createElement('button');
                row.type = 'button';
                row.dataset.i = String(i);
                row.dataset.id = s.id;
                row.dataset.name = s.name;
                row.dataset.code = s.code || '';
                row.className = 'ac-row';
                row.style.display = 'block';
                row.style.width = '100%';
                row.style.padding = '.55rem .75rem';
                row.style.textAlign = 'left';
                row.style.border = '0';
                row.style.background = 'white';
                row.style.cursor = 'pointer';
                row.innerHTML =
                        `<div class="fw-semibold">${escapeHtml(s.name)}</div>
           <div class="text-muted small">${escapeHtml(s.city || '')}${s.code ? ' (' + escapeHtml(s.code) + ')' : ''}</div>`;
                row.addEventListener('mouseenter', () => highlight(i));
                row.addEventListener('mouseleave', () => highlight(-1));
                row.addEventListener('click', () => choose(s));
                list.appendChild(row);
            });
            highlight(-1);
            show();
        }

        function highlight(i) {
            idx = i;
            [...list.children].forEach((el, k) => {
                el.style.background = (k === idx) ? '#f3f4f6' : 'white';
            });
        }

        function choose(s) {
            inputEl.value = s.name;
            hiddenIdEl.value = s.id;
            hide();
            inputEl.dispatchEvent(new Event('change'));
        }

        function fetchSuggest(q) {
            const url = API_SUGGEST + '?q=' + encodeURIComponent(q) + '&n=8';
            fetch(url, {credentials: 'same-origin'})
                    .then(r => r.ok ? r.json() : [])
                    .then(arr => {
                        if (inputEl.value.trim() !== lastQ)
                            return; // user tiếp tục gõ
                        render(arr);
                    })
                    .catch(() => hide());
        }

        inputEl.addEventListener('input', () => {
            hiddenIdEl.value = ''; // sửa text → reset id
            const q = inputEl.value.trim();
            lastQ = q;
            clearTimeout(timer);
            if (!q) {
                hide();
                return;
            }
            timer = setTimeout(() => fetchSuggest(q), 180);
        });

        inputEl.addEventListener('focus', () => {
            const q = inputEl.value.trim();
            if (q) {
                lastQ = q;
                fetchSuggest(q);
            }
        });

        inputEl.addEventListener('keydown', (e) => {
            const rows = list.children.length;
            if (list.style.display === 'block' && rows) {
                if (e.key === 'ArrowDown') {
                    e.preventDefault();
                    highlight(Math.min(rows - 1, idx + 1));
                } else if (e.key === 'ArrowUp') {
                    e.preventDefault();
                    highlight(Math.max(-1, idx - 1));
                } else if (e.key === 'Enter' && idx >= 0) {
                    e.preventDefault();
                    const s = {
                        id: list.children[idx].dataset.id,
                        name: list.children[idx].dataset.name,
                        code: list.children[idx].dataset.code
                    };
                    choose(s);
                } else if (e.key === 'Escape') {
                    hide();
                }
            }
        });

        document.addEventListener('click', (ev) => {
            if (!wrap.contains(ev.target))
                hide();
        });
    }

    // Khởi tạo autocomplete cho 2 ô
    if (originInput && originIdEl)
        makeAutocomplete(originInput, originIdEl);
    if (destInput && destIdEl)
        makeAutocomplete(destInput, destIdEl);

    // Init trạng thái ban đầu
    setRound(!!tabRound?.classList.contains('active'));
    syncMinReturn();
})();
