# Control de ingresos de OdontoPoint

## Acceso y funcionamiento

En el menú lateral elige **Control de ingresos**.

El panel reúne los pagos guardados de todas las historias clínicas. La fecha
que determina el período es la **fecha de pago**, no la fecha en que se creó la
historia ni la fecha de la cita. Los importes se expresan en soles.

1. Selecciona **Mensual**, **Trimestral**, **Anual** o **Personalizado**.
2. Indica el año y mes/trimestre, o escribe las fechas en formato dd/mm/aaaa.
3. Opcionalmente busca por nombre, DNI o número de historia, por ejemplo HC-000001.
4. Pulsa **Ver ingresos**.
5. Revisa los indicadores, la gráfica y el detalle de pagos.
6. Usa **Exportar Excel** para descargar todos los pagos del filtro, aunque
   el detalle se muestre en varias páginas.

El botón **Actualizar** repite la consulta con los filtros visibles. Si modificas
un filtro, se indica que debes consultar nuevamente antes de exportar.

## Indicadores

- **Total cobrado:** suma de los importes de los pagos del período.
- **Pagos recibidos:** cantidad de registros de pago.
- **Pacientes con pagos:** cantidad de pacientes distintos, sin duplicarlos
  si hicieron más de un pago.
- **Promedio por pago:** total cobrado dividido entre la cantidad de pagos,
  redondeado a dos decimales.
- **Capturas:** cantidad de pagos con y sin una imagen de comprobante adjunta.

Un presupuesto o tratamiento registrado no incrementa los ingresos por sí solo:
el ingreso aparece al guardar el pago correspondiente.

El panel mide cobros registrados. No calcula utilidad, egresos, impuestos ni
cierres contables, y los registros conservan las mismas posibilidades de edición
que en las historias del sistema.

## Períodos y comparación

- Mes: del primer al último día del mes, incluidos ambos.
- Trimestre 1: enero a marzo; 2: abril a junio; 3: julio a septiembre;
  4: octubre a diciembre.
- Año: del 1 de enero al 31 de diciembre.
- Personalizado: desde y hasta, incluidos, con un máximo de 366 días.
- Años admitidos: 1900 a 2100.
- La fecha inicial del panel se obtiene en la zona America/Lima.

Los meses se comparan con el mes calendario anterior; los trimestres, con el
trimestre anterior; los años, con el año anterior. Un rango personalizado
se compara con el mismo número de días inmediatamente anterior.

Se comparan períodos completos, incluso si el mes actual aún no ha terminado.
La misma búsqueda de paciente se aplica al período actual y al anterior.

La diferencia es total actual menos total anterior. El porcentaje es
(diferencia / total anterior) × 100. Si el total anterior es cero, se informa
que no hay base para un porcentaje, en lugar de mostrar un valor infinito.

## Gráfica y detalle

La gráfica mensual muestra los días; la trimestral y anual muestran los meses.
En rangos personalizados de hasta 62 días se muestran días; en los mayores se
agrupa por mes, contando solo las fechas incluidas en el filtro. Los períodos
sin pagos aparecen con valor cero.

Cada barra permite consultar su importe pasando el cursor, haciendo clic o
seleccionándola con el teclado.

El detalle se ordena por fecha descendente y, en caso de empate, por ID de pago
descendente. Tiene 20 registros por página. Los enlaces permiten abrir la
historia para revisar o editar el pago y ver su captura de comprobante, cuando
existe. Registrar o corregir un pago se realiza en la historia correspondiente.

Los cambios en los pagos se reflejan al volver a consultar. No existe una
segunda copia de los importes que pueda desincronizarse.

## Exportación Excel

El archivo contiene:
1. **Resumen:** filtro, totales, comparación y momento de generación.
2. **Evolución:** importes por día o mes.
3. **Detalle de pagos:** todos los pagos que coinciden con el filtro.

Incluye fechas como celdas de fecha con formato dd/mm/aaaa, importes numéricos,
cabeceras, filtros y filas inmovilizadas. Los nombres se almacenan como texto
literal para que no se interpreten como fórmulas.

Se exporta por lotes de 500 pagos y se utiliza un libro de escritura progresiva.
El máximo por descarga es 50 000 pagos; para un volumen mayor, reduce el período
o filtra por paciente. Las imágenes de comprobantes no se incrustan en el Excel;
se indica si están adjuntas y pueden verse desde el panel.

## Base de datos

**No se necesitan tablas nuevas.** Se consultan:
- pagos: fecha_pago, importe, id_historia, firmante e ID de pago.
- historias_clinicas: identificación del paciente.
- comprobantes_pago: existencia de la captura.

Se añadió el índice idx_pagos_fecha_historia sobre fecha_pago e id_historia.
Está declarado en la entidad Payment para que Hibernate pueda crearlo con
ddl-auto=update. Si administras los cambios manualmente en Workbench, utiliza
sql/06-indice-ingresos.sql después de los scripts de instalación anteriores.
El archivo es repetible y no borra registros ni reinicia numeraciones.

Este índice mejora las búsquedas por rango de fechas. Las consultas de resumen
leen metadatos y agregados, sin descargar el contenido binario de las imágenes.

## Organización por capas

| Capa o componente | Archivos | Responsabilidad |
|---|---|---|
| Entity | Payment.java | Mapeo de pagos existente e índice de fecha/historia. |
| Repository | PaymentRepository.java; IngresoProyecciones.java | Consultas agregadas y paginadas, sin cargar imágenes. |
| Service | IngresosService.java | Validación de filtros, límites de períodos, sumas decimales, comparación y serie. |
| Service | IngresosExcelService.java | Exportación del mismo conjunto filtrado, en una lectura consistente. |
| DTO | ConsultaIngresos.java; IngresosVista.java | Contratos de filtros y resultados, sin exponer entidades JPA. |
| Controller | IngresosController.java | Rutas GET /api/ingresos y /exports/ingresos.xlsx. |
| Controller | PageController.java | Página /ingresos. |
| Vista | templates/ingresos.html | Indicadores, filtros, gráfica y tabla. |
| Recursos | static/js/ingresos.js; static/css/ingresos.css | Interacción, descarga y diseño adaptable. |
| Pruebas | IngresosIntegrationTest.java | Totales, límites, filtros, exportación y acceso autenticado. |

Los cálculos monetarios se realizan con BigDecimal en Java. La consulta de un
panel y la exportación emplean transacciones de lectura REPEATABLE_READ.
Las rutas utilizan la autenticación existente y no admiten acceso anónimo.
El panel solo consulta datos; los cambios siguen pasando por los servicios
de historias clínicas.

## Ejecución

Se mantiene Java 21, Spring Boot, MySQL y el odontograma 3D.
El ZIP incluye el JAR actualizado en bin/odontopoint-web.jar.

Extrae el ZIP, abre OdontoPoint-Web en VS Code y ejecuta Iniciar-Windows.bat.
Introduce el usuario y contraseña que utilizas para tu conexión a MySQL.
Luego abre http://localhost:8080 y entra en Control de ingresos.

Para una prueba local con otra base H2:
java -jar bin/odontopoint-web.jar --spring.profiles.active=h2

Si modificas código fuente, ejecuta Compilar-Windows.bat para actualizar el JAR
de bin. Si usas Run de VS Code, aplica tus credenciales en la configuración o
en las variables ODONTO_MYSQL_USER y ODONTO_MYSQL_PASSWORD de ese proceso.
