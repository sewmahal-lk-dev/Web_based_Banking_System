document.addEventListener('click', event => {
  document.querySelectorAll('.notification-center[open]').forEach(center => {
    if (!center.contains(event.target)) center.open = false;
  });
});
document.addEventListener('keydown', event => {
  if (event.key !== 'Escape') return;
  document.querySelectorAll('.notification-center[open]').forEach(center => {
    center.open = false;
    center.querySelector('summary').focus();
  });
});
