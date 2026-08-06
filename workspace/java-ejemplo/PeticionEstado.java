package gr.persona.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Registro de la vista {@code v_estado_final_peticiones_atr}.
 * Antes: arrays de columnas sueltas (UUID, ESTADO, CREACION, SISTEMA_DESTINO_NOMBRE...)
 * Ahora: record inmutable con tipos concretos.
 */
public record PeticionEstado(
        UUID uuid,
        Optional<UUID> uuidBloqueante,
        String idEntidad,
        SistemaGoldenRecord sistema,
        Instant creacion,
        Instant actualizacion,
        Instant fechaPrimeraPeticion,
        EstadoGoldenRecord estado,
        String sistemaDestinoNombre,
        String sistemaOrigenNombre
) {
    public boolean estaBloqueada() {
        return estado == EstadoGoldenRecord.BLOQUEO && uuidBloqueante.isPresent();
    }

    public boolean esAnomala() {
        return estado.tipo() == EstadoGoldenRecord.TipoEstado.INCIDENCIA
                || estado.estaFueraDeUmbral(creacion);
    }
}
