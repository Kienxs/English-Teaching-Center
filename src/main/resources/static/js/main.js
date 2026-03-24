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