# Avance PA4 — pedidos-reportes-service

Ampliación del servicio de reportes con consultas del estado de revisión y estadísticas de pedidos. Los recursos nuevos se consumen desde la pantalla de supervisión de `sistema-pedidos-springboot`.

## Funcionalidades desarrolladas

- Consulta de pedidos por estado `PENDIENTE` o `REVISADO`.
- Publicación de observación y fecha para pedidos revisados.
- Resumen con total de pedidos, revisados, pendientes y porcentaje revisado.
- Consultas con Spring JDBC sobre las tablas `pedido` y `pedido_revision` de MySQL.
- Respuestas JSON mediante DTO, sin exponer entidades JPA.

## Organización por capas

```text
RevisionReporteController
    -> RevisionReporteService
        -> RevisionReporteServiceImpl
            -> RevisionReporteJdbcRepository
                -> MySQL
```

| Componente | Responsabilidad |
| --- | --- |
| `RevisionReporteController` | Recibir solicitudes y convertir los resultados del Service en DTO JSON. |
| `RevisionReporteService` | Contrato de listado y resumen; define el enum `Estado`. |
| `RevisionReporteServiceImpl` | Convertir proyecciones al modelo de dominio y calcular indicadores. |
| `RevisionReporteJdbcRepository` | Ejecutar el listado parametrizado y consultar los conteos. |
| `PedidoRevisionProjection` | Representar las filas obtenidas mediante JDBC. |
| `PedidoRevision` y `ResumenRevision` | Modelos de dominio del Service. |
| `PedidoRevisionDto` y `ResumenRevisionDto` | Contratos de respuesta HTTP. |

Se conserva el patrón del servicio existente: Repository/Projection, Service/Domain y Controller/DTO. El Controller no contiene SQL ni realiza los cálculos estadísticos.

## Listado de revisiones

```http
GET /api/v1/pedidos/revisiones?estado=PENDIENTE
GET /api/v1/pedidos/revisiones?estado=REVISADO
```

URL local completa:

```text
http://localhost:8083/pedidos-reportes-service/api/v1/pedidos/revisiones?estado=REVISADO
```

El parámetro `estado` es obligatorio y admite `PENDIENTE` o `REVISADO`. Un valor no admitido devuelve HTTP 400.

La consulta utiliza `LEFT JOIN` entre `pedido` y `pedido_revision`, filtra con `WHERE p.revisado = ?` y ordena por ID descendente. Así también puede devolver pedidos pendientes sin registro de revisión.

Ejemplo ilustrativo de respuesta HTTP 200:

```json
[
  {
    "id": 1,
    "cliente": "Ana Torres",
    "productoNombre": "Laptop",
    "total": 200.00,
    "revisado": true,
    "observacion": "Pedido verificado",
    "fechaRevision": "2026-11-24T10:30:00"
  }
]
```

Para pendientes, `revisado` es `false` y los datos de trazabilidad no tienen valor. Cuando el filtro no encuentra pedidos, se devuelve una lista vacía.

## Estadísticas de revisión

```http
GET /api/v1/pedidos/estadisticas/revisiones
```

URL local completa:

```text
http://localhost:8083/pedidos-reportes-service/api/v1/pedidos/estadisticas/revisiones
```

Ejemplo ilustrativo de respuesta HTTP 200:

```json
{
  "totalPedidos": 4,
  "totalRevisados": 3,
  "totalPendientes": 1,
  "porcentajeRevisado": 75.00
}
```

El Repository obtiene el total y la cantidad de revisados utilizando agregaciones SQL. El Service calcula:

```text
totalPendientes = totalPedidos - totalRevisados
porcentajeRevisado = totalRevisados * 100 / totalPedidos
```

El porcentaje se calcula con `BigDecimal`, dos decimales y redondeo `HALF_UP`. Si no existen pedidos, se devuelve cero sin dividir entre cero.

## Relación con Pedidos

Este módulo consulta las tablas compartidas de MySQL. El atributo `revisado` y la tabla `pedido_revision` se definen desde `sistema-pedidos-springboot`.

La escritura de revisiones corresponde al Service transaccional de Pedidos. Reportes publica la información mediante HTTP y su cliente consumidor utiliza DTO propios.

## Pruebas de PA4

`RevisionReporteControllerTest` contiene tres pruebas:

| Prueba | Verificación |
| --- | --- |
| `debeRetornarPedidoRevisadoConTrazabilidad` | HTTP 200, JSON y campos del pedido revisado, incluida observación y fecha. |
| `debeRetornarResumenEnJson` | HTTP 200 y los cuatro indicadores del resumen. |
| `debeResponder400ParaEstadoInvalido` | HTTP 400 sin invocar al Service. |

Se utiliza MockMvc con el Service simulado mediante Mockito. Estas pruebas verifican el contrato del Controller; no ejecutan las consultas JDBC ni prueban el cálculo real del Service.


Para ejecutar todas las pruebas del servicio:

```powershell
mvn test
```