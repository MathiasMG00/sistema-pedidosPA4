<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="cssUrl" value="/assets/css/supervision-pedidos.css" />
<c:url var="pedidosUrl" value="/pedidos" />
<c:url var="logoutUrl" value="/logout" />
<c:url var="supervisionUrl" value="/supervision/pedidos" />

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Supervisión de pedidos</title>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>

<main class="page">
    <header class="page-header">
        <div>
            <p class="eyebrow">SUPERVISIÓN DE PEDIDOS</p>
            <h1>Revisión de pedidos</h1>
            <p class="subtitle">
                Consulta el estado de los pedidos y registra su revisión.
            </p>
        </div>

        <nav class="header-actions" aria-label="Acciones principales">
            <a class="button button-secondary" href="${pedidosUrl}">
                ← Volver a pedidos
            </a>

            <form method="post" action="${logoutUrl}">
                <input type="hidden"
                       name="${_csrf.parameterName}"
                       value="${_csrf.token}">
                <button class="button button-primary" type="submit">
                    Cerrar sesión
                </button>
            </form>
        </nav>
    </header>

    <c:if test="${not empty mensaje}">
        <div class="alert alert-success" role="status">
            <c:out value="${mensaje}" />
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="alert alert-error" role="alert">
            <c:out value="${error}" />
        </div>
    </c:if>

    <section class="card" aria-labelledby="resumen-title">
        <h2 id="resumen-title">Resumen general</h2>

        <c:choose>
            <c:when test="${not empty resumen}">
                <div class="table-container">
                    <table class="summary-table">
                        <thead>
                        <tr>
                            <th scope="col">Total pedidos</th>
                            <th scope="col">Revisados</th>
                            <th scope="col">Pendientes</th>
                            <th scope="col">Porcentaje revisado</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>
                                <c:out value="${resumen.totalPedidos()}" />
                            </td>
                            <td>
                                <c:out value="${resumen.totalRevisados()}" />
                            </td>
                            <td>
                                <c:out value="${resumen.totalPendientes()}" />
                            </td>
                            <td>
                                <c:out value="${resumen.porcentajeRevisado()}" /> %
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </c:when>

            <c:otherwise>
                <p class="muted">El resumen no está disponible.</p>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="card" aria-labelledby="filtro-title">
        <h2 id="filtro-title">Consultar pedidos por estado</h2>

        <form class="filter-form" method="get" action="${supervisionUrl}">
            <div class="field">
                <label for="estado">Estado de revisión</label>

                <select id="estado" name="estado">
                    <option value="PENDIENTE"
                        ${estado == 'PENDIENTE' ? 'selected' : ''}>
                        Pendientes
                    </option>
                    <option value="REVISADO"
                        ${estado == 'REVISADO' ? 'selected' : ''}>
                        Revisados
                    </option>
                </select>
            </div>

            <button class="button button-primary" type="submit">
                Consultar
            </button>
        </form>

        <p class="help-text">
            Selecciona un estado para consultar los pedidos.
        </p>
    </section>

    <section class="card" aria-labelledby="pedidos-title">
        <div class="section-heading">
            <h2 id="pedidos-title">
                ${estado == 'REVISADO'
                    ? 'Pedidos revisados'
                    : 'Pedidos pendientes'}
            </h2>

            <span class="badge badge-neutral">
                ${estado == 'REVISADO' ? 'REVISADO' : 'PENDIENTE'}
            </span>
        </div>

        <c:choose>
            <c:when test="${not empty pedidos}">
                <div class="table-container">
                    <table class="orders-table">
                        <thead>
                        <tr>
                            <th scope="col">ID</th>
                            <th scope="col">Cliente</th>
                            <th scope="col">Producto</th>
                            <th scope="col">Total</th>
                            <th scope="col">Estado</th>
                            <th scope="col">Revisión</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:forEach items="${pedidos}" var="p">
                            <tr>
                                <td class="nowrap">
                                    #<c:out value="${p.id()}" />
                                </td>

                                <td>
                                    <c:out value="${p.cliente()}" />
                                </td>

                                <td>
                                    <c:out value="${p.productoNombre()}" />
                                </td>

                                <td class="nowrap">
                                    S/ <c:out value="${p.total()}" />
                                </td>

                                <td>
                                    <span class="badge ${p.revisado()
                                        ? 'badge-success'
                                        : 'badge-pending'}">
                                        ${p.revisado()
                                            ? 'Revisado'
                                            : 'Pendiente'}
                                    </span>
                                </td>

                                <td class="revision-cell">
                                    <c:choose>
                                        <c:when test="${p.revisado()}">
                                            <p class="observation">
                                                <c:out value="${p.observacion()}" />
                                            </p>

                                            <p class="help-text revision-date">
                                                Fecha:
                                                <c:out value="${p.fechaRevision()}" />
                                            </p>
                                        </c:when>

                                        <c:when test="${pageContext.request
                                            .isUserInRole('OPERADOR')}">

                                            <c:url var="revisarUrl"
                                                value="/supervision/pedidos/${p.id()}/revisar" />

                                            <form class="revision-form"
                                                  method="post"
                                                  action="${revisarUrl}">

                                                <input type="hidden"
                                                       name="${_csrf.parameterName}"
                                                       value="${_csrf.token}">

                                                <label class="sr-only"
                                                       for="observacion-${p.id()}">
                                                    Observación del pedido
                                                    <c:out value="${p.id()}" />
                                                </label>

                                                <textarea
                                                    id="observacion-${p.id()}"
                                                    name="observacion"
                                                    rows="2"
                                                    maxlength="500"
                                                    placeholder="Escribe la observación"
                                                    required></textarea>

                                                <button class="button button-primary"
                                                        type="submit">
                                                    Marcar como revisado
                                                </button>
                                            </form>
                                        </c:when>

                                        <c:otherwise>
                                            <span class="muted">
                                                Pendiente de revisión por un operador.
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>

            <c:otherwise>
                <div class="empty-state">
                    <c:choose>
                        <c:when test="${not empty error}">
                            No hay datos para mostrar en este momento.
                        </c:when>
                        <c:otherwise>
                            No se encontraron pedidos con el estado seleccionado.
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

</body>
</html>