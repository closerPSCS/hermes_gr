-- Sustituir {{ID_SISTEMA}} por 3 (Salesforce) o 4 (PeopleSoft).
-- Sustituir {{ID_EXTERNO}} por el valor sin comillas; la plantilla ya incluye comillas simples.
SELECT
    ID_ENTIDAD,
    ID_SISTEMA,
    ID_EXTERNO,
    ID_TIPO_ENTIDAD
FROM LGC_GOLDENRECORD.V_CORRELACIONES
WHERE ID_TIPO_ENTIDAD = 2
  AND ID_SISTEMA = {{ID_SISTEMA}}
  AND ID_EXTERNO = '{{ID_EXTERNO}}';
