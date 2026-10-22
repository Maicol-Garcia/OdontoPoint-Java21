"use strict";
(() => {
  const $ = (s, r = document) => r.querySelector(s),
    $$ = (s, r = document) => [...r.querySelectorAll(s)];
  let id = document.body.dataset.historyId || null,
    version = 0,
    dirty = false,
    ready = false,
    busy = false,
    teeth = {},
    treatments = [],
    payments = [],
    undo = [],
    color = "NEGRO",
    timer;
  const csrf = $('meta[name="_csrf"]').content,
    csrfHeader = $('meta[name="_csrf_header"]').content;
  const money = (n) => "S/ " + Number(n).toFixed(2),
    today = () =>
      new Intl.DateTimeFormat("en-GB", { timeZone: "America/Lima" })
        .format(new Date());
  function toast(text, error = false) {
    const n = $("#toast");
    n.textContent = text;
    n.classList.toggle("error", error);
    n.hidden = false;
    clearTimeout(timer);
    timer = setTimeout(() => (n.hidden = true), error ? 10000 : 5000);
  }
  async function request(url, options = {}) {
    const r = await fetch(url, {
        ...options,
        headers: { [csrfHeader]: csrf, ...options.headers },
      }),
      b = await r.json().catch(() => ({}));
    if (!r.ok)
      throw Error(
        b.message ||
          "No se pudo completar la operación. Revisa tu sesión y los datos.",
      );
    return b;
  }
  function changed() {
    if (ready) dirty = true;
  }
  function panel(key) {
    $$(".tab").forEach((t) => {
      t.classList.toggle("active", t.dataset.panel === key);
      t.setAttribute("aria-selected", String(t.dataset.panel === key));
    });
    $$("[data-section]").forEach((n) => (n.hidden = n.dataset.section !== key));
    document.dispatchEvent(new CustomEvent("odontopoint:panel", { detail: key }));
  }
  $$(".tab").forEach((t) =>
    t.addEventListener("click", () => panel(t.dataset.panel)),
  );
  $$(".form-field,[data-condition]").forEach((i) =>
    i.addEventListener("input", changed),
  );
  window.addEventListener("beforeunload", (e) => {
    if (dirty) {
      e.preventDefault();
      e.returnValue = "";
    }
  });
  $("#fullName").addEventListener(
    "input",
    () =>
      ($("#patient-title").textContent =
        $("#fullName").value || "Registro del paciente"),
  );
  function age() {
    const m = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec($("#birthDate").value);
    let a = "";
    if (m) {
      const [, d, mo, y] = m.map(Number),
        date = new Date(y, mo - 1, d),
        [td, tm, ty] = today().split("/").map(Number);
      if (date.getDate() === d && date.getMonth() === mo - 1)
        a = ty - y - (tm < mo || (tm === mo && td < d) ? 1 : 0);
    }
    $("#age").value = a === "" || a < 0 || a > 125 ? "" : a + " años";
    const minor = a !== "" && a >= 0 && a < 18;
    ["guardianName", "guardianDni", "guardianPhone"].forEach(
      (k) => ($("#" + k).required = minor),
    );
    $("#guardian-note").textContent = minor
      ? "Paciente menor de edad: los datos del apoderado son obligatorios."
      : "Completa estos datos si el paciente es menor de 18 años.";
  }
  $("#birthDate").addEventListener("input", age);
  function form() {
    const cleanPayments = payments.map((p) => ({
      id: p.id ?? null,
      date: p.date,
      amount: p.amount,
      signer: p.signer || "",
    }));
    const f = { version, conditions: {}, teeth, treatments, payments: cleanPayments };
    $$(".form-field").forEach((i) => (f[i.name] = i.value.trim()));
    $$("[data-condition]").forEach(
      (i) => (f.conditions[i.dataset.condition] = i.value),
    );
    return f;
  }
  function populate(v) {
    id = String(v.id);
    version = v.version;
    $$(".form-field").forEach((i) => (i.value = v.form[i.name] ?? ""));
    $$("[data-condition]").forEach(
      (i) =>
        (i.value = v.form.conditions[i.dataset.condition] || "No informado"),
    );
    teeth = { ...v.form.teeth };
    treatments = [...v.form.treatments];
    payments = [...v.form.payments];
    undo = [];
    $("#history-number").textContent = v.number;
    $("#patient-title").textContent = v.form.fullName;
    $("#history-date").textContent =
      "Registro: " + v.createdAt + " · Actualización: " + v.updatedAt;
    $("#export-pdf").href = "/exports/patients/" + id + ".pdf";
    $("#export-excel").href = "/exports/patients/" + id + ".xlsx";
    age();
    chart();
    finance();
    dirty = false;
  }
  $("#history-form").noValidate = true;
  $("#history-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    // Solo el botón Guardar historia puede enviar este formulario.
    // Esto evita que controles auxiliares (evidencias/documentos) disparen
    // accidentalmente el guardado general del paciente.
    if (e.submitter && e.submitter.id !== "save-history") return;
    if (busy) return;
    const invalid = $$(".form-field").find((i) => !i.checkValidity());
    if (invalid) {
      panel(invalid.closest("[data-section]").dataset.section);
      invalid.reportValidity();
      return;
    }
    busy = true;
    $("#save-history").disabled = true;
    try {
      const pendingReceipts = payments.map((p) => p._receiptFile || null);
      const v = await request(id ? "/api/patients/" + id : "/api/patients", {
        method: id ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(form()),
      });
      id = String(v.id);
      history.replaceState(null, "", "/patients/" + id);

      let receiptUploadError = null;
      for (let i = 0; i < pendingReceipts.length; i++) {
        const file = pendingReceipts[i];
        const savedPayment = v.form.payments[i];
        if (!file || !savedPayment?.id) continue;
        const data = new FormData();
        data.append("file", file);
        try {
          await request("/api/payments/" + savedPayment.id + "/receipt", {
            method: "POST",
            body: data,
          });
        } catch (uploadError) {
          receiptUploadError = uploadError;
          break;
        }
      }

      populate(await request("/api/patients/" + id));
      if (receiptUploadError) {
        toast(
          "La historia se guardó, pero no se pudo adjuntar una captura de pago: " +
            receiptUploadError.message,
          true,
        );
      } else {
        toast("Historia " + v.number + " guardada correctamente.");
      }
      await documents();
      await evidences();
    } catch (err) {
      toast(err.message, true);
    } finally {
      busy = false;
      $("#save-history").disabled = false;
    }
  });
  [$("#export-pdf"), $("#export-excel")].forEach((a) =>
    a.addEventListener("click", (e) => {
      if (!id || dirty) {
        e.preventDefault();
        toast("Guarda los cambios antes de exportar.", true);
      }
    }),
  );
  const rows = [
    [18, 17, 16, 15, 14, 13, 12, 11, 21, 22, 23, 24, 25, 26, 27, 28],
    [55, 54, 53, 52, 51, 61, 62, 63, 64, 65],
    [85, 84, 83, 82, 81, 71, 72, 73, 74, 75],
    [48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38],
  ];
  function dentalState() {
    return { teeth: { ...teeth }, color, wholeTooth: $("#whole-tooth").checked };
  }
  function notifyDental() {
    document.dispatchEvent(new CustomEvent("odontopoint:dental-change", { detail: dentalState() }));
  }
  function applyMark(tooth, surface) {
    const t = Number(tooth), quadrant = Math.floor(t / 10), position = t % 10;
    if (quadrant < 1 || quadrant > 8 || position < 1 || position > (quadrant < 5 ? 8 : 5)
      || !["V", "L", "M", "D", "O"].includes(surface)) return;
    undo.push({ ...teeth });
    if (undo.length > 100) undo.shift();
    ($("#whole-tooth").checked ? ["V", "M", "L", "D", "O"] : [surface]).forEach((face) => {
      if (color === "BORRAR") delete teeth[t + ":" + face];
      else teeth[t + ":" + face] = color;
    });
    changed();
    chart();
  }
  window.OdontoPointDental = Object.freeze({ getState: dentalState, applyMark });
  let dentalView = "3d";
  function setDentalView(view) {
    dentalView = view;
    $("#dental-3d").hidden = view !== "3d";
    $("#odontogram").hidden = view !== "2d";
    ["3d", "2d"].forEach((v) => {
      const button = $("#dental-view-" + v);
      button.classList.toggle("active", v === view);
      button.setAttribute("aria-pressed", String(v === view));
    });
    document.dispatchEvent(new CustomEvent("odontopoint:dental-view", { detail: view }));
  }
  $("#dental-view-3d").addEventListener("click", () => setDentalView("3d"));
  $("#dental-view-2d").addEventListener("click", () => setDentalView("2d"));
  $("#whole-tooth").addEventListener("change", notifyDental);
  // Also used by the 3D renderer when WebGL is unavailable.
  document.addEventListener("odontopoint:dental-fallback", () => setDentalView("2d"));
  function svg(tag, attrs, parent) {
    const n = document.createElementNS("http://www.w3.org/2000/svg", tag);
    Object.entries(attrs).forEach(([k, v]) => n.setAttribute(k, v));
    parent.append(n);
    return n;
  }
  function chart() {
    const root = $("#odontogram");
    root.replaceChildren();
    const s = svg(
      "svg",
      {
        viewBox: "0 0 1020 464",
        role: "group",
        "aria-label": "Odontograma de 52 dientes",
      },
      root,
    );
    svg("text", { x: 28, y: 18, class: "jaw-label" }, s).textContent =
      "DERECHA DEL PACIENTE";
    svg("text", { x: 804, y: 18, class: "jaw-label" }, s).textContent =
      "IZQUIERDA DEL PACIENTE";
    svg("line", { x1: 510, x2: 510, y1: 35, y2: 447, stroke: "#e1ebe6" }, s);
    const points = [
      "5,0 49,0 37,12 17,12",
      "49,0 49,42 37,30 37,12",
      "49,42 5,42 17,30 37,30",
      "5,42 5,0 17,12 17,30",
      "17,12 37,12 37,30 17,30",
    ];
    rows.forEach((r, ri) =>
      r.forEach((t, c) => {
        const x = (1020 - r.length * 59) / 2 + c * 59,
          y = 40 + ri * 100;
        svg(
          "text",
          { x: x + 19, y: y + 10, class: "tooth-number" },
          s,
        ).textContent = t;
        const g = svg("g", { transform: `translate(${x} ${y + 24})` }, s);
        svg(
          "path",
          { d: "M10 38 C14 62 20 66 26 46 C33 66 42 62 44 38Z", class: "root" },
          g,
        );
        const right = [1, 4, 5, 8].includes(Math.floor(t / 10));
        ["V", right ? "M" : "D", "L", right ? "D" : "M", "O"].forEach(
          (surface, i) => {
            const key = t + ":" + surface,
              v = teeth[key],
              p = svg(
                "polygon",
                {
                  points: points[i],
                  class:
                    "surface " +
                    ({ NEGRO: "black", AMARILLO: "yellow", VERDE: "green" }[
                      v
                    ] || ""),
                  tabindex: "0",
                  role: "button",
                  "data-surface": key,
                  "aria-label": `Diente ${t}, ${surface}, ${v || "sin marca"}`,
                },
                g,
              );
            svg("title", {}, p).textContent =
              `Diente ${t} · ${surface} · ${v || "Sin marca"}`;
            const mark = () => {
              applyMark(t, surface);
            };
            p.addEventListener("click", mark);
            p.addEventListener("keydown", (e) => {
              if (["Enter", " "].includes(e.key)) {
                e.preventDefault();
                mark();
              }
            });
          },
        );
      }),
    );
    $("#marks-count").textContent =
      Object.keys(teeth).length + " superficies marcadas";
    notifyDental();
  }
  $$(".color-tool").forEach((b) =>
    b.addEventListener("click", () => {
      color = b.dataset.color;
      $$(".color-tool").forEach((t) => t.classList.toggle("active", t === b));
      notifyDental();
    }),
  );
  $("#undo-tooth").addEventListener("click", () => {
    if (undo.length) {
      teeth = undo.pop();
      changed();
      chart();
    }
  });
  function cell(r, v) {
    const c = document.createElement("td");
    c.textContent = v ?? "";
    r.append(c);
    return c;
  }
  function remove(r, callback) {
    const b = document.createElement("button");
    b.type = "button";
    b.className = "remove-row";
    b.textContent = "Quitar";
    b.addEventListener("click", () => {
      if (confirm("¿Quitar esta fila del formulario?")) callback();
    });
    cell(r, "").append(b);
  }
  function finance() {
    const tr = $("#treatment-rows"),
      pr = $("#payment-rows");
    tr.replaceChildren();
    pr.replaceChildren();
    treatments.forEach((t, i) => {
      const r = document.createElement("tr");
      cell(r, t.description);
      cell(r, money(t.amount));
      remove(r, () => {
        treatments.splice(i, 1);
        changed();
        finance();
      });
      tr.append(r);
    });
    payments.forEach((p, i) => {
      const r = document.createElement("tr");
      cell(r, p.date);
      cell(r, money(p.amount));
      const receiptCell = document.createElement("td");
      if (p._receiptFile) {
        const pending = document.createElement("span");
        pending.className = "receipt-pending";
        pending.textContent = "📎 " + p._receiptFile.name + " · se adjuntará al guardar";
        receiptCell.append(pending);
      } else if (p.hasReceipt && p.id) {
        const preview = document.createElement("a");
        preview.className = "receipt-preview";
        preview.href = "/payments/" + p.id + "/receipt";
        preview.target = "_blank";
        preview.rel = "noopener";
        const img = document.createElement("img");
        img.src = preview.href;
        img.alt = "Captura del comprobante de pago";
        const label = document.createElement("span");
        label.textContent = p.receiptName || "Ver captura";
        preview.append(img, label);
        receiptCell.append(preview);
      } else {
        const empty = document.createElement("span");
        empty.className = "receipt-empty";
        empty.textContent = "Sin captura";
        receiptCell.append(empty);
      }
      r.append(receiptCell);
      cell(r, p.signer || "");
      remove(r, () => {
        payments.splice(i, 1);
        changed();
        finance();
      });
      pr.append(r);
    });
    [
      [tr, treatments, 3, "No hay tratamientos registrados."],
      [pr, payments, 5, "No hay pagos registrados."],
    ].forEach(([table, list, cols, text]) => {
      if (!list.length) {
        const r = document.createElement("tr");
        cell(r, text).colSpan = cols;
        table.append(r);
      }
    });
    const t =
        treatments.reduce((s, t) => s + Math.round(Number(t.amount) * 100), 0) /
        100,
      p =
        payments.reduce((s, p) => s + Math.round(Number(p.amount) * 100), 0) /
        100;
    $("#total").textContent = money(t);
    $("#paid").textContent = money(p);
    $("#balance").textContent = money(t - p);
  }
  $("#add-treatment").addEventListener("click", () => {
    $("#treatment-form").reset();
    $("#treatment-dialog").showModal();
  });
  $("#add-payment").addEventListener("click", () => {
    $("#payment-form").reset();
    $("#payment-form [name=date]").value = today();
    $("#payment-dialog").showModal();
  });
  $$("[data-close]").forEach((b) =>
    b.addEventListener("click", () => b.closest("dialog").close()),
  );
  $("#treatment-form").addEventListener("submit", (e) => {
    e.preventDefault();
    const f = new FormData(e.target);
    treatments.push({
      description: String(f.get("description")).trim(),
      amount: Number(f.get("amount")),
    });
    $("#treatment-dialog").close();
    changed();
    finance();
  });
  $("#payment-form").addEventListener("submit", (e) => {
    e.preventDefault();
    const f = new FormData(e.target),
      amount = Number(f.get("amount")),
      balance =
        treatments.reduce((s, t) => s + Number(t.amount), 0) -
        payments.reduce((s, p) => s + Number(p.amount), 0);
    if (Math.round(amount * 100) > Math.round(balance * 100)) {
      toast("El pago supera el saldo pendiente.", true);
      return;
    }
    const receiptFile = e.target.elements.receiptFile.files[0] || null;
    if (receiptFile && receiptFile.size > 5 * 1024 * 1024) {
      toast("La captura del comprobante debe pesar como máximo 5 MB.", true);
      return;
    }
    payments.push({
      id: null,
      date: String(f.get("date")),
      amount,
      signer: String(f.get("signer")),
      receiptName: receiptFile ? receiptFile.name : "",
      hasReceipt: false,
      _receiptFile: receiptFile,
    });
    $("#payment-dialog").close();
    changed();
    finance();
  });
  let evidenceFilter = "";
  async function evidences() {
    if (!id) return;
    const suffix = evidenceFilter ? "?type=" + encodeURIComponent(evidenceFilter) : "";
    const items = await request("/api/patients/" + id + "/evidences" + suffix);
    const container = $("#evidence-list");
    container.replaceChildren();
    items.forEach((e) => {
      const card = document.createElement("article");
      card.className = "document-card";
      const image = document.createElement("img");
      image.src = "/evidences/" + e.id + "/content";
      image.alt = e.title;
      image.loading = "lazy";
      const info = document.createElement("div");
      info.className = "document-info";
      const badge = document.createElement("span");
      badge.className = "evidence-badge";
      badge.textContent = e.type === "TRATAMIENTO" ? "Tratamiento" : (e.type === "PAGO" ? "Comprobante histórico" : "Otro");
      const title = document.createElement("h3");
      title.textContent = e.title;
      const description = document.createElement("p");
      description.textContent = e.description || "Sin descripción";
      const meta = document.createElement("p");
      meta.textContent = e.uploadedAt + " · " + e.uploadedBy;
      const actions = document.createElement("div");
      actions.className = "evidence-actions";
      const link = document.createElement("a");
      link.href = image.src;
      link.target = "_blank";
      link.rel = "noopener";
      link.textContent = "Ver imagen ↗";
      const del = document.createElement("button");
      del.type = "button";
      del.className = "danger-link";
      del.textContent = "Eliminar";
      del.addEventListener("click", async () => {
        if (!confirm("¿Eliminar esta evidencia clínica?")) return;
        try {
          await request("/api/evidences/" + e.id, { method: "DELETE" });
          await evidences();
          toast("Evidencia eliminada.");
        } catch (err) {
          toast(err.message, true);
        }
      });
      actions.append(link, del);
      info.append(badge, title, description, meta, actions);
      card.append(image, info);
      container.append(card);
    });
    if (!items.length) {
      const p = document.createElement("p");
      p.className = "muted";
      p.textContent = "No hay evidencias clínicas en esta categoría.";
      container.append(p);
    }
  }

  $$(".evidence-filter").forEach((button) =>
    button.addEventListener("click", async () => {
      evidenceFilter = button.dataset.evidenceFilter;
      $$(".evidence-filter").forEach((b) => b.classList.toggle("active", b === button));
      if (id) await evidences();
    }),
  );

  $("#evidence-upload-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (!id || dirty) {
      toast("Guarda la historia y sus cambios antes de adjuntar evidencias.", true);
      return;
    }
    const file = $("#evidence-file").files[0];
    const title = $("#evidence-title").value.trim();
    if (!title) {
      toast("Escribe un título para la evidencia.", true);
      return;
    }
    if (!file || file.size > 8 * 1024 * 1024) {
      toast("Selecciona una imagen JPG, JPEG, JFIF o PNG de hasta 8 MB.", true);
      return;
    }
    const validEvidenceImage = /\.(png|jpe?g|jfif)$/i.test(file.name);
    if (!validEvidenceImage) {
      toast("Formato no permitido. Usa JPG, JPEG, JFIF o PNG.", true);
      return;
    }
    const data = new FormData();
    data.append("type", $("#evidence-type").value);
    data.append("title", title);
    data.append("description", $("#evidence-description").value.trim());
    data.append("file", file);
    $("#upload-evidence").disabled = true;
    try {
      await request("/api/patients/" + id + "/evidences", { method: "POST", body: data });
      $("#evidence-file").value = "";
      $("#evidence-title").value = "";
      $("#evidence-description").value = "";
      await evidences();
      toast("Evidencia clínica adjuntada correctamente.");
    } catch (err) {
      toast(err.message, true);
    } finally {
      $("#upload-evidence").disabled = false;
    }
  });

  async function documents() {
    if (!id) return;
    const [docs, events] = await Promise.all([
      request("/api/patients/" + id + "/documents"),
      request("/api/patients/" + id + "/audit"),
    ]);
    const container = $("#document-list");
    container.replaceChildren();
    docs.forEach((d) => {
      const card = document.createElement("article");
      card.className = "document-card";
      const image = document.createElement("img");
      image.src = "/documents/" + d.id + "/content";
      image.alt = d.description || "Documento firmado";
      image.loading = "lazy";
      const info = document.createElement("div");
      info.className = "document-info";
      const title = document.createElement("h3");
      title.textContent = d.description || d.originalName;
      const date = document.createElement("p");
      date.textContent = d.uploadedAt + " · " + d.uploadedBy;
      const link = document.createElement("a");
      link.href = image.src;
      link.target = "_blank";
      link.rel = "noopener";
      link.textContent = "Ver documento ↗";
      info.append(title, date, link);
      card.append(image, info);
      container.append(card);
    });
    if (!docs.length) {
      const p = document.createElement("p");
      p.className = "muted";
      p.textContent = "No hay documentos firmados adjuntos.";
      container.append(p);
    }
    const list = $("#audit-list");
    list.replaceChildren();
    events.forEach((a) => {
      const li = document.createElement("li"),
        span = document.createElement("span");
      li.textContent = a.action;
      span.textContent = a.actor + " · " + a.date;
      li.append(span);
      list.append(li);
    });
  }
  $("#upload-document").addEventListener("click", async () => {
    if (!id || dirty) {
      toast(
        "Guarda la historia y sus cambios antes de adjuntar el documento.",
        true,
      );
      return;
    }
    const file = $("#signed-file").files[0];
    if (!file || file.size > 5 * 1024 * 1024) {
      toast("Selecciona una imagen de hasta 5 MB.", true);
      return;
    }
    const data = new FormData();
    data.append("file", file);
    data.append("description", $("#document-description").value);
    $("#upload-document").disabled = true;
    try {
      await request("/api/patients/" + id + "/documents", {
        method: "POST",
        body: data,
      });
      $("#signed-file").value = "";
      $("#document-description").value = "";
      await documents();
      toast("Documento firmado adjuntado correctamente.");
    } catch (e) {
      toast(e.message, true);
    } finally {
      $("#upload-document").disabled = false;
    }
  });
  (async () => {
    try {
      if (id) {
        populate(await request("/api/patients/" + id));
        await documents();
        await evidences();
      } else {
        $("#blackLegend").value = "Hallazgo / defecto";
        $("#yellowLegend").value = "Pendiente de atención";
        $("#greenLegend").value = "Tratamiento realizado";
        chart();
        finance();
      }
      ready = true;
    } catch (e) {
      toast(e.message, true);
      $("#save-history").disabled = true;
    }
  })();
})();
