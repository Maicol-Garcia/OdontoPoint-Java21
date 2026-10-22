/**
 * OdontoPoint: modelo paramétrico ilustrativo, sin archivos ni servicios externos.
 * Coordenadas locales de cada corona: X tangente, Y hacia la mordida,
 * Z vestibular. Cada triángulo pertenece a una superficie V/L/M/D/O.
 * No es un modelo anatómico de diagnóstico ni una reconstrucción del paciente.
 */
import * as THREE from "./vendor/three.module.js";

export const SURFACES = ["V", "L", "M", "D", "O"];
export const MARK_COLORS = { NEGRO: 0x232b36, AMARILLO: 0xf2bd27, VERDE: 0x28a46f };
const ivory = 0xf5ecd9;
const gumMaterial = new THREE.MeshPhysicalMaterial({
  color: 0xb95e6c, roughness: 0.46, metalness: 0, clearcoat: 0.24, clearcoatRoughness: 0.4,
  side: THREE.DoubleSide
});
const palateMaterial = new THREE.MeshPhysicalMaterial({
  color: 0xc67881, roughness: 0.56, clearcoat: 0.12
});
const toothNames = ["", "Incisivo central", "Incisivo lateral", "Canino",
  "Primer premolar", "Segundo premolar", "Primer molar", "Segundo molar", "Tercer molar"];

