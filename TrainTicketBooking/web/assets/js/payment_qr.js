(function () {
    const cfg = window.TT_QR || {};
    const COUNT = Number.isFinite(+cfg.countdownSec) ? +cfg.countdownSec : 300;
    const KEY = `tt_pay_${cfg.bookingId || 'default'}`;
    const el = document.getElementById('qrRemain');

    let expires = parseInt(localStorage.getItem(KEY) || '0', 10);
    const now = Date.now();
    if (!expires || expires < now - 1000) {
        expires = now + COUNT * 1000;
        localStorage.setItem(KEY, String(expires));
    }

    const fmt = s => `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`;
    const tick = () => {
        const left = Math.max(0, Math.floor((expires - Date.now()) / 1000));
        if (el)
            el.textContent = fmt(left);
        if (left <= 0) {
            alert('Hết thời gian giữ vé. Vui lòng đặt lại.');
            localStorage.removeItem(KEY);
            window.close(); // đóng tab QR nếu được quyền
        } else {
            setTimeout(tick, 1000);
        }
    };
    tick();

    // Nút copy
    document.querySelectorAll('[data-copy]').forEach(btn => {
        btn.addEventListener('click', () => {
            const sel = btn.getAttribute('data-copy');
            const text = document.querySelector(sel)?.textContent?.trim() || '';
            if (!text)
                return;
            navigator.clipboard.writeText(text.replace(/\s?đ$/, ''));
            btn.textContent = 'Đã chép';
            setTimeout(() => btn.textContent = 'Sao chép', 1200);
        });
    });
})();
