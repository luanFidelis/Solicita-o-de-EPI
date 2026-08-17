// ============================================================
// HHTEC FX — transições de mosaico + onda 3D para TODAS as telas
//
// Uso: colocar logo após a abertura do <body>:
//   <script src="/lib/js/hhtec-fx.js"></script>
//
// Atributos opcionais na própria tag:
//   (sem atributo)      SORTEIA a forma a cada carregamento: fita ou círculo
//   data-wave="ribbon"  força a fita ondulada
//   data-wave="circle"  força o anel giratório de cartões
//   data-wave="none"    sem onda 3D
//   data-entry="off"    sem mosaico de entrada
//
// O que ele faz:
//   1. Mosaico de saída ao clicar em links internos (quadradinhos fecham)
//   2. Mosaico de entrada na página seguinte (quadradinhos abrem) —
//      só quando a navegação veio de outra tela do sistema (sem replay no F5)
//   3. Cria a onda 3D automaticamente (se a página ainda não tiver #waveCanvas)
//      com dispersão atrelada ao scroll e pausa total quando dispersa
//   4. Neutraliza o sistema legado de quadrados DOM (motion-squares)
// ============================================================
(function () {
    'use strict';
    if (window.HHTECFx) return;
    window.HHTECFx = { version: 1 };

    // neutraliza o legado ANTES dele carregar (guarda interna dos scripts antigos)
    if (!window.HHTECMotionSquares) window.HHTECMotionSquares = { init: function () {} };

    var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    var cs = document.currentScript || { dataset: {} };
    var ds = cs.dataset || {};
    // sem data-wave definido: cada carregamento sorteia entre fita e círculo
    var waveMode = ds.wave || (Math.random() < 0.5 ? 'circle' : 'ribbon');
    var entryOn = ds.entry !== 'off';
    var FLAG = 'hhfx-nav';

    // ---------- tema (claro | escuro "Barcelona": carvão + dourado) ----------
    function temaEscuro() {
        try { return localStorage.getItem('hhtec_tema') === 'escuro'; } catch (e) { return false; }
    }
    // aplica a classe ANTES da primeira pintura (este script roda logo após <body>)
    if (temaEscuro()) document.documentElement.classList.add('hhtec-dark');

    // paleta do mosaico conforme o tema (tons de OURO no escuro)
    function getPal() {
        return temaEscuro()
            ? ['#f7e08a', '#efd67c', '#e9cc6a', '#ddbb50', '#d4af37', '#c49a2b', '#b8860b', '#a3781d', '#8a6a1f']
            : ['#f9a8d4', '#ec4899', '#e879f9', '#c084fc', '#a78bfa', '#8b5cf6', '#818cf8', '#67e8f9', '#5eead4'];
    }

    // ---------- CSS injetado ----------
    var style = document.createElement('style');
    style.textContent =
        '#hhfxMosaic{position:fixed;inset:0;z-index:9000;display:grid;pointer-events:none;contain:strict}' +
        '#hhfxMosaic .t{border-radius:3px;will-change:transform}' +
        '#hhfxMosaic.mode-cover .t{transform:scale(0)}' +
        '#hhfxMosaic.mode-reveal .t{transform:scale(1.04)}' +
        '#hhfxMosaic.mode-cover.go .t{animation:hhfxIn .5s cubic-bezier(.22,1,.36,1) forwards;animation-delay:var(--d)}' +
        '#hhfxMosaic.mode-reveal.go .t{animation:hhfxOut .55s cubic-bezier(.22,1,.36,1) forwards;animation-delay:var(--d)}' +
        '@keyframes hhfxIn{from{transform:scale(0) rotate(6deg)}to{transform:scale(1.04) rotate(0)}}' +
        '@keyframes hhfxOut{from{transform:scale(1.04) rotate(0)}to{transform:scale(0) rotate(-7deg)}}' +
        '#waveCanvas{position:fixed;inset:0;width:100%;height:100%;z-index:0;pointer-events:none;opacity:0;' +
            'transform:translate3d(0,8px,0);transition:opacity .7s cubic-bezier(.22,1,.36,1),transform .7s cubic-bezier(.22,1,.36,1)}' +
        '#waveCanvas.wave-ready{opacity:1;transform:translate3d(0,0,0)}' +
        '@media (prefers-reduced-motion:reduce){#hhfxMosaic{display:none!important}}' +
        // ----- botão de configurações (engrenagem) + painel -----
        '#hhfxGear{position:fixed;right:0;top:50%;transform:translateY(-50%);z-index:8000;width:38px;height:54px;' +
            'border:1px solid rgba(20,19,22,.14);border-right:none;border-radius:14px 0 0 14px;background:rgba(246,245,243,.92);' +
            'display:flex;align-items:center;justify-content:center;cursor:pointer;color:#141316;padding:0;' +
            'box-shadow:0 10px 26px -14px rgba(20,19,22,.4);transition:transform .25s cubic-bezier(.22,1,.36,1)}' +
        '#hhfxGear:hover{transform:translateY(-50%) translateX(-3px)}' +
        '#hhfxGear svg{transition:transform .5s cubic-bezier(.22,1,.36,1)}' +
        '#hhfxGear:hover svg{transform:rotate(60deg)}' +
        '#hhfxPanel{position:fixed;right:12px;top:50%;transform:translateY(-50%) translateX(16px);z-index:8001;width:266px;' +
            'background:rgba(246,245,243,.96);border:1px solid rgba(20,19,22,.12);border-radius:18px;padding:18px;' +
            'opacity:0;pointer-events:none;box-shadow:0 24px 50px -24px rgba(20,19,22,.45);' +
            'font-family:Archivo,"IBM Plex Sans",sans-serif;color:#141316;text-align:left;' +
            'transition:opacity .25s,transform .3s cubic-bezier(.22,1,.36,1)}' +
        '#hhfxPanel.open{opacity:1;pointer-events:auto;transform:translateY(-50%) translateX(0)}' +
        '#hhfxPanel::before{content:"";position:absolute;top:0;left:18px;right:18px;height:3px;border-radius:0 0 4px 4px;' +
            'background:linear-gradient(115deg,#f9a8d4,#e879f9,#a78bfa,#818cf8,#5eead4)}' +
        '#hhfxPanel .ttl{font-family:"IBM Plex Mono",monospace;font-size:10px;font-weight:600;letter-spacing:.14em;text-transform:uppercase;color:#7b7980;margin-bottom:14px}' +
        '#hhfxPanel .row{display:flex;justify-content:space-between;align-items:center;gap:12px}' +
        '#hhfxPanel .lbl{font-size:13.5px;font-weight:600}' +
        '#hhfxPanel .sub{font-family:"IBM Plex Mono",monospace;font-size:9.5px;letter-spacing:.06em;text-transform:uppercase;color:#7b7980;margin-top:4px}' +
        '.hhfx-switch{position:relative;width:46px;height:26px;border-radius:999px;background:#d8d6d1;border:1px solid rgba(20,19,22,.12);cursor:pointer;padding:0;flex-shrink:0;transition:background .25s}' +
        '.hhfx-switch::after{content:"";position:absolute;top:2px;left:2px;width:20px;height:20px;border-radius:50%;background:#fff;box-shadow:0 2px 6px rgba(20,19,22,.3);transition:transform .25s cubic-bezier(.22,1,.36,1)}' +
        '.hhfx-switch.on{background:linear-gradient(115deg,#e879f9,#8b5cf6,#5eead4)}' +
        '.hhfx-switch.on::after{transform:translateX(20px)}' +
        '#hhfxPanel .note{font-family:"IBM Plex Mono",monospace;font-size:9px;color:#7b7980;letter-spacing:.04em;margin-top:12px;padding-top:10px;border-top:1px solid rgba(20,19,22,.08);text-transform:uppercase}' +
        // ----- seletor de tema (Claro | Escuro) -----
        '.hhfx-seg{display:flex;gap:4px;background:rgba(20,19,22,.06);border:1px solid rgba(20,19,22,.12);border-radius:999px;padding:3px;flex-shrink:0}' +
        '.hhfx-seg button{border:none;background:transparent;font-family:"IBM Plex Mono",monospace;font-size:9.5px;font-weight:600;letter-spacing:.08em;text-transform:uppercase;color:#7b7980;padding:6px 10px;border-radius:999px;cursor:pointer;transition:background .2s,color .2s}' +
        '.hhfx-seg button.on{background:#141316;color:#edecea}' +
        // ----- tema escuro: engrenagem, painel e controles em carvão + dourado -----
        'html.hhtec-dark #hhfxGear{background:rgba(21,22,26,.94);color:#f3efe4;border-color:rgba(212,175,55,.3)}' +
        'html.hhtec-dark #hhfxPanel{background:rgba(18,19,23,.97);color:#f3efe4;border-color:rgba(212,175,55,.25);box-shadow:0 24px 50px -20px rgba(0,0,0,.85)}' +
        'html.hhtec-dark #hhfxPanel .ttl,html.hhtec-dark #hhfxPanel .sub{color:#97906f}' +
        'html.hhtec-dark #hhfxPanel .note{color:#97906f;border-top-color:rgba(212,175,55,.15)}' +
        'html.hhtec-dark #hhfxPanel::before{background:linear-gradient(115deg,#f7e08a,#d4af37,#8a6a1f)}' +
        'html.hhtec-dark .hhfx-switch{background:#2c2e35;border-color:rgba(212,175,55,.25)}' +
        'html.hhtec-dark .hhfx-switch.on{background:linear-gradient(115deg,#e9cc6a,#b8860b)}' +
        'html.hhtec-dark .hhfx-seg{background:rgba(255,255,255,.05);border-color:rgba(212,175,55,.25)}' +
        'html.hhtec-dark .hhfx-seg button{color:#97906f}' +
        'html.hhtec-dark .hhfx-seg button.on{background:linear-gradient(135deg,#e9cc6a,#c49a2b);color:#17140a}' +
        '@media print{#hhfxMosaic,#waveCanvas,#hhfxGear,#hhfxPanel{display:none!important}}';
    document.head.appendChild(style);

    // ---------- mosaico ----------
    function buildMosaic(mode) {
        var old = document.getElementById('hhfxMosaic');
        if (old && old.parentNode) old.parentNode.removeChild(old);

        var wrap = document.createElement('div');
        wrap.id = 'hhfxMosaic';
        wrap.className = 'mode-' + mode;
        wrap.setAttribute('aria-hidden', 'true');

        var tam = 124;
        var cols = Math.ceil(window.innerWidth / tam);
        var rows = Math.ceil(window.innerHeight / tam);
        wrap.style.gridTemplateColumns = 'repeat(' + cols + ',1fr)';
        wrap.style.gridTemplateRows = 'repeat(' + rows + ',1fr)';

        var PAL = getPal();
        var html = '';
        for (var r = 0; r < rows; r++) {
            for (var c = 0; c < cols; c++) {
                var t = (c / cols + r / rows) / 2;
                var i = Math.min(Math.floor(t * (PAL.length - 1)), PAL.length - 2);
                var d = ((c + r) * 30 + Math.random() * 60);
                html += '<div class="t" style="--d:' + d.toFixed(0) + 'ms;background:linear-gradient(135deg,' + PAL[i] + ',' + PAL[i + 1] + ')"></div>';
            }
        }
        wrap.innerHTML = html;
        (document.body || document.documentElement).appendChild(wrap);
        return { el: wrap, total: (cols + rows) * 30 + 60 + 620 };
    }

    // ---------- entrada (quadradinhos abrindo) ----------
    var entering = null;
    if (entryOn && !reduce && document.body) {
        var veioDeNavegacao = false;
        try {
            veioDeNavegacao = sessionStorage.getItem(FLAG) === '1';
            sessionStorage.removeItem(FLAG);
        } catch (e) {}

        if (veioDeNavegacao) {
            // cobre a tela ANTES da primeira pintura (este script roda logo após <body>)
            entering = buildMosaic('reveal');
            var startReveal = function () {
                requestAnimationFrame(function () {
                    requestAnimationFrame(function () {
                        entering.el.classList.add('go');
                        setTimeout(function () {
                            if (entering && entering.el.parentNode) entering.el.parentNode.removeChild(entering.el);
                            entering = null;
                        }, entering.total);
                    });
                });
            };
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', startReveal, { once: true });
            } else {
                startReveal();
            }
        }
    }

    // ---------- saída (quadradinhos fechando) ----------
    var leaving = false;

    function leave() {
        if (leaving) return 0;
        leaving = true;
        try { sessionStorage.setItem(FLAG, '1'); } catch (e) {}
        document.documentElement.classList.add('app-transitioning');
        if (document.body) document.body.classList.add('app-leaving');
        try { window.dispatchEvent(new CustomEvent('hhtec:app-leaving')); } catch (e) {}
        if (reduce) return 1;
        var m = buildMosaic('cover');
        requestAnimationFrame(function () { m.el.classList.add('go'); });
        return Math.min(m.total, 1300);
    }

    function go(url) {
        var delay = leave();
        window.setTimeout(function () { window.location.href = url; }, delay);
    }

    function shouldTransition(anchor, event) {
        if (!anchor || leaving) return false;
        if (event && (event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey)) return false;
        if (anchor.target && anchor.target !== '_self') return false;
        if (anchor.hasAttribute('download') || anchor.hasAttribute('data-no-fx')) return false;

        var raw = anchor.getAttribute('href') || '';
        if (!raw || raw[0] === '#') return false;
        if (/^(javascript|mailto|tel):/i.test(raw)) return false;

        try {
            var next = new URL(anchor.href, window.location.href);
            if (next.origin !== window.location.origin) return false;
            return !(next.pathname === window.location.pathname && next.search === window.location.search && next.hash);
        } catch (err) {
            return false;
        }
    }

    document.addEventListener('click', function (event) {
        var anchor = event.target.closest && event.target.closest('a[href]');
        if (!shouldTransition(anchor, event)) return;
        event.preventDefault();
        go(anchor.href);
    }, true);

    // volta pelo botão do navegador (bfcache): remove resíduos da saída
    window.addEventListener('pageshow', function (ev) {
        if (!ev.persisted && !leaving) return;
        leaving = false;
        document.documentElement.classList.remove('app-transitioning');
        if (document.body) document.body.classList.remove('app-leaving');
        var m = document.getElementById('hhfxMosaic');
        if (m && m.parentNode && !entering) m.parentNode.removeChild(m);
    });

    // API compatível com o antigo app-transition.js (que vira no-op se carregado depois)
    window.HHTECTransition = window.HHTECTransition || { leave: leave, go: go, prefetch: function () {} };
    window.HHTECFx.leave = leave;
    window.HHTECFx.go = go;

    // ---------- onda 3D ----------
    // preferência do usuário (painel de configurações): '0' = desligada
    function pref3d() {
        try { return localStorage.getItem('hhtec_anim3d') !== '0'; } catch (e) { return true; }
    }

    function initWave(force) {
        // se a página já tem a própria onda (#waveCanvas), não cria outra —
        // exceto no religar ao vivo (force), quando um canvas inerte é recriado
        var existente = document.getElementById('waveCanvas');
        if (existente) {
            // dataset.hhtecWave = onda de verdade rodando (o stub não marca)
            if (!force || existente.dataset.hhtecWave === '1') return;
            existente.parentNode.removeChild(existente);
        }
        if (reduce || waveMode === 'none' || !pref3d()) return;

        var canvas = document.createElement('canvas');
        canvas.id = 'waveCanvas';
        canvas.setAttribute('aria-hidden', 'true');
        document.body.insertBefore(canvas, document.body.firstChild);

        import('/lib/js/wave-hhtec.js').then(function (mod) {
            var mobile = window.innerWidth < 768;
            var wave = mod.createWave(canvas, {
                shape: waveMode,
                count: waveMode === 'circle' ? (mobile ? 40 : 64) : (mobile ? 44 : 58),
                yOffset: waveMode === 'circle' ? 0 : -2.4,
                amplitude: 0.68,
                opacity: 0.9,
                fade: 0.95,
                radius: mobile ? 2.4 : 3.1
            });
            window.__hhWave = wave;
            requestAnimationFrame(function () { canvas.classList.add('wave-ready'); });

            // dispersão atrelada à posição do scroll (abre descendo, fecha subindo)
            function aoRolar() {
                var p = Math.min(Math.max(window.scrollY / (window.innerHeight * 0.85), 0), 1);
                wave.setScatter(p);
            }
            window.addEventListener('scroll', aoRolar, { passive: true });
            aoRolar();
        }).catch(function (e) {
            console.warn('HHTEC FX: onda 3D indisponível', e);
            if (canvas.parentNode) canvas.parentNode.removeChild(canvas);
        });
    }

    if (waveMode !== 'none' && !reduce && pref3d()) {
        setTimeout(initWave, 430);
    }

    // ---------- botão de configurações (engrenagem na borda direita) ----------
    function desligarOnda() {
        // encerra qualquer onda ativa (do fx ou das páginas com init próprio)
        ['__hhWave', '__stWave', '__materialsWave', '__wave'].forEach(function (k) {
            try { if (window[k] && window[k].dispose) window[k].dispose(); } catch (e) {}
            try { window[k] = null; } catch (e) {}
        });
        var canvases = document.querySelectorAll('#waveCanvas');
        for (var i = 0; i < canvases.length; i++) {
            if (canvases[i].parentNode) canvases[i].parentNode.removeChild(canvases[i]);
        }
    }

    (function montarConfig() {
        if (document.getElementById('hhfxGear') || !document.body) return;

        var gear = document.createElement('button');
        gear.id = 'hhfxGear';
        gear.type = 'button';
        gear.title = 'Configurações';
        gear.setAttribute('aria-label', 'Configurações');
        gear.innerHTML = '<svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>';

        var panel = document.createElement('div');
        panel.id = 'hhfxPanel';
        panel.innerHTML =
            '<div class="ttl">Configurações</div>' +
            '<div class="row">' +
                '<div><div class="lbl">Animação 3D</div><div class="sub">Onda de cartões no fundo</div></div>' +
                '<button type="button" class="hhfx-switch" id="hhfxSw3d" aria-label="Ligar ou desligar a animação 3D"></button>' +
            '</div>' +
            '<div class="row" style="margin-top:14px">' +
                '<div><div class="lbl">Tema</div><div class="sub">Aparência do sistema</div></div>' +
                '<div class="hhfx-seg" role="group" aria-label="Tema">' +
                    '<button type="button" data-tema="claro">Claro</button>' +
                    '<button type="button" data-tema="escuro">Escuro</button>' +
                '</div>' +
            '</div>' +
            '<div class="note">Preferências salvas neste aparelho</div>';

        document.body.appendChild(gear);
        document.body.appendChild(panel);

        var sw = panel.querySelector('#hhfxSw3d');
        function refresh() { sw.classList.toggle('on', pref3d()); }
        refresh();

        gear.addEventListener('click', function (e) {
            e.stopPropagation();
            panel.classList.toggle('open');
        });

        // fecha ao clicar fora
        document.addEventListener('click', function (e) {
            if (!panel.classList.contains('open')) return;
            if (panel.contains(e.target) || gear.contains(e.target)) return;
            panel.classList.remove('open');
        });

        sw.addEventListener('click', function () {
            var ligar = !pref3d();
            try { localStorage.setItem('hhtec_anim3d', ligar ? '1' : '0'); } catch (e) {}
            refresh();
            if (ligar) initWave(true);  // liga na hora (recria canvas inerte se preciso)
            else desligarOnda();        // desliga na hora (GPU dorme)
        });

        // ----- seletor de tema (troca ao vivo + onda renasce na paleta nova) -----
        var segBtns = panel.querySelectorAll('.hhfx-seg button');
        function refreshTema() {
            var atual = temaEscuro() ? 'escuro' : 'claro';
            for (var i = 0; i < segBtns.length; i++) {
                segBtns[i].classList.toggle('on', segBtns[i].dataset.tema === atual);
            }
        }
        refreshTema();

        for (var i = 0; i < segBtns.length; i++) {
            segBtns[i].addEventListener('click', function () {
                var t = this.dataset.tema;
                try { localStorage.setItem('hhtec_tema', t); } catch (e) {}
                document.documentElement.classList.toggle('hhtec-dark', t === 'escuro');
                refreshTema();
                // recria a onda na paleta do tema novo (DOURADA no escuro)
                desligarOnda();
                if (pref3d()) initWave(true);
            });
        }
    })();
})();
