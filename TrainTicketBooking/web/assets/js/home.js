// /static/js/voyage-home.js
(function () {
    const $$ = (sel) => document.querySelector(sel);

    const tripTypeInput = $$('#tripType');          // hidden input (ONEWAY | ROUNDTRIP)
    const tabOneway = $$('#tab-oneway');        // nút tab Một chiều (button)
    const tabRound = $$('#tab-round');         // nút tab Khứ hồi (button)
    const departInput = $$('#departDate');        // input[type=date] ngày đi
    const returnInput = $$('#returnDate');        // input[type=date] ngày về
    const clearBtn = $$('#clearReturnBtn');    // nút xóa ngày về (✕)
    const swapBtn = $$('#swapBtn');           // (nếu có) nút đổi ga đi/đến
    const originInput = $$('#originStation');
    const destInput = $$('#destStation');

    function setRound(isRound) {
        tripTypeInput.value = isRound ? 'ROUNDTRIP' : 'ONEWAY';

        if (isRound) {
            // mở ngày về
            returnInput.removeAttribute('disabled');
            // ràng buộc min = ngày đi
            if (departInput && departInput.value) {
                returnInput.min = departInput.value;
                if (!returnInput.value || returnInput.value < departInput.value) {
                    returnInput.value = departInput.value;
                }
            }
        } else {
            // khóa ngày về
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

    // ── Gắn sự kiện Bootstrap tab: dùng event 'shown.bs.tab'
    if (tabOneway) {
        tabOneway.addEventListener('shown.bs.tab', () => setRound(false));
    }
    if (tabRound) {
        tabRound.addEventListener('shown.bs.tab', () => setRound(true));
    }

    // Ngày đi thay đổi ⇒ cập nhật min của ngày về (nếu khứ hồi)
    if (departInput)
        departInput.addEventListener('change', onDepartChange);

    // Nút xóa ngày về
    if (clearBtn)
        clearBtn.addEventListener('click', onClearReturn);

    // Nút đổi chiều ga (nếu có trong DOM)
    if (swapBtn)
        swapBtn.addEventListener('click', onSwap);

    // Khởi tạo mặc định: Một chiều
    setRound(false);
})();
