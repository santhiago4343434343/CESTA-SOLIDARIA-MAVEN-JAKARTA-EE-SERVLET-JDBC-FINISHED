/** @type {import('tailwindcss').Config} */
module.exports = {
  // Tailwind LÊ estes arquivos para descobrir quais classes você usou
  // e gerar só o CSS necessário. Inclui os JSP/JSPF/HTML da aplicação.
  content: [
    "../src/main/webapp/**/*.{jsp,jspf,html}"
  ],
  theme: {
    extend: {
      colors: {
        // atalho opcional: use como bg-marca / text-marca nas classes Tailwind
        marca: "#2563eb",
      },
    },
  },
  // Desligamos o "container" do Tailwind porque o projeto ja tem uma classe
  // .container propria (definida no SCSS). Assim nao ha conflito de nomes.
  corePlugins: {
    container: false,
  },
  plugins: [],
};
