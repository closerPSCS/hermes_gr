# N0 — Contexto funcional GoldenRecord Persona

**Version:** 0.2
**Fecha:** 2026-08-04
**Estado:** Borrador funcional consolidado
**Ambito:** Hermes · perfil/agente N0 · GoldenRecord · entidad Persona

> Documento de dominio. Conserva las decisiones funcionales estables del agente N0. Las reglas operativas, consultas SQL y procedimientos viven en `skills/gr-consulta-persona/` y `AGENTS.md`. Mantener versionado en Git.

---

## 1. Proposito del agente

N0 es un agente consultivo de GoldenRecord para la entidad Persona.

Su objetivo es responder, desde el chat de Hermes, al estado de integracion de una persona en los diferentes sistemas destino gestionados por GoldenRecord.

N0 debe:

- resolver una persona a partir de un identificador admitido;
- consultar el estado actual de sus sincronizaciones por destino;
- detectar estados anomalos segun umbrales definidos;
- comprobar siempre si existe algun error pendiente de Data Quality;
- seguir una cadena de bloqueos mientras permanezca en `BLOQUEO`, hasta alcanzar un estado final o una salvaguarda;
- presentar un diagnostico claro y un enlace a MANTGR.

N0 no debe:

- actualizar datos;
- reprocesar peticiones;
- corregir errores;
- decidir si una persona deberia estar integrada en un destino no devuelto por la vista;
- interpretar la ausencia de un destino como una incidencia;
- escribir en Jira en esta primera version;
- mostrar datos personales en la respuesta.

---

## 2. Canal y alcance de la primera version

La primera version funciona unicamente mediante el chat de Hermes.

La integracion con Jira se contempla para una fase posterior. La intencion futura es que N0 pueda redactar o publicar una respuesta en el ticket, aplicando los controles y revisiones que se definan.

---

## 3. Identificadores admitidos

N0 recibe texto libre. Por ahora debe reconocer los siguientes identificadores:

| Tipo | Sistema | Formato | Ejemplo |
|---|---:|---|---|
| ID GoldenRecord | GoldenRecord | 9 digitos | `100890864` |
| EMPLID | PeopleSoft | 8 digitos, incluidos ceros iniciales | `00089108` |
| ID Salesforce | Salesforce | 18 caracteres alfanumericos | `0032400001LBiigAAD` |

### 3.1 Formato tecnico

- EMPLID: 8 digitos, conservar ceros iniciales.
- ID GoldenRecord: 9 digitos.
- ID Salesforce: 18 caracteres alfanumericos.

### 3.2 Ampliacion futura

En una version posterior se podra buscar por nombre y apellidos.

> Las reglas de deteccion y los mensajes de respuesta exactos estan en `skills/gr-consulta-persona/SKILL.md` S Reglas de identificacion.

---

## 4. Sistemas conocidos

| ID sistema | Aplicacion |
|---:|---|
| 1 | ESADE XXI |
| 2 | IDM_RECTORAT |
| 3 | SALESFORCE |
| 4 | PEOPLESOFT |
| 7 | SAP |
| 10 | JOBTEASER |
| 12 | SAP_PROVEEDOR |
| 16 | TIMEEDIT ALU |
| 17 | TIMEEDIT PAS |
| 18 | ACADEM |
| 19 | WORKDAY |

Para la resolucion inicial de identificadores:

- `ID_SISTEMA = 3`: Salesforce.
- `ID_SISTEMA = 4`: PeopleSoft.
- `ID_TIPO_ENTIDAD = 2`: Persona.

N0 no mantiene una matriz de destinos esperados. Solo informa sobre los destinos devueltos por GoldenRecord.

---

## 5. Fuentes de datos

### 5.1 Correlaciones

Objeto:

```sql
LGC_GOLDENRECORD.V_CORRELACIONES
```

Campos relevantes:

| Campo | Descripcion |
|---|---|
| `ID_ENTIDAD` | Identificador GoldenRecord de la entidad |
| `ID_SISTEMA` | Identificador del sistema externo |
| `ID_EXTERNO` | Identificador de la persona en el sistema externo |
| `ID_TIPO_ENTIDAD` | Tipo de entidad; para Persona siempre debe ser `2` |

Usos:

- resolver un EMPLID o un ID Salesforce a `ID_ENTIDAD`;
- obtener todas las correlaciones externas de una persona;
- relacionar una persona con los errores pendientes de Data Quality.

### 5.2 Estado final de peticiones por destino

Objeto:

```sql
LGC_GOLDENRECORD.v_estado_final_peticiones_atr
```

La vista devuelve una fila por peticion y destino.

Ejemplo: si PeopleSoft envia una peticion y falla en Salesforce y SAP, la vista devuelve dos registros para esa peticion, uno por cada destino afectado.

La vista ya resuelve cual es la ultima peticion vigente de la persona. Para consultar el estado actual basta con filtrar por `ID_ENTIDAD`.

N0 no debe implementar logica adicional para:

- buscar la ultima peticion;
- agrupar peticiones;
- ordenar historicos;
- reconstruir el estado actual.

