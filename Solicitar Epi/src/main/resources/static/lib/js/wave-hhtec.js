// ============================================================
// HHTEC — onda 3D de cartões iridescentes (compartilhada)
// Usada no login e na seleção de módulo.
// Uso:  import { createWave } from '/lib/js/wave-hhtec.js?v=20260703-system-ui';
//       const wave = createWave(canvas, { yOffset: 0 });
//       wave.burst(); // dispersa os cartões (transição de login)
// ============================================================
import * as THREE from '/lib/js/three.module.min.js';

export function createWave(canvas, opts = {}) {
    // Preferência do usuário (botão de configurações): animação 3D desligada.
    // Devolve um objeto inerte — nenhum renderer é criado, custo zero.
    try {
        if (localStorage.getItem('hhtec_anim3d') === '0') {
            return { burst() {}, gather() {}, setScatter() {}, dispose() {} };
        }
    } catch (e) {}

    const cfg = Object.assign({
        count: 96,          // quantidade de cartões
        amplitude: 1.0,     // altura da ondulação
        yOffset: 0,         // desloca a fita verticalmente (unidades de mundo)
        speed: 0.85,        // velocidade da ondulação
        opacity: 1.0,       // opacidade máxima do material
        fade: 0.9,          // quanto os cartões somem ao dispersar (0 = ficam visíveis)
        shape: 'ribbon',    // 'ribbon' (fita ondulada) ou 'circle' (anel giratório)
        radius: 3.1         // raio do anel quando shape === 'circle'
    }, opts);

    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // Resolução limitada a 1.5x: em telas de alta densidade o próprio pixel ratio
    // já disfarça o serrilhado, então o antialias só liga em telas comuns (1x).
    const ratio = Math.min(window.devicePixelRatio || 1, 1.5);
    const renderer = new THREE.WebGLRenderer({
        canvas,
        alpha: true,
        antialias: ratio < 1.5,
        powerPreference: 'high-performance'
    });
    renderer.setPixelRatio(ratio);

    // marca que este canvas tem uma onda DE VERDADE rodando (o stub não marca) —
    // usado pelo hhtec-fx para saber se pode recriar um canvas inerte ao religar
    canvas.dataset.hhtecWave = '1';
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.outputColorSpace = THREE.SRGBColorSpace;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(40, 1, 0.1, 100);
    camera.position.set(0, 0, 16);

    // tema escuro "Barcelona": onda DOURADA com luzes quentes
    let temaEscuro = false;
    try { temaEscuro = localStorage.getItem('hhtec_tema') === 'escuro'; } catch (e) {}

    scene.add(new THREE.AmbientLight(0xffffff, temaEscuro ? 0.85 : 1.1));
    const sun = new THREE.DirectionalLight(temaEscuro ? 0xfff3d0 : 0xffffff, temaEscuro ? 2.0 : 2.2);
    sun.position.set(4, 8, 6);
    scene.add(sun);
    const luzA = new THREE.PointLight(temaEscuro ? 0xf5d576 : 0xf472b6, 220, 40);
    luzA.position.set(-7, -3, 5);
    scene.add(luzA);
    const luzB = new THREE.PointLight(temaEscuro ? 0xb8860b : 0x5eead4, 200, 40);
    luzB.position.set(7, 3, 4);
    scene.add(luzB);

    const COUNT = cfg.count;
    const geo = new THREE.BoxGeometry(1.05, 1.5, 0.05);
    const mat = new THREE.MeshPhysicalMaterial({
        roughness: 0.25,
        metalness: 0.15,
        clearcoat: 1,
        clearcoatRoughness: 0.2,
        transparent: true,
        opacity: cfg.opacity
    });

    const mesh = new THREE.InstancedMesh(geo, mat, COUNT);
    mesh.instanceMatrix.setUsage(THREE.DynamicDrawUsage);
    const group = new THREE.Group();
    group.add(mesh);
    group.position.y = cfg.yOffset;
    scene.add(group);

    // paleta: claro = rosa→magenta→violeta→teal | escuro = tons de OURO
    const stops = (temaEscuro
        ? ['#f7e08a', '#e9cc6a', '#d4af37', '#e3bf5a', '#b8860b', '#caa53d', '#8a6a1f']
        : ['#f9a8d4', '#ec4899', '#e879f9', '#a78bfa', '#8b5cf6', '#818cf8', '#5eead4'])
        .map(c => new THREE.Color(c));

    const colorAt = (t) => {
        const f = t * (stops.length - 1);
        const i = Math.min(Math.floor(f), stops.length - 2);
        return stops[i].clone().lerp(stops[i + 1], f - i);
    };

    const tmp = new THREE.Color();
    for (let i = 0; i < COUNT; i++) {
        tmp.copy(colorAt(i / (COUNT - 1)));
        tmp.offsetHSL((Math.random() - 0.5) * 0.03, 0, (Math.random() - 0.5) * 0.08);
        mesh.setColorAt(i, tmp);
    }
    mesh.instanceColor.needsUpdate = true;

    const scatter = [];
    for (let i = 0; i < COUNT; i++) {
        scatter.push({
            y: (Math.random() - 0.5) * 12,
            z: (Math.random() - 0.5) * 7,
            r: (Math.random() - 0.5) * 3
        });
    }

    const dummy = new THREE.Object3D();
    let spreadX = 24;
    let mouseX = 0, mouseY = 0;
    let s = 0;              // fator de dispersão atual (0 = fita, 1 = explodido)
    let sTarget = 0;        // para onde o fator caminha

    function resize() {
        const w = canvas.clientWidth || window.innerWidth;
        const h = canvas.clientHeight || window.innerHeight;
        renderer.setSize(w, h, false);
        camera.aspect = w / h;
        camera.updateProjectionMatrix();
        spreadX = 2 * camera.position.z * Math.tan(THREE.MathUtils.degToRad(camera.fov / 2)) * camera.aspect * 1.25;
    }
    resize();
    window.addEventListener('resize', resize);

    window.addEventListener('pointermove', (e) => {
        mouseX = (e.clientX / window.innerWidth) * 2 - 1;
        mouseY = (e.clientY / window.innerHeight) * 2 - 1;
    }, { passive: true });

    function layout(time) {
        const eased = s * s * (3 - 2 * s);

        for (let i = 0; i < COUNT; i++) {
            const t = i / (COUNT - 1);

            if (cfg.shape === 'circle') {
                // anel giratório: cartões tangentes a um círculo que respira
                const ang = t * Math.PI * 2 + time * 0.22;
                const wob = Math.sin(ang * 3 + time * 0.9) * 0.35 * cfg.amplitude;
                const R = cfg.radius * (1 + eased * 1.9) + wob;

                let x = Math.cos(ang) * R * 1.15 + eased * scatter[i].y * 0.6;
                let y = Math.sin(ang) * R * 0.95 + eased * scatter[i].z * 0.6;
                let z = Math.cos(ang * 2 + time * 0.6) * 0.6 + eased * scatter[i].z;

                dummy.position.set(x, y, z);
                dummy.rotation.set(
                    Math.cos(ang + time * 0.5) * 0.3 + eased * scatter[i].r * 0.7,
                    Math.sin(ang + time * 0.4) * 0.35 + eased * scatter[i].r,
                    ang + Math.PI / 2 + eased * scatter[i].r * 0.5
                );
            } else {
                // fita ondulada (padrão)
                const x = (t - 0.5) * spreadX * (1 + eased * 0.6);

                const ph = x * 0.55 + time * cfg.speed;
                let y = Math.sin(ph) * 1.15 * cfg.amplitude + Math.sin(ph * 0.37 + 2.1) * 0.6 * cfg.amplitude;
                let z = Math.cos(ph * 0.5) * 0.7;

                y += eased * scatter[i].y;
                z += eased * scatter[i].z;

                dummy.position.set(x, y, z);
                dummy.rotation.set(
                    -0.14 + Math.cos(ph * 0.8) * 0.12 + eased * scatter[i].r * 0.7,
                    Math.cos(ph) * 0.5 + eased * scatter[i].r,
                    Math.sin(ph * 0.5) * 0.2 + eased * scatter[i].r * 0.5
                );
            }

            dummy.updateMatrix();
            mesh.setMatrixAt(i, dummy.matrix);
        }
        mesh.instanceMatrix.needsUpdate = true;

        mat.opacity = cfg.opacity * (1 - eased * cfg.fade);

        group.rotation.y += ((mouseX * 0.10) - group.rotation.y) * 0.04;
        group.rotation.x += ((-mouseY * 0.06) - group.rotation.x) * 0.04;
    }

    const clock = new THREE.Clock();

    if (reduceMotion) {
        layout(2.5);
        renderer.render(scene, camera);
    } else {
        let ocioso = false;
        renderer.setAnimationLoop(() => {
            if (document.hidden) return;

            // easing independente de frame rate: mesma velocidade a 30, 60 ou 120fps
            const dt = Math.min(clock.getDelta(), 0.1);
            s += (sTarget - s) * Math.min(1, dt * 3.5);

            // totalmente disperso E apagado: para de desenhar (estilo Almoxarifado —
            // rolar a página fica sem nenhum custo de GPU)
            const sumido = sTarget >= 0.999 && s > 0.985 && cfg.fade >= 0.85;
            if (sumido) {
                if (!ocioso) { renderer.clear(); ocioso = true; }
                return;
            }
            ocioso = false;

            layout(clock.elapsedTime);
            renderer.render(scene, camera);
        });
    }

    return {
        // dispersa os cartões (usado na transição do login)
        burst() { sTarget = 1; },
        // junta de volta em fita
        gather() { sTarget = 0; },
        // controle fino da dispersão (0 = fita fechada, 1 = toda aberta)
        setScatter(v) { sTarget = Math.min(Math.max(v, 0), 1); },
        dispose() {
            renderer.setAnimationLoop(null);
            geo.dispose();
            mat.dispose();
            renderer.dispose();
            delete canvas.dataset.hhtecWave;
        }
    };
}
