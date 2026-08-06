package gr.persona.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Todos los estados posibles de GoldenRecord, con interpretación y reglas de antigüedad.
 * Antes: tabla Markdown en references/estados-gr.md, interpretada manualmente.
 * Ahora: enum tipado con lógica de clasificación encapsulada.
 */
public enum EstadoGoldenRecord {

    INICIADO("Primer estado de la petición, asignado por el BPEL Iniciador.",
            Duration.ofHours(1), TipoEstado.TRANSITORIO),

    REGISTRADO("La entidad empieza a procesarse en el BPEL Generar Evento.",
            Duration.ofHours(1), TipoEstado.TRANSITORIO),

    PROCESANDO("La entidad ya ha pasado el primer destino, GoldenRecord.",
            Duration.ofHours(2), TipoEstado.TRANSITORIO),

    ERROR("Se produjo un error al guardar en GR, generar el evento o ejecutar un BPEL finalizador.",
            null, TipoEstado.INCIDENCIA),

    BLOQUEO("Otra petición de la entidad está pendiente y bloquea la actual.",
            null, TipoEstado.BLOQUEO),

    INCONSISTENTE("Un estado PROCESANDO ha permanecido demasiado tiempo.",
            null, TipoEstado.INCIDENCIA),

    DESCARTADO_MANUAL("El registro se descartó mediante actualización manual de la tabla.",
            null, TipoEstado.INCIDENCIA),

    COMPLETADO("La petición se procesó correctamente y actualizó todos los sistemas aplicables.",
            null, TipoEstado.CORRECTO),

    // --- Estados pendientes de definición ---
    DESCARTADO_AUTO(null, null, TipoEstado.NO_DETERMINADO),
    DEFERRED_ERR(null, null, TipoEstado.NO_DETERMINADO),
    DEFERRED_BLOQ(null, null, TipoEstado.NO_DETERMINADO),
    DESCART_CLASIF(null, null, TipoEstado.NO_DETERMINADO),
    DESCART_PDT_CLASIF(null, null, TipoEstado.NO_DETERMINADO);

    public enum TipoEstado { TRANSITORIO, CORRECTO, INCIDENCIA, BLOQUEO, NO_DETERMINADO }

    private final String interpretacion;
    private final Duration umbralAnomalo;
    private final TipoEstado tipo;

    EstadoGoldenRecord(String interpretacion, Duration umbralAnomalo, TipoEstado tipo) {
        this.interpretacion = interpretacion;
        this.umbralAnomalo = umbralAnomalo;
        this.tipo = tipo;
    }

    public boolean estaFueraDeUmbral(Instant creacion) {
        return umbralAnomalo != null
                && Duration.between(creacion, Instant.now()).compareTo(umbralAnomalo) > 0;
    }

    public TipoEstado tipo() { return tipo; }

    public static final Set<EstadoGoldenRecord> ESTADOS_NO_INTERPRETADOS =
            Set.of(DESCARTADO_AUTO, DEFERRED_ERR, DEFERRED_BLOQ, DESCART_CLASIF, DESCART_PDT_CLASIF);
}
