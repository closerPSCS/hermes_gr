package gr.persona.domain;

import java.time.Instant;

/**
 * Error Data Quality pendiente de {@code v_hist_error_dqdg_pendientes}.
 */
public record ErrorDataQuality(
        long id,
        SistemaGoldenRecord sistema,
        String idExterno,
        Instant fechaPeticion,
        String codigoError,
        String descripcionError
) {}
