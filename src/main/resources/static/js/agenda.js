import {
  $,
  api,
  mensaje,
  el,
  fechaISO,
  fechaVisible,
  opcion,
} from "./agenda-common.js";
const lima = new Intl.DateTimeFormat("en-CA", {
  timeZone: "America/Lima",
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
}).formatToParts(new Date());
const parte = (t) => lima.find((p) => p.type === t).value;
const hoy = `${parte("year")}-${parte("month")}-${parte("day")}`;
let seleccionado = hoy,
  mes = new Date(Number(parte("year")), Number(parte("month")) - 1, 1),
  citas = [],
  servicios = [],
  actual = null,
  peticion = 0;
function pintar() {
  $("mes").textContent = new Intl.DateTimeFormat("es-PE", {
    month: "long",
    year: "numeric",
  }).format(mes);
  const primero = new Date(mes.getFullYear(), mes.getMonth(), 1),
    desfase = (primero.getDay() + 6) % 7;
  $("calendario").replaceChildren();
  for (let i = 0; i < 42; i++) {
    const d = new Date(mes.getFullYear(), mes.getMonth(), 1 - desfase + i),
      iso = fechaISO(d),
      lista = citas.filter((c) => c.inicio.startsWith(iso));
    const b = el(
      "button",
      undefined,
      "calendar-day" +
        (iso === hoy ? " today" : "") +
        (iso === seleccionado ? " selected" : "") +
        (d.getMonth() !== mes.getMonth() ? " outside" : ""),
    );
    b.setAttribute("aria-label", `${fechaVisible(iso)}, ${lista.length} citas`);
    b.setAttribute("aria-pressed", String(iso === seleccionado));
    b.append(el("b", String(d.getDate())));
    if (lista.length)
      b.append(
        el("small", `${lista.length} cita${lista.length === 1 ? "" : "s"}`),
      );
    b.onclick = () => {
      seleccionado = iso;
      pintar();
    };
    $("calendario").append(b);
  }
  $("dia").textContent = fechaVisible(seleccionado);
  const lista = citas.filter((c) => c.inicio.startsWith(seleccionado));
  $("resumen").textContent =
    `${lista.length} citas · ${lista.filter((c) => c.estado !== "CANCELADA" && c.estado !== "ATENDIDA").length} pendientes`;
  $("citas").replaceChildren();
  if (!lista.length)
    $("citas").append(
      el(
        "p",
        "No hay citas en este día. Selecciona «Nueva cita» para reservar un horario.",
        "empty-agenda",
      ),
    );
  for (const c of lista) {
    const card = el("article", undefined, "appointment");
    card.append(
      el(
        "div",
        c.inicio.slice(11, 16) + " – " + c.fin.slice(11, 16),
        "appointment-time",
      ),
      el("h3", c.paciente),
      el("p", c.servicio, "muted"),
      el("span", c.estado, "badge " + c.estado),
      el("p", c.motivo),
    );
    if (c.profesional) card.append(el("p", c.profesional, "muted"));
    const b = el("button", "Ver / editar cita", "button secondary");
    b.onclick = () => editar(c).catch((e) => mensaje(e.message, true));
    card.append(b);
    $("citas").append(card);
  }
}
async function cargar() {
  const turno = ++peticion;
  const primero = new Date(mes.getFullYear(), mes.getMonth(), 1),
    desfase = (primero.getDay() + 6) % 7;
  const desde = new Date(mes.getFullYear(), mes.getMonth(), 1 - desfase),
    hasta = new Date(mes.getFullYear(), mes.getMonth(), 43 - desfase);
  const resultado = await api(
    `/api/agenda?desde=${fechaISO(desde)}&hasta=${fechaISO(hasta)}`,
  );
  if (turno !== peticion) return;
  citas = resultado;
  pintar();
}
async function buscar() {
  const lista = await api(
    "/api/agenda/pacientes?q=" + encodeURIComponent($("busqueda").value),
  );
  const elegido = $("paciente").selectedOptions[0];
  $("paciente").replaceChildren(opcion("", "Selecciona un paciente"));
  if (elegido?.value && !lista.some((p) => String(p.id) === elegido.value))
    $("paciente").append(elegido);
  for (const p of lista)
    $("paciente").append(
      opcion(p.id, `${p.nombre} · HC ${p.id} · ${p.dni || "Sin DNI"}`),
    );
  if (elegido?.value) $("paciente").value = elegido.value;
}
async function editar(c) {
  actual = c;
  servicios = await api("/api/catalogo");
  $("formulario").reset();
  mensaje("", false, "error-editor");
  $("titulo-editor").textContent = c ? "Editar cita" : "Nueva cita";
  $("paciente").replaceChildren(opcion("", "Selecciona un paciente"));
  if (c) {
    $("paciente").append(
      opcion(c.historiaId, c.paciente + " · HC " + c.historiaId),
    );
    $("paciente").value = c.historiaId;
  }
  $("servicio").replaceChildren(
    opcion("", "Consulta / sin servicio específico"),
  );
  for (const s of servicios.filter((s) => s.activo || s.id === c?.servicioId))
    $("servicio").append(
      opcion(
        s.id,
        `${s.nombre} · ${s.duracionMinutos} min${s.activo ? "" : " (inactivo)"}`,
      ),
    );
  $("servicio").value = c?.servicioId || "";
  $("fecha").value = c ? c.inicio.slice(0, 10) : seleccionado;
  $("inicio").value = c ? c.inicio.slice(11, 16) : "09:00";
  $("fin").value = c ? c.fin.slice(11, 16) : "09:30";
  $("estado").value = c?.estado || "PROGRAMADA";
  $("motivo").value = c?.motivo || "";
  $("profesional").value = c?.profesional || "";
  $("notas").value = c?.notas || "";
  $("editor").showModal();
  if (!c) await buscar();
}
function duracion() {
  const s = servicios.find((s) => s.id === Number($("servicio").value));
  if (!s || !$("inicio").value) return;
  const [h, m] = $("inicio").value.split(":").map(Number),
    total = h * 60 + m + s.duracionMinutos;
  if (total >= 1440) {
    mensaje(
      "El servicio terminaría al día siguiente. Elige otro horario.",
      true,
      "error-editor",
    );
    return;
  }
  $("fin").value =
    String(Math.floor(total / 60)).padStart(2, "0") +
    ":" +
    String(total % 60).padStart(2, "0");
  if (!$("motivo").value) $("motivo").value = s.nombre;
}
$("servicio").onchange = duracion;
$("inicio").onchange = duracion;
$("buscar").onclick = () =>
  buscar().catch((e) => mensaje(e.message, true, "error-editor"));
$("busqueda").onkeydown = (e) => {
  if (e.key === "Enter") {
    e.preventDefault();
    $("buscar").click();
  }
};
$("nueva").onclick = () => editar(null).catch((e) => mensaje(e.message, true));
$("cerrar").onclick = () => $("editor").close();
for (const [id, delta] of [
  ["anterior", -1],
  ["siguiente", 1],
])
  $(id).onclick = () => {
    mes = new Date(mes.getFullYear(), mes.getMonth() + delta, 1);
    seleccionado = fechaISO(mes);
    cargar().catch((e) => mensaje(e.message, true));
  };
$("hoy").onclick = () => {
  seleccionado = hoy;
  mes = new Date(Number(parte("year")), Number(parte("month")) - 1, 1);
  cargar().catch((e) => mensaje(e.message, true));
};
$("formulario").onsubmit = async (e) => {
  e.preventDefault();
  $("guardar").disabled = true;
  try {
    await api(
      "/api/agenda" + (actual ? "/" + actual.id : ""),
      {
        version: actual?.version ?? 0,
        historiaId: Number($("paciente").value),
        servicioId: $("servicio").value ? Number($("servicio").value) : null,
        inicio: $("fecha").value + "T" + $("inicio").value + ":00",
        fin: $("fecha").value + "T" + $("fin").value + ":00",
        estado: $("estado").value,
        motivo: $("motivo").value.trim(),
        profesional: $("profesional").value,
        notas: $("notas").value,
      },
      actual ? "PUT" : "POST",
    );
    seleccionado = $("fecha").value;
    const [a, m] = seleccionado.split("-").map(Number);
    mes = new Date(a, m - 1, 1);
    $("editor").close();
    mensaje("Cita guardada.");
    await cargar();
  } catch (error) {
    mensaje(error.message, true, "error-editor");
  } finally {
    $("guardar").disabled = false;
  }
};
cargar().catch((e) => mensaje(e.message, true));
