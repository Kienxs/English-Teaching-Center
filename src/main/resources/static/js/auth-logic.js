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

    // kiểm tra xem chuỗi message có thật sự chứa nội dung không
    const isValidMsg = (msg) => {
        return msg && msg !== "null" && msg.trim() !== "" && !msg.includes("[[${");
    };

    // 2. Xử lý hiển thị SweetAlert2
    
    // Xử lý thông báo thành công (Có rẽ nhánh cho trường hợp Check Mail)
    if (isValidMsg(successMsg)) {
        const msgLower = successMsg.toLowerCase();
        
        if (msgLower.includes("kiểm tra") && (msgLower.includes("email") || msgLower.includes("hộp thư"))) {
            Swal.fire({
                title: 'Gần xong rồi! 🚀',
                text: successMsg,
                icon: 'info',
                showCancelButton: true,
                confirmButtonText: 'Đi tới Gmail ngay',
                cancelButtonText: 'Đóng',
                confirmButtonColor: '#00c8ff'
            }).then((result) => {
                if (result.isConfirmed) {
                    window.open('https://mail.google.com', '_blank'); // Mở tab Gmail
                }
            });
        } else {
            Swal.fire({
                title: 'Thành công!',
                text: successMsg,
                icon: 'success',
                confirmButtonText: 'OK',
                confirmButtonColor: '#28a745'
            });
        }
    }

    // Xử lý thông báo lỗi
    if (isValidMsg(errorMsg)) {
        Swal.fire({
            title: 'Lỗi!',
            text: errorMsg,
            icon: 'error',
            confirmButtonText: 'Đã rõ',
            confirmButtonColor: '#dc3545'
        });
    }

    // Xử lý thông báo lỗi Captcha
    if (isValidMsg(captchaError)) {
        Swal.fire({
            title: 'Xác thực!',
            text: captchaError,
            icon: 'warning',
            confirmButtonText: 'OK',
            confirmButtonColor: '#f39c12' 
        });
    }
}