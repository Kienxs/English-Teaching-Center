// Chạy hàm này khi trang được tải xong
document.addEventListener("DOMContentLoaded", function() {
    fetchCourses();
});

// Hàm để lấy và hiển thị các khóa học
function fetchCourses() {
    // Gọi đến API bạn vừa tạo
    fetch('http://localhost:8080/api/courses')
        .then(response => response.json()) // Chuyển đổi phản hồi sang JSON
        .then(courses => {
            // "courses" bây giờ là một mảng (Array) các đối tượng khóa học
            const grid = document.getElementById('course-grid-container');
            
            // Xóa nội dung cũ (nếu có)
            grid.innerHTML = ''; 

            // Lặp qua từng khóa học và tạo HTML
            courses.forEach(course => {
                // *** THAY ĐỔI Ở ĐÂY ***
                // 1. Tạo một thẻ 'a' (link)
                const link = document.createElement('a');
                // 2. Đặt đường dẫn (URL) cho link. Chúng ta sẽ dùng cấu trúc /user/course/ID
                //    (Giống như trang list của bạn là /user/courseList)
                link.href = `/user/course/${course.id}`;
                link.className = 'course-card-link'; // Thêm class để CSS nếu cần

                // 3. Tạo nội dung HTML cho card, giống như cũ
                const cardHtml = `
                    <div class="course-card">
                        <img src="${course.imageUrl || 'default-image.jpg'}" alt="${course.name}" class="card-thumbnail">
                        <div class="card-content">
                            <h3>${course.name}</h3>
                            <p class="current-price course-price-info">${course.fee}</p>
                            <p>Hình thức: ${course.mode}</p>
                        </div>
                    </div>
                `;
                
                // 4. Đặt HTML của card vào bên trong thẻ 'a'
                link.innerHTML = cardHtml;
                
                // 5. Chèn thẻ 'a' (đã chứa card) vào grid
                grid.appendChild(link);
            });
        })
        .catch(error => {
            console.error('Lỗi khi tải khóa học:', error);
            // Hiển thị thông báo lỗi cho người dùng
            const grid = document.getElementById('course-grid-container');
            grid.innerHTML = '<p>Không thể tải danh sách khóa học. Vui lòng thử lại sau.</p>';
        });
}