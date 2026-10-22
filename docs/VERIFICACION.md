# Verificación de la entrega

- Compilación Maven con Java 21: correcta.
- 9 pruebas de integración: sin fallos ni errores.
- Prueba en Chromium: creación inicial de usuario, login, alta de paciente, marcado dental en negro/amarillo/verde, guardado, carga PNG y exportaciones PDF/Excel correctos.
- Sin errores JavaScript durante ese recorrido.
- Revisión visual de login, odontograma, listado y vista móvil de 390 px.
- PDF generado mediante JasperReports y revisado visualmente.

Las capturas de esta carpeta muestran datos ficticios de prueba. El paquete se entrega sin base de pacientes ni usuarios preconfigurados.

## Adaptación MySQL

- Compilación correcta con Connector/J 9.4.0 administrado por Spring Boot.
- 9 pruebas de integración H2: sin fallos ni errores.
- Prueba HTTP contra MySQL Community Server 8.0.46 real con TLS: creación de tablas, configuración inicial, login, alta de historia, texto clínico de más de 9.000 caracteres, tres colores del odontograma, carga y recuperación íntegra de PNG mayor de 64 KB, Excel y PDF correctos.
- Verificadas columnas TEXT para texto clínico y MEDIUMBLOB para imágenes.
- El script PowerShell debe ejecutarse en Windows; no se ha ejecutado en este entorno Linux.
- No se han importado datos del usuario ni probado una conexión a su computadora.
