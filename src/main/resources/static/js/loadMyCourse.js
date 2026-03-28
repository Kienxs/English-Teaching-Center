// loadMyCourse.js
document.addEventListener("DOMContentLoaded", function() {
    
    // 1. Lấy các phần tử
    const tabActive = document.getElementById('tab-active');
    const tabExpired = document.getElementById('tab-expired');
    const grid = document.getElementById('course-grid-container');

    // 2. Gán sự kiện click cho tab "Đang học"
    tabActive.addEventListener('click', function(e) {
        e.preventDefault(); 
        if (this.classList.contains('active-category')) return;
        tabActive.classList.add('active-category');
        tabExpired.classList.remove('active-category');
        fetchMyCourses('ENROLLED'); 
    });

    tabExpired.addEventListener('click', function(e) {
        e.preventDefault(); 
        if (this.classList.contains('active-category')) return;
        tabExpired.classList.add('active-category');
        tabActive.classList.remove('active-category');
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
    const grid = document.getElementById('course-grid-container');
    grid.innerHTML = '<p><i class="fas fa-spinner fa-spin"></i> Đang tải khóa học...</p>';

    const apiUrl = `/user/my-courses?status=${status}`;

    // 🚀 Dùng Wrapper MỚI: Rất ngắn gọn và mạnh mẽ!
    fetchAPI(apiUrl)
    .then(courses => {
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
            
            if (status === 'ENROLLED') {
                link.href = `/user/my-course-detail/${course.slug}`;
                link.className = 'course-card-link';
            } else {
                link.href = `/user/course-detail/${course.slug}`;
                link.className = 'course-card-link expired-link'; 
            }
            
            const cardHtml = `
                <div class="course-card">
                    <img src="${course.imageUrl || '/images/default-course.jpg'}" alt="${course.name}" class="card-thumbnail">
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
        // Lỗi 401 (văng do Netflix) đã bị api-client chặn lại và báo SweetAlert rồi. 
        // Ở đây ta chỉ cần lo các lỗi mạng bình thường khác!
        if (error !== 'Unauthorized') {
            console.error('Lỗi khi tải khóa học:', error);
            grid.innerHTML = '<p>Không thể tải danh sách khóa học của bạn.</p>';
        }
    });
}