Campos funcionales conocidos:

| Campo | Descripcion |
|---|---|
| `ID_ENTIDAD` | Identificador de la entidad |
| `ID_SISTEMA` | ID de sistema propio de la aplicacion |
| `CREACION` | Timestamp de entrada de la peticion en GR |
| `ACTUALIZACION` | Timestamp de actualizacion del registro |
| `FECHA_PRIMERA_PETICION` | Fecha de la primera peticion |
| `USUARIO` | Usuario que realiza la peticion en el sistema origen |
| `ESTADO` | Estado actual de la sincronizacion |
| `UUID_BLOQUEANTE` | UUID que provoca el bloqueo cuando `ESTADO = BLOQUEO` |
| `ID_EXTERNO_ORIGINAL` | ID externo conservado en peticiones de alta |
| `SISTEMA_DESTINO_NOMBRE` | Nombre del sistema destino |
| `SISTEMA_ORIGEN_NOMBRE` | Nombre del sistema origen |

### 5.3 Errores pendientes de Data Quality

Objeto:

```sql
LGC_GOLDENRECORD.v_hist_error_dqdg_pendientes
```

Los bloqueos de Data Quality no aparecen en `v_estado_final_peticiones_atr`.

Por este motivo, N0 debe ejecutar siempre la consulta de DQ despues de resolver la persona, para detectar si algun sistema ha intentado enviar una integracion y no lo ha conseguido.

Campos conocidos:

| Campo | Descripcion |
|---|---|
| `ID` | ID incremental |
| `ID_SISTEMA` | Sistema origen de la peticion |
| `ID_TIPO_ENTIDAD` | Tipo de entidad |
| `ID_EXTERNO` | ID externo en el sistema origen |
| `USUARIO_PETICION` | Usuario que realiza la peticion |
| `ROL_PETICION` | Rol del usuario que realiza la peticion |
| `FECHA_PETICION` | Timestamp de ejecucion del DQ/DG |
| `CODIGO_ERROR` | Codigo del error DQ/DG |
| `VALOR_ERROR` | Valor que provoca el error |
| `DESCRIPCION_ERROR` | Descripcion del error en castellano |

La vista DQ no contiene `ID_ENTIDAD`. La relacion con la persona se realiza mediante:

- `ID_SISTEMA`;
- `ID_EXTERNO`;
- `ID_TIPO_ENTIDAD = 2`;
- las correlaciones obtenidas de `V_CORRELACIONES`.

`VALOR_ERROR` no se mostrara por defecto, ya que podria contener datos personales o informacion sensible.

### 5.4 Detalle de errores de sincronizacion

Cuando una peticion termina en `ERROR`, N0 muestra la evidencia disponible en la vista principal.

No existe todavia una consulta aprobada sobre `RESULTADOS_ATR`: la definicion del objeto, sus campos y su relacion tecnica con la peticion siguen pendientes de documentar.

Hasta cerrar esta informacion, N0 no debe inventar el detalle del error ni afirmar que lo ha consultado.

---

## 6. Estados de sincronizacion

Estados conocidos:

```text
COMPLETADO
ERROR
INCONSISTENTE
INICIADO
PROCESANDO
REGISTRADO
BLOQUEO
DESCARTADO_MANUAL
DESCARTADO_AUTO
DEFERRED-ERR
DEFERRED-BLOQ
DESCART_CLASIF
DESCART_PDT_CLASIF
```

### 6.1 Interpretacion aprobada

| Estado | Significado funcional | Tratamiento por N0 |
|---|---|---|
| `INICIADO` | Primer estado de la peticion. Lo establece el BPEL Iniciador. | Estado inicial. Es anomalo si permanece mas de 1 hora desde `CREACION`. |
| `REGISTRADO` | La entidad empieza a ser procesada por el BPEL Generar Evento. | Estado inicial. Es anomalo si permanece mas de 1 hora desde `CREACION`. |
| `PROCESANDO` | La entidad ya ha superado el primer destino, GoldenRecord. | Estado temporal. Es anomalo si permanece mas de 2 horas desde `CREACION`. |
| `ERROR` | Se ha producido un error al guardar la peticion en GoldenRecord, al generar el evento o en algun BPEL finalizador. | Incidencia. Mostrar la evidencia disponible; el detalle tecnico requiere una consulta aprobada futura. |
| `BLOQUEO` | Ha entrado una nueva peticion de una entidad que ya tiene otra peticion en espera o *stand by*. La nueva peticion queda bloqueada hasta procesar la primera. | Incidencia. Debe seguirse `UUID_BLOQUEANTE` mientras la cadena continue en `BLOQUEO`; si termina en `ERROR`, se muestra como estado final localizado. |
| `INCONSISTENTE` | El estado `PROCESANDO` se ha mantenido durante demasiado tiempo. | Incidencia. Debe informarse como procesamiento inconsistente. |
| `DESCARTADO_MANUAL` | Registro descartado mediante una actualizacion manual de la tabla para retirar peticiones surgidas por algun problema. | Estado terminal no correcto. Debe indicarse que fue descartado manualmente, sin inferir la causa si no existe detalle adicional. |
| `COMPLETADO` | La peticion se ha procesado correctamente y se ha actualizado con exito en todos los sistemas que correspondian a esa peticion. | Correcto para la peticion y los destinos devueltos por la vista. |

