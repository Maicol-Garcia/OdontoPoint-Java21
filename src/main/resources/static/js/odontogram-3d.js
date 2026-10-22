/**
 * Vista 3D del odontograma. La persistencia pertenece a history.js y al servicio
 * Spring existente. Girar, abrir o cambiar dentición nunca modifica la historia.
 */
import * as THREE from "./vendor/three.module.js";
import { OrbitControls } from "./vendor/OrbitControls.js";
import { RoomEnvironment } from "./vendor/RoomEnvironment.js";
import { buildDentition, describeTooth, SURFACES } from "./dental-model.js";

const $ = (id) => document.getElementById(id);
const container = $("dental-3d");
const host = $("dental-canvas-host");
if (container && host) {
  const labels = $("dental-label-layer"), select = $("dental-tooth-select"),
    status = $("dental-surface-status"), faceSelect = $("dental-surface-select"),
    paletteNames = { NEGRO: "Negro", AMARILLO: "Amarillo", VERDE: "Verde", BORRAR: "Borrar" };
  let renderer, scene, camera, controls, model, environment, resizeObserver;
  let selected = null, started = false, failed = false, suspended = false;
  let currentState = { teeth: {}, color: "NEGRO", wholeTooth: false }, pointerStart;
  let selectedLabelElements = new Map(), reducedMotion = matchMedia("(prefers-reduced-motion: reduce)").matches;
  const pointer = new THREE.Vector2(), raycaster = new THREE.Raycaster();
  const projected = new THREE.Vector3(), world = new THREE.Vector3(), normal = new THREE.Vector3();
  const faceLabels = { V: "Vestibular", L: "Lingual / palatina", M: "Mesial", D: "Distal", O: "Oclusal / incisal" };

  function syncState() {
    currentState = window.OdontoPointDental?.getState() || currentState;
    model?.updateMarks(currentState.teeth, selected);
    updateInspector();
  }
  function updateInspector() {
    $("dental-selected-title").textContent = selected ? "Diente " + selected : "Selecciona un diente";
    $("dental-selected-name").textContent = selected ? describeTooth(selected) : "Haz clic en la dentadura o elige su número.";
    select.value = selected ? String(selected) : "";
    $("dental-apply").disabled = !selected || failed;
    $("dental-apply").textContent = currentState.color === "BORRAR"
      ? currentState.wholeTooth ? "Borrar diente completo" : "Borrar superficie"
      : currentState.wholeTooth ? "Marcar diente completo" : "Aplicar marca";
    $("dental-active-tool").textContent = "Herramienta activa: " + paletteNames[currentState.color];
    status.replaceChildren();
    SURFACES.forEach((face) => {
      const mark = selected ? currentState.teeth[selected + ":" + face] : "";
      const chip = document.createElement("div");
      chip.className = "dental-face" + (faceSelect.value === face ? " active" : "");
      chip.dataset.color = mark || "";
      chip.title = faceLabels[face] + ": " + (paletteNames[mark] || "sin marca");
      chip.textContent = face;
      chip.append(document.createElement("span"));
      status.append(chip);
    });
    selectedLabelElements.forEach((element, number) => element.classList.toggle("selected", number === selected));
  }
  function selectTooth(number, surface) {
    if (!model?.entries.has(Number(number))) return;
    selected = Number(number);
    if (surface) faceSelect.value = surface;
    model.updateMarks(currentState.teeth, selected);
    updateInspector();
  }
  function rebuildModel() {
    if (model) { scene.remove(model.root); model.dispose(); }
    model = buildDentition($("dental-dentition").value === "primary");
    model.setOpening(Number($("dental-opening").value));
    model.setJaws($("dental-jaws").value);
    scene.add(model.root);
    selected = null;
    labels.replaceChildren(); selectedLabelElements = new Map();
    select.replaceChildren(new Option("Seleccionar pieza…", ""));
    [...model.entries.keys()].sort((a, b) => a - b).forEach((number) => {
      select.append(new Option(number + " · " + describeTooth(number), number));
      const label = document.createElement("span");
      label.className = "dental-tooth-label"; label.textContent = number;
      label.hidden = true; labels.append(label); selectedLabelElements.set(number, label);
    });
    syncState();
  }
  function preset(name) {
    if (!camera) return;
    if (name === "occlusal" && $("dental-jaws").value === "both") {
      $("dental-jaws").value = "lower"; model.setJaws("lower");
    }
    const occlusalY = $("dental-jaws").value === "upper" ? -20 : 20;
    const positions = { front: [0, 2.6, 17.5], right: [-18, 3, 3], left: [18, 3, 3], occlusal: [0, occlusalY, 0.01] };
    camera.position.fromArray(positions[name] || positions.front);
    controls.target.set(0, -1.05, 0);
    camera.up.set(0, 1, 0); controls.update();
    document.querySelectorAll("[data-dental-camera]").forEach((b) => b.classList.toggle("active", b.dataset.dentalCamera === name));
  }
  function resize() {
    if (!renderer || !isVisible()) return;
    const width = host.clientWidth, height = host.clientHeight;
    if (width < 1 || height < 1) return;
    renderer.setSize(width, height, false);
    camera.aspect = width / height;
    camera.updateProjectionMatrix();
  }
  function isVisible() {
    return !suspended && !document.hidden && !container.hidden &&
      !container.closest("[data-section]")?.hidden;
  }
  function fail(message, error) {
    failed = true;
    if (renderer) renderer.setAnimationLoop(null);
    $("dental-loading").hidden = true;
    $("dental-3d-error").hidden = false;
    $("dental-3d-error").textContent = message;
    document.dispatchEvent(new CustomEvent("odontopoint:dental-fallback"));
    console.warn("OdontoPoint 3D:", error?.message || message);
  }
  function start() {
    if (started || failed || !isVisible()) return;
    started = true;
    try {
      renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, powerPreference: "default" });
      renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 1.75));
      renderer.outputColorSpace = THREE.SRGBColorSpace;
      renderer.toneMapping = THREE.ACESFilmicToneMapping; renderer.toneMappingExposure = 0.96;
      renderer.shadowMap.enabled = true; renderer.shadowMap.type = THREE.PCFSoftShadowMap;
      const canvas = renderer.domElement;
      canvas.tabIndex = 0; canvas.setAttribute("aria-label", "Dentadura 3D. Arrastra para girar; usa la lista de piezas para seleccionar con teclado.");
      canvas.addEventListener("webglcontextlost", (event) => {
        event.preventDefault();
        fail("La vista 3D perdió la conexión gráfica. Puedes seguir registrando en 2D; recarga la página después de guardar para reintentar.");
      });
      host.prepend(canvas);
      scene = new THREE.Scene();
      camera = new THREE.PerspectiveCamera(36, 1, 0.1, 160);
      camera.position.set(4.8, 3.4, 17.5);
      controls = new OrbitControls(camera, canvas);
      controls.target.set(0, -1.05, 0);
      controls.enableDamping = true; controls.dampingFactor = 0.08;
      controls.minDistance = 8; controls.maxDistance = 42;
      controls.minPolarAngle = 0.02; controls.maxPolarAngle = Math.PI - 0.02;
      controls.autoRotateSpeed = 0.8;
      controls.addEventListener("start", () => setSpin(false));
      const pmrem = new THREE.PMREMGenerator(renderer);
      const room = new RoomEnvironment();
      environment = pmrem.fromScene(room, 0.04);
      scene.environment = environment.texture;
      scene.environmentIntensity = 0.45;
      room.dispose(); pmrem.dispose();
      scene.add(new THREE.HemisphereLight(0xe6f6ff, 0x7f6d6b, 0.75));
      const key = new THREE.DirectionalLight(0xfff1e0, 2.5);
      key.position.set(-6, 12, 14); key.castShadow = true;
      key.shadow.mapSize.set(2048, 2048); key.shadow.camera.left = -10; key.shadow.camera.right = 10;
      key.shadow.camera.top = 10; key.shadow.camera.bottom = -10;
      key.shadow.normalBias = 0.035; key.shadow.bias = -0.0001; scene.add(key);
      const rim = new THREE.DirectionalLight(0xb5d8ef, 1.9); rim.position.set(9, 4, -8); scene.add(rim);
      const fill = new THREE.DirectionalLight(0xffffff, 0.7); fill.position.set(1, -5, 10); scene.add(fill);
      rebuildModel();
      controls.update(); resize();
      resizeObserver = new ResizeObserver(resize); resizeObserver.observe(host);
      canvas.addEventListener("pointerdown", (event) => {
        if (event.button === 0 && event.isPrimary) pointerStart = { x: event.clientX, y: event.clientY, id: event.pointerId };
        else pointerStart = null;
      });
      canvas.addEventListener("pointercancel", () => { pointerStart = null; });
      canvas.addEventListener("pointerup", (event) => {
        const start = pointerStart; pointerStart = null;
        if (!start || start.id !== event.pointerId ||
            Math.hypot(event.clientX - start.x, event.clientY - start.y) > 6) return;
        const rect = canvas.getBoundingClientRect();
        pointer.set((event.clientX - rect.left) / rect.width * 2 - 1, -(event.clientY - rect.top) / rect.height * 2 + 1);
        scene.updateMatrixWorld(true);
        raycaster.setFromCamera(pointer, camera);
        const visibleMeshes = [];
        model.root.traverseVisible((object) => { if (object.isMesh) visibleMeshes.push(object); });
        const hits = raycaster.intersectObjects(visibleMeshes, false);
        // Gingiva and palate occlude teeth too: clicking tissue must not select
        // a hidden crown on the other side of the jaw.
        if (hits[0]?.object.userData.tooth)
          selectTooth(hits[0].object.userData.tooth, SURFACES[hits[0].face.materialIndex]);
      });
      // Rendering pauses when hidden; the model and current camera remain available.
      renderer.setAnimationLoop(() => {
        if (!isVisible()) return;
        controls.update(); scene.updateMatrixWorld(true);
        renderer.render(scene, camera);
        const show = $("dental-labels").checked;
        labels.hidden = !show;
        if (show) model.entries.forEach((entry, number) => {
          const el = selectedLabelElements.get(number);
          const visibleJaw = (entry.upper ? model.upperPivot : model.lowerPivot).visible;
          entry.anchor.getWorldPosition(world);
          normal.set(0, 0, 1).transformDirection(entry.group.matrixWorld);
          const toCamera = projected.copy(camera.position).sub(world).normalize();
          const facing = normal.dot(toCamera) > 0.14;
          projected.copy(world).project(camera);
          const inView = Math.abs(projected.x) < 0.97 && Math.abs(projected.y) < 0.87 && projected.z < 1;
          el.hidden = !visibleJaw || !facing || !inView;
          if (!el.hidden) el.style.transform = "translate(" +
            ((projected.x * 0.5 + 0.5) * host.clientWidth).toFixed(1) + "px," +
            ((-projected.y * 0.5 + 0.5) * host.clientHeight).toFixed(1) + "px) translate(-50%,-50%)";
        });
        host.querySelector(".dental-orientation").hidden = !(camera.position.z > 8 && Math.abs(camera.position.x) < 4);
      });
      $("dental-loading").hidden = true;
    } catch (error) {
      fail("No se pudo activar WebGL en este navegador. Puedes trabajar en el odontograma 2D. Prueba Chrome, Edge o Firefox con aceleración gráfica habilitada.", error);
    }
  }
  function setSpin(enabled) {
    if (!controls) return;
    controls.autoRotate = enabled && !reducedMotion;
    $("dental-spin").setAttribute("aria-pressed", String(controls.autoRotate));
    $("dental-spin").textContent = controls.autoRotate ? "Detener giro" : "Giro automático";
  }
  document.addEventListener("odontopoint:panel", (event) => {
    if (event.detail === "dental") { start(); resize(); }
  });
  document.addEventListener("odontopoint:dental-view", (event) => {
    if (event.detail === "3d") { start(); resize(); }
  });
  document.addEventListener("odontopoint:dental-change", syncState);
  select.addEventListener("change", () => {
    if (select.value) selectTooth(select.value);
    else { selected = null; syncState(); }
  });
  faceSelect.addEventListener("change", updateInspector);
  $("dental-apply").addEventListener("click", () => {
    if (selected && window.OdontoPointDental) window.OdontoPointDental.applyMark(selected, faceSelect.value);
  });
  $("dental-dentition").addEventListener("change", () => { if (model) rebuildModel(); });
  $("dental-jaws").addEventListener("change", () => {
    model?.setJaws($("dental-jaws").value);
    if (selected && !model.entries.get(selected)?.group.parent.parent.visible) { selected = null; syncState(); }
  });
  $("dental-opening").addEventListener("input", () => {
    const degrees = Number($("dental-opening").value);
    $("dental-opening-value").textContent = degrees + "°"; model?.setOpening(degrees);
  });
  document.querySelectorAll("[data-dental-camera]").forEach((button) => button.addEventListener("click", () => {
    setSpin(false); preset(button.dataset.dentalCamera);
  }));
  $("dental-reset").addEventListener("click", () => {
    setSpin(false); preset("front");
    $("dental-opening").value = "20"; $("dental-opening-value").textContent = "20°";
    $("dental-jaws").value = "both"; model?.setOpening(20); model?.setJaws("both");
  });
  $("dental-spin").addEventListener("click", () => setSpin(!controls?.autoRotate));
  $("dental-fullscreen").addEventListener("click", async () => {
    try {
      if (document.fullscreenElement) await document.exitFullscreen();
      else if (container.requestFullscreen) await container.requestFullscreen();
      else throw new Error("No disponible");
    } catch {
      $("dental-3d-error").hidden = false;
      $("dental-3d-error").textContent = "Este navegador no permite pantalla completa. Puedes ampliar la ventana y seguir utilizando el visor.";
    }
  });
  document.addEventListener("fullscreenchange", () => {
    $("dental-fullscreen").textContent = document.fullscreenElement ? "Salir" : "Ampliar";
    resize();
  });
  window.addEventListener("pagehide", (event) => {
    suspended = true;
    // Keep resources for bfcache; otherwise release the WebGL context and buffers.
    if (!event.persisted) {
      renderer?.setAnimationLoop(null); controls?.dispose(); resizeObserver?.disconnect();
      model?.dispose(); environment?.dispose(); renderer?.dispose();
    }
  });
  window.addEventListener("pageshow", () => { suspended = false; resize(); });
  syncState(); start();
}
