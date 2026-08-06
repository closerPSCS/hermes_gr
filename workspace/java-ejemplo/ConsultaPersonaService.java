package gr.persona.service;

import gr.persona.domain.*;
import gr.persona.domain.DiagnosticoPersona.ClasificacionResultado;
import gr.persona.repository.PersonaRepository;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Orquesta la consulta completa de una persona en GoldenRecord.
 * Antes: el agente interpretaba el SKILL.md y ejecutaba SQL paso a paso.
 * Ahora: método único con entrada/salida tipadas, toda la lógica de negocio centralizada.
 */
@Service
public class ConsultaPersonaService {

    private static final int MAX_SALTOS_BLOQUEO = 10;
    private static final String URL_BASE_MANTGR = "https://projava8.esade.edu/mantgr/detalle?idTipoEntidad=2&idGoldenRecord=";

    private final PersonaRepository repo;

    public ConsultaPersonaService(PersonaRepository repo) {
        this.repo = repo;
    }

    /**
     * Ejecuta el flujo completo de consulta.
     *
     * @param raw identificador en texto (EMPLID 8d, ID GR 9d, o ID Salesforce 18c)
     * @return diagnóstico completo
     */
    public DiagnosticoPersona diagnosticar(String raw) {
        var identificadorEntrada = IdentificadorPersona.parse(raw);
        var idGoldenRecord = resolverGoldenRecord(identificadorEntrada);
        var destinos = repo.consultarEstadoDestinos(idGoldenRecord);
        var bloqueos = resolverBloqueos(destinos);
        List<ErrorDataQuality> erroresDQ = consultarDQ(idGoldenRecord);
        var clasificacion = clasificar(destinos, bloqueos, erroresDQ);
        var diagnostico = redactarDiagnostico(destinos, bloqueos, erroresDQ, clasificacion);
        var enlace = URL_BASE_MANTGR + idGoldenRecord.valor();

        return new DiagnosticoPersona(
                idGoldenRecord, identificadorEntrada, clasificacion,
                destinos, bloqueos, erroresDQ, diagnostico, enlace);
    }

    // --- Paso 1-2: resolver ID GoldenRecord ---

    private IdentificadorPersona.IdGoldenRecord resolverGoldenRecord(IdentificadorPersona id) {
        if (id instanceof IdentificadorPersona.IdGoldenRecord gr) return gr;

        var sistema = id instanceof IdentificadorPersona.Emplid
                ? SistemaGoldenRecord.PEOPLESOFT
                : SistemaGoldenRecord.SALESFORCE;

        var valor = id instanceof IdentificadorPersona.Emplid e ? e.valor()
                : ((IdentificadorPersona.IdSalesforce) id).valor();

        return repo.resolverIdGoldenRecord(sistema, valor)
                .orElseThrow(() -> new NoSuchElementException(
                        "No se encontró ID GoldenRecord para " + sistema.nombre() + ":" + valor));
    }

    // --- Paso 5: seguimiento de bloqueos ---

    private List<CadenaBloqueo> resolverBloqueos(List<PeticionEstado> destinos) {
        return destinos.stream()
                .filter(PeticionEstado::estaBloqueada)
                .map(this::seguirCadena)
                .toList();
    }

    private CadenaBloqueo seguirCadena(PeticionEstado origen) {
        var visitados = new HashSet<UUID>();
        var saltos = new ArrayList<PeticionEstado>();

        var actual = origen;
        visitados.add(actual.uuid());
        saltos.add(actual);

        for (int i = 0; i < MAX_SALTOS_BLOQUEO && actual.uuidBloqueante().isPresent(); i++) {
            var siguiente = repo.consultarPorUuid(actual.uuidBloqueante().get());

            if (siguiente.isEmpty()) {
                return new CadenaBloqueo(saltos, actual, false,
                        "UUID_BLOQUEANTE " + actual.uuidBloqueante().get() + " no encontrado");
            }

            var sig = siguiente.get();
            if (!visitados.add(sig.uuid())) {
                return new CadenaBloqueo(saltos, sig, false,
                        "Ciclo detectado en UUID " + sig.uuid());
            }

            saltos.add(sig);
            if (sig.estado() != EstadoGoldenRecord.BLOQUEO) {
                return new CadenaBloqueo(saltos, sig, true, null);
            }
            actual = sig;
        }

        return new CadenaBloqueo(saltos, actual, false,
                "Alcanzado límite de " + MAX_SALTOS_BLOQUEO + " saltos sin resolver");
    }

    // --- Paso 6: Data Quality ---

