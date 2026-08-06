package gr.persona.domain;

import java.util.List;

/**
 * Diagnóstico completo de una persona en GoldenRecord.
 * Reemplaza al Markdown manual de references/formato-respuesta.md.
 */
public record DiagnosticoPersona(
        IdentificadorPersona.IdGoldenRecord idGoldenRecord,
        IdentificadorPersona identificadorEntrada,
        ClasificacionResultado resultado,
        List<PeticionEstado> destinos,
        List<CadenaBloqueo> bloqueos,
        List<ErrorDataQuality> erroresDQ,
        String diagnostico,
        String enlaceMantgr
) {
    public enum ClasificacionResultado { CORRECTO, INCIDENCIA_DETECTADA, EN_PROCESO, NO_DETERMINADO }
}
