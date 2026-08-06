package gr.persona.repository;

import gr.persona.domain.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Única clase de toda la aplicación que contiene SQL.
 * Si se migra a MongoDB, REST API, o cualquier otra fuente, solo se cambia aquí.
 */
@Repository
class OraclePersonaRepository implements PersonaRepository {

    private static final int ID_TIPO_ENTIDAD_PERSONA = 2;

    private final JdbcTemplate jdbc;

    OraclePersonaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // --- Row mappers (privados) ---

    private static final RowMapper<PeticionEstado> PETICION_MAPPER = (rs, _rowNum) ->
            new PeticionEstado(
                    UUID.fromString(rs.getString("UUID")),
                    Optional.ofNullable(rs.getString("UUID_BLOQUEANTE")).map(UUID::fromString),
                    rs.getString("ID_ENTIDAD"),
                    SistemaGoldenRecord.porId(rs.getInt("ID_SISTEMA"))
                            .orElseThrow(() -> new IllegalStateException("Sistema desconocido: " + rs.getInt("ID_SISTEMA"))),
                    tsToInstant(rs.getTimestamp("CREACION")),
                    tsToInstant(rs.getTimestamp("ACTUALIZACION")),
                    tsToInstant(rs.getTimestamp("FECHA_PRIMERA_PETICION")),
                    EstadoGoldenRecord.valueOf(rs.getString("ESTADO")),
                    rs.getString("SISTEMA_DESTINO_NOMBRE"),
                    rs.getString("SISTEMA_ORIGEN_NOMBRE")
            );

    private static final RowMapper<ErrorDataQuality> DQ_MAPPER = (rs, _rowNum) ->
            new ErrorDataQuality(
                    rs.getLong("ID"),
                    SistemaGoldenRecord.porId(rs.getInt("ID_SISTEMA"))
                            .orElseThrow(() -> new IllegalStateException("Sistema DQ desconocido: " + rs.getInt("ID_SISTEMA"))),
                    rs.getString("ID_EXTERNO"),
                    tsToInstant(rs.getTimestamp("FECHA_PETICION")),
                    rs.getString("CODIGO_ERROR"),
                    rs.getString("descripcion_error")
            );

    private static Instant tsToInstant(Timestamp ts) {
        return ts == null ? null : ts.toInstant();
    }

    // --- Implementaciones (antes: archivos .sql sueltos) ---

    @Override
    public Optional<IdentificadorPersona.IdGoldenRecord> resolverIdGoldenRecord(
            SistemaGoldenRecord sistemaOrigen, String idExterno) {

        return jdbc.query(
                """
                SELECT ID_ENTIDAD, ID_SISTEMA, ID_EXTERNO, ID_TIPO_ENTIDAD
                FROM LGC_GOLDENRECORD.V_CORRELACIONES
                WHERE ID_TIPO_ENTIDAD = ?
                  AND ID_SISTEMA = ?
                  AND ID_EXTERNO = ?
                """,
                (rs, _rowNum) -> new IdentificadorPersona.IdGoldenRecord(rs.getString("ID_ENTIDAD")),
                ID_TIPO_ENTIDAD_PERSONA,
                sistemaOrigen.id(),
                idExterno
        ).stream().findFirst();
    }

    @Override
    public List<PeticionEstado> consultarEstadoDestinos(
            IdentificadorPersona.IdGoldenRecord idGoldenRecord) {

        return jdbc.query(
                """
                SELECT UUID, UUID_BLOQUEANTE, ID_ENTIDAD, ID_SISTEMA,
                       CREACION, ACTUALIZACION, FECHA_PRIMERA_PETICION,
                       USUARIO, ESTADO,
                       ID_EXTERNO_ORIGINAL,
                       SISTEMA_DESTINO_NOMBRE, SISTEMA_ORIGEN_NOMBRE
                FROM LGC_GOLDENRECORD.v_estado_final_peticiones_atr
                WHERE ID_ENTIDAD = ?
                """,
                PETICION_MAPPER,
                idGoldenRecord.valor()
        );
    }

    @Override
    public Optional<PeticionEstado> consultarPorUuid(UUID uuid) {
        return jdbc.query(
                """
                SELECT UUID, UUID_BLOQUEANTE, ID_ENTIDAD, ID_SISTEMA,
                       CREACION, ACTUALIZACION, FECHA_PRIMERA_PETICION,
                       USUARIO, ESTADO,
                       ID_EXTERNO_ORIGINAL,
                       SISTEMA_DESTINO_NOMBRE, SISTEMA_ORIGEN_NOMBRE
                FROM LGC_GOLDENRECORD.v_estado_final_peticiones_atr
                WHERE UUID = ?
                """,
                PETICION_MAPPER,
                uuid.toString()
        ).stream().findFirst();
    }

    @Override
    public List<ErrorDataQuality> consultarDqPendiente(
            IdentificadorPersona.IdGoldenRecord idGoldenRecord) {

        return jdbc.query(
                """
                SELECT DQ.ID, DQ.ID_SISTEMA, DQ.ID_TIPO_ENTIDAD,
                       DQ.ID_EXTERNO, DQ.FECHA_PETICION,
                       DQ.CODIGO_ERROR, DQ."descripcion_error"
                FROM LGC_GOLDENRECORD.v_hist_error_dqdg_pendientes DQ
                INNER JOIN LGC_GOLDENRECORD.V_CORRELACIONES C
                        ON C.ID_SISTEMA = DQ.ID_SISTEMA
                       AND C.ID_EXTERNO = DQ.ID_EXTERNO
                       AND C.ID_TIPO_ENTIDAD = DQ.ID_TIPO_ENTIDAD
                WHERE C.ID_TIPO_ENTIDAD = ?
                  AND C.ID_ENTIDAD = ?
                """,
                DQ_MAPPER,
                ID_TIPO_ENTIDAD_PERSONA,
                idGoldenRecord.valor()
        );
    }
}
