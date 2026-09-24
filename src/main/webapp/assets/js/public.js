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
    // Reveal once on entry. Content remains visible without JS or observer support.
    const reducedMotion = matchMedia('(prefers-reduced-motion: reduce)');
    const revealTargets = [...document.querySelectorAll('.section-heading, .split > *, .service-row > a, .feature-grid > div, .achievement-grid > div')];
    let revealObserver;
    function configureReveals() {
        revealObserver?.disconnect();
        if (reducedMotion.matches || !('IntersectionObserver' in window)) {
            revealTargets.forEach(target => target.classList.remove('motion-ready', 'is-visible'));
            return;
        }
        revealObserver = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('is-visible');
                    revealObserver.unobserve(entry.target);
                }
            });
        }, { threshold: 0, rootMargin: '0px 0px -35px 0px' });
        revealTargets.forEach(target => {
            const siblings = [...target.parentElement.children];
            target.style.setProperty('--reveal-delay', (siblings.indexOf(target) % 3) * .1 + 's');
            target.classList.add('motion-ready');
            if (target.getBoundingClientRect().top < innerHeight) target.classList.add('is-visible');
            else revealObserver.observe(target);
        });
    }
    configureReveals();
    reducedMotion.addEventListener('change', configureReveals);
})();
