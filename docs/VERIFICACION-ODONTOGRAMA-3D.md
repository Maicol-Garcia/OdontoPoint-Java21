# Verificación de la actualización del odontograma 3D

## Entorno

- Backend compilado y probado con JDK 21.
- Spring Boot 3.5.6 y Maven 3.9.11.
- Navegador de prueba: Chromium 141, automatizado con Playwright.
- Base H2 en memoria para las pruebas; sin utilizar datos reales de pacientes.
- Three.js 0.180.0 servido desde los recursos del propio proyecto.

## Pruebas del backend

Se ejecutó la suite existente mediante Maven:
- WebIntegrationTest: 9 pruebas.
- AgendaIntegrationTest: 4 pruebas.
- Resultado: 13 pruebas, 0 fallos, 0 errores.

Estas pruebas cubren el backend existente. No equivalen a probar todos los
dispositivos ni sustituyen la validación con la instalación MySQL del usuario.

## Pruebas de geometría

Se verificaron las 52 piezas de ambas denticiones:
- Coordenadas finitas y cinco grupos de superficie por corona.
- V hacia vestibular y L hacia lingual/palatina.
- M orientada hacia la línea media y D en dirección opuesta en los ocho cuadrantes.
- O situada en la zona de mordida.

## Pruebas de navegador sobre el servidor Spring Boot

1. Inicio del modelo con 32 piezas permanentes y la política CSP existente.
2. Marca creada desde el inspector 3D y reflejada en el odontograma 2D.
3. Marca creada en 2D y reflejada en el inspector 3D.
4. Diente completo, borrar y deshacer utilizando un estado compartido.
5. Cambio a 20 piezas temporales sin perder marcas permanentes.
6. Apertura, cámaras, zoom, arrastre y giro automático sin alterar los datos clínicos.
7. Selección de una corona mediante un clic real en el lienzo WebGL.
8. Guardado y recarga con recuperación de marcas de ambas denticiones.
9. Vista móvil de 390 píxeles con selección de pieza e inspector utilizable.
10. Respuestas PDF y Excel válidas después de guardar.
11. WebGL no disponible: retorno a la vista 2D con sus marcas conservadas.

Además, una comprobación focalizada verificó que pulsar la encía no seleccione
coronas ocultas detrás de ella y que un clic sobre una corona visible sí la seleccione.

También se revisó visualmente el visor renderizado y se corrigió un patrón HTML
del formulario inicial para que el guion sea válido en navegadores con expresiones
regulares Unicode v.

## Alcance

El modelo es ilustrativo. No se ha certificado precisión anatómica, medición
clínica ni correspondencia con un paciente. Las fotografías y los comprobantes
mantienen los módulos originales. El PDF conserva su odontograma 2D.
