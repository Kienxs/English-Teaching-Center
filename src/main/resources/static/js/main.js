window.addEventListener("scroll", function() {
  const navbar = document.querySelector(".navbar");
  if (navbar) { // Thêm check null cho an toàn
      if (window.scrollY > 50) { 
        navbar.classList.add("shrink");
      } else {
        navbar.classList.remove("shrink");
      }
  }
});

document.addEventListener("DOMContentLoaded", function() {
    const menuButton = document.getElementById("menu-toggle-button");
    const navLinks = document.getElementById("nav-links");

    if (menuButton && navLinks) {
        menuButton.addEventListener("click", function() {
            navLinks.classList.toggle("active"); 
        });
    }
});

function confirmLogout(event){
  event.preventDefault();

  Swal.fire({
      title: 'Đăng xuất?',
      text: "Bạn có chắc chắn muốn thoát khỏi phiên làm việc hiện tại?",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#9f224e',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Vâng, Đăng xuất',
      cancelButtonText: 'Hủy bỏ'
  }).then((result) => {
      if (result.isConfirmed) {
          window.location.href = '/logout';
      }
  })
}