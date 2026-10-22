-- =====================================================
-- ODONTOPOINT - BASE DE DATOS EN ESPAÑOL (MySQL 8)
-- Versión mejorada: documentos, comprobantes de pago y fotos de tratamientos
-- =====================================================

CREATE DATABASE IF NOT EXISTS odontopoint
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE odontopoint;

-- Usuario técnico de la aplicación. Cambie la clave antes de ejecutar.
CREATE USER IF NOT EXISTS 'odontopoint'@'localhost' IDENTIFIED BY 'root';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
  ON odontopoint.* TO 'odontopoint'@'localhost';

CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre_usuario VARCHAR(80) NOT NULL,
  nombre_completo VARCHAR(120) NOT NULL,
  contrasena_hash VARCHAR(255) NOT NULL,
  rol VARCHAR(255) NOT NULL DEFAULT 'ADMIN',
  PRIMARY KEY (id),
  CONSTRAINT unico_nombre_usuario UNIQUE (nombre_usuario)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS contadores_secuencia (
  clave_contador VARCHAR(255) NOT NULL,
  siguiente_valor BIGINT NOT NULL,
  PRIMARY KEY (clave_contador)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS historias_clinicas (
  id BIGINT NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  fecha_creacion DATETIME(6) NOT NULL,
  fecha_actualizacion DATETIME(6) NOT NULL,
  creado_por VARCHAR(255) NOT NULL,
  actualizado_por VARCHAR(255) NOT NULL,
  nombre_completo VARCHAR(180),
  dni VARCHAR(8),
  sexo VARCHAR(40),
  fecha_nacimiento DATE,
  domicilio VARCHAR(250),
  lugar_procedencia VARCHAR(150),
  ocupacion VARCHAR(150),
  estado_civil VARCHAR(40),
  telefono VARCHAR(40),
  nombre_apoderado VARCHAR(180),
  dni_apoderado VARCHAR(8),
  telefono_apoderado VARCHAR(40),
  motivo_consulta TEXT,
  signos_sintomas TEXT,
  tiempo_enfermedad VARCHAR(180),
  detalle_alergias TEXT,
  otros_antecedentes TEXT,
  examen_clinico TEXT,
  diagnostico TEXT,
  observaciones_odontograma TEXT,
  leyenda_negro VARCHAR(180),
  leyenda_amarillo VARCHAR(180),
  leyenda_verde VARCHAR(180),
  estado_consentimiento VARCHAR(30),
  fecha_consentimiento DATE,
  detalle_consentimiento TEXT,
  detalle_revocacion TEXT,
  profesional_responsable VARCHAR(180),
  PRIMARY KEY (id),
  CONSTRAINT unico_dni_historia UNIQUE (dni)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS antecedentes_medicos (
  id_historia BIGINT NOT NULL,
  nombre_antecedente VARCHAR(255) NOT NULL,
  respuesta VARCHAR(255),
  PRIMARY KEY (id_historia, nombre_antecedente),
  CONSTRAINT relacion_antecedentes_historia FOREIGN KEY (id_historia)
    REFERENCES historias_clinicas(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS marcas_odontograma (
  id_historia BIGINT NOT NULL,
  clave_superficie VARCHAR(255) NOT NULL,
  color_marca VARCHAR(255),
  PRIMARY KEY (id_historia, clave_superficie),
  CONSTRAINT relacion_marcas_historia FOREIGN KEY (id_historia)
    REFERENCES historias_clinicas(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tratamientos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_historia BIGINT NOT NULL,
  descripcion VARCHAR(1000) NOT NULL,
  importe DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_tratamientos_historia (id_historia),
  CONSTRAINT relacion_tratamientos_historia FOREIGN KEY (id_historia)
    REFERENCES historias_clinicas(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pagos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_historia BIGINT NOT NULL,
  fecha_pago DATE NOT NULL,
  importe DECIMAL(12,2) NOT NULL,
  comprobante VARCHAR(180),
  firmante VARCHAR(180),
  PRIMARY KEY (id),
  INDEX idx_pagos_historia_fecha (id_historia, fecha_pago),
  CONSTRAINT relacion_pagos_historia FOREIGN KEY (id_historia)
    REFERENCES historias_clinicas(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Captura asociada directamente a cada pago. Un pago puede tener una captura.
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

CREATE TABLE IF NOT EXISTS documentos_firmados (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_historia BIGINT NOT NULL,
  nombre_original VARCHAR(200) NOT NULL,
  descripcion VARCHAR(250),
  tipo_archivo VARCHAR(255) NOT NULL,
  huella_sha256 VARCHAR(64) NOT NULL,
  fecha_carga DATETIME(6) NOT NULL,
  cargado_por VARCHAR(255) NOT NULL,
  contenido MEDIUMBLOB NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_documentos_historia_fecha (id_historia, fecha_carga),
  CONSTRAINT relacion_documentos_historia FOREIGN KEY (id_historia)
    REFERENCES historias_clinicas(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Evidencias clínicas: fotos de tratamientos y otros adjuntos. Los comprobantes van en comprobantes_pago.
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

CREATE TABLE IF NOT EXISTS eventos_auditoria (
  id BIGINT NOT NULL AUTO_INCREMENT,
  id_historia BIGINT,
  accion VARCHAR(255) NOT NULL,
  usuario_responsable VARCHAR(255) NOT NULL,
  fecha_evento DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_auditoria_historia_fecha (id_historia, fecha_evento)
) ENGINE=InnoDB;

INSERT INTO contadores_secuencia (clave_contador, siguiente_valor)
SELECT 'historia', COALESCE(MAX(id),0) FROM historias_clinicas
WHERE NOT EXISTS (
  SELECT 1 FROM contadores_secuencia WHERE clave_contador='historia'
);

INSERT INTO contadores_secuencia (clave_contador, siguiente_valor)
SELECT 'configuracion', 0
WHERE NOT EXISTS (
  SELECT 1 FROM contadores_secuencia WHERE clave_contador='configuracion'
);

SHOW TABLES;
