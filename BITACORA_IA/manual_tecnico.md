# Manual Tecnico

Documentacion tecnica: arquitectura, configuraciones, dependencias.

---

## Agente N0 (GoldenRecord Persona) — Perfil `grncero`

### Busqueda por correo electronico (2026-08-06)

Permite resolver un ID GoldenRecord a partir de una direccion de correo electronico cuando no se detecta EMPLID, ID GR ni ID Salesforce en el texto del usuario.

#### Vista utilizada

```sql
LGC_GOLDENRECORD.v_loc
```

| Campo | Tipo | Descripcion |
|---|---|---|
| `ID_ENT` | NUMBER | ID GoldenRecord de la entidad |
| `ID_TCANCOM` | NUMBER | Tipo de canal de comunicacion (fijo `48397`) |
| `DIRECCION_INTERNET` | VARCHAR2 | Direccion de correo electronico |

#### Consulta SQL aprobada

**Archivo:** `skills/gr-consulta-persona/references/buscar-por-correo.sql`

```sql
SELECT ID_ENT
FROM lgc_goldenrecord.v_loc vl
WHERE vl.ID_TCANCOM = 48397
AND vl.DIRECCION_INTERNET = '{{CORREO}}';
```

#### Flujo (AGENTS.md §5, Paso 1b)

1. Si no se detecta EMPLID, ID GR ni ID Salesforce pero hay un correo → ejecutar `buscar-por-correo.sql`
2. 1 fila → usar `ID_ENT` como ID GoldenRecord y continuar al Paso 3
3. 0 filas → informar que no se encontro
4. >1 fila → pedir confirmacion al usuario

#### Reglas de prioridad

- Si hay varios identificadores y uno es un correo, se ignoran los correos y se usan los otros tipos
- El correo solo se usa como fallback cuando no hay otro identificador
- Solo se aceptan correos con dominio `@esade.edu` o `@alumni.esade.edu`
- La deteccion de correo tiene prioridad sobre pedir aclaraciones: si se detecta un correo valido sin otro ID, se resuelve directamente sin preguntar al usuario

#### Excepcion de privacidad

El correo utilizado como identificador de busqueda **si se muestra** en la respuesta (campo `Identificador utilizado`), a diferencia de la regla general que prohibe mostrar correos.

#### Archivos modificados

| Archivo | Cambio |
|---|---|
| `skills/gr-consulta-persona/references/buscar-por-correo.sql` | Nuevo — consulta de lookup |
| `skills/gr-consulta-persona/references/identificadores.md` | Nuevo tipo "Correo electronico" |
| `skills/gr-consulta-persona/SKILL.md` | Paso 1b, reglas de deteccion, mensajes de error, excepcion de proteccion |
| `workspace/AGENTS.md` | Paso 1b en secuencia, tabla de IDs, formato de respuesta, proteccion de datos |
| `workspace/knowledge/N0_contexto_funcional.md` | §5.5 documentando `v_loc`, flujo actualizado, casos de prueba 17-19 |

