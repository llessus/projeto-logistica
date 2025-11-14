document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector("form");

  if (!form) {
    console.error("Formulário não encontrado!");
    return;
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.querySelector("#email").value;
    const senha = document.querySelector("#senha").value;

    try {
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, senha }),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem("token", data.token);
        window.location.href = "painel.html";
      } else {
        alert("❌ Falha no login. Verifique o email ou senha.");
      }
    } catch (error) {
      console.error("Erro ao fazer login:", error);
      alert("Erro ao conectar com o servidor.");
    }
  });
});
