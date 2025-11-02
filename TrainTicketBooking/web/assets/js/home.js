// /static/js/voyage-home.js
(function () {
    const $$ = (sel) => document.querySelector(sel);

    const tripTypeInput = $$('#tripType');          // <input type="hidden" id="tripType" name="tripType">
    const tabOneway = $$('#tab-oneway');            // nút "Một chiều"
    const tabRound = $$('#tab-round');             // nút "Khứ hồi"
    const departInput = $$('#departDate');
    const returnInput = $$('#returnDate');
    const clearBtn = $$('#clearReturnBtn');
    const swapBtn = $$('#swapBtn');
    const originInput = $$('#originStation');
    const destInput = $$('#destStation');

    function setRound(isRound) {
        tripTypeInput.value = isRound ? 'ROUNDTRIP' : 'ONEWAY';

        if (isRound) {
            returnInput.removeAttribute('disabled');
            if (departInput && departInput.value) {
                returnInput.min = departInput.value;
                if (!returnInput.value || returnInput.value < departInput.value) {
                    returnInput.value = departInput.value;
                }
            }
        } else {
            returnInput.value = '';
            returnInput.setAttribute('disabled', 'disabled');
            returnInput.removeAttribute('min');
        }
    }

    function onDepartChange() {
        if (tripTypeInput.value === 'ROUNDTRIP') {
            if (departInput && departInput.value) {
                returnInput.min = departInput.value;
                if (returnInput.value && returnInput.value < departInput.value) {
                    returnInput.value = departInput.value;
                }
            } else {
                returnInput.removeAttribute('min');
            }
        }
    }

    function onClearReturn(e) {
        e.preventDefault();
        if (!returnInput.disabled) {
            returnInput.value = '';
            returnInput.focus();
        }
    }

    function onSwap() {
        if (!originInput || !destInput)
            return;
        const a = originInput.value;
        originInput.value = destInput.value;
        destInput.value = a;
    }

    // Nếu bạn có dùng Bootstrap Tab đúng chuẩn:
    tabOneway?.addEventListener('shown.bs.tab', () => setRound(false));
    tabRound?.addEventListener('shown.bs.tab', () => setRound(true));

    // Fallback: luôn bắt click để chắc chắn
    tabOneway?.addEventListener('click', () => setRound(false));
    tabRound?.addEventListener('click', () => setRound(true));

    departInput?.addEventListener('change', onDepartChange);
    clearBtn?.addEventListener('click', onClearReturn);
    swapBtn?.addEventListener('click', onSwap);

    // Khởi tạo theo tab đang active thay vì mặc định ONEWAY
    const roundActive = tabRound?.classList.contains('active');
    setRound(!!roundActive);
})();