export function describeTooth(number) {
  const q = Math.floor(number / 10), p = number % 10, primary = q > 4;
  const name = primary && p >= 4 ? (p === 4 ? "Primer molar" : "Segundo molar") : toothNames[p];
  return name + " " + ([1, 2, 5, 6].includes(q) ? "superior" : "inferior") + " " +
    ([1, 4, 5, 8].includes(q) ? "derecho" : "izquierdo") + (primary ? " · temporal" : "");
}
function gaussian(x, z, cx, cz, width) {
  return Math.exp(-((x - cx) ** 2 + (z - cz) ** 2) / width);
}
function dimensions(position, upper, primary) {
  const molar = primary ? position >= 4 : position >= 6;
  const premolar = !primary && (position === 4 || position === 5);
  const canine = position === 3;
  const scale = primary ? 0.85 : 1;
  const width = (molar ? 1.2 : premolar ? 0.91 : canine ? 0.86 : position === 1 && upper ? 0.96 : 0.76) * scale;
  return { molar, premolar, canine, width, depth: (molar ? 1.12 : premolar ? 0.98 : canine ? 0.69 : 0.56) * scale,
    height: (molar ? 0.92 : premolar ? 1.05 : canine ? 1.38 : 1.23) * scale };
}
// Surface changes color without painting the gingiva or losing the other faces.
function crownGeometry(d, right) {
  const segments = 48, rings = 16, topRings = 12;
  const vertices = [], groups = SURFACES.map(() => []), uv = [];
  const halfW = d.width / 2, halfD = d.depth / 2;
  const exponent = d.molar || d.premolar ? 0.64 : 0.80;
  const signedPow = (v) => Math.sign(v) * Math.abs(v) ** exponent;
  function shape(a) { return [signedPow(Math.cos(a)) * halfW, signedPow(Math.sin(a)) * halfD]; }
  function top(x, z) {
    const nx = x / halfW, nz = z / halfD;
    if (d.molar || d.premolar) {
      let relief = 0;
      const cuspXs = d.molar ? [-0.55, 0.52] : [0];
      for (const cx of cuspXs) for (const cz of [-0.54, 0.52])
        relief += gaussian(nx, nz, cx, cz, d.molar ? 0.19 : 0.27) * 0.24;
      // Rounded cusps surrounding a shallow central occlusal groove.
      return d.height * (0.83 + relief - 0.055 * Math.exp(-nx * nx / 0.06) * Math.exp(-nz * nz / 0.8));
    }
    if (d.canine) return d.height * (0.83 + 0.21 * gaussian(nx, nz, 0, 0, 0.4));
    return d.height * (0.99 - 0.075 * nx * nx - 0.13 * nz * nz + 0.015 * Math.cos(nx * 9));
  }
  function sideSurface(a) {
    const x = Math.cos(a), z = Math.sin(a);
    if (Math.abs(z) > Math.abs(x)) return z > 0 ? 0 : 1;
    return (x > 0) === right ? 2 : 3;
  }
  for (let j = 0; j <= rings; j++) {
    const t = j / rings;
    // A cervical constriction and a gentle vestibular bulge, rather than cubes.
    const profile = 0.76 + 0.25 * Math.sin(t * Math.PI * 0.78);
    for (let i = 0; i < segments; i++) {
      const a = i * Math.PI * 2 / segments, [x, z] = shape(a);
      const incisalDepth = !d.molar && !d.premolar ? 1 - 0.47 * t ** 3 : 1;
      const px = x * profile, pz = z * profile * incisalDepth;
      vertices.push(px, top(px, pz) * t, pz);
      uv.push(i / segments, t);
      if (j < rings) {
        const a0 = j * segments + i, b = j * segments + (i + 1) % segments,
          c = a0 + segments, e = b + segments;
        groups[sideSurface(a + Math.PI / segments)].push(a0, c, b, b, c, e);
      }
    }
  }
  // Oclusal/incisal cap shares its boundary with the crown for continuous normals.
  let previous = rings * segments;
  const edgeScale = 0.76 + 0.25 * Math.sin(Math.PI * 0.78);
  const edgeDepth = !d.molar && !d.premolar ? 0.53 : 1;
  for (let r = 1; r <= topRings; r++) {
    const radius = 1 - r / (topRings + 1), current = vertices.length / 3;
    for (let i = 0; i < segments; i++) {
      const [x, z] = shape(i * Math.PI * 2 / segments),
        px = x * edgeScale * radius, pz = z * edgeScale * edgeDepth * radius;
      vertices.push(px, top(px, pz), pz); uv.push(0.5 + px, 0.5 + pz);
      const next = (i + 1) % segments;
      groups[4].push(previous + i, current + i, previous + next, previous + next, current + i, current + next);
    }
    previous = current;
  }
  const center = vertices.length / 3;
  vertices.push(0, top(0, 0), 0); uv.push(0.5, 0.5);
  for (let i = 0; i < segments; i++) groups[4].push(previous + i, center, previous + (i + 1) % segments);
  const g = new THREE.BufferGeometry();
  g.setAttribute("position", new THREE.Float32BufferAttribute(vertices, 3));
  g.setAttribute("uv", new THREE.Float32BufferAttribute(uv, 2));
  g.setIndex(groups.flat());
  let offset = 0;
  groups.forEach((indices, index) => { g.addGroup(offset, indices.length, index); offset += indices.length; });
  g.computeVertexNormals();
  g.computeBoundingSphere();
  return g;
}
function gumGeometry(rx, rz) {
  const positions = [], indices = [], uCount = 120, vCount = 32;
  for (let u = 0; u <= uCount; u++) {
    const angle = -1.69 + u / uCount * 3.38;
    for (let v = 0; v <= vCount; v++) {
      const a = v / vCount * Math.PI * 2, radial = Math.cos(a) * 0.66;
      const scallop = 0.045 * Math.cos(angle * 29) * Math.max(0, Math.sin(a));
      positions.push((rx + radial) * Math.sin(angle), -0.75 + Math.sin(a) * 0.8 + scallop,
        (rz + radial) * Math.cos(angle) - 1.5);
      if (u < uCount && v < vCount) {
        const p = u * (vCount + 1) + v, n = p + vCount + 1;
        indices.push(p, p + 1, n, p + 1, n + 1, n);
      }
    }
  }
  const g = new THREE.BufferGeometry();
  g.setAttribute("position", new THREE.Float32BufferAttribute(positions, 3));
  g.setIndex(indices); g.computeVertexNormals();
  return g;
}
export function buildDentition(primary = false) {
  const root = new THREE.Group(), upperPivot = new THREE.Group(), lowerPivot = new THREE.Group();
  root.add(upperPivot, lowerPivot);
  upperPivot.position.set(0, 1.29, -2.7);
  lowerPivot.position.set(0, -1.29, -2.7);
  const entries = new Map(), pickables = [];
  [true, false].forEach((upper) => {
    const frame = new THREE.Group(), pivot = upper ? upperPivot : lowerPivot;
    frame.position.z = 2.7;
    frame.scale.y = upper ? -1 : 1;
    pivot.add(frame);
    const rx = primary ? 3.45 : upper ? 4.35 : 4.13, rz = primary ? 4.05 : upper ? 5.15 : 4.94;
    const gums = new THREE.Mesh(gumGeometry(rx, rz), gumMaterial);
    gums.castShadow = true; gums.receiveShadow = true; frame.add(gums);
    // Palate and tongue stay behind the teeth; they are never selectable.
    const tissue = new THREE.Mesh(new THREE.SphereGeometry(1, 48, 24), palateMaterial);
    tissue.scale.set(rx - 0.7, upper ? 0.23 : 0.37, rz - 1.12);
    tissue.position.set(0, -0.9, -0.9); frame.add(tissue);
    const count = primary ? 5 : 8, maxAngle = 1.61;
    const ds = Array.from({ length: count }, (_, i) => dimensions(i + 1, upper, primary));
    const total = ds.reduce((sum, d) => sum + d.width, 0);
    [-1, 1].forEach((side) => {
      let elapsed = 0;
      ds.forEach((d, i) => {
        const angle = (elapsed + d.width / 2) / total * maxAngle;
        elapsed += d.width;
        const quadrant = (upper ? side < 0 ? 1 : 2 : side < 0 ? 4 : 3) + (primary ? 4 : 0);
        const number = quadrant * 10 + i + 1, right = [1, 4, 5, 8].includes(quadrant);
        const group = new THREE.Group();
        group.position.set(side * rx * Math.sin(angle), -0.015, rz * Math.cos(angle) - 1.5);
        group.rotation.y = side * angle;
        const materials = SURFACES.map(() => new THREE.MeshPhysicalMaterial({
          color: ivory, roughness: 0.29, metalness: 0, clearcoat: 0.42, clearcoatRoughness: 0.23,
          emissive: 0x000000
        }));
        const tooth = new THREE.Mesh(crownGeometry(d, right), materials);
        tooth.userData.tooth = number; tooth.castShadow = true; tooth.receiveShadow = true;
        group.add(tooth); frame.add(group); pickables.push(tooth);
        // Local label anchors project to screen, with occlusion handled by the viewer.
        const anchor = new THREE.Object3D();
        anchor.position.set(0, d.height * 0.54, d.depth / 2 + 0.18); group.add(anchor);
        entries.set(number, { number, upper, group, tooth, materials, anchor });
      });
    });
  });
  return {
    root, entries, pickables, upperPivot, lowerPivot,
    setOpening(degrees) { lowerPivot.rotation.x = THREE.MathUtils.degToRad(degrees); },
    setJaws(mode) { upperPivot.visible = mode !== "lower"; lowerPivot.visible = mode !== "upper"; },
    updateMarks(marks, selected) {
      entries.forEach(({ number, materials }) => materials.forEach((material, i) => {
        const mark = marks[number + ":" + SURFACES[i]];
        material.color.setHex(MARK_COLORS[mark] ?? ivory);
        material.roughness = mark ? 0.47 : 0.29;
        material.emissive.setHex(number === selected ? 0x0a3339 : 0x000000);
        material.emissiveIntensity = number === selected ? 0.4 : 0;
      }));
    },
    dispose() {
      root.traverse((obj) => {
        if (obj.geometry) obj.geometry.dispose();
        if (Array.isArray(obj.material)) obj.material.forEach((m) => m.dispose());
      });
      // Shared gum/tissue materials remain reusable across dentition changes.
    }
  };
}
