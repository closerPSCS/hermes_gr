# Seguimiento de bloqueos

Para cada fila con `ESTADO = BLOQUEO`:

1. Leer `UUID_BLOQUEANTE`.
2. Consultar la misma vista con `UUID = UUID_BLOQUEANTE`.
3. Registrar el UUID visitado.
4. Mostrar el estado real de la fila localizada.
5. Si también está en `BLOQUEO`, repetir con su `UUID_BLOQUEANTE`.
6. Detenerse al encontrar un estado distinto de `BLOQUEO`.

Detener también cuando:

- `UUID_BLOQUEANTE` sea nulo o vacío;
- no exista la fila referenciada;
- se repita un UUID ya visitado;
- se alcancen 10 saltos.

No asumir que la cadena termina en `ERROR`. Si termina en otro estado, indicar que no coincide con el flujo esperado y requiere revisión. Si se alcanza `ERROR`, presentarlo como estado final localizado, sin atribuir una causa más concreta mientras no se consulte una fuente de detalle aprobada.
