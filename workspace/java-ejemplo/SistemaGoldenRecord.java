package gr.persona.domain;

import java.util.Optional;

/**
 * Sistemas origen/destino conocidos en GoldenRecord.
 * Antes: IDs numéricos sueltos (ID_SISTEMA = 3, 4, etc.)
 * Ahora: enum autodocumentado con mapeo ID <-> nombre.
 */
public enum SistemaGoldenRecord {

    PEOPLESOFT(4, "PeopleSoft"),
    SALESFORCE(3, "Salesforce"),
    ESADE(1, "ESADE"),
    IDM_RECTORAT(2, "IDM_RECTORAT"),
    SAP(5, "SAP"),
    JOBTEASER(6, "JOBTEASER"),
    SAP_PROVEEDOR(7, "SAP_PROVEEDOR"),
    TIMEEDIT(8, "TIMEEDIT"),
    ACADEM(9, "ACADEM"),
    WORKDAY(10, "WORKDAY");

    private final int idSistema;
    private final String nombre;

    SistemaGoldenRecord(int idSistema, String nombre) {
        this.idSistema = idSistema;
        this.nombre = nombre;
    }

    public int id() { return idSistema; }
    public String nombre() { return nombre; }

    public static Optional<SistemaGoldenRecord> porId(int id) {
        for (var s : values()) if (s.idSistema == id) return Optional.of(s);
        return Optional.empty();
    }

    public static Optional<SistemaGoldenRecord> porNombre(String nombre) {
        for (var s : values()) if (s.nombre.equalsIgnoreCase(nombre)) return Optional.of(s);
        return Optional.empty();
    }
}
