---
name: gr-consulta-persona
description: Consultar el estado de integración de una persona en GoldenRecord mediante Oracle SQLcl MCP, a partir de un ID GoldenRecord de 9 dígitos, un EMPLID PeopleSoft de 8 dígitos o un ID Salesforce de 18 caracteres. Usar cuando el usuario pregunte por sincronizaciones, destinos, bloqueos, estados anómalos o errores Data Quality de la entidad Persona. Resolver primero un único ID GR, consultar siempre la vista de estado y DQ cuando su plantilla esté validada, seguir UUID_BLOQUEANTE sin asumir que termina en ERROR y devolver un diagnóstico read-only con enlace MANTGR. No usar para modificar, reprocesar ni decidir destinos esperados.
---

# Consultar una persona en GoldenRecord

## Objetivo

Obtener un diagnóstico de solo lectura sobre el estado actual de una persona en GoldenRecord, sus destinos visibles, posibles bloqueos y errores Data Quality pendientes.

Usar exclusivamente Oracle SQLcl MCP con `sql_run`. No ejecutar escrituras ni generar consultas distintas de las plantillas aprobadas de esta skill.

## Flujo operativo

La secuencia de trabajo esta definida en `AGENTS.md §5`. Esta skill proporciona las reglas y referencias a los archivos SQL y documentacion necesarios en cada paso.

### Recursos por paso

| Paso (AGENTS.md §5) | Recurso en esta skill |
|---|---|
| 1. Detectar/clasificar identificador | `references/identificadores.md` |
| 2. Resolver ID GoldenRecord | `references/resolver-identificador.sql` |
| 3. Consultar estado vigente | `references/consultar-estado-destinos.sql` |
| 4. Interpretar estados y umbrales | `references/estados-gr.md` |
| 5. Resolver bloqueos | `references/bloqueos.md` + `references/consultar-bloqueo.sql` |
| 6. Consultar Data Quality | `references/consultar-dq-pendiente.sql` |
| 7. Informar detalle de errores | Evidencia de la vista principal; `RESULTADOS_ATR` pendiente |
| 8. Generar enlace MANTGR | `references/mantgr.md` |
| 9. Redactar respuesta | `references/formato-respuesta.md` |

## Reglas de identificación

- Aceptar exactamente 8 dígitos como EMPLID PeopleSoft, `ID_SISTEMA = 4`.
- Aceptar exactamente 9 dígitos como ID GoldenRecord.
- Aceptar exactamente 18 caracteres alfanuméricos como ID Salesforce, `ID_SISTEMA = 3`.
- Tratar siempre la entidad Persona con `ID_TIPO_ENTIDAD = 2`.
- Si no se puede clasificar de forma inequivoca, pedir un identificador explicito.
- Si aparecen varios identificadores distintos, no elegir uno automaticamente.
- Si una resolucion devuelve cero filas:
  > No he podido identificar de forma univoca a la persona. ?Puedes ser mas preciso o facilitarme el ID GoldenRecord, el EMPLID o el ID de Salesforce?
- Si devuelve mas de una fila:
  > He encontrado mas de un resultado. ?Puedes ser mas preciso o facilitarme alguno de los identificadores admitidos?
- No eliminar ceros iniciales de un EMPLID.

## Reglas de consulta

- Ejecutar solo `SELECT` mediante `sql_run`.
- Sustituir únicamente los parámetros indicados en las plantillas.
- No usar `SELECT *`.
- No añadir tablas, joins, columnas o filtros no aprobados.
- No usar `sqlcl_run`, PL/SQL, DML ni DDL.
- No calcular manualmente la última petición: la vista de estado ya resuelve el estado vigente.
- No inferir que una persona debería aparecer en un destino que no devuelve la vista.
- Consultar DQ siempre, incluso si todos los destinos visibles están en `COMPLETADO`, una vez validada técnicamente la plantilla. Si no está validada o no puede ejecutarse, informar de la limitación y clasificar el resultado como `NO DETERMINADO`.

## Seguimiento de bloqueos

- No asumir que `UUID_BLOQUEANTE` referencia una petición en `ERROR`.
- Consultar `UUID = UUID_BLOQUEANTE` y mostrar el estado real localizado.
- Si el registro localizado también está en `BLOQUEO`, continuar con su `UUID_BLOQUEANTE`.
- Detenerse al alcanzar un estado distinto de `BLOQUEO`, un UUID inexistente, un UUID repetido o 10 saltos.
- Si la cadena termina en un estado distinto de `ERROR`, indicarlo como comportamiento inesperado que requiere revisión.
- Si no puede determinarse el final, explicarlo sin inventar la causa.

## Protección de datos

- Permitir identificadores personales como entrada de consulta.
- No mostrar nombre, apellidos, correo, `USUARIO_PETICION`, `ROL_PETICION` ni otros datos personales.
- No mostrar `VALOR_ERROR` por defecto; puede contener información sensible.
- Mostrar solo identificadores técnicos necesarios, estados, sistemas, fechas, códigos y descripciones de error autorizadas.

## Límites

- No modificar datos ni estados.
- No reprocesar peticiones.
- No decidir qué destinos deberían existir.
- No publicar en Jira en esta versión.
- No consultar personas por nombre o apellidos en esta versión.
- No afirmar que se consultó `RESULTADOS_ATR`: su relación y estructura todavía no están incorporadas a esta skill.
- Si falla Oracle o MCP, indicar que no fue posible completar la consulta; no presentar un diagnóstico parcial como definitivo.

## Generar enlace MANTGR

Cuando se haya resuelto un único `ID_ENTIDAD`, generar el enlace definido en
`references/mantgr.md`.

Añadirlo siempre al final de la respuesta.

No utilizar el identificador de entrada si todavía es un EMPLID o un ID de
Salesforce. Utilizar exclusivamente el ID GoldenRecord resuelto.
