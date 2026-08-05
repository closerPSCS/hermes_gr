# Formato de respuesta

Usar una respuesta ejecutiva y evitar tablas grandes cuando haya pocos destinos.

```markdown
## Persona consultada

- ID GoldenRecord: <id>
- Identificador utilizado: <tipo y valor>

## Estado general

- Resultado: CORRECTO | EN PROCESO | INCIDENCIA DETECTADA | NO DETERMINADO

## Sincronización por destinos

- <destino>: <estado y observación relevante>

## Bloqueos

- <resumen de la cadena, estado final real y anomalías>

## Data Quality

- Sin errores DQ pendientes.

<!-- O, si la consulta DQ no está disponible o su plantilla aún no está validada -->
- No se ha podido verificar Data Quality; el resultado general es NO DETERMINADO.

<!-- O, si existen -->
- Sistema origen: <sistema>
- Código: <código>
- Descripción: <descripción>
- Fecha: <fecha>

## Diagnóstico

<Conclusión breve basada solo en los resultados consultados.>

## Enlace de detalle

[MANTGR](<enlace generado según references/mantgr.md>)
```

Omitir secciones vacías salvo `Data Quality`, que debe confirmar expresamente si no hay errores pendientes.

No mostrar nombres, correos, `USUARIO_PETICION`, roles ni `VALOR_ERROR`.
