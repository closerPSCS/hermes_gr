package gr.persona.domain;

import java.util.List;
import java.util.UUID;

/**
 * Cadena de bloqueos resuelta.
 */
public record CadenaBloqueo(
        List<PeticionEstado> saltos,
        PeticionEstado estadoFinal,
        boolean pudoResolver,
        String motivoCorte
) {}