    private List<ErrorDataQuality> consultarDQ(IdentificadorPersona.IdGoldenRecord idGoldenRecord) {
        try {
            return repo.consultarDqPendiente(idGoldenRecord);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // --- Paso 4 + clasificación ---

    private ClasificacionResultado clasificar(
            List<PeticionEstado> destinos,
            List<CadenaBloqueo> bloqueos,
            List<ErrorDataQuality> erroresDQ) {

        if (destinos.isEmpty()) return ClasificacionResultado.NO_DETERMINADO;

        boolean hayNoDeterminado = destinos.stream()
                .anyMatch(d -> EstadoGoldenRecord.ESTADOS_NO_INTERPRETADOS.contains(d.estado()));

        if (hayNoDeterminado) return ClasificacionResultado.NO_DETERMINADO;

        boolean hayIncidencia = destinos.stream().anyMatch(PeticionEstado::esAnomala)
                || bloqueos.stream().anyMatch(b -> !b.pudoResolver()
                        || b.estadoFinal().estado() == EstadoGoldenRecord.ERROR)
                || !erroresDQ.isEmpty();

        if (hayIncidencia) return ClasificacionResultado.INCIDENCIA_DETECTADA;

        boolean todasCompletadas = destinos.stream()
                .allMatch(d -> d.estado() == EstadoGoldenRecord.COMPLETADO);

        if (todasCompletadas) return ClasificacionResultado.CORRECTO;

        return ClasificacionResultado.EN_PROCESO;
    }

    // --- Paso 9: redactar diagnóstico ---

    private String redactarDiagnostico(
            List<PeticionEstado> destinos,
            List<CadenaBloqueo> bloqueos,
            List<ErrorDataQuality> erroresDQ,
            ClasificacionResultado clasificacion) {

        var sb = new StringBuilder();
        sb.append("Resultado: ").append(clasificacion).append("\n\n");

        sb.append("Destinos:\n");
        destinos.forEach(d -> sb.append("  - ").append(d.sistemaDestinoNombre())
                .append(": ").append(d.estado())
                .append(d.esAnomala() ? " [ANOMALO]" : "")
                .append("\n"));

        if (!bloqueos.isEmpty()) {
            sb.append("\nBloqueos:\n");
            bloqueos.forEach(b -> {
                sb.append("  - Origen: ").append(b.saltos().getFirst().uuid())
                        .append(" -> Final: ").append(b.estadoFinal().estado());
                if (!b.pudoResolver()) sb.append(" (").append(b.motivoCorte()).append(")");
                sb.append("\n");
            });
        }

        sb.append("\nData Quality: ");
        if (erroresDQ.isEmpty()) {
            sb.append("sin errores pendientes.\n");
        } else {
            sb.append(erroresDQ.size()).append(" error(es)\n");
            erroresDQ.forEach(e -> sb.append("  - [")
                    .append(e.codigoError()).append("] ").append(e.descripcionError()).append("\n"));
        }

        return sb.toString();
    }

    // --- Formateo de respuesta (antes: formato-respuesta.md) ---

    public String formatearRespuesta(DiagnosticoPersona d) {
        return """
                ## Persona consultada
                - ID GoldenRecord: %s
                - Identificador utilizado: %s

                ## Estado general
                - Resultado: %s

                ## Sincronización por destinos
                %s

                ## Bloqueos
                %s

                ## Data Quality
                %s

                ## Diagnóstico
                %s

                ## Enlace de detalle
                [MANTGR](%s)
                """.formatted(
                d.idGoldenRecord().valor(),
                descripcionIdentificador(d.identificadorEntrada()),
                d.resultado(),
                formatearDestinos(d.destinos()),
                formatearBloqueos(d.bloqueos()),
                formatearDQ(d.erroresDQ()),
                d.diagnostico(),
                d.enlaceMantgr());
    }

    private String descripcionIdentificador(IdentificadorPersona id) {
        return switch (id) {
            case IdentificadorPersona.Emplid e -> "EMPLID: " + e.valor();
            case IdentificadorPersona.IdGoldenRecord g -> "ID GR: " + g.valor();
            case IdentificadorPersona.IdSalesforce s -> "ID Salesforce: " + s.valor();
        };
    }

    private String formatearDestinos(List<PeticionEstado> destinos) {
        if (destinos.isEmpty()) return "- Sin destinos visibles.\n";
        return destinos.stream()
                .map(d -> "- %s: %s%s".formatted(
                        d.sistemaDestinoNombre(), d.estado(),
                        d.esAnomala() ? " (anómalo)" : ""))
                .collect(Collectors.joining("\n"));
    }

    private String formatearBloqueos(List<CadenaBloqueo> bloqueos) {
        if (bloqueos.isEmpty()) return "- Sin bloqueos.\n";
        return bloqueos.stream()
                .map(b -> "- Origen: %s -> Final: %s%s".formatted(
                        b.saltos().getFirst().uuid(),
                        b.estadoFinal().estado(),
                        b.pudoResolver() ? "" : " (" + b.motivoCorte() + ")"))
                .collect(Collectors.joining("\n"));
    }

    private String formatearDQ(List<ErrorDataQuality> errores) {
        if (errores.isEmpty()) return "Sin errores DQ pendientes.\n";
        return errores.stream()
                .map(e -> "- %s: [%s] %s".formatted(e.sistema().nombre(), e.codigoError(), e.descripcionError()))
                .collect(Collectors.joining("\n"));
    }
}
