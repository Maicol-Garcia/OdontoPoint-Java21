-- Migración para instalaciones existentes de OdontoPoint.
-- Puede ejecutarse nuevamente: utiliza CREATE TABLE IF NOT EXISTS.
-- Los comprobantes de pago quedan separados de las evidencias clínicas.
USE odontopoint;

CREATE TABLE IF NOT EXISTS comprobantes_pago (
  id_pago BIGINT NOT NULL,
  nombre_original VARCHAR(200) NOT NULL,
  tipo_archivo VARCHAR(100) NOT NULL,
  huella_sha256 VARCHAR(64) NOT NULL,
  fecha_carga DATETIME(6) NOT NULL,
  cargado_por VARCHAR(255) NOT NULL,
  contenido MEDIUMBLOB NOT NULL,
  PRIMARY KEY (id_pago),
  CONSTRAINT relacion_comprobante_pago FOREIGN KEY (id_pago)
    REFERENCES pagos(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS evidencias_clinicas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_historia BIGINT NOT NULL,
  tipo_evidencia VARCHAR(30) NOT NULL,
  titulo VARCHAR(180) NOT NULL,
  descripcion VARCHAR(500),
  nombre_original VARCHAR(200) NOT NULL,
  tipo_archivo VARCHAR(100) NOT NULL,
  huella_sha256 VARCHAR(64) NOT NULL,
  fecha_carga DATETIME(6) NOT NULL,
  cargado_por VARCHAR(255) NOT NULL,
  contenido MEDIUMBLOB NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_evidencias_historia_tipo_fecha (id_historia, tipo_evidencia, fecha_carga),
  CONSTRAINT relacion_evidencias_historia FOREIGN KEY (id_historia)
    REFERENCES historias_clinicas(id) ON DELETE CASCADE,
  CONSTRAINT chk_tipo_evidencia CHECK (tipo_evidencia IN ('PAGO','TRATAMIENTO','OTRO'))
) ENGINE=InnoDB;

-- Nota: si ya existen evidencias antiguas de tipo PAGO no se eliminan.
-- La nueva versión de la aplicación deja de mostrarlas y los nuevos comprobantes
-- se adjuntan exclusivamente desde "Acta / comprobante" al registrar el pago.


-- Corrige una instalación existente aunque la tabla ya haya sido creada.
ALTER TABLE evidencias_clinicas
  MODIFY COLUMN contenido MEDIUMBLOB NOT NULL;

ALTER TABLE comprobantes_pago
  MODIFY COLUMN contenido MEDIUMBLOB NOT NULL;
