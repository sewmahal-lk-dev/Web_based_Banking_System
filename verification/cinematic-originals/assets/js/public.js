'use strict';
(() => {
    const menuButton = document.querySelector('.menu-toggle');
    const navigation = document.querySelector('#nav-links');
    if (!menuButton || !navigation) return;
    function closeMenu() {
        navigation.classList.remove('open');
        menuButton.setAttribute('aria-expanded', 'false');
    }
    menuButton.addEventListener('click', () => {
        const open = navigation.classList.toggle('open');
        menuButton.setAttribute('aria-expanded', String(open));
    });
    navigation.addEventListener('click', event => {
        if (event.target.closest('a')) closeMenu();
    });
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && navigation.classList.contains('open')) {
            closeMenu();
            menuButton.focus();
        }
    });
    document.addEventListener('click', event => {
        if (!event.target.closest('.nav')) closeMenu();
    });
    matchMedia('(max-width: 1320px)').addEventListener('change', closeMenu);
    const links = [...navigation.querySelectorAll('a[href^="#"]')];
    function markActive(id) {
        links.forEach(link => {
            if (link.hash === '#' + id) link.setAttribute('aria-current', 'location');
            else link.removeAttribute('aria-current');
        });
    }
    markActive(location.hash.slice(1) || 'home');
    window.addEventListener('hashchange', () => markActive(location.hash.slice(1) || 'home'));
    if ('IntersectionObserver' in window) {
        const observer = new IntersectionObserver(entries => {
            entries.forEach(entry => { if (entry.isIntersecting) markActive(entry.target.id); });
        }, { rootMargin: '-20% 0px -55% 0px' });
        links.forEach(link => {
            const section = document.getElementById(link.hash.slice(1));
            if (section) observer.observe(section);
        });
    }
})();
