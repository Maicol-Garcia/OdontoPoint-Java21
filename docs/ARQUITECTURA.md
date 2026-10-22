# Arquitectura por capas de OdontoPoint

OdontoPoint conserva una arquitectura web por capas para separar responsabilidades y facilitar mantenimiento.

## 1. Capa de presentación

Ubicación: `src/main/resources/templates`, `static/css` y `static/js`.

Responsabilidad: interfaz web, formularios, odontograma, presupuesto, pagos, documentos y evidencias clínicas. La pantalla de historia clínica permite adjuntar comprobantes de pago y fotos de tratamientos sin mezclar esos archivos con los consentimientos firmados.

## 2. Capa de controladores

Ubicación: `src/main/java/pe/odontopoint/controller`.

Responsabilidad: recibir peticiones HTTP y delegarlas a los servicios. No contiene reglas de persistencia.

Controladores principales:
- `PatientController`: historias clínicas.
- `DocumentController`: documentos firmados.
- `ClinicalEvidenceController`: comprobantes de pago y fotos de tratamientos.
- `AuthController`: acceso y configuración inicial.
- `ExportController`: exportación PDF/Excel.

## 3. Capa de servicios / negocio

Ubicación: `src/main/java/pe/odontopoint/service`.

Responsabilidad: validaciones, reglas clínicas y financieras, numeración de historias, auditoría, carga segura de imágenes, reportes y exportaciones.

Servicios destacados:
- `HistoryService`
- `ClinicalEvidenceService`
- `DocumentService`
- `AuditService`
- `AccountService`
- `ReportService`
- `ExcelService`

## 4. Capa de persistencia

Ubicación: `src/main/java/pe/odontopoint/repository`.

Responsabilidad: acceso a MySQL mediante Spring Data JPA. Los repositorios no implementan lógica de interfaz.

## 5. Capa de entidades / dominio

Ubicación: `src/main/java/pe/odontopoint/entity`.

Las entidades Java se mapean a las tablas en español:
- `AppUser` → `usuarios`
- `PatientHistory` → `historias_clinicas`
- `Treatment` → `tratamientos`
- `Payment` → `pagos`
- `SignedDocument` → `documentos_firmados`
- `ClinicalEvidence` → `evidencias_clinicas`
- `AuditEvent` → `eventos_auditoria`
- `SequenceCounter` → `contadores_secuencia`

Las colecciones de antecedentes y odontograma se almacenan en `antecedentes_medicos` y `marcas_odontograma`.

## 6. DTO y mapeadores

Ubicaciones: `dto` y `mapper`.

Los DTO evitan exponer directamente las entidades JPA al navegador y los mapeadores concentran las conversiones entre formulario y dominio.

## Flujo recomendado

`Navegador → Controller → Service → Repository → MySQL`

La respuesta vuelve por el mismo flujo, usando DTO para la comunicación con el navegador.

## Evidencias clínicas

La tabla `evidencias_clinicas` admite tres categorías:
- `PAGO`: captura o fotografía del comprobante.
- `TRATAMIENTO`: foto del procedimiento, antes/después o resultado clínico.
- `OTRO`: otra evidencia visual relacionada con la historia.

Cada archivo registra historia clínica, título, descripción, nombre original, tipo MIME, hash SHA-256, fecha, usuario que lo cargó y contenido binario. La carga queda registrada en auditoría.
