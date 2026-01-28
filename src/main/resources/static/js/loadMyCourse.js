document.addEventListener("DOMContentLoaded", function() {
    
    // 1. Lấy các phần tử
    const tabActive = document.getElementById('tab-active');
    const tabExpired = document.getElementById('tab-expired');
    const grid = document.getElementById('course-grid-container');

    // 2. Gán sự kiện click cho tab "Đang học"
    tabActive.addEventListener('click', function(e) {
        e.preventDefault(); // Ngăn link nhảy trang

        // Cập nhật class active
        tabActive.classList.add('active-category');
        tabExpired.classList.remove('active-category');

        // Tải danh sách khóa học đang học
        fetchMyCourses('ENROLLED'); 
    });

    // 3. Gán sự kiện click cho tab "Hết hạn"
    tabExpired.addEventListener('click', function(e) {
        e.preventDefault(); // Ngăn link nhảy trang

        // Cập nhật class active
        tabExpired.classList.add('active-category');
        tabActive.classList.remove('active-category');

        // Tải danh sách khóa học hết hạn
        fetchMyCourses('EXPIRED');
    });

    // 4. Mặc định tải khóa học "Đang học" khi mở trang
    fetchMyCourses('ENROLLED');
});

/**
 * Hàm chính để tải khóa học theo trạng thái
 * @param {string} status - ("ENROLLED" hoặc "EXPIRED")
 */
function fetchMyCourses(status) {
    
    // Hiển thị thông báo đang tải...
    const grid = document.getElementById('course-grid-container');
    grid.innerHTML = '<p>Đang tải khóa học...</p>';

    // 1. Tạo URL động với tham số status
    const apiUrl = `/user/my-courses?status=${status}`;

    // 2. Gọi API
    fetch(apiUrl, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
            // Cookie (JSESSIONID) sẽ được trình duyệt tự động gửi
        }
    })
    .then(response => {
        if (response.status === 401 || response.status === 403) {
            console.error("Xác thực thất bại. Vui lòng đăng nhập lại.");
            window.location.href = '/login'; 
            throw new Error('Unauthorized');
        }
        if (!response.ok) {
            throw new Error('Lỗi mạng hoặc server');
        }
        return response.json();
    })
    .then(courses => {
        // Xóa thông báo "Đang tải..."
        grid.innerHTML = ''; 

        // 3. Hiển thị nếu không có khóa học
        if (courses.length === 0) {
            if (status === 'ENROLLED') {
                grid.innerHTML = '<p>Bạn chưa đăng ký khóa học nào đang hoạt động.</p>';
            } else {
                grid.innerHTML = '<p>Bạn không có khóa học nào đã hết hạn.</p>';
            }
            return;
        }

        // 4. Lặp và hiển thị các thẻ khóa học
        courses.forEach(course => {
            const link = document.createElement('a');
            
            // Nếu khóa học đang học, link tới trang học.
            // Nếu hết hạn, link về trang chi tiết (để mua lại)
            if (status === 'ENROLLED') {
                link.href = `/user/my-course-detail/${course.slug}`; // Link đến trang HỌC
                link.className = 'course-card-link';
            } else {
                link.href = `/user/course-detail/${course.slug}`; // Link đến trang CHI TIẾT
                link.className = 'course-card-link expired-link'; // Thêm class để CSS nếu muốn
            }
            
            const cardHtml = `
                <div class="course-card">
                    <img src="${course.imageUrl || 'default-image.jpg'}" alt="${course.name}" class="card-thumbnail">
                    <div class="card-content">
                        <h3>${course.name}</h3>
                        <p>Trạng thái: ${status === 'ENROLLED' ? 'Đã đăng ký' : 'Đã hết hạn'}</p>
                    </div>
                </div>
            `;
            
            link.innerHTML = cardHtml;
            grid.appendChild(link);
        });
    })
    .catch(error => {
        if (error.message !== 'Unauthorized') {
            console.error('Lỗi khi tải khóa học:', error);
            grid.innerHTML = '<p>Không thể tải danh sách khóa học của bạn.</p>';
        }
    });
}