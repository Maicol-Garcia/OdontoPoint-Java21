import { $, api, mensaje, el, dinero } from "./agenda-common.js";
let servicios = [],
  actual = null;
function pintar() {
  const filtro = $("filtro").value.toLocaleLowerCase(),
    estado = $("estado-filtro").value;
  const lista = servicios.filter(
    (s) =>
      (s.nombre + " " + (s.descripcion || ""))
        .toLocaleLowerCase()
        .includes(filtro) &&
      (estado === "todos" || s.activo === (estado === "activos")),
  );
  $("cantidad").textContent = `${lista.length} servicios`;
  $("servicios").replaceChildren();
  if (!lista.length)
    $("servicios").append(
      el(
        "p",
        "No hay servicios para mostrar. Agrega tu primer servicio o cambia la búsqueda.",
        "empty-agenda",
      ),
    );
  for (const s of lista) {
    const card = el("article", undefined, "card service-card");
    card.append(
      el(
        "span",
        s.activo ? "ACTIVO" : "INACTIVO",
        "badge " + (s.activo ? "ATENDIDA" : "CANCELADA"),
      ),
      el("h2", s.nombre),
      el("p", s.descripcion || "Sin descripción.", "muted"),
      el("strong", dinero(s.precio), "service-price"),
    );
    const pie = el("div", undefined, "service-footer"),
      boton = el("button", "Editar servicio", "button secondary");
    boton.onclick = () => editar(s);
    pie.append(
      el("span", `${s.duracionMinutos} min estimados`, "muted"),
      boton,
    );
    card.append(pie);
    $("servicios").append(card);
  }
}
async function cargar() {
  servicios = await api("/api/catalogo");
  pintar();
}
function editar(s) {
  actual = s;
  $("formulario").reset();
  $("titulo-editor").textContent = s ? "Editar servicio" : "Nuevo servicio";
  mensaje("", false, "error-editor");
  $("nombre").value = s?.nombre || "";
  $("descripcion").value = s?.descripcion || "";
  $("precio").value = s?.precio ?? "";
  $("duracion").value = s?.duracionMinutos ?? 30;
  $("activo").value = String(s?.activo ?? true);
  $("editor").showModal();
}
$("nuevo").onclick = () => editar(null);
$("cerrar").onclick = () => $("editor").close();
$("filtro").oninput = pintar;
$("estado-filtro").onchange = pintar;
$("formulario").onsubmit = async (e) => {
  e.preventDefault();
  $("guardar").disabled = true;
  try {
    await api(
      "/api/catalogo" + (actual ? "/" + actual.id : ""),
      {
        version: actual?.version ?? 0,
        nombre: $("nombre").value.trim(),
        descripcion: $("descripcion").value,
        precio: Number($("precio").value),
        duracionMinutos: Number($("duracion").value),
        activo: $("activo").value === "true",
      },
      actual ? "PUT" : "POST",
    );
    $("editor").close();
    mensaje("Servicio guardado.");
    await cargar();
  } catch (error) {
    mensaje(error.message, true, "error-editor");
  } finally {
    $("guardar").disabled = false;
  }
};
cargar().catch((e) => mensaje(e.message, true));
