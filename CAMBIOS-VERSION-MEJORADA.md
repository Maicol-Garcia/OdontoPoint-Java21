# OdontoPoint - versión mejorada

## Cambios principales

1. Compatibilidad real con la base MySQL en español proporcionada.
2. Mapeo JPA hacia `usuarios`, `historias_clinicas`, `tratamientos`, `pagos`, `documentos_firmados`, etc.
3. Nueva tabla `evidencias_clinicas`.
4. Carga de capturas de pagos.
5. Carga de fotografías de tratamientos.
6. Clasificación de evidencias por PAGO, TRATAMIENTO u OTRO.
7. Galería con vista previa y filtros dentro de la historia clínica.
8. Eliminación controlada de evidencias con registro de auditoría.
9. Hash SHA-256 para verificar integridad de los archivos.
10. Límite de 8 MB y validación real de PNG/JPG para evidencias.
11. Índices adicionales para consultas por historia, fecha y tipo.
12. Claves foráneas con `ON DELETE CASCADE` en las tablas dependientes.
13. Contador de historias corregido para iniciar desde el mayor ID existente.
14. Documentación de la arquitectura por capas.
15. Consultas SQL actualizadas con encabezados en español.

## Arquitectura

- Presentación: Thymeleaf + CSS + JavaScript.
- Controladores: endpoints HTTP/REST.
- Servicios: reglas de negocio y validaciones.
- Repositorios: acceso a datos con Spring Data JPA.
- Entidades: modelo persistente.
- DTO: comunicación segura entre capas.
- Mapper: transformación DTO/entidad/reporte.

Consulte `docs/ARQUITECTURA.md` para el detalle.


## Ajuste: comprobantes dentro de cada pago
- El campo **Acta / comprobante** ya no es texto: permite seleccionar una captura PNG/JPG al registrar el pago.
- Cada captura queda vinculada al ID del pago en la tabla `comprobantes_pago`.
- Al abrir una historia, la tabla **Pagos realizados** muestra una miniatura y el enlace **Ver captura**.
- El consentimiento firmado sigue visible en **05 Consentimiento y archivos**, con miniatura y apertura del documento.
- **Evidencias clínicas** ya no admite comprobantes de pago; queda solo para fotos de tratamientos y otras evidencias clínicas.
- Los pagos existentes conservan su ID al editar la historia para no perder la relación con sus comprobantes.

### Base existente
Ejecute nuevamente `sql/03-mejora-evidencias.sql`; es idempotente y crea `comprobantes_pago` si todavía no existe.
