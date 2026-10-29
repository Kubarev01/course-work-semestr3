// Highlight the current section without changing server-side access rules.
(() => {
  const path = window.location.pathname;
  document.querySelectorAll('.sidebar nav a').forEach(link => {
    const href = link.getAttribute('href');
    if (path === href || (href !== '/dashboard' && path.startsWith(href + '/'))) {
      link.setAttribute('aria-current', 'page');
    }
  });
})();
