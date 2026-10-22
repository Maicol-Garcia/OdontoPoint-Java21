-- ODONTOPOINT: CONSULTAS DE INGRESOS
-- MySQL 8.0 / 8.4. No crea tablas, no modifica pagos ni cambia sus importes.
-- El panel funciona con la tabla pagos existente.
-- Este índice también está declarado en Payment.java y Hibernate puede crearlo
-- con ddl-auto=update. Este archivo permite aplicarlo manualmente en Workbench.
USE odontopoint;

SET @existe_indice_ingresos = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'pagos'
      AND index_name = 'idx_pagos_fecha_historia'
);

SET @sql_indice_ingresos = IF(
    @existe_indice_ingresos = 0,
    'CREATE INDEX idx_pagos_fecha_historia ON pagos (fecha_pago, id_historia)',
    'SELECT ''El índice de ingresos ya existe'' AS resultado'
);
PREPARE instruccion_indice_ingresos FROM @sql_indice_ingresos;
EXECUTE instruccion_indice_ingresos;
DEALLOCATE PREPARE instruccion_indice_ingresos;

-- Consulta informativa del índice.
SHOW INDEX FROM pagos WHERE Key_name = 'idx_pagos_fecha_historia';
