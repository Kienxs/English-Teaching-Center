function initAuthFeatures(successMsg, errorMsg, captchaError) {
    // 1. Xử lý Loading khi submit form
    const form = document.getElementById('registerForm') || document.getElementById('loginForm');
    const btn = document.getElementById('btnRegister') || document.getElementById('btnLogin');

    if (form && btn) {
        form.addEventListener('submit', function() {
            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
            document.body.style.cursor = 'wait';
        });
    }

    // 2. Hàm hiển thị SweetAlert2
    const showModal = (title, text, icon) => {
        if (text && text !== "null" && text.trim() !== "" && text !== "[[${successMessage}]]") {
            Swal.fire({
                title: title,
                text: text,
                icon: icon,
                confirmButtonText: 'OK',
                confirmButtonColor: icon === 'success' ? '#28a745' : '#dc3545'
            });
        }
    };

    // Gọi hiển thị
    if (successMsg) showModal('Thành công!', successMsg, 'success');
    if (errorMsg) showModal('Lỗi!', errorMsg, 'error');
    if (captchaError) showModal('Xác thực!', captchaError, 'warning');
}