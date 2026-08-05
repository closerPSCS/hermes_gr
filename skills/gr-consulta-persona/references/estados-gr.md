# Estados GoldenRecord

Usar `CREACION` para calcular la antigüedad.

| Estado | Interpretación aprobada | Regla de antigüedad |
|---|---|---|
| `INICIADO` | Primer estado de la petición, asignado por el BPEL Iniciador. | Más de 1 hora: anómalo. |
| `REGISTRADO` | La entidad empieza a procesarse en el BPEL Generar Evento. | Más de 1 hora: anómalo. |
| `PROCESANDO` | La entidad ya ha pasado el primer destino, GoldenRecord. | Más de 2 horas: anómalo. |
| `ERROR` | Se produjo un error al guardar en GR, generar el evento o ejecutar un BPEL finalizador. | Incidencia. |
| `BLOQUEO` | Otra petición de la entidad está pendiente y bloquea la actual. | Seguir `UUID_BLOQUEANTE`. |
| `INCONSISTENTE` | Un estado `PROCESANDO` ha permanecido demasiado tiempo. | Incidencia. |
| `DESCARTADO_MANUAL` | El registro se descartó mediante actualización manual de la tabla. | Incidencia que requiere contexto operativo. |
| `COMPLETADO` | La petición se procesó correctamente y actualizó todos los sistemas aplicables. | Correcto. |

No interpretar más allá de su literal los estados aún no definidos:

- `DESCARTADO_AUTO`
- `DEFERRED-ERR`
- `DEFERRED-BLOQ`
- `DESCART_CLASIF`
- `DESCART_PDT_CLASIF`

Clasificar el resultado general con prudencia:

- `CORRECTO`: todas las filas visibles están en `COMPLETADO` y no hay DQ pendiente.
- `INCIDENCIA DETECTADA`: existe `ERROR`, `BLOQUEO`, `INCONSISTENTE`, `DESCARTADO_MANUAL`, un estado inicial/procesando fuera de umbral o un DQ pendiente.
- `EN PROCESO`: solo existen estados transitorios dentro de sus umbrales y no hay DQ pendiente.
- `NO DETERMINADO`: aparecen estados sin interpretación aprobada, datos insuficientes o una cadena de bloqueo no resoluble.
