const revealElements = document.querySelectorAll('.reveal');

const revealObserver = new IntersectionObserver(
  (entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible');
        revealObserver.unobserve(entry.target);
      }
    });
  },
  {
    threshold: 0.16,
    rootMargin: '0px 0px -40px 0px',
  }
);

revealElements.forEach((element) => revealObserver.observe(element));

const metrics = document.querySelectorAll('[data-count]');

const metricsObserver = new IntersectionObserver(
  (entries) => {
    entries.forEach((entry) => {
      if (!entry.isIntersecting) {
        return;
      }

      const target = Number(entry.target.getAttribute('data-count'));
      let current = 0;
      const increment = Math.max(1, Math.ceil(target / 24));

      const timer = window.setInterval(() => {
        current = Math.min(target, current + increment);
        entry.target.textContent = current.toString();

        if (current >= target) {
          window.clearInterval(timer);
        }
      }, 36);

      metricsObserver.unobserve(entry.target);
    });
  },
  { threshold: 0.5 }
);

metrics.forEach((metric) => metricsObserver.observe(metric));

const spotlightCard = document.getElementById('spotlight-card');
const spotlightSteps = document.querySelectorAll('.journey-step');

const spotlightContent = {
  crear: {
    label: 'Crear la jornada',
    title: 'Preparar un partido no rompe el ritmo.',
    text: 'La pantalla de nuevo partido permite fijar fecha, detectar si ese día ya existe una sesión y confirmar la selección de jugadores sin pasos de sobra.',
    className: 'spotlight-preview spotlight-players',
    items: ['Rubén', 'Marta', 'Laura', 'Diego'],
  },
  editar: {
    label: 'Editar y ordenar sets',
    title: 'Los resultados se ajustan hasta que reflejan la pista.',
    text: 'Los sets se pueden añadir, reordenar por arrastre, editar o eliminar, con aviso si hay cambios pendientes antes de salir.',
    className: 'spotlight-preview results',
    items: ['Set 1        Pareja 1', 'Set 2        Pareja 2', 'Set 3        Pareja 1'],
  },
  detalle: {
    label: 'Consultar el detalle',
    title: 'Cada jornada tiene una lectura clara y compartible.',
    text: 'La vista de detalle reúne jugadores, clasificación, sets y un resumen listo para compartir cuando termina el partido.',
    className: 'spotlight-preview detail',
    items: ['Clasificación        82%', 'Resumen compartible        listo', 'Edición posterior        rápida'],
  },
  analizar: {
    label: 'Analizar la evolución',
    title: 'Las estadísticas cuentan cómo cambia el grupo con el tiempo.',
    text: 'Desde la pestaña de estadísticas puedes abrir la comparativa general o profundizar en el histórico de cada jugador sesión a sesión.',
    className: 'spotlight-preview analytics',
    items: ['Ratio actual        55.7%', 'Sesiones jugadas        18', 'Tendencia        al alza'],
  },
};

function renderSpotlight(key) {
  const content = spotlightContent[key];

  if (!content || !spotlightCard) {
    return;
  }

  spotlightCard.innerHTML = `
    <p class="spotlight-label">${content.label}</p>
    <h3>${content.title}</h3>
    <p>${content.text}</p>
    <div class="${content.className}" aria-hidden="true">
      ${content.items.map((item) => `<span>${item}</span>`).join('')}
    </div>
  `;
}

spotlightSteps.forEach((step) => {
  step.addEventListener('click', () => {
    spotlightSteps.forEach((item) => item.classList.remove('active'));
    step.classList.add('active');
    renderSpotlight(step.dataset.spotlight);
  });
});
