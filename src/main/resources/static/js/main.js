window.addEventListener("scroll", function() {
  const navbar = document.querySelector(".navbar");
  if (window.scrollY > 50) { // khi cuộn xuống hơn 50px
    navbar.classList.add("shrink");
  } else {
    navbar.classList.remove("shrink");
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