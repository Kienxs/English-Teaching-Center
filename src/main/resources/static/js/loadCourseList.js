let currentPage = 0;
const pageSize = 9;

// Chạy hàm này khi trang được tải xong
document.addEventListener("DOMContentLoaded", function() {
    fetchCourses(currentPage);
});

// Hàm để lấy và hiển thị các khóa học
function fetchCourses(page) {
    // Gọi đến API bạn vừa tạo
    fetch(`/api/courses?page=${page}&size=${pageSize}`)
        .then(response =>{
            if(!response.ok){
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {

            // 1.Render danh sách khóa học
            renderCourses(data.content);
            
            // 2. Render các nút phân trang
            renderPagination(data.totalPages, data.number);

            // 3.Cập nhật trang hiện tại
            currentPage = data.number;

        })
        .catch(error => {
            console.error('Lỗi khi tải khóa học:', error);
            document.getElementById('course-grid-container').innerHTML = 
            '<p>Không thể tải danh sách khóa học. Vui lòng thử lại sau.</p>';
        });
}

function renderCourses(courses){
    const grid = document.getElementById('course-grid-container');
    grid.innerHTML = ''; 

    if(!courses || courses.length === 0){ // Thêm check !courses cho an toàn
        grid.innerHTML = '<p>Không có khóa học nào để hiển thị.</p>';
        return;
    }

    courses.forEach(course => {
        const link = document.createElement('a');
        link.href = `/user/course-detail/${course.slug}`;
        link.className = 'course-card-link';
        // Định dạng tiền VND
        const formattedFee = new Intl.NumberFormat('vi-VN', { 
            style: 'currency', 
            currency: 'VND' 
        }).format(course.fee);
        // ------------------------

        const cardHtml = `
            <div class="course-card">
                <img src="${course.imageUrl || '/images/default-course.jpg'}" alt="${course.name}" class="card-thumbnail">
                <div class="card-content">
                    <h3 class="course-title">${course.name}</h3>
                    <p class="course-price-info">${formattedFee}</p> <p class="course-mode">Hình thức: ${course.mode === 'ONLINE' ? 'Trực tuyến' : 'Tại trung tâm'}</p>
                </div>
            </div>
        `;
        
        link.innerHTML = cardHtml;
        grid.appendChild(link);
    });
}

// Hàm tạo các nút phân trang
function renderPagination(totalPages, currentPage) {
    const paginationContainer = document.getElementById('pagination-container');
    paginationContainer.innerHTML = ''; // Xóa nút cũ

    // Nếu chỉ có 1 trang hoặc không có trang nào thì ẩn phân trang đi
    if (totalPages <= 1) return;

    // --- Nút PREVIOUS (Trang trước) ---
    const prevBtn = document.createElement('button');
    prevBtn.innerText = '«';
    prevBtn.className = 'page-btn';
    prevBtn.disabled = currentPage === 0; // Vô hiệu hóa nếu đang ở trang đầu
    prevBtn.onclick = () => {
        if (currentPage > 0) fetchCourses(currentPage - 1);
    };
    paginationContainer.appendChild(prevBtn);

    // --- Các nút SỐ TRANG (1, 2, 3...) ---
    // Logic hiển thị thông minh (để tránh hiển thị 100 nút nếu có 100 trang)
    // Ở đây làm đơn giản: Hiển thị tối đa 5 trang xung quanh trang hiện tại
    let startPage = Math.max(0, currentPage - 2);
    let endPage = Math.min(totalPages - 1, currentPage + 2);

    // Điều chỉnh nếu ở đầu hoặc cuối danh sách
    if (currentPage <= 2) {
        endPage = Math.min(totalPages - 1, 4);
    }
    if (currentPage >= totalPages - 3) {
        startPage = Math.max(0, totalPages - 5);
    }

    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.innerText = i + 1; // Hiển thị số trang (bắt đầu từ 1 cho user dễ nhìn)
        pageBtn.className = `page-btn ${i === currentPage ? 'active' : ''}`;
        
        // Khi bấm vào số trang
        pageBtn.onclick = () => {
            if (i !== currentPage) {
                fetchCourses(i);
                // Cuộn màn hình lên đầu danh sách cho trải nghiệm tốt hơn
                document.getElementById('course-grid-container').scrollIntoView({ behavior: 'smooth' });
            }
        };
        paginationContainer.appendChild(pageBtn);
    }

    // --- Nút NEXT (Trang sau) ---
    const nextBtn = document.createElement('button');
    nextBtn.innerText = '»';
    nextBtn.className = 'page-btn';
    nextBtn.disabled = currentPage === totalPages - 1; // Vô hiệu hóa nếu đang ở trang cuối
    nextBtn.onclick = () => {
        if (currentPage < totalPages - 1) fetchCourses(currentPage + 1);
    };
    paginationContainer.appendChild(nextBtn);
}