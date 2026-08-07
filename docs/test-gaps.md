# Testgap — rimfrost-framework-regel

## Rimligt att lämna utan test

| Krav           | Kommentar                                                                                                                                                        |
|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| FRALL-FR-03.1  | Kontraktsdefinition — verifieras vid kompilering av implementationer                                                                                             |
| FRALL-NFR-01.1 | Hälsokontroll tillhandahålls av Quarkus-ramverket — infrastrukturnivå                                                                                            |
| FRALL-NFR-02.1 | Konfigurerbara egenskaper utan kodändringar är infrastrukturnivå — täcks av testkonfigurationen i sin helhet                                                     |
| FRALL-NFR-03.1 | Loggning kräver log-assertion-infrastruktur som inte finns på plats; verifieras via kodinspektion                                                                |
| FRALL-NFR-02.2 | Reject av start vid ogiltig config är en konsekvens av FR-02.2/FR-02.3; täcks när den valideringen implementeras — separerat starttest tillför inget ytterligare |

## Bör åtgärdas

| Krav          | Beskrivning                                                                                   |
|---------------|-----------------------------------------------------------------------------------------------|
| FRALL-FR-02.2 | Testa att ramverket avvisar config där obligatoriska sektioner saknas (implementation saknas) |
| FRALL-FR-02.3 | Testa att ramverket avvisar config som inte uppfyller JSON Schema (implementation saknas)     |
