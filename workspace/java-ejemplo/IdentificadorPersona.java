package gr.persona.domain;

/**
 * Identificador de una persona en GoldenRecord.
 * Antes: templates SQL con {{ID_SISTEMA}}, {{ID_EXTERNO}} y {@code ID_TIPO_ENTIDAD = 2} sueltos.
 * Ahora: tipo sellado con parseo automático, sin números mágicos.
 */
public sealed interface IdentificadorPersona
        permits IdentificadorPersona.Emplid,
                IdentificadorPersona.IdGoldenRecord,
                IdentificadorPersona.IdSalesforce {

    record Emplid(String valor) implements IdentificadorPersona {
        public Emplid {
            if (valor == null || !valor.matches("\\d{8}"))
                throw new IllegalArgumentException("EMPLID debe ser exactamente 8 dígitos. Recibido: " + valor);
        }
    }

    record IdGoldenRecord(String valor) implements IdentificadorPersona {
        public IdGoldenRecord {
            if (valor == null || !valor.matches("\\d{9}"))
                throw new IllegalArgumentException("ID GoldenRecord debe ser exactamente 9 dígitos. Recibido: " + valor);
        }
    }

    record IdSalesforce(String valor) implements IdentificadorPersona {
        public IdSalesforce {
            if (valor == null || valor.length() != 18)
                throw new IllegalArgumentException("ID Salesforce debe ser 18 caracteres. Longitud: "
                        + (valor == null ? 0 : valor.length()));
        }
    }

    static IdentificadorPersona parse(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Identificador vacío");
        String trimmed = raw.trim();
        if (trimmed.matches("\\d{8}"))  return new Emplid(trimmed);
        if (trimmed.matches("\\d{9}"))  return new IdGoldenRecord(trimmed);
        if (trimmed.length() == 18)     return new IdSalesforce(trimmed);
        throw new IllegalArgumentException("Formato no reconocido: " + raw);
    }
}
