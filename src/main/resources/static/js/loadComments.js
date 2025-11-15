document.addEventListener("DOMContentLoaded", function() {

    // --- 1. XỬ LÝ CHUYỂN TAB ---
    const tabLinks = document.querySelectorAll(".course-nav-tabs .tab-item");
    const tabPanes = document.querySelectorAll(".tab-content-container .tab-pane");

    tabLinks.forEach(link => {
        link.addEventListener("click", function(event) {
            event.preventDefault(); // Ngăn nhảy trang
            
            const tabId = this.getAttribute("data-tab");

            // Tắt active ở tất cả các tab
            tabLinks.forEach(item => item.classList.remove("active"));
            tabPanes.forEach(pane => pane.classList.remove("active"));

            // Bật active cho tab vừa click
            this.classList.add("active");
            const activePane = document.getElementById(tabId);
            if(activePane) {
                activePane.classList.add("active");
            }
        });
    });

    // --- 2. XỬ LÝ NÚT GỬI BÌNH LUẬN ---
    const commentTextarea = document.getElementById("comment-textarea");
    const submitButton = document.getElementById("btn-submit-comment");

    if (commentTextarea && submitButton) {
        // Kích hoạt/Vô hiệu hóa nút Gửi
        commentTextarea.addEventListener("input", function() {
            if (this.value.trim().length > 0) {
                submitButton.removeAttribute("disabled");
            } else {
                submitButton.setAttribute("disabled", "true");
            }
        });
    }

});