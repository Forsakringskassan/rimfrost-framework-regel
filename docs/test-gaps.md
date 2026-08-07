# Testgap — rimfrost-framework-regel

## Rimligt att lämna utan test

| Krav           | Kommentar                                                                                                                                                        |
|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| FRALL-FR-03.1  | Kontraktsdefinition — verifieras vid kompilering av implementationer                                                                                             |
| FRALL-NFR-01.1 | Hälsokontroll tillhandahålls av Quarkus-ramverket — infrastrukturnivå                                                                                            |
| FRALL-NFR-02.1 | Konfigurerbara egenskaper utan kodändringar är infrastrukturnivå — täcks av testkonfigurationen i sin helhet                                                     |
| FRALL-NFR-03.1 | Loggning kräver log-assertion-infrastruktur som inte finns på plats; verifieras via kodinspektion                                                                |
| FRALL-NFR-02.2 | Startavvisning vid ogiltig config täcks av FR-02.2/FR-02.3 (implementerade och testade); separerat starttest på Quarkus-nivå tillför inget ytterligare            |
| FRALL-FR-02.1  | Miljövariabeln REGEL_CONFIG_PATH kan inte sättas programmatiskt i Java utan OS-manipulation — samma kodväg som application.config.path täcks av befintligt test   |

## Bör åtgärdas

Inga kända testgap.
