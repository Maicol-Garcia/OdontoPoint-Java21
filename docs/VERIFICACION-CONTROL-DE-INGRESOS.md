# Verificación del control de ingresos

## Compilación y pruebas del backend

Se compiló el proyecto con JDK 21 y Maven 3.9.11.
Resultado de la suite completa: **21 pruebas correctas, 0 fallos y 0 errores**.

- WebIntegrationTest: 9 pruebas del sistema existente.
- AgendaIntegrationTest: 4 pruebas de agenda.
- IngresosIntegrationTest: 8 pruebas nuevas.

Las nuevas pruebas verifican:
- Sumas decimales exactas, promedio, comparación y pacientes distintos.
- Inclusión de ambos límites de fecha y uso de la fecha del pago.
- Trimestres, cambios de año y febrero bisiesto.
- Períodos vacíos y comparación con base cero.
- Fechas inválidas, rangos demasiado largos y filtros inválidos.
- Búsqueda por nombre, DNI e historia y tratamiento literal de % y _.
- Paginación estable y exportación completa aunque se esté en una página intermedia.
- Fechas reales y texto literal en Excel.
- Lectura de capturas existentes y reflejo de modificaciones o eliminaciones de pagos.
- Acceso autenticado y respuestas HTTP de los filtros y la exportación.

Las pruebas de integración utilizan una base H2 en memoria, en modo de
compatibilidad MySQL para el nuevo módulo. El script opcional de índice
está destinado a MySQL 8.0/8.4; no se ejecutó sobre la base particular del usuario.

## Navegador sobre el servidor real

Se ejecutaron 10 comprobaciones con Chromium 141 y datos sintéticos:

1. Navegación desde el menú, indicadores y gráfica anual.
2. Filtro trimestral y límites correspondientes.
3. Vista mensual con paginación y acceso a historia/comprobante.
4. Búsqueda de paciente que modifica indicadores, gráfica y detalle.
5. Descarga real del Excel filtrado.
6. Rango personalizado escrito en dd/mm/aaaa.
7. Período vacío con ceros y mensaje sin resultados.
8. Filtro inválido que elimina los resultados previos y permite corregir.
9. Diseño móvil a 390 píxeles y selección de barras por teclado.
10. Carga del odontograma 3D y recuperación de sus marcas existentes.

También se revisó la presentación del panel en escritorio y móvil.
La versión móvil reserva todo el ancho para los indicadores monetarios para
evitar que se partan sus decimales.

## Reproducción

Con JDK 21 y Maven disponibles:
mvn test

Prueba dirigida al módulo:
mvn -Dtest=IngresosIntegrationTest test

Las cuentas y pagos de las pruebas de navegador se crearon en una base en memoria.
La entrega no contiene esa base ni las cuentas de demostración.
