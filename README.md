# rimfrost-framework-regel

Delat ramverksbibliotek med gemensam infrastruktur för alla typer av regler i Rimfrost — både
maskinella och manuella. Ramverket hanterar mottagning av regelförfrågningar via Kafka, laddning
och validering av regelkonfiguration, integrationsadapter mot Handläggningstjänsten samt publicering
av regelsvar. Konkreta regelimplementationer ärver från ramverket och behöver enbart implementera
den regelspecifika affärslogiken.

## Aktörer

| Aktör                 | Roll                                                                               |
|-----------------------|------------------------------------------------------------------------------------|
| Kundbehovsflödet      | Initierar regelkörningar via Kafka och tar emot regelsvar                          |
| Regelimplementationer | Bygger vidare på detta ramverk och implementerar regelspecifik logik               |
| Handläggningstjänsten | Tillhandahåller ärendeinformation som regelimplementationer hämtar under bedömning |
| Förvaltningsteam      | Förvaltar och vidareutvecklar ramverket                                            |

## Struktur

```
src/main/java/.../
├── presentation/kafka/   # Kafka-konsument, deserializer och handler-kontrakt
├── logic/                # Domänlogik, konfigurationsobjekt och abstrakt basklass för regelimplementationer
└── integration/          # Adapter för YAML-konfiguration och Kafka-producent

src/test/java/.../        # Basklasser och hjälpklasser för regelimplementationernas tester
```

---

## Kafka

Ramverket hanterar följande Kafka-kanaler:

| Kanal             | Riktning   | Trigger                                        |
|-------------------|------------|------------------------------------------------|
| `regel-requests`  | Inkommande | Regelförfrågan från kundbehovsflödet           |
| `regel-responses` | Utgående   | Regelbehandling klar (lyckat eller misslyckat) |

Svarstopicen är dynamisk — ramverket dirigerar svaret till den topic som angavs i `replyTo`-fältet
i inkommande meddelande. Meddelandescheman definieras i **rimfrost-framework-regel-asyncapi**.

---

## Konfiguration

| Property                    | Beskrivning                                                                                                               | Standardvärde                           |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------|-----------------------------------------|
| `application.config.path`   | Sökväg till regelns YAML-konfigurationsfil                                                                                | `src/main/resources/config.yaml`        |
| `REGEL_CONFIG_PATH` (env)   | Åsidosätter `application.config.path` vid körning                                                                         | —                                       |
| `kafka.source`              | Identifierar avsändarservice i utgående CloudEvents (`source`-fältet är obligatoriskt enligt CloudEvents-specifikationen) | —                                       |
| `handlaggning.api.base-url` | Bas-URL till Handläggningstjänstens REST API                                                                              | `http://rimfrost-k8s-handlaggning:8080` |

---

## Konfiguration av regelns verksamhetsdata

Reglers verksamhetsdata konfigureras i `src/main/resources/config.yaml`.
Implementation av inläsning finns i katalogen _integration/config_.

Fullständig attributlista med typer och obligatoriska fält definieras i
`src/main/resources/schema/regel_schema.yaml`.

### Versionshantering

Attributet `version` (integer) finns på `uppgift`, `specifikation`, `regel` och `lagrum`.
Det är ett manuellt hanterat versionsnummer — börja på `1` och öka med `1` varje gång
innehållet i det aktuella objektet förändras på ett sätt som påverkar beteendet.

### Exempel

```yaml
uppgift:
  id: 10386a9e-cee0-454f-89ba-f16abf5052f2
  version: 1
  path: /regel/bekraftabeslut        # utelämnas för maskinella regler
  aktivitet: "Bekräfta beslut"

specifikation:
  id: a42ffaed-2f20-47e8-8499-f2f79ae2f45e
  version: 1
  namn: "Bekräfta beslut"
  uppgiftbeskrivning: "Bekräfta beslut"
  verksamhetslogik: B
  roll: ANSVARIG_HANDLAGGARE
  applikationsId: 3.5.1
  applikationsversion: bekrafta_beslut_1.0

regel:
  id: a11f3429-3a6b-4389-ba1c-c2747b0fb45a
  version: 1
  namn: "Bekräfta beslut"
  beskrivning: "Bekräfta beslut"

lagrum:
  id: f0c927a9-b995-4a12-b4fd-4bcbf4281b43
  version: 1
  giltigFom: 2010-02-11
  forfattning: "Husdjursbalken"
  kapitel: 3
  paragraf: 5
  stycke: 1
  punkt: 4

utokadUppgiftsbeskrivning:           # valfritt
  beskrivning: "Utvärdera beslutet och bekräfta eller avvisa"
```

### Schemavalidering

Schemat definieras i `src/main/resources/schema/regel_schema.yaml` (JSON Schema draft 2020-12).
Regelimplementationer valideras automatiskt mot schemat i CI via det återanvändbara GitHub
Actions-workflow som finns i detta repo.

Validera lokalt med:

```bash
pip install check-jsonschema
check-jsonschema \
  --schemafile <path-to-rimfrost-framework-regel>/src/main/resources/schema/regel_schema.yaml \
  src/main/resources/config.yaml
```

---

## Test-JAR

Ramverket levererar en test-JAR med basklasser och hjälpklasser för regelimplementationernas tester.

| Klass                  | Användning                                                           |
|------------------------|----------------------------------------------------------------------|
| `RegelTestBase`        | Abstrakt basklass med in-memory Kafka och grundkonfiguration         |
| `RegelKafkaConnector`  | Hanterar request/response-kanaler i tester                           |
| `RegelTestData`        | Skapar testdata för regelförfrågningar                               |
| `WireMockHandlaggning` | WireMock-setup mot `/handlaggning` — utökas av regelspecifika tester |

---

## Regel-initiering via Kafka

Alla regler initieras/avslutas med samma typ av request/response-meddelanden över Kafka-topics.

_integration/kafka_ och _presentation/kafka_ innehåller DTO's och handlers för att konsumera och producera
kafka-meddelanden på kanaler _regel-requests_ och _regel-responses_. <br>
Notera att kanalnamnen konfigureras till regel-specifika topic-namn i reglers _application.properties_.

## Integration med handläggning

Alla regler hämtar och uppdaterar handläggning-info med samma mekanismer.

`HandlaggningAdapter` med metoder för Get och Update av handläggning-info tillhandahålls av beroendet `rimfrost-framework-handlaggning-adapter`.

## Kogito och cloudevents

Vid all asynkron kommunikation med Kogito-processer (som t.ex. vid regel-initiering)
måste kafka-meddelandet innehålla cloud-event-data.<br>
_logic/entity/CloudEventData_ innehåller DTO som kan användas av alla regler vid kommunikation med Kogito-processer via kafka.

## RegelTestBase

Innehåller testkomponenter som är gemensamma för alla typer av regler.
Testkomponenter för manuella- resp. maskinella regler ärver dessa komponenter så att reglers testklasser
kan ärva komponenter från rimfrost-framework-manuell/maskinell.

## Kafka connector

Utility-klass som underlättar hantering av en inMemory kafka-connector.

## RegelKafkaConnector

Extendar KafkaConnector för att hantera reglers request/response-kanaler.

## RegelTestData

Utility-klass som skapar testdata.

## WireMockHandlaggning

Utility-klass för hantering av Wiremock-setup.<br>
Innehåller mappning mot api /handlaggning eftersom alla regler nyttjar det API't.<br>
Regler kan extenda WireMockHandlaggning för att utöka med regelspecifika endpoints som mockas.