// assets/js/auth.js
document.addEventListener('DOMContentLoaded', () => {

    /**
     * Gắn chức năng toggle hiện/ẩn mật khẩu cho nút và input tương ứng
     * @param {string} btnId  ID của nút toggle
     * @param {string} inputId ID của ô input password
     */
    function bindToggle(btnId, inputId) {
        const btn = document.getElementById(btnId);
        const input = document.getElementById(inputId);
        if (!btn || !input)
            return; // Không tồn tại => bỏ qua

        btn.addEventListener('click', () => {
            const isHidden = input.type === 'password';
            input.type = isHidden ? 'text' : 'password';
            btn.innerHTML = isHidden
                    ? '<i class="bi bi-eye-slash"></i>'
                    : '<i class="bi bi-eye"></i>';
        });
    }

    // --- Dành cho trang Đăng nhập ---
    bindToggle('togglePassword', 'passwordField');

    // --- Dành cho trang Đăng ký ---
    bindToggle('toggleRegPassword', 'regPassword');
    bindToggle('toggleRegConfirm', 'regConfirm');

    // --- Kiểm tra xác nhận mật khẩu trên trang đăng ký ---
    const pw = document.getElementById('regPassword');
    const cf = document.getElementById('regConfirm');
    const form = document.querySelector('form[action$="/auth/register"]');
    const confirmHelp = document.getElementById('confirmHelp');

    if (pw && cf && form) {
        function validateConfirm() {
            if (cf.value.trim() === "") {
                cf.classList.remove('is-invalid');
                confirmHelp?.classList.add('d-none');
                return true;
            }

            const match = pw.value === cf.value;
            cf.classList.toggle('is-invalid', !match);
            if (confirmHelp)
                confirmHelp.classList.toggle('d-none', match);
            return match;
        }

        pw.addEventListener('input', validateConfirm);
        cf.addEventListener('input', validateConfirm);

        form.addEventListener('submit', (e) => {
            const ok = validateConfirm();
            if (!ok) {
                e.preventDefault();
                e.stopPropagation();
                cf.focus();
            }
        });
    }
});
