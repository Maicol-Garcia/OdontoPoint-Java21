-- OdontoPoint · Agenda y catálogo. MySQL 8. Tablas y columnas en español.
-- Ejecutar sobre la base existente en español, después de los scripts 01 a 04.
-- No elimina ni cambia historias, pagos, documentos o imágenes existentes.
USE odontopoint;
CREATE TABLE IF NOT EXISTS catalogo_servicios (
 id BIGINT NOT NULL AUTO_INCREMENT,
 version BIGINT NOT NULL DEFAULT 0,
 nombre VARCHAR(120) NOT NULL,
 descripcion VARCHAR(1000),
 precio DECIMAL(12,2) NOT NULL,
 duracion_minutos INT NOT NULL,
 activo BOOLEAN NOT NULL,
 PRIMARY KEY(id),
 CONSTRAINT precio_servicio_valido CHECK (precio >= 0),
 CONSTRAINT duracion_servicio_valida CHECK (duracion_minutos BETWEEN 5 AND 480)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS citas (
 id BIGINT NOT NULL AUTO_INCREMENT,
 version BIGINT NOT NULL DEFAULT 0,
 historia_id BIGINT NOT NULL,
 servicio_id BIGINT,
 inicio DATETIME(6) NOT NULL,
 fin DATETIME(6) NOT NULL,
 estado VARCHAR(20) NOT NULL,
 motivo VARCHAR(500) NOT NULL,
 profesional VARCHAR(180),
 notas VARCHAR(1000),
 PRIMARY KEY(id),
 INDEX indice_citas_inicio (inicio),
 CONSTRAINT fk_cita_historia FOREIGN KEY(historia_id) REFERENCES historias_clinicas(id),
 CONSTRAINT fk_cita_servicio FOREIGN KEY(servicio_id) REFERENCES catalogo_servicios(id),
 CONSTRAINT intervalo_cita_valido CHECK (fin > inicio),
 CONSTRAINT estado_cita_valido CHECK (estado IN ('PROGRAMADA','CONFIRMADA','ATENDIDA','CANCELADA'))
) ENGINE=InnoDB;
INSERT INTO contadores_secuencia(clave_contador,siguiente_valor)
 SELECT 'agenda',0 WHERE NOT EXISTS (SELECT 1 FROM contadores_secuencia WHERE clave_contador='agenda');
