# Actualización: control de ingresos

El menú **Control de ingresos** permite consultar cobros por mes, trimestre, año
o rango de fechas, buscar pacientes, revisar comprobantes y exportar a Excel.
Guía: [Control de ingresos](docs/CONTROL-DE-INGRESOS.md).
El JAR de bin está actualizado para Java 21. El índice opcional de consultas
está en sql/06-indice-ingresos.sql; se utilizan las tablas existentes.

# Actualización: dentadura 3D interactiva

Esta entrega incluye el visor 3D y el JAR compilado con Java 21.
Lee [la guía del odontograma 3D](docs/ODONTOGRAMA-3D.md) para conocer sus controles,
archivos y compatibilidad con tu base de datos.

# OdontoPoint · Java 21

Sistema web odontológico con Spring Boot 3.5.6, MySQL, Thymeleaf, JasperReports y exportación Excel. Esta entrega incluye el código fuente; el JAR se genera al compilar.

## Abrir en VS Code

1. Instala **JDK 21** y **Apache Maven 3.9**. En una terminal nueva, `java -version` y `mvn -version` deben indicar Java 21. Configura `JAVA_HOME` con la carpeta del JDK 21 si Maven utiliza otro Java.
2. Descomprime y abre **la carpeta OdontoPoint-Web que contiene pom.xml**, no solo `src`.
3. Instala las extensiones recomendadas de Java y Spring Boot. En `Java: Configure Java Runtime`, selecciona JDK 21 para el proyecto. Si VS Code conserva errores de la versión anterior: `Java: Clean Java Language Server Workspace` y recarga la ventana.
4. No hace falta instalar Lombok: esta versión usa constructores y métodos Java explícitos, sin procesamiento de anotaciones Lombok ni funciones preview.

## Compilar y ejecutar con MySQL

En la terminal de VS Code, situada junto a `pom.xml`:

```powershell
mvn clean package
.\Iniciar-Windows.bat
```

El comando Maven genera `target/odontopoint-web-2.0.0.jar`. El iniciador pide el usuario y la contraseña de MySQL; usa el usuario técnico `odontopoint` configurado en tu servidor. Al terminar el inicio abre **http://localhost:8080**. Si es una instalación nueva, crea tu cuenta desde la pantalla de configuración inicial. La cuenta de acceso web es diferente del usuario de MySQL.

También puedes ejecutar `Compilar-Windows.bat`: compila y copia el JAR a `bin/odontopoint-web.jar`.

### Base de datos existente

Haz una copia de seguridad antes de actualizar. Conserva tus credenciales y tu base `odontopoint`. Ejecuta **sql/05-agenda-catalogo.sql** en MySQL Workbench si ya aplicaste las actualizaciones 01 a 04 de tu versión. Este script solo agrega catálogo, citas y su contador de bloqueo; no borra información.

Para una base nueva, revisa `docs/MYSQL-WORKBENCH.md`, cambia la contraseña de ejemplo de `sql/01-crear-base.sql` y aplica los scripts de estructura 01, 03, 04 y 05 en ese orden. `02-consultas.sql` contiene consultas de consulta, no una migración obligatoria.

La conexión está en `src/main/resources/application-mysql.yml`. Usa las variables `ODONTO_MYSQL_USER`, `ODONTO_MYSQL_PASSWORD` y, si necesitas otro host/puerto, `ODONTO_MYSQL_URL`. No se incluye una contraseña real. Si inicias con F5, define estas variables en el entorno de VS Code o usa el iniciador de Windows.

### Probar sin MySQL

```powershell
mvn clean package
java -jar target/odontopoint-web-2.0.0.jar --spring.profiles.active=h2
```

Este modo guarda datos de prueba en `datos-web`, independientes de MySQL. No importa automáticamente datos entre bases.

## Agenda

- Calendario mensual con navegación anterior/siguiente, botón Hoy y citas del día seleccionado.
- Citas vinculadas a una historia clínica. Búsqueda por paciente, DNI o número de historia.
- Servicio opcional del catálogo; su duración propone la hora de finalización.
- Crear, reprogramar y cambiar estado: programada, confirmada, atendida o cancelada.
- Cancelar conserva el registro y libera el horario. Las citas atendidas mantienen su horario histórico ocupado.
- Se evitan cruces incluso al reservar desde dos sesiones simultáneas. La agenda corresponde a **un consultorio**, con un único horario de atención compartido. El profesional es un dato descriptivo; no crea agendas independientes.
- Horarios locales de Lima, sin conversión UTC. Fechas visibles `dd/MM/yyyy`; el selector nativo de fecha utiliza el formato del navegador. API y MySQL usan fechas tipadas ISO, sin guardar fechas como texto.

## Catálogo

Servicios con nombre, descripción, precio referencial en soles, duración y estado. Permite buscar, editar y desactivar servicios. Los servicios inactivos se conservan en las citas anteriores; no se ofrecen para nuevas citas. Los precios no crean tratamientos, deudas ni cobros automáticos. El catálogo empieza vacío para registrar los servicios reales del consultorio.

## Funciones conservadas

Historias clínicas, odontograma, login web, imágenes de documentos firmados, comprobantes de pago separados de las fotos clínicas, fotos de tratamientos, PDF con JasperReports y exportación a Excel.

## Capas

- `entity`: entidades y mapeo de tablas; `Cita`, `ServicioCatalogo`, `EstadoCita`.
- `repository`: acceso a datos; consultas de agenda y catálogo.
- `service`: validación de horarios, bloqueo de reservas, versiones y auditoría.
- `controller`: páginas y API autenticada con CSRF.
- `dto`: solicitudes validadas y datos de respuesta; las citas no exponen entidades completas de pacientes.
- `mapper`, `report`, `config`, `exception`, `util`: responsabilidades auxiliares existentes.
- `resources/templates`: páginas HTML; `static/css/agenda.css`: estilo de agenda y catálogo; `static/js`: interacción de sus pantallas.

La nueva funcionalidad tiene nombres de clases y base de datos en español; se conservan los nombres Java existentes para mantener compatibilidad.

## Verificación

`mvn test` ejecuta las pruebas del sistema y `AgendaIntegrationTest`: horarios contiguos, solapamientos, reservas simultáneas, cancelación, versión desactualizada, servicios inactivos, validación, autenticación y CSRF. Revisa `docs/VERIFICACION-JAVA21.md` para los resultados de esta entrega.
