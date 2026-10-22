# OdontoPoint con MySQL Workbench

## 1. Requisitos

Java 21, MySQL Server 8.0 u 8.4 en ejecución y MySQL Workbench. Workbench es la interfaz de administración; instalar solo Workbench no instala necesariamente el servidor. Para compilar el código también necesita Maven.

## 2. Crear la base y el usuario

1. Abra Workbench y conéctese a su servidor local con su cuenta administradora (por ejemplo, root).
2. Seleccione File → Open SQL Script y abra `sql/01-crear-base.sql`.
3. Cambie `REEMPLAZAR_CON_TU_CLAVE` por una contraseña propia. Será la contraseña de MySQL para el usuario `odontopoint`, no la contraseña del login web.
4. Ejecute el script con el botón del rayo.

Se crea el esquema `odontopoint` con utf8mb4. El script no elimina datos. Si el usuario ya existía, conserve su contraseña actual: el script no la cambia.

## 3. Iniciar la aplicación

Abra `Iniciar-Windows.bat` y escriba el usuario MySQL (`odontopoint` por defecto) y la contraseña definida en el paso anterior. La contraseña no se muestra ni se guarda en el código. Cuando aparezca `Started OdontoPointApplication`, entre en http://localhost:8080.

En VS Code puede abrir la carpeta con `pom.xml` y pulsar F5. La configuración le pedirá las mismas credenciales. Para ejecutar el código modificado, compile con `Compilar-Windows.bat` antes de usar el JAR.

Para otro puerto o servidor, establezca `ODONTO_MYSQL_URL` antes de iniciar, por ejemplo:

```powershell
$env:ODONTO_MYSQL_URL = 'jdbc:mysql://localhost:3307/odontopoint?sslMode=REQUIRED&connectionTimeZone=America/Lima'
.\Iniciar-Windows.bat
```

La conexión exige TLS. Una instalación estándar de MySQL 8 puede generar sus certificados de servidor. Si el servidor indica que no admite SSL, configure TLS en MySQL; no es necesario desactivar la protección en el código. Para un servidor remoto use certificados de confianza y `sslMode=VERIFY_IDENTITY`.

## 4. Ver las tablas en Workbench

Después del primer arranque, actualice **SCHEMAS** y expanda **odontopoint → Tables**. Pulse botón derecho sobre una tabla → **Select Rows - Limit 1000**.

Puede crear una conexión en Workbench con:

- Connection Name: OdontoPoint.
- Connection Method: Standard (TCP/IP).
- Hostname: localhost.
- Port: 3306, salvo que haya configurado otro.
- Username: odontopoint.
- Password: la contraseña del usuario MySQL.
- Default Schema: odontopoint.

Principales tablas: `historias_clinicas`, `tratamientos`, `pagos`, `documentos_firmados`, `evidencias_clinicas`, `antecedentes_medicos`, `marcas_odontograma`, `usuarios`, `contadores_secuencia` y `eventos_auditoria`. El nombre exacto de cada tabla está declarado en las entidades JPA; use SHOW TABLES para ver todas. `sql/02-consultas.sql` incluye consultas de ejemplo.

La fecha se guarda como fecha/hora real; Workbench puede mostrarla como `aaaa-mm-dd`. La web y los reportes la presentan como `dd/mm/aaaa`. Use DATE_FORMAT en sus consultas para verla con ese formato.

Las imágenes se guardan en columnas binarias. Los campos clínicos extensos usan TEXT. Los documentos firmados admiten hasta 5 MB y las evidencias clínicas (pagos/tratamientos) hasta 8 MB. Configure `max_allowed_packet` de MySQL al menos en 16 MB si modificó su valor predeterminado.

## 5. Datos anteriores y respaldos

**Los datos de H2 no se importan automáticamente.** No elimine ni sobrescriba `datos-web/odontopoint.mv.db`. Esta entrega comienza con la base MySQL que configure. Para trasladar historias y usuarios hay que realizar una migración separada y verificar los registros y documentos.

`Iniciar-H2-Windows.bat` permite usar la base local anterior si copia su carpeta `datos-web` a esta carpeta, con la aplicación detenida y conservando un respaldo. H2 y MySQL son bases independientes; no se sincronizan.

Con MySQL los datos están en el servidor: copiar `datos-web` ya no respalda los nuevos registros. Use **Server → Data Export** en Workbench con una cuenta autorizada para exportar el esquema `odontopoint`, incluyendo datos. Conserve el respaldo fuera del proyecto.

## Problemas habituales

- **Communications link failure**: compruebe que MySQL Server esté iniciado y que host/puerto sean correctos.
- **Access denied**: revise usuario, contraseña y permisos; la cuenta web es distinta de la cuenta MySQL.
- **Unknown database**: ejecute primero `01-crear-base.sql`.
- **Could not resolve placeholder ODONTO_MYSQL_PASSWORD**: inicie mediante el BAT o F5 para proporcionar la contraseña.
- **Puerto 8080 ocupado**: detenga la instancia anterior de OdontoPoint.

## Referencias técnicas

- Connector/J: https://dev.mysql.com/doc/connector-j/en/
- Seguridad de la conexión: https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-security.html
- Límites de columnas MySQL: https://dev.mysql.com/doc/refman/8.4/en/column-count-limit.html