Para calcular la antiguedad de `PROCESANDO`, `INICIADO` y `REGISTRADO`, se utiliza el campo `CREACION`.

La definicion operativa de estados y umbrales esta tambien en `skills/gr-consulta-persona/references/estados-gr.md`.

### 6.2 Estados pendientes de definicion funcional

Mientras no exista una definicion aprobada, N0 mostrara literalmente estos estados y explicara que su interpretacion funcional no esta documentada:

```text
DESCARTADO_AUTO
DEFERRED-ERR
DEFERRED-BLOQ
DESCART_CLASIF
DESCART_PDT_CLASIF
```

No debe clasificarlos automaticamente como correctos, errores o bloqueos.

---

## 7. Flujo funcional

Resumen del flujo operativo (la secuencia detallada paso a paso esta en `AGENTS.md §5`):

```text
1. Detectar identificador (EMPLID, ID GR, ID Salesforce) del texto del usuario.
2. Si no es ID GR, resolver mediante V_CORRELACIONES.
3. Consultar v_estado_final_peticiones_atr por ID_ENTIDAD.
4. Evaluar estados y umbrales segun §6 de este documento; bloqueos segun AGENTS.md §5 (Paso 5).
5. Consultar v_hist_error_dqdg_pendientes (plantilla pendiente de validacion).
6. Construir diagnostico y generar enlace MANTGR cuando exista un unico ID GR.
7. Responder segun el formato definido en AGENTS.md §9.
```

---

## 8. Casos minimos de prueba

1. ID GR valido con todos los destinos en `COMPLETADO` y sin DQ.
2. EMPLID valido que resuelve a un unico ID GR.
3. ID Salesforce valido que resuelve a un unico ID GR.
4. Identificador no encontrado.
5. Resolucion con mas de un resultado.
6. `PROCESANDO` durante menos de 2 horas.
7. `PROCESANDO` durante mas de 2 horas.
8. `INICIADO` durante mas de 1 hora.
9. `REGISTRADO` durante mas de 1 hora.
10. `BLOQUEO` que conduce directamente a `ERROR`.
11. Cadena de varios `BLOQUEO` hasta `ERROR`.
12. Cadena circular o superior al maximo permitido.
13. Destinos completados con DQ pendiente.
14. Estado sin interpretacion aprobada.
15. Peticion para actualizar o reprocesar datos: debe rechazarse.
16. Peticion para mostrar datos personales: debe rechazarse o minimizarse.

---

## 9. Decisiones pendientes

1. Definicion completa de `RESULTADOS_ATR`:
   - nombre exacto del objeto;
   - campos;
   - relacion con la peticion o destino;
   - datos que pueden mostrarse.
2. Significado funcional aprobado de:
   - `DESCARTADO_AUTO`;
   - `DEFERRED-ERR`;
   - `DEFERRED-BLOQ`;
   - `DESCART_CLASIF`;
   - `DESCART_PDT_CLASIF`.
3. Profundidad maxima definitiva para seguir bloqueos.
4. Politica futura de escritura en Jira y Human in the Loop.
5. Sustitucion futura de SQL libre por tools de dominio.

---

## 10. Referencias al skill

Los procedimientos operativos, consultas SQL, reglas de seguridad y formato de respuesta estan mantenidos en el skill. Este documento solo conserva el conocimiento de dominio.

| Recurso | Ubicacion |
|---|---|
| Consultas SQL aprobadas | `skills/gr-consulta-persona/references/*.sql` |
| Reglas de seguridad SQL | `skills/gr-consulta-persona/SKILL.md` § Reglas de consulta |
| Seguimiento de bloqueos | `skills/gr-consulta-persona/SKILL.md` § Seguimiento de bloqueos + `references/bloqueos.md` |
| Formato de respuesta | `skills/gr-consulta-persona/references/formato-respuesta.md` |
| Enlace MANTGR | `skills/gr-consulta-persona/references/mantgr.md` |
| Procedimiento completo | `skills/gr-consulta-persona/SKILL.md` |
| Instrucciones operativas | `AGENTS.md` |

---

## 11. Regla de mantenimiento

Actualizar este documento unicamente cuando cambie una decision funcional estable.

No almacenar aqui:

- secretos;
- credenciales;
- cadenas de conexion;
- datos de personas reales;
- resultados de ejecuciones;
- logs;
- sesiones;
- instrucciones temporales de despliegue.

Los cambios deben reflejarse tambien, segun corresponda, en:

- `AGENTS.md`, para reglas obligatorias del agente;
- `SKILL.md`, para el procedimiento operativo;
- ficheros `.sql`, para consultas aprobadas;
- tests, para evitar regresiones;
- `distribution.yaml`, para versionar la distribucion.
