document.addEventListener('DOMContentLoaded', function() {
    const links = document.querySelectorAll('.sidebar-link');
    const tabs = document.querySelectorAll('.content-tab');
    const breadcrumbCurrent = document.getElementById('breadcrumb-current'); 
    const urlParams = new URLSearchParams(window.location.search);

    const depositInput = document.getElementById('depositAmount');
    const amountPreview = document.getElementById('amountPreview');

    if (depositInput && amountPreview) {
    depositInput.addEventListener('input', function() {
        const val = this.value;
        if (val && !isNaN(val)) {
            amountPreview.textContent = 'Thực nhận: ' + Number(val).toLocaleString('vi-VN') + ' đ';
        } else {
            amountPreview.textContent = '';
        }
    });
}
    
    // Điều hướng Tab
    let activeTabId = 'tab-profile';
    if (window.location.hash) {
        activeTabId = window.location.hash.substring(1); 
    } else if (urlParams.get('tab')) {
        activeTabId = 'tab-' + urlParams.get('tab');
    }
    
    tabs.forEach(tab => tab.classList.remove('active'));
    links.forEach(l => l.classList.remove('active'));
    
    const tabToActivate = document.getElementById(activeTabId) || document.getElementById('tab-profile');
    const linkToActivate = document.querySelector(`a[data-target="${tabToActivate.id}"]`) || document.querySelector('a[data-target="tab-profile"]');
    
    if(tabToActivate && linkToActivate) {
        tabToActivate.classList.add('active');
        linkToActivate.classList.add('active');
        if (window.location.hash) window.scrollTo(0, 0);
    }
    
    if (breadcrumbCurrent && linkToActivate) {
        updateBreadcrumb(linkToActivate.textContent.trim());
    }

    links.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('data-target');
            window.history.pushState(null, null, '#' + targetId); 
            
            const targetTab = document.getElementById(targetId);
            
            links.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
            
            tabs.forEach(tab => tab.classList.remove('active'));
            if (targetTab) targetTab.classList.add('active');
            
            if (breadcrumbCurrent) updateBreadcrumb(this.textContent.trim());
        });
    });

    function updateBreadcrumb(linkText) {
        if (!breadcrumbCurrent) return; 
        if (linkText === "Thông tin & liên hệ") {
            breadcrumbCurrent.textContent = "Thông tin tài khoản";
        } else {
            breadcrumbCurrent.textContent = linkText;
        }
    }

    // Xem trước Avatar
    const avatarInput = document.getElementById('avatar-input');
    const avatarPreview = document.getElementById('avatar-preview');
    if (avatarInput && avatarPreview) {
        avatarInput.addEventListener('change', function() {
            const file = this.files[0]; 
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    avatarPreview.src = e.target.result;
                }
                reader.readAsDataURL(file);
            }
        });
    }

    // Ẩn/Hiện Mật Khẩu
    document.querySelectorAll('.input-icon-wrapper i').forEach(icon => {
        icon.addEventListener('click', function() {
            const input = this.previousElementSibling;
            if (input.type === 'password') {
                input.type = 'text';
                this.classList.remove('fa-eye-slash');
                this.classList.add('fa-eye');
            } else {
                input.type = 'password';
                this.classList.remove('fa-eye');
                this.classList.add('fa-eye-slash');
            }
        });
    });

    // Xác thực độ mạnh Mật khẩu mới
    const newPasswordInput = document.getElementById('new-password');
    const hint = document.getElementById('passwordHint');
    const strongRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&._-])[A-Za-z\d@$!%*?&._-]{8,}$/;

    if (newPasswordInput && hint) {
        newPasswordInput.addEventListener('input', () => {
            if (strongRegex.test(newPasswordInput.value)) {
                hint.style.color = '#28a745'; 
                hint.innerHTML = '<i class="fas fa-check"></i> Mật khẩu hợp lệ';
                newPasswordInput.setCustomValidity(""); 
            } else {
                hint.style.color = '#dc3545'; 
                hint.innerHTML = '(Tối thiểu 8 ký tự, có chữ hoa, thường, số và ký tự đặc biệt)';
                newPasswordInput.setCustomValidity("Mật khẩu chưa đủ mạnh"); 
            }
        });
    }

    // Khóa ngày sinh ở quá khứ
    const dobInput = document.getElementById('dob');
    if (dobInput) {
        const today = new Date().toISOString().split("T")[0];
        dobInput.setAttribute('max', today);
        dobInput.addEventListener('change', function() {
            if (this.value > today) {
                this.setCustomValidity("Ngày sinh không thể lớn hơn ngày hiện tại!");
            } else {
                this.setCustomValidity("");
            }
        });
    }

    // Kiểm tra ReCaptcha và hiệu ứng Loading cho các Form
    const forms = [
        { formElement: document.getElementById('profileForm'), btnElement: document.getElementById('btnUpdate') },
        { formElement: document.getElementById('passwordForm'), btnElement: document.querySelector('#passwordForm button[type="submit"]') },
        { formElement: document.getElementById('usernameForm'), btnElement: document.querySelector('#usernameForm button[type="submit"]') },
        { formElement: document.getElementById('depositForm'), btnElement: document.querySelector('#depositForm button[type="submit"]'), needCaptcha: false }
    ];

    forms.forEach(item => {
    if (item.formElement && item.btnElement) {
        item.formElement.addEventListener('submit', function(e) {
            
            if (item.needCaptcha) {
                const captchaResponse = item.formElement.querySelector('[name="g-recaptcha-response"]');
                if (captchaResponse && !captchaResponse.value) {
                    e.preventDefault(); 
                    Swal.fire({
                        title: 'Cảnh báo!',
                        text: 'Vui lòng xác nhận bạn không phải người máy!',
                        icon: 'warning',
                        confirmButtonText: 'Đã rõ',
                        confirmButtonColor: '#f39c12'
                    });
                    return; 
                }
            }

            item.btnElement.disabled = true;
            item.btnElement.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
            document.body.style.cursor = 'wait';
        });
    }
});

});