(function () {
    const cfg = window.TT_PAYMENT || {};
    const COUNT = Number.isFinite(+cfg.countdownSec) ? +cfg.countdownSec : 300;
    const KEY = `tt_pay_${cfg.bookingId || 'default'}`;
    const remainEl = document.getElementById('payRemain');

    let expires = parseInt(localStorage.getItem(KEY) || '0', 10);
    const now = Date.now();
    if (!expires || expires < now - 1000) {
        expires = now + COUNT * 1000;
        localStorage.setItem(KEY, String(expires));
    }

    const fmt = s => `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`;
    const tick = () => {
        const left = Math.max(0, Math.floor((expires - Date.now()) / 1000));
        if (remainEl)
            remainEl.textContent = fmt(left);
        if (left <= 0) {
            alert('Hết thời gian giữ vé. Vui lòng đặt lại.');
            localStorage.removeItem(KEY);
            window.location.href = `${location.origin}${(window.TT_PAYMENT_CTX || '')}/`;
            return;
        }
        setTimeout(tick, 1000);
    };
    tick();

    document.getElementById('payForm')?.addEventListener('submit', () => {
        localStorage.removeItem(KEY);
    });
})();
