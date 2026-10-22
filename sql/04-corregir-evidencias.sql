-- =====================================================
-- ODONTOPOINT - CORRECCIÓN DE EVIDENCIAS CLÍNICAS
-- Ejecutar UNA VEZ en MySQL Workbench sobre la base odontopoint.
-- Es seguro repetirlo.
--
-- Corrige instalaciones donde Hibernate pudo haber creado el campo
-- contenido con un BLOB demasiado pequeño antes de ejecutar la migración.
-- =====================================================

USE odontopoint;

ALTER TABLE evidencias_clinicas
  MODIFY COLUMN tipo_evidencia VARCHAR(30) NOT NULL,
  MODIFY COLUMN titulo VARCHAR(180) NOT NULL,
  MODIFY COLUMN descripcion VARCHAR(500) NULL,
  MODIFY COLUMN nombre_original VARCHAR(200) NOT NULL,
  MODIFY COLUMN tipo_archivo VARCHAR(100) NOT NULL,
  MODIFY COLUMN huella_sha256 VARCHAR(64) NOT NULL,
  MODIFY COLUMN fecha_carga DATETIME(6) NOT NULL,
  MODIFY COLUMN cargado_por VARCHAR(255) NOT NULL,
  MODIFY COLUMN contenido MEDIUMBLOB NOT NULL;

ALTER TABLE comprobantes_pago
  MODIFY COLUMN contenido MEDIUMBLOB NOT NULL;

ALTER TABLE documentos_firmados
  MODIFY COLUMN contenido MEDIUMBLOB NOT NULL;

-- Verificación del tipo real de las columnas de imagen.
SELECT
  TABLE_NAME AS 'Tabla',
  COLUMN_NAME AS 'Columna',
  COLUMN_TYPE AS 'Tipo actual'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'odontopoint'
  AND TABLE_NAME IN ('evidencias_clinicas','comprobantes_pago','documentos_firmados')
  AND COLUMN_NAME = 'contenido';
