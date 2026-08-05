# Identificadores admitidos

| Tipo | Formato | Sistema |
|---|---|---|
| EMPLID PeopleSoft | Exactamente 8 dígitos, por ejemplo `00089108` | `ID_SISTEMA = 4` |
| ID GoldenRecord | Exactamente 9 dígitos, por ejemplo `100890864` | No requiere resolución previa |
| ID Salesforce | Exactamente 18 caracteres alfanuméricos, por ejemplo `0032400001LBiigAAD` | `ID_SISTEMA = 3` |

Aplicar siempre `ID_TIPO_ENTIDAD = 2`, correspondiente a Persona.

No eliminar ceros iniciales del EMPLID. No convertir identificadores a números cuando ello pueda alterar su valor.
