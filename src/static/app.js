const form = document.getElementById("projectForm");
const formMessage = document.getElementById("formMessage");
const modal = document.getElementById("projectModal");
const openButtons = [
    document.getElementById("openModalButton"),
    document.getElementById("openHeroModalButton")
].filter(Boolean);
const closeButton = document.getElementById("closeModalButton");

function openModal() {
    modal.classList.add("is-open");
    modal.setAttribute("aria-hidden", "false");
}

function closeModal() {
    modal.classList.remove("is-open");
    modal.setAttribute("aria-hidden", "true");
}

async function saveProject(event) {
    event.preventDefault();

    const submitButton = form.querySelector("button[type='submit']");
    const formData = new FormData(form);
    const body = new URLSearchParams(formData);

    submitButton.disabled = true;
    submitButton.textContent = "Duke ruajtur...";
    formMessage.className = "form-message";
    formMessage.textContent = "";

    try {
        const response = await fetch("/add-project", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body
        });

        const payload = await response.json();

        if (!response.ok) {
            throw new Error(payload.error || "Ruajtja deshtoi.");
        }

        form.reset();
        formMessage.className = "form-message success";
        formMessage.textContent = "Projekti u ruajt me sukses ne databaze.";
        setTimeout(() => {
            window.location.reload();
        }, 500);
    } catch (error) {
        console.error(error);
        formMessage.className = "form-message error";
        formMessage.textContent = error.message;
    } finally {
        submitButton.disabled = false;
        submitButton.textContent = "Ruaj";
    }
}

openButtons.forEach((button) => {
    button.addEventListener("click", openModal);
});

closeButton.addEventListener("click", closeModal);
modal.addEventListener("click", (event) => {
    if (event.target.dataset.closeModal === "true") {
        closeModal();
    }
});

document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && modal.classList.contains("is-open")) {
        closeModal();
    }
});

form.addEventListener("submit", saveProject);
