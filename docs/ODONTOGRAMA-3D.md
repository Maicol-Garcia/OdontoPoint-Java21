# OdontoPoint — Odontograma con dentadura 3D

## Qué se incorporó

El apartado **Odontograma** de la historia permite alternar entre:
- **Dentadura 3D**: encías, coronas con formas diferenciadas y dos arcadas.
- **Odontograma 2D**: el esquema de 52 piezas que ya utilizaba la aplicación.

El modelo tridimensional es ilustrativo y paramétrico. No es un escaneo intraoral,
no reproduce la anatomía particular de un paciente y no se utiliza para medir
distancias, oclusión ni planificar una intervención.

Se diferencian incisivos, caninos, premolares y molares; la dentición temporal
tiene 20 piezas y la permanente 32. La selección utiliza los números FDI.

## Cómo utilizarlo

1. Abre o crea una historia y entra en **Odontograma**.
2. Arrastra con el botón izquierdo para girar. Usa la rueda para acercar o alejar
   y el botón derecho para desplazar la dentadura. En pantalla táctil usa un dedo
   para girar y dos para desplazar/acercar.
3. Cambia la apertura de la boca con el deslizador (0 a 40 grados).
4. Utiliza **Frente**, **Derecha**, **Izquierda**, **Oclusal** o **Restablecer**.
   La vista oclusal aísla la arcada inferior si estaban visibles ambas. Para ver
   la superior desde su cara oclusal, elige primero **Solo superior** y luego
   **Oclusal**.
5. Elige una pieza haciendo clic sobre su corona o mediante la lista FDI.
   Un clic también identifica la superficie visible; arrastrar no registra marcas.
6. Elige la superficie V, L, M, D u O y un color en la paleta.
7. Pulsa **Aplicar marca**. Para las cinco superficies activa **Diente completo**.
   **Borrar** y **Deshacer** funcionan en ambas vistas.
8. Pulsa **Guardar historia** para persistir los cambios.

**Girar, abrir, aislar una arcada o cambiar la dentición no guarda ni borra marcas.**
La dentición temporal y la permanente conservan sus registros independientemente.
Los colores mantienen la leyenda editable del consultorio; el modelo no asigna
por sí solo un diagnóstico a un color.

## Datos y compatibilidad

No requiere tablas, columnas, migraciones ni cambios de contraseña de MySQL.
Utiliza el contrato existente: claves como `11:V` y valores
`NEGRO`, `AMARILLO`, `VERDE`; borrar elimina la clave.

Las dos vistas comparten el mismo estado en history.js, el control de cambios,
el guardado y el historial de deshacer (máximo 100 operaciones).
Al recargar se recuperan las marcas de la historia existente.

Los reportes PDF y Excel continúan con el formato del proyecto original.
**El PDF mantiene el odontograma 2D; no incluye una captura del visor 3D.**
La posición de cámara y la apertura son controles visuales y no se almacenan.

## Organización de archivos

| Archivo | Responsabilidad |
|---|---|
| templates/history.html | Controles y estructura del visor dentro de la historia. |
| static/css/odontogram-3d.css | Estilos del visor, inspector, pantalla completa y adaptación móvil. |
| static/js/dental-model.js | Geometría de encías y coronas; identificación FDI; superficies y materiales. |
| static/js/odontogram-3d.js | Cámara, luces, selección, controles, apertura y estados del visor. |
| static/js/history.js | Estado clínico compartido, marcas, deshacer y guardado existente. |
| static/js/vendor/ | Three.js 0.180.0 y controles locales con licencia MIT. |
| templates/fragments.html | Carga de la nueva hoja de estilos local. |

El backend sigue organizado en entity, repository, service y controller.
No se trasladaron reglas clínicas ni persistencia al motor 3D. Three.js se ocupa
de la representación en el navegador y Spring Boot conserva la seguridad y los datos.

La librería se distribuye dentro del proyecto: no se carga desde una CDN,
no se envían datos clínicos a servicios de modelado y no necesitas instalar Node.js
ni npm para compilar o ejecutar la aplicación. Se conserva la política CSP
del servidor sin habilitar scripts externos ni código inline.

## Requisitos y arranque

- JDK 21 y una versión reciente de Chrome, Edge o Firefox con WebGL 2 habilitado.
- MySQL configurado como en el proyecto anterior, o el perfil H2 para una prueba local.
- El ZIP incluye bin/odontopoint-web.jar compilado con Java 21.

En PowerShell, desde la carpeta OdontoPoint-Web:

```powershell
java -version
.\Iniciar-Windows.bat
```

También puedes arrancar el JAR directamente:

```powershell
java -jar .\bin\odontopoint-web.jar
```

Para probar con H2 (base local distinta de tu MySQL):

```powershell
java -jar .\bin\odontopoint-web.jar --spring.profiles.active=h2
```

Abre http://localhost:8080. Al modificar código fuente, recompila con
`mvn clean package` o `Compilar-Windows.bat`; este último actualiza el JAR de bin.

Si ya usas la base de datos anterior, mantén tus variables ODONTO_MYSQL_* y tu
configuración. El proyecto no incluye datos, sesiones ni cuentas creadas en las pruebas.

## Si no aparece la dentadura

La vista 2D siempre está disponible mediante su botón. Si el navegador no logra
crear un contexto WebGL, el visor vuelve a 2D y muestra un aviso sin perder marcas.
Tras guardar, puedes habilitar la aceleración gráfica del navegador y recargar.
Si se pierde el contexto gráfico durante el uso, también se ofrece la vista 2D.

El modelo se crea al abrir el apartado dental y deja de dibujarse mientras
la pestaña del navegador o el apartado está oculto. El giro automático se desactiva
cuando el usuario mueve la cámara y respeta la preferencia de movimiento reducido.

## Licencia y referencias

La geometría paramétrica del modelo se incorpora como código fuente en dental-model.js.
Three.js y sus complementos mantienen su licencia MIT en
static/js/vendor/THREE-LICENSE.txt.

Documentación técnica:
- https://threejs.org/docs/pages/OrbitControls.html
- https://threejs.org/manual/pages/installation.html
