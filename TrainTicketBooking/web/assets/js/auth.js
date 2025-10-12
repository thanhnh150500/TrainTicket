document.addEventListener('DOMContentLoaded', () => {
  function bindToggle(btnId, inputId) {
    const btn = document.getElementById(btnId);
    const input = document.getElementById(inputId);
    if (!btn || !input) return;
    btn.addEventListener('click', () => {
      const to = input.type === 'password' ? 'text' : 'password';
      input.type = to;
      btn.innerHTML = to === 'password'
        ? '<i class="bi bi-eye"></i>'
        : '<i class="bi bi-eye-slash"></i>';
    });
  }

  // Login page
  bindToggle('togglePassword', 'passwordField');

  // Register page
  bindToggle('toggleRegPassword', 'regPassword');
  bindToggle('toggleRegConfirm', 'regConfirm');
});
