# Verificación de esta entrega

- Compilación y empaquetado con OpenJDK 21 (Corretto) y Maven 3.9.9: correctos.
- 13 pruebas automatizadas: 13 correctas, sin fallos ni errores.
- Se actualizaron los datos de prueba antiguos que aún usaban guiones para ajustarlos al formato dd/MM/yyyy del proyecto adjunto.
- Agenda: reserva, reprogramación, horarios contiguos, rechazo de cruces, dos reservas simultáneas, cancelación y control de versiones.
- Catálogo: desactivación conservando citas antiguas y rechazo del servicio inactivo en nuevas citas.
- Seguridad: sesión requerida, protección CSRF y validación de solicitudes.
- Funciones existentes: exportación PDF y Excel, login, numeración y adjuntos cubiertos por la suite recibida.
- Navegador Chromium: alta de usuario, login, creación de servicio, selección de paciente, reserva, rechazo de cruce, edición de estado y fecha visible correctos. Sin errores JavaScript en el recorrido.
- Vista de 390 px: sin desbordamiento horizontal en la agenda.

Las pruebas se ejecutaron con H2 en memoria. Se entrega el script para MySQL 8, pero no se ejecutó contra la base de datos particular del usuario. No se incluye información de pacientes ni contraseñas reales en los datos de prueba.
