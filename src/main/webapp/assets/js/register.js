function togglePassword(inputId, button) {

    const input = document.getElementById(inputId);

    if (input.type === "password") {

        input.type = "text";
        button.textContent = "Hide";

    } else {

        input.type = "password";
        button.textContent = "Show";
    }
}


document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("registerForm");

    if (!form) {
        return;
    }

    form.addEventListener("submit", function (event) {

        const password =
            document.getElementById("password").value;

        const confirmPassword =
            document.getElementById("confirmPassword").value;


        if (password.length < 8) {

            event.preventDefault();

            alert(
                "Password must contain at least 8 characters."
            );

            return;
        }


        if (password !== confirmPassword) {

            event.preventDefault();

            alert(
                "Password and Confirm Password do not match."
            );

            return;
        }

    });

});