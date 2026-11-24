# Avance PA4 — sistema-pedidos-springboot

Módulo de revisión y trazabilidad de pedidos. Permite marcar un pedido como revisado, registrar una observación y consultar el estado y los indicadores de revisión desde `pedidos-reportes-service`.

## Cambios de persistencia

- Se agregó a `Pedido` el atributo booleano `revisado`, inicialmente `false` en la entidad.
- Se creó `PedidoRevision`, que define la tabla `pedido_revision` con `id`, `pedido_id`, `observacion` y `fecha_revision`.
- El mapeo incluye la clave foránea `fk_revision_pedido` y la restricción única `uq_pedido_revision` sobre `pedido_id`.
- La observación está definida con un máximo de 500 caracteres en la entidad y en el formulario.


## Revisión transaccional

Se incorporaron estos componentes:

| Componente                     | Responsabilidad                                                                   |
| ------------------------------ | --------------------------------------------------------------------------------- |
| `PedidoRevisionService`        | Contrato de la operación `revisarPedido(Long pedidoId, String observacion)`.      |
| `PedidoRevisionServiceImpl`    | Validaciones y coordinación de las dos escrituras mediante `@Transactional`.      |
| `PedidoRevisionJdbcRepository` | Comprobar existencia, marcar el pedido e insertar la revisión con `JdbcTemplate`. |

El Service valida ID mayor que cero, observación no nula ni vacía y existencia del pedido. Los rechazos de negocio utilizan `PedidoException`.

La operación sigue este recorrido:

```text
Controller MVC
    -> PedidoRevisionService
        -> PedidoRevisionJdbcRepository
            -> UPDATE pedido
            -> INSERT pedido_revision
```

El UPDATE utiliza `WHERE id = ? AND revisado = FALSE`. Si no afecta una fila, el Service rechaza la segunda revisión. La restricción única refuerza esa regla en el esquema.

Ambas escrituras pertenecen a una sola transacción local. Un fallo del INSERT provoca rollback del UPDATE. Dentro de este método no se realizan llamadas HTTP a Inventario ni a Reportes.

## Supervisión mediante Spring MVC

Se creó `PedidoSupervisionController` con las siguientes rutas relativas al contexto `/sistema-pedidos`:

| Método | Ruta                                    | Función                                           |
| ------ | --------------------------------------- | ------------------------------------------------- |
| GET    | `/supervision/pedidos?estado=PENDIENTE` | Consultar pendientes y resumen remoto.            |
| GET    | `/supervision/pedidos?estado=REVISADO`  | Consultar revisados y resumen remoto.             |
| POST   | `/supervision/pedidos/{id}/revisar`     | Revisar el pedido con el parámetro `observacion`. |

El GET prepara el `Model` con `pedidos`, `resumen` y `estado`. Un estado distinto de `PENDIENTE` o `REVISADO` devuelve HTTP 400.

El POST delega en el Service y aplica Post/Redirect/Get. Los mensajes de éxito o rechazo se trasladan mediante atributos flash. La revisión duplicada utiliza un mensaje controlado; no se implementó una API de escritura que responda 409.

## Vista y navegación

- `supervision-pedidos.jsp`: resumen general, filtro por estado y tabla con ID, cliente, producto, total, estado, observación y fecha de revisión.
- Formulario de revisión para pedidos pendientes y usuarios con rol `OPERADOR`.
- `supervision-pedidos.css`: estilos propios, tarjetas, estados visuales y adaptación a pantallas pequeñas.
- Enlace desde `pedidos.jsp` hacia supervisión y enlace de retorno a Pedidos.
- Presentación del contenido ingresado por el usuario mediante `c:out`.

Acceso local:

```text
http://localhost:8081/sistema-pedidos/supervision/pedidos
```

## Integración con Reportes

Se amplió `PedidoReporteClient` y su implementación `PedidoReporteRestClient` con:

- `listarRevisiones(String estado)`.
- `obtenerResumenRevisiones()`.

Se agregaron los DTO de transporte `PedidoRevisionResponse` y `ResumenRevisionResponse`, independientes de las entidades JPA.

El cliente reutiliza el `RestClient` y los timeouts configurados. Los errores HTTP y de conexión se traducen a `PedidoReporteNoDisponibleException`. El Controller devuelve HTTP 503 con un mensaje comprensible cuando no puede consultar Reportes.

La consulta remota ocurre fuera de la transacción de revisión. Una caída de Reportes no revierte una revisión ya confirmada.

## Seguridad

- GET de supervisión habilitado para `OPERADOR` y `CONSULTA`.
- POST de revisión habilitado únicamente para `OPERADOR`.
- Protección CSRF conservada en los formularios.
- Cierre de sesión mediante POST con token CSRF.

## Pruebas de PA4

| Clase                                  | Cobertura implementada                                                                           |
| -------------------------------------- | ------------------------------------------------------------------------------------------------ |
| `PedidoRevisionServiceIntegrationTest` | Commit, rollback real ante fallo del INSERT, revisión duplicada e ID inexistente.                |
| `PedidoReporteRestClientTest`          | Traducción de HTTP 500 y error de conexión.                                                      |
| `PedidoSupervisionControllerTest`      | Model del GET, POST con redirección, rechazo sin CSRF, rechazo del rol CONSULTA y respuesta 503. |

Las pruebas transaccionales utilizan el perfil `test` y H2. El rollback se provoca mediante una restricción CHECK temporal que rechaza `FORZAR_FALLO`, y se comprueba que no queden cambios parciales. El cliente HTTP y los colaboradores del Controller se simulan para aislar esos escenarios.

Para ejecutar también las pruebas del proyecto base:

```powershell
mvn test
```