"use strict";
import { $, el, dinero } from "./agenda-common.js";

const tipos = ["MES", "TRIMESTRE", "ANIO", "PERSONALIZADO"];
const entrada = new URLSearchParams(location.search);
const hoy = new Intl.DateTimeFormat("en-GB", { timeZone: "America/Lima" }).format(new Date());
const partes = hoy.split("/");
let tipo = tipos.includes(entrada.get("tipo")) ? entrada.get("tipo") : "MES";
let pagina = Number(entrada.get("pagina") || 0);
let consultaGuardada = null, pendiente = false, versionSolicitud = 0, abortador, vista;
$("ing-anio").value = entrada.get("anio") || partes[2];
$("ing-mes").value = entrada.get("mes") || Number(partes[1]);
$("ing-trimestre").value = entrada.get("trimestre") || Math.floor((Number(partes[1]) - 1) / 3) + 1;
$("ing-desde").value = entrada.get("desde") || "01/" + partes[1] + "/" + partes[2];
$("ing-hasta").value = entrada.get("hasta") || hoy;
$("ing-busqueda").value = entrada.get("q") || "";

function mensaje(texto, error = false) {
  $("ing-mensaje").textContent = texto;
  $("ing-mensaje").hidden = !texto;
  $("ing-mensaje").classList.toggle("error", error);
}
function campos() {
  const personalizado = tipo === "PERSONALIZADO";
  [["anio", !personalizado], ["mes", tipo === "MES"], ["trimestre", tipo === "TRIMESTRE"],
    ["desde", personalizado], ["hasta", personalizado]].forEach(([id, visible]) => {
    $("ing-" + id + "-campo").hidden = !visible;
    $("ing-" + id).disabled = !visible;
  });
  $("ing-desde").required = personalizado; $("ing-hasta").required = personalizado;
  document.querySelectorAll("[data-ing-tipo]").forEach((boton) => {
    const activo = boton.dataset.ingTipo === tipo;
    boton.classList.toggle("active", activo); boton.setAttribute("aria-pressed", String(activo));
  });
}
function parametros(nuevaPagina) {
  const params = new URLSearchParams({ tipo: tipo, q: $("ing-busqueda").value.trim(),
    pagina: String(nuevaPagina), tamano: "20" });
  if (tipo === "PERSONALIZADO") {
    params.set("desde", $("ing-desde").value.trim()); params.set("hasta", $("ing-hasta").value.trim());
  } else {
    params.set("anio", $("ing-anio").value);
    if (tipo === "MES") params.set("mes", $("ing-mes").value);
    if (tipo === "TRIMESTRE") params.set("trimestre", $("ing-trimestre").value);
  }
  return params;
}
function cambiarFiltros() {
  pendiente = true;
  $("ing-exportar").disabled = true; $("ing-anterior").disabled = true; $("ing-siguiente").disabled = true;
  mensaje("Filtros modificados. Pulsa «Ver ingresos» para actualizar el resumen.");
}
document.querySelectorAll("[data-ing-tipo]").forEach((boton) => boton.addEventListener("click", () => {
  tipo = boton.dataset.ingTipo; campos(); cambiarFiltros();
}));
$("ing-filtros").addEventListener("input", cambiarFiltros);
$("ing-filtros").addEventListener("change", cambiarFiltros);
$("ing-filtros").addEventListener("submit", (event) => { event.preventDefault(); cargar(0); });
$("ing-actualizar").addEventListener("click", () => $("ing-filtros").requestSubmit());
$("ing-anterior").addEventListener("click", () => cargar(Math.max(0, pagina - 1)));
$("ing-siguiente").addEventListener("click", () => cargar(pagina + 1));

async function cargar(nuevaPagina) {
  const numero = ++versionSolicitud;
  abortador?.abort(); abortador = new AbortController();
  const params = parametros(nuevaPagina);
  $("ing-resultados").setAttribute("aria-busy", "true");
  $("ing-consultar").disabled = true; $("ing-exportar").disabled = true;
  $("ing-anterior").disabled = true; $("ing-siguiente").disabled = true;
  mensaje("Consultando los pagos del período…");
  pendiente = false;
  try {
    const respuesta = await fetch("/api/ingresos?" + params, {
      headers: { Accept: "application/json" }, signal: abortador.signal, cache: "no-store"
    });
    if (respuesta.status === 401) throw new Error("La sesión expiró. Vuelve a iniciar sesión.");
    const datos = await respuesta.json().catch(() => ({ message: "No se pudo leer el resumen de ingresos." }));
    if (!respuesta.ok) throw new Error(datos.message || "No se pudo consultar el período.");
    if (numero !== versionSolicitud) return;
    vista = datos; pagina = datos.detalle.pagina; consultaGuardada = new URLSearchParams(params);
    pintar(datos);
    history.replaceState(null, "", "/ingresos?" + params);
    if (pendiente) cambiarFiltros(); else mensaje("");
  } catch (error) {
    if (error.name === "AbortError" || numero !== versionSolicitud) return;
    consultaGuardada = null; vista = null;
    limpiar();
    mensaje(error.message || "No se pudo cargar el resumen. Intenta nuevamente.", true);
  } finally {
    if (numero === versionSolicitud) {
      $("ing-resultados").setAttribute("aria-busy", "false");
      $("ing-consultar").disabled = false;
      $("ing-exportar").disabled = !consultaGuardada || pendiente;
    }
  }
}
function limpiar() {
  ["total", "pagos", "pacientes", "promedio", "comp-actual", "comp-anterior", "diferencia"].forEach((id) => $("ing-" + id).textContent = "—");
  ["comparacion", "comprobantes", "comp-fecha-actual", "comp-fecha-anterior", "variacion", "pagina-info", "actualizado"].forEach((id) => $("ing-" + id).textContent = "");
  $("ing-periodo-titulo").textContent = "Consulta no disponible"; $("ing-periodo-fechas").textContent = "";
  $("ing-grafica").replaceChildren(); $("ing-grafica-lectura").textContent = "Revisa los filtros y vuelve a consultar.";
  $("ing-detalle-subtitulo").textContent = "No se muestran resultados de una consulta anterior.";
  filaVacia("No se pudieron consultar los pagos.");
}
function pintar(datos) {
  const r = datos.resumen, p = datos.periodo;
  $("ing-periodo-titulo").textContent = p.etiqueta.charAt(0).toUpperCase() + p.etiqueta.slice(1);
  $("ing-periodo-fechas").textContent = p.desde + " — " + p.hasta + (datos.busqueda ? " · Búsqueda: " + datos.busqueda : " · Todos los pacientes");
  $("ing-actualizado").textContent = "Actualizado: " + datos.actualizado;
  $("ing-total").textContent = dinero(r.total); $("ing-pagos").textContent = r.pagos;
  $("ing-pacientes").textContent = r.pacientes; $("ing-promedio").textContent = dinero(r.promedio);
  const signo = Number(r.diferencia) > 0 ? "+" : "";
  $("ing-comparacion").textContent = signo + dinero(r.diferencia) + " frente al período anterior";
  $("ing-comprobantes").textContent = r.conComprobante + " con captura · " + r.sinComprobante + " sin captura";
  $("ing-comp-actual").textContent = dinero(r.total); $("ing-comp-anterior").textContent = dinero(r.totalAnterior);
  $("ing-comp-fecha-actual").textContent = p.desde + " — " + p.hasta;
  $("ing-comp-fecha-anterior").textContent = p.anteriorDesde + " — " + p.anteriorHasta;
  $("ing-diferencia").textContent = signo + dinero(r.diferencia);
  $("ing-diferencia").classList.toggle("negativo", Number(r.diferencia) < 0);
  $("ing-variacion").textContent = r.variacionPorcentual === null
    ? Number(r.total) === 0 ? "Ambos períodos sin ingresos." : "Período anterior sin ingresos; variación porcentual sin base."
    : (Number(r.variacionPorcentual) > 0 ? "+" : "") + new Intl.NumberFormat("es-PE", { maximumFractionDigits: 2 }).format(r.variacionPorcentual) + " % respecto al período anterior";
  grafica(datos.serie, p.agrupacion);
  tabla(datos.detalle);
}
function grafica(serie, agrupacion) {
  const root = $("ing-grafica");
  root.replaceChildren(); root.classList.toggle("dias", serie.length > 20);
  const maximo = Math.max(0, ...serie.map((punto) => Number(punto.total)));
  $("ing-grafica-subtitulo").textContent = agrupacion === "MES" ? "Importes agrupados por mes" : "Importes agrupados por día";
  const resumen = (punto) => punto.etiqueta + " · " + dinero(punto.total) + " · " + punto.pagos + (punto.pagos === 1 ? " pago" : " pagos");
  $("ing-grafica-lectura").textContent = maximo > 0
    ? resumen(serie.reduce((a, b) => Number(a.total) >= Number(b.total) ? a : b))
    : "Sin pagos registrados en este período.";
  serie.forEach((punto) => {
    const columna = el("div", undefined, "ingresos-bar-column"), barra = el("button", undefined, "ingresos-bar");
    barra.type = "button"; barra.dataset.cero = String(Number(punto.total) === 0);
    barra.style.height = (maximo ? Math.max(Number(punto.total) > 0 ? 1 : 0, Number(punto.total) / maximo * 190) : 0) + "px";
    barra.setAttribute("aria-label", resumen(punto)); barra.title = resumen(punto);
    const mostrar = () => { $("ing-grafica-lectura").textContent = resumen(punto); };
    barra.addEventListener("mouseenter", mostrar); barra.addEventListener("focus", mostrar); barra.addEventListener("click", mostrar);
    const etiqueta = agrupacion === "DIA" ? punto.etiqueta.slice(0, 2) : punto.etiqueta;
    columna.append(barra, el("span", etiqueta, "ingresos-bar-label")); root.append(columna);
  });
}
function filaVacia(texto) {
  const fila = el("tr"), celda = el("td", texto, "ingresos-empty");
  celda.colSpan = 6; fila.append(celda); $("ing-filas").replaceChildren(fila);
}
function tabla(detalle) {
  const root = $("ing-filas"); root.replaceChildren();
  $("ing-detalle-subtitulo").textContent = detalle.totalRegistros + " pagos encontrados · Del más reciente al más antiguo";
  detalle.pagos.forEach((pago) => {
    const fila = el("tr");
    fila.append(el("td", pago.fecha));
    const paciente = el("td"); paciente.append(el("strong", pago.paciente), el("small", "DNI " + pago.dni)); fila.append(paciente);
    const historia = el("td"), enlace = el("a", pago.historia); enlace.href = "/patients/" + pago.historiaId; historia.append(enlace); fila.append(historia);
    fila.append(el("td", dinero(pago.importe), "ingresos-money"));
    const captura = el("td");
    if (pago.tieneComprobante) {
      const ver = el("a", "Ver captura ↗"); ver.href = "/payments/" + pago.id + "/receipt"; ver.target = "_blank"; ver.rel = "noopener";
      captura.append(ver);
    } else captura.append(el("span", "Sin captura", "ingresos-sin-captura"));
    fila.append(captura);
    const gestion = el("td"), abrir = el("a", "Ver / editar historia →");
    abrir.href = "/patients/" + pago.historiaId; gestion.append(abrir); fila.append(gestion);
    root.append(fila);
  });
  if (!detalle.pagos.length) filaVacia(detalle.totalRegistros ? "Esta página no tiene pagos. Vuelve a la página anterior." : "No hay pagos para este período y esta búsqueda.");
  const primero = detalle.pagos.length ? detalle.pagina * detalle.tamano + 1 : 0;
  const ultimo = detalle.pagos.length ? primero + detalle.pagos.length - 1 : 0;
  $("ing-pagina-info").textContent = primero + "–" + ultimo + " de " + detalle.totalRegistros + " pagos";
  $("ing-anterior").disabled = detalle.pagina === 0 || pendiente;
  $("ing-siguiente").disabled = detalle.pagina + 1 >= detalle.totalPaginas || pendiente;
}
$("ing-exportar").addEventListener("click", async () => {
  if (!consultaGuardada || pendiente) return;
  const filtro = new URLSearchParams(consultaGuardada); filtro.delete("pagina"); filtro.delete("tamano");
  const boton = $("ing-exportar"); boton.disabled = true; boton.textContent = "Preparando Excel…";
  try {
    const respuesta = await fetch("/exports/ingresos.xlsx?" + filtro, { cache: "no-store" });
    if (respuesta.status === 401) throw new Error("La sesión expiró. Vuelve a iniciar sesión.");
    if (!respuesta.ok) {
      const datos = await respuesta.json().catch(() => ({}));
      throw new Error(datos.message || "No se pudo exportar. Intenta con un período menor.");
    }
    const archivo = await respuesta.blob(), url = URL.createObjectURL(archivo), enlace = document.createElement("a");
    enlace.href = url; enlace.download = "OdontoPoint-ingresos.xlsx"; document.body.append(enlace); enlace.click(); enlace.remove();
    setTimeout(() => URL.revokeObjectURL(url), 10000);
    mensaje("Excel descargado: incluye el resumen, la evolución y todos los pagos que coinciden con el filtro.");
  } catch (error) { mensaje(error.message, true); }
  finally { boton.disabled = !consultaGuardada || pendiente; boton.textContent = "↓ Exportar Excel"; }
});
campos(); cargar(Number.isInteger(pagina) && pagina >= 0 ? pagina : 0);
