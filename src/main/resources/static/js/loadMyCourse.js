// loadMyCourse.js
document.addEventListener("DOMContentLoaded", function() {
    const tabActive = document.getElementById('tab-active');
    const tabExpired = document.getElementById('tab-expired');

    tabActive.addEventListener('click', function(e) {
        e.preventDefault(); 
        if (this.classList.contains('active-category')) return;
        this.classList.add('active-category');
        tabExpired.classList.remove('active-category');
        fetchMyCourses('ENROLLED'); 
    });

    tabExpired.addEventListener('click', function(e) {
        e.preventDefault(); 
        if (this.classList.contains('active-category')) return;
        this.classList.add('active-category');
        tabActive.classList.remove('active-category');
        fetchMyCourses('EXPIRED');
    });

    fetchMyCourses('ENROLLED');
});

function fetchMyCourses(status) {
    const grid = document.getElementById('course-grid-container');
    if (!grid) return;

    grid.innerHTML = '<p><i class="fas fa-spinner fa-spin"></i> Đang tải khóa học...</p>';

    fetch(`/user/my-courses?status=${status}`)
        .then(response => {
            if (response.status === 401) {
                // Bạn có thể copy hàm handleUnauthorized() vào một file js dùng chung 
                // hoặc viết trực tiếp tại đây:
                Swal.fire({
                    icon: 'warning',
                    title: 'Hết phiên làm việc',
                    text: 'Vui lòng đăng nhập lại để tiếp tục.',
                    confirmButtonText: 'OK'
                }).then(() => { window.location.href = '/login'; });
                return Promise.reject('Unauthorized');
            }
            return response.json();
        })
        .then(courses => {
            grid.innerHTML = ''; 
            if (courses.length === 0) {
                grid.innerHTML = status === 'ENROLLED' ? 
                    '<p>Bạn chưa đăng ký khóa học nào.</p>' : '<p>Không có khóa học hết hạn.</p>';
                return;
            }

            courses.forEach(course => {
                const link = document.createElement('a');
                link.className = `course-card-link ${status === 'EXPIRED' ? 'expired-link' : ''}`;
                link.href = status === 'ENROLLED' ? 
                    `/user/my-course-detail/${course.slug}` : `/user/course-detail/${course.slug}`;
                
                link.innerHTML = `
                    <div class="course-card">
                        <img src="${course.imageUrl || '/images/default-course.jpg'}" class="card-thumbnail">
                        <div class="card-content">
                            <h3>${course.name}</h3>
                            <p>Trạng thái: ${status === 'ENROLLED' ? 'Đang học' : 'Hết hạn'}</p>
                        </div>
                    </div>`;
                grid.appendChild(link);
            });
        })
        .catch(error => {
            if (error !== 'Unauthorized') {
                console.error('Lỗi:', error);
                grid.innerHTML = '<p>Lỗi tải dữ liệu.</p>';
            }
        });
}