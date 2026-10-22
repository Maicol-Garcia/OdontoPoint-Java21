export const $ = (id) => document.getElementById(id);
export async function api(url, body, method = "POST") {
  const headers = { Accept: "application/json" };
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
    headers[document.querySelector('meta[name="_csrf_header"]').content] =
      document.querySelector('meta[name="_csrf"]').content;
  }
  const r = await fetch(url, {
    method: body === undefined ? "GET" : method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (r.status === 401)
    throw new Error("La sesión expiró. Vuelve a iniciar sesión.");
  const data = await r
    .json()
    .catch(() => ({ message: "No se pudo completar la solicitud." }));
  if (!r.ok)
    throw new Error(data.message || "No se pudo guardar. Revisa los datos.");
  return data;
}
export function mensaje(texto, error = false, id = "mensaje") {
  const e = $(id);
  e.textContent = texto;
  e.hidden = !texto;
  e.classList.toggle("error", error);
}
export function el(tag, texto, clase) {
  const n = document.createElement(tag);
  if (texto !== undefined) n.textContent = texto;
  if (clase) n.className = clase;
  return n;
}
export function fechaISO(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}
export function fechaVisible(s) {
  return s.slice(0, 10).split("-").reverse().join("/");
}
export function opcion(valor, texto) {
  const o = el("option", texto);
  o.value = valor;
  return o;
}
export const dinero = (n) =>
  new Intl.NumberFormat("es-PE", { style: "currency", currency: "PEN" }).format(
    n,
  );
