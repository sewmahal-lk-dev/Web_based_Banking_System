const passwordInput = document.getElementById("password");
const showPasswordButton = document.getElementById("showPassword");

if (passwordInput && showPasswordButton) {

    showPasswordButton.addEventListener("click", function () {

        const hidden = passwordInput.type === "password";

        passwordInput.type = hidden ? "text" : "password";

        showPasswordButton.textContent =
            hidden ? "Hide" : "Show";
    });
}