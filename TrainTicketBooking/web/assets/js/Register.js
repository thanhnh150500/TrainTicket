// assets/js/register.js
(() => {
    const $ = (s, r = document) => r.querySelector(s);

    const form = $('#regForm');
    if (!form)
        return;

    const fullName = $('#fullName');
    const email = $('#email');
    const pw = $('#regPassword');
    const cf = $('#confirmPassword');

    const fbFull = $('#fullNameFeedback');
    const fbEmail = $('#emailFeedback');
    const fbPw = $('#passwordFeedback');
    const fbCf = $('#confirmHelp');

    const tPw = $('#toggleRegPassword');
    const tCf = $('#toggleRegConfirm');

    // ===== helpers =====
    const setErr = (el, fb, msg) => {
        if (!el)
            return;
        el.classList.add('is-invalid');
        if (fb)
            fb.textContent = msg || '';
    };
    const clearErr = (el, fb) => {
        if (!el)
            return;
        el.classList.remove('is-invalid');
        if (fb)
            fb.textContent = '';
    };

    function bindToggle(btn, input) {
        if (!btn || !input)
            return;
        btn.addEventListener('click', () => {
            input.type = (input.type === 'password') ? 'text' : 'password';
            const i = btn.querySelector('i');
            if (i) {
                i.classList.toggle('bi-eye');
                i.classList.toggle('bi-eye-slash');
            }
            input.focus();
        });
    }

    // ===== rules =====
    // Tên: chỉ chữ Unicode + khoảng trắng, 2–60 ký tự
    const reName = /^[\p{L}]+(?:\s+[\p{L}]+){0,29}$/u; // tối đa ~30 từ
    const reEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    function validateFullName() {
        const v = (fullName.value || '').trim();
        if (!v) {
            setErr(fullName, fbFull, 'Vui lòng nhập họ và tên.');
            return false;
        }
        if (v.length < 2 || v.length > 60 || !reName.test(v)) {
            setErr(fullName, fbFull, 'Họ và tên chỉ bao gồm chữ).');
            return false;
        }
        clearErr(fullName, fbFull);
        return true;
    }

    function validateEmail() {
        const v = (email.value || '').trim();
        if (!v) {
            setErr(email, fbEmail, 'Vui lòng nhập email.');
            return false;
        }
        if (!reEmail.test(v)) {
            setErr(email, fbEmail, 'Email không hợp lệ.');
            return false;
        }
        clearErr(email, fbEmail);
        return true;
    }

    function validatePassword() {
        const v = pw.value || '';
        if (v.length < 8) {
            setErr(pw, fbPw, 'Mật khẩu tối thiểu 8 ký tự.');
            return false;
        }
        clearErr(pw, fbPw);
        return true;
    }

    function validateConfirm() {
        if ((cf.value || '') !== (pw.value || '')) {
            setErr(cf, fbCf, 'Mật khẩu nhập lại không khớp.');
            return false;
        }
        clearErr(cf, fbCf);
        return true;
    }

    // ===== live validate =====
    fullName?.addEventListener('input', validateFullName);
    email?.addEventListener('input', validateEmail);
    pw?.addEventListener('input', () => {
        validatePassword();
        validateConfirm();
    });
    cf?.addEventListener('input', validateConfirm);

    // ===== submit =====
    form.addEventListener('submit', (e) => {
        const ok = [
            validateFullName(),
            validateEmail(),
            validatePassword(),
            validateConfirm()
        ].every(Boolean);

        if (!ok) {
            e.preventDefault();
            e.stopPropagation();
            (form.querySelector('.is-invalid') || fullName)?.focus();
        }
    });

    // ===== toggles =====
    bindToggle(tPw, pw);
    bindToggle(tCf, cf);
})();
