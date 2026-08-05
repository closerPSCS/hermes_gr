-- Sustituir {{UUID}} por el valor sin comillas; la plantilla ya incluye comillas simples.
SELECT
    UUID,
    UUID_BLOQUEANTE,
    ID_ENTIDAD,
    ID_SISTEMA,
    CREACION,
    ACTUALIZACION,
    FECHA_PRIMERA_PETICION,
    USUARIO,
    ESTADO,
    ID_EXTERNO_ORIGINAL,
    SISTEMA_DESTINO_NOMBRE,
    SISTEMA_ORIGEN_NOMBRE
FROM LGC_GOLDENRECORD.v_estado_final_peticiones_atr
WHERE UUID = '{{UUID}}';
