package gr.persona.repository;

import gr.persona.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de dominio para consultar Persona en GoldenRecord. Sin SQL, sin JDBC.
 * Antes: 4 archivos .sql con placeholders {{...}} ejecutados directamente.
 * Ahora: interfaz con métodos de negocio y parámetros tipados.
 */
public interface PersonaRepository {

    /**
     * Resuelve un ID GoldenRecord desde cualquier identificador externo.
     * Antes: resolver-identificador.sql con {{ID_SISTEMA}}, {{ID_EXTERNO}}.
     */
    Optional<IdentificadorPersona.IdGoldenRecord> resolverIdGoldenRecord(
            SistemaGoldenRecord sistemaOrigen, String idExterno);

    /**
     * Obtiene todas las peticiones vigentes de una persona.
     * Antes: consultar-estado-destinos.sql con {{ID_ENTIDAD}}.
     */
    List<PeticionEstado> consultarEstadoDestinos(
            IdentificadorPersona.IdGoldenRecord idGoldenRecord);

    /**
     * Consulta una petición concreta por UUID (seguimiento de bloqueos).
     * Antes: consultar-bloqueo.sql con {{UUID}}.
     */
    Optional<PeticionEstado> consultarPorUuid(UUID uuid);

    /**
     * Obtiene los errores Data Quality pendientes de una persona.
     * Antes: consultar-dq-pendiente.sql con {{ID_ENTIDAD}}.
     */
    List<ErrorDataQuality> consultarDqPendiente(
            IdentificadorPersona.IdGoldenRecord idGoldenRecord);
}
