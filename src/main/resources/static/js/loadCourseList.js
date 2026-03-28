// loadCourseList.js
let currentPage = 0;
const pageSize = 9;

// 1. Khai báo biến global để Cache các DOM element
let courseGridContainer;
let paginationContainer;

// Chạy hàm này khi trang được tải xong
document.addEventListener("DOMContentLoaded", function() {
    // Chỉ query DOM đúng 1 lần duy nhất lúc khởi tạo
    courseGridContainer = document.getElementById('course-grid-container');
    paginationContainer = document.getElementById('pagination-container');
    
    fetchCourses(currentPage);
});

// Hàm để lấy và hiển thị các khóa học
function fetchCourses(page) {
    // Thêm hiệu ứng loading nhỏ cho xịn xò trong lúc chờ API
    if (courseGridContainer) {
        courseGridContainer.innerHTML = '<p><i class="fas fa-spinner fa-spin"></i> Đang tải khóa học...</p>';
    }

    fetchAPI(`/api/courses?page=${page}&size=${pageSize}`)
        .then(data => {
            // 1. Render danh sách khóa học
            renderCourses(data.content);
            
            // 2. Render các nút phân trang
            renderPagination(data.totalPages, data.number);

            // 3. Cập nhật trang hiện tại
            currentPage = data.number;
        })
        .catch(error => {
            // Lỗi 401 đã được Global xử lý
            if (error !== 'Unauthorized') {
                console.error('Lỗi khi tải khóa học:', error);
                if (courseGridContainer) {
                    courseGridContainer.innerHTML = '<p>Không thể tải danh sách khóa học. Vui lòng thử lại sau.</p>';
                }
            }
        });
}

function renderCourses(courses) {
    if (!courseGridContainer) return;
    courseGridContainer.innerHTML = ''; 

    if (!courses || courses.length === 0) { 
        courseGridContainer.innerHTML = '<p>Không có khóa học nào để hiển thị.</p>';
        return;
    }

    // 2. Dùng DocumentFragment để tối ưu hiệu năng (tránh re-flow UI liên tục)
    const fragment = document.createDocumentFragment();

    courses.forEach(course => {
        const link = document.createElement('a');
        link.href = `/user/course-detail/${course.slug}`;
        link.className = 'course-card-link';
        
        // Định dạng tiền VND
        const formattedFee = new Intl.NumberFormat('vi-VN', { 
            style: 'currency', 
            currency: 'VND' 
        }).format(course.fee);

        const cardHtml = `
            <div class="course-card">
                <img src="${course.imageUrl || '/images/default-course.jpg'}" alt="${course.name}" class="card-thumbnail">
                <div class="card-content">
                    <h3 class="course-title">${course.name}</h3>
                    <p class="course-price-info">${formattedFee}</p> 
                    <p class="course-mode">Hình thức: ${course.mode === 'ONLINE' ? 'Trực tuyến' : 'Tại trung tâm'}</p>
                </div>
            </div>
        `;
        
        link.innerHTML = cardHtml;
        fragment.appendChild(link);
    });

    // Append toàn bộ fragment vào DOM 1 lần duy nhất
    courseGridContainer.appendChild(fragment);
}

// Hàm tạo các nút phân trang
function renderPagination(totalPages, currentPage) {
    if (!paginationContainer) return;
    paginationContainer.innerHTML = ''; 

    if (totalPages <= 1) return;

    const fragment = document.createDocumentFragment();

    // --- Nút PREVIOUS (Trang trước) ---
    const prevBtn = document.createElement('button');
    prevBtn.innerText = '«';
    prevBtn.className = 'page-btn';
    prevBtn.disabled = currentPage === 0; 
    prevBtn.onclick = () => {
        if (currentPage > 0) fetchCourses(currentPage - 1);
    };
    fragment.appendChild(prevBtn);

    // --- Các nút SỐ TRANG (1, 2, 3...) ---
    let startPage = Math.max(0, currentPage - 2);
    let endPage = Math.min(totalPages - 1, currentPage + 2);

    if (currentPage <= 2) {
        endPage = Math.min(totalPages - 1, 4);
    }
    if (currentPage >= totalPages - 3) {
        startPage = Math.max(0, totalPages - 5);
    }

    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.innerText = i + 1; 
        pageBtn.className = `page-btn ${i === currentPage ? 'active' : ''}`;
        
        pageBtn.onclick = () => {
            if (i !== currentPage) {
                fetchCourses(i);
                // Dùng biến cache thay vì query document.getElementById lại
                if (courseGridContainer) {
                    courseGridContainer.scrollIntoView({ behavior: 'smooth' });
                }
            }
        };
        fragment.appendChild(pageBtn);
    }

    // --- Nút NEXT (Trang sau) ---
    const nextBtn = document.createElement('button');
    nextBtn.innerText = '»';
    nextBtn.className = 'page-btn';
    nextBtn.disabled = currentPage === totalPages - 1; 
    nextBtn.onclick = () => {
        if (currentPage < totalPages - 1) fetchCourses(currentPage + 1);
    };
    fragment.appendChild(nextBtn);

    paginationContainer.appendChild(fragment);
}