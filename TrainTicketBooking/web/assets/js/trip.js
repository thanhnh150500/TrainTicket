/* trips.js - robust UI behavior for trip list page */
(function () {
    'use strict';

    // ========= CTX (origin/dest/date base) =========
    // Ưu tiên window.TRIPS_CTX; fallback tìm #tripPage[data-*]
    const pageEl = document.getElementById('tripPage');
    const CTX = (function () {
        const w = (window.TRIPS_CTX || {});
        if (pageEl) {
            w.baseUrl = w.baseUrl || pageEl.dataset.baseUrl;
            w.originId = w.originId || pageEl.dataset.originId;
            w.destId = w.destId || pageEl.dataset.destId;
        }
        return w;
    })();

    function isValidISODate(s) {
        // yyyy-MM-dd
        return /^\d{4}-\d{2}-\d{2}$/.test(s || '');
    }

    function goToDate(dateStr) {
        if (!CTX.baseUrl || !CTX.originId || !CTX.destId || !isValidISODate(dateStr))
            return;
        const url = `${CTX.baseUrl}/trips?originId=${encodeURIComponent(CTX.originId)}&destId=${encodeURIComponent(CTX.destId)}&date=${encodeURIComponent(dateStr)}`;
        window.location.href = url;
    }

    // ========= Date picker & "Tìm lại" =========
    const dateEl = document.getElementById('datePicker');
    const btnSearchAgain = document.getElementById('btnSearchAgain');

    if (dateEl) {
        dateEl.addEventListener('change', () => {
            if (dateEl.value)
                goToDate(dateEl.value);
        });
        // Enter để tìm nhanh
        dateEl.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && dateEl.value) {
                e.preventDefault();
                goToDate(dateEl.value);
            }
        });
    }

    if (btnSearchAgain && dateEl) {
        btnSearchAgain.addEventListener('click', () => {
            if (dateEl.value)
                goToDate(dateEl.value);
        });
    }

    // ========= Days scroller (support 2 kiểu DOM) =========
    const scroller =
            document.getElementById('daysbarScroll') ||
            document.querySelector('.days-strip');

    if (!scroller)
        return;

    const activeItem =
            scroller.querySelector('.daysbar-item.is-active') ||
            scroller.querySelector('.day-tab.active');

    // Auto-center active day
    if (activeItem) {
        const left = activeItem.offsetLeft - (scroller.clientWidth - activeItem.clientWidth) / 2;
        scroller.scrollTo({left, behavior: 'auto'});
    }

    // Drag-to-scroll (+ chặn click khi vừa kéo)
    let isDown = false, startX = 0, startScroll = 0, moved = false, pid = null;
    const MIN_DRAG_PX = 4;

    function onDown(e) {
        isDown = true;
        moved = false;
        pid = e.pointerId;
        startX = e.clientX;
        startScroll = scroller.scrollLeft;
        scroller.setPointerCapture(pid);
    }
    function onMove(e) {
        if (!isDown)
            return;
        const dx = e.clientX - startX;
        if (Math.abs(dx) > MIN_DRAG_PX)
            moved = true;
        scroller.scrollLeft = startScroll - dx;
    }
    function onUp() {
        isDown = false;
        pid = null;
    }

    scroller.addEventListener('pointerdown', onDown);
    scroller.addEventListener('pointermove', onMove);
    ['pointerup', 'pointercancel', 'mouseleave'].forEach(ev => scroller.addEventListener(ev, onUp));

    // Ngăn click link khi vừa drag
    scroller.querySelectorAll('a').forEach(a => {
        a.addEventListener('click', (e) => {
            if (moved)
                e.preventDefault();
        }, true);
    });

    // Wheel ngang (trackpad/mouse)
    scroller.addEventListener('wheel', (e) => {
        if (Math.abs(e.deltaX) < Math.abs(e.deltaY)) {
            // chuyển dọc thành ngang để cuộn mượt
            scroller.scrollLeft += e.deltaY;
            e.preventDefault();
        }
    }, {passive: false});

    // ========= Phím mũi tên trái/phải để nhảy ngày =========
    // Tự tìm prev/next URL nếu có gán data-attr trên scroller, hoặc lấy từ 2 nút header
    const prevBtn = document.querySelector('[data-role="prevDay"]') ||
            document.querySelector('.btn-outline-secondary[href*="date="]:first-child');
    const nextBtn = document.querySelector('[data-role="nextDay"]') ||
            document.querySelector('.btn-outline-secondary[href*="date="]:last-child');

    document.addEventListener('keydown', (e) => {
        if (['INPUT', 'TEXTAREA', 'SELECT'].includes((e.target && e.target.tagName) || ''))
            return;
        if (e.key === 'ArrowLeft' && prevBtn) {
            const href = prevBtn.getAttribute('href');
            if (href) {
                e.preventDefault();
                window.location.href = href;
            }
        } else if (e.key === 'ArrowRight' && nextBtn) {
            const href = nextBtn.getAttribute('href');
            if (href) {
                e.preventDefault();
                window.location.href = href;
            }
        }
    });

    // ========= Safe restore scroll position when navigating back =========
    if ('scrollRestoration' in history)
        history.scrollRestoration = 'manual';
})();
