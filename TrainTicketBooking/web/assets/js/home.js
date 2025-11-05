
(() => {
    // ---------- helpers ----------
    const $ = (s) => document.querySelector(s);

    // DOM refs (khớp ID trong JSP)
    const tabOne = $('#tab-oneway');
    const tabRound = $('#tab-round');
    const tripType = $('#tripType');

    const departDate = $('#departDate');
    const returnDate = $('#returnDate');
    const clearReturn = $('#clearReturnBtn');

    const originInput = $('#originStation');
    const destInput = $('#destStation');
    const originId = $('#originId');
    const destId = $('#destId');
    const swapBtn = $('#swapBtn');

    const datalist = $('#stationList'); // <datalist> chứa <option value="Tên" data-id="..">

    // Tìm option trong datalist theo value (tên ga)
    function findOptionByValue(val) {
        if (!datalist || !val)
            return null;
        const opts = datalist.options;
        for (let i = 0; i < opts.length; i++) {
            if (opts[i].value === val)
                return opts[i];
        }
        return null;
    }

    // Đồng bộ hidden ID theo input hiện tại
    function syncOriginId() {
        const opt = findOptionByValue(originInput.value);
        originId.value = opt?.dataset.id || '';
    }
    function syncDestId() {
        const opt = findOptionByValue(destInput.value);
        destId.value = opt?.dataset.id || '';
    }

    // ONE WAY / ROUND TRIP toggle
    function setRound(isRound) {
        tripType.value = isRound ? 'ROUNDTRIP' : 'ONEWAY';

        // set style active (nếu bạn dùng button thường, không dùng Bootstrap Tabs)
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

    // Cập nhật min cho ngày về khi đổi ngày đi
    function syncMinReturn() {
        if (tripType.value === 'ROUNDTRIP') {
            if (departDate?.value) {
                returnDate.min = departDate.value;
                if (returnDate.value && returnDate.value < departDate.value) {
                    returnDate.value = departDate.value;
                }
            } else {
                returnDate.removeAttribute('min');
            }
        }
    }

    // Xoá ngày về
    function onClearReturn(e) {
        e?.preventDefault?.();
        if (!returnDate.disabled)
            returnDate.value = '';
    }

    // Đổi chiều (swap) cả tên ga rồi sync lại ID
    function onSwap() {
        const a = originInput.value;
        originInput.value = destInput.value;
        destInput.value = a;
        syncOriginId();
        syncDestId();
        originInput.focus();
    }

    // (Tuỳ chọn) ép Chrome bung gợi ý datalist khi focus
    function forceOpenDatalist(input) {
        if (!input)
            return;
        input.addEventListener('focus', () => {
            input.value = ' ';
            input.dispatchEvent(new Event('input', {bubbles: true}));
            setTimeout(() => (input.value = ''), 0);
        });
    }

    tabOne?.addEventListener('click', () => setRound(false));
    tabRound?.addEventListener('click', () => setRound(true));

    departDate?.addEventListener('change', syncMinReturn);
    clearReturn?.addEventListener('click', onClearReturn);

    originInput?.addEventListener('change', syncOriginId);
    destInput?.addEventListener('change', syncDestId);

    swapBtn?.addEventListener('click', onSwap);

    // Nếu tab Khứ hồi đang có class active -> set round; ngược lại ONEWAY
    setRound(!!tabRound?.classList.contains('active'));

    // Nếu có dữ liệu từ session (server fill sẵn), sync ID ngay
    syncOriginId();
    syncDestId();

    // Set min ngày về ban đầu
    syncMinReturn();

    // Bật auto-open gợi ý (nếu vẫn dùng datalist)
    forceOpenDatalist(originInput);
    forceOpenDatalist(destInput);
})();
