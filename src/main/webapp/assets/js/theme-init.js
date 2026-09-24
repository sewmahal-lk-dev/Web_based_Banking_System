/* Blocking, local and tiny: apply preference before CSS paints. */
(() => {
 let theme;
 try { theme = localStorage.getItem('lankatrust-theme'); } catch (_) { /* Storage can be unavailable. */ }
 if (theme !== 'light' && theme !== 'dark') theme = matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
 document.documentElement.dataset.theme = theme;
 document.documentElement.style.colorScheme = theme;
 document.documentElement.classList.add('ui-js');
})();
