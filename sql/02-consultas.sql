USE odontopoint;

-- Pacientes registrados
SELECT
  CONCAT('HC-', LPAD(CAST(id AS CHAR), GREATEST(6, CHAR_LENGTH(CAST(id AS CHAR))), '0')) AS 'Historia clínica',
  nombre_completo AS 'Paciente',
  dni AS 'DNI',
  sexo AS 'Sexo',
  telefono AS 'Teléfono',
  DATE_FORMAT(fecha_nacimiento, '%d/%m/%Y') AS 'Nacimiento',
  DATE_FORMAT(fecha_creacion, '%d/%m/%Y %H:%i') AS 'Fecha de registro'
FROM historias_clinicas
ORDER BY id DESC;

-- Resumen financiero por paciente
SELECT
  h.id AS 'Historia',
  h.nombre_completo AS 'Paciente',
  COALESCE(t.total_tratamientos, 0) AS 'Presupuesto',
  COALESCE(p.total_pagos, 0) AS 'Pagado',
  COALESCE(t.total_tratamientos, 0) - COALESCE(p.total_pagos, 0) AS 'Saldo'
FROM historias_clinicas h
LEFT JOIN (
  SELECT id_historia, SUM(importe) total_tratamientos
  FROM tratamientos GROUP BY id_historia
) t ON t.id_historia = h.id
LEFT JOIN (
  SELECT id_historia, SUM(importe) total_pagos
  FROM pagos GROUP BY id_historia
) p ON p.id_historia = h.id
ORDER BY h.id DESC;

-- Evidencias: comprobantes y fotos de tratamientos
SELECT
  e.id,
  e.id_historia AS 'Historia',
  h.nombre_completo AS 'Paciente',
  e.tipo_evidencia AS 'Tipo',
  e.titulo AS 'Título',
  e.descripcion AS 'Descripción',
  e.nombre_original AS 'Archivo',
  DATE_FORMAT(e.fecha_carga, '%d/%m/%Y %H:%i') AS 'Fecha de carga',
  e.cargado_por AS 'Cargado por'
FROM evidencias_clinicas e
JOIN historias_clinicas h ON h.id = e.id_historia
ORDER BY e.fecha_carga DESC;

-- Últimos eventos de auditoría
SELECT
  id_historia AS 'Historia',
  accion AS 'Acción',
  usuario_responsable AS 'Usuario',
  DATE_FORMAT(fecha_evento, '%d/%m/%Y %H:%i:%s') AS 'Fecha'
FROM eventos_auditoria
ORDER BY id DESC
LIMIT 100;

-- Comprobantes/capturas vinculados directamente a los pagos
SELECT
  p.id AS 'ID pago',
  CONCAT('HC-', LPAD(CAST(p.id_historia AS CHAR), 6, '0')) AS 'Historia clínica',
  DATE_FORMAT(p.fecha_pago, '%d/%m/%Y') AS 'Fecha de pago',
  p.importe AS 'Importe',
  cp.nombre_original AS 'Captura / comprobante',
  DATE_FORMAT(cp.fecha_carga, '%d/%m/%Y %H:%i') AS 'Fecha de carga',
  cp.cargado_por AS 'Cargado por'
FROM pagos p
LEFT JOIN comprobantes_pago cp ON cp.id_pago = p.id
ORDER BY p.id_historia, p.fecha_pago, p.id;
