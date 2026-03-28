// file: public/js/api-client.js

const fetchAPI = async (url, options = {}) => {
    // 1. Tự động lấy token và gắn vào Header cho MỌI request
    const token = localStorage.getItem('accessToken');
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    // 2. Thực hiện gọi API thực tế
    try {
        const response = await fetch(url, { ...options, headers });

        // 3. XỬ LÝ LỖI GLOBAL (Kẻ đánh chặn)
        if (response.status === 401) {
            // Xóa sạch Token
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            
            // Hiển thị thông báo
            Swal.fire({
                icon: 'error',
                title: 'Phiên đăng nhập hết hạn!',
                text: 'Tài khoản của bạn đã được đăng nhập ở nơi khác hoặc phiên làm việc đã kết thúc. Vui lòng đăng nhập lại.',
                confirmButtonText: 'Đồng ý',
                allowOutsideClick: false // Ép người dùng phải bấm nút
            }).then(() => {
                window.location.href = '/login'; 
            });

            // Ném ra lỗi để các hàm gọi fetchAPI bên dưới dừng lại, không chạy tiếp
            return Promise.reject('Unauthorized'); 
        }

        // Bắt thêm các lỗi hệ thống khác nếu thích (500, 403...)
        if (response.status === 403) {
            Swal.fire('Lỗi quyền truy cập', 'Bạn không có quyền thực hiện hành động này!', 'error');
            return Promise.reject('Forbidden');
        }

        // 4. Trả về data JSON luôn cho tiện
        return await response.json();

    } catch (error) {
        console.error('Lỗi gọi API:', error);
        throw error;
    }
};