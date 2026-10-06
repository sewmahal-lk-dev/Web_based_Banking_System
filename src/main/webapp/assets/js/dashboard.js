const menuButton = document.getElementById("menuButton");
const sidebar = document.getElementById("sidebar");

if (menuButton && sidebar) {
    menuButton.addEventListener("click", () => {
        sidebar.classList.toggle("open");
    });
}


const balanceToggle =
    document.getElementById("balanceToggle");

const balanceAmount =
    document.getElementById("balanceAmount");

const originalBalance = balanceAmount ? balanceAmount.textContent.trim() : "";
let balanceVisible = true;

if (balanceToggle && balanceAmount) {

    balanceToggle.addEventListener("click", () => {

        balanceVisible = !balanceVisible;
        balanceToggle.setAttribute('aria-pressed', String(!balanceVisible));
        balanceToggle.setAttribute('aria-label', balanceVisible ? 'Hide total balance' : 'Show total balance');

        if (balanceVisible) {
            balanceAmount.textContent = originalBalance;
            balanceToggle.textContent = "◉";
        } else {
            balanceAmount.textContent = "••••••••";
            balanceToggle.textContent = "○";
        }

    });
}
