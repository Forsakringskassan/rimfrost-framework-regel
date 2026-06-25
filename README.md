# rimfrost-framework-regel
Ramverkskomponenter som är gemensamma för alla typer av regler (både maskinella och manuella).
Innehåller både framework-logik och hjälpklasser vid test av regler.

- **`core`** – Framework-logik
- **`test-base`** – Återanvändbara testkomponenter för implementation av reglers tester

```text
root
├── core
│   └── (framework implementation)
├── test-base
│   └── (test-klasser)
└── pom.xml (parent)
```
# Core

## Konfiguration av regelns verksamhetsdata

Reglers verksamhetsdata konfigureras i `src/main/resources/config.yaml`.
Implementation av inläsning finns i katalogen _integration/config_.

### Attribut

**`uppgift`** — beskriver den operativa uppgiften

| Attribut | Typ | Obligatorisk | Beskrivning |
|------|-----|:---:|-------------|
| `id` | uuid | Nej | Identifierare för uppgiften |
| `version` | integer | Ja | Versionsnummer, börja på `1` och öka vid förändring |
| `aktivitet` | string | Ja | Aktivitetsnamn |
| `path` | string | Manuell | Callback-URL mot OUL — obligatorisk för manuella regler, utelämnas av maskinella |
| `metadata` | object | Nej | Valfria nycklar |

**`specifikation`** — metadata om regelns specifikation

| Attribut | Typ | Obligatorisk | Beskrivning |
|------|-----|:---:|-------------|
| `id` | uuid | Ja | Specifikationens identifierare |
| `version` | integer | Ja | Versionsnummer |
| `namn` | string | Ja | Regelns namn |
| `uppgiftbeskrivning` | string | Ja | Kort beskrivning av uppgiften |
| `verksamhetslogik` | string | Ja | Kod för verksamhetslogik |
| `roll` | string | Ja | Handläggarroll |
| `applikationsId` | string | Ja | Applikationens identifierare |
| `applikationsversion` | string | Ja | Applikationens version |
| `metadata` | object | Nej | Valfria nycklar |

**`regel`** — beskriver den specifika regeln

| Attribut | Typ | Obligatorisk | Beskrivning |
|------|-----|:---:|-------------|
| `id` | uuid | Ja | Regelns identifierare |
| `version` | integer | Ja | Versionsnummer |
| `namn` | string | Ja | Regelns namn |
| `beskrivning` | string | Ja | Detaljerad beskrivning |
| `metadata` | object | Nej | Valfria nycklar |

**`lagrum`** — juridisk grund för regeln

| Attribut | Typ | Obligatorisk | Beskrivning |
|------|-----|:---:|-------------|
| `id` | uuid | Ja | Lagrummets identifierare |
| `version` | integer | Ja | Versionsnummer |
| `giltigFom` | date | Ja | Giltig från och med (ISO 8601, t.ex. `2010-02-11`) |
| `forfattning` | string | Ja | Namn på författning |
| `kapitel` | integer ≥ 1 | Ja | Kapitel |
| `paragraf` | integer ≥ 1 | Ja | Paragraf |
| `stycke` | integer ≥ 1 | Ja | Stycke |
| `punkt` | integer ≥ 1 | Ja | Punkt |
| `metadata` | object | Nej | Valfria nycklar |

**`utokadUppgiftsbeskrivning`** _(valfritt)_ — utökad text som visas i handläggningsgränssnittet

| Attribut | Typ | Obligatorisk | Beskrivning |
|------|-----|:---:|-------------|
| `beskrivning` | string | Nej | Längre beskrivning av uppgiften |

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

## Regel-initiering via Kafka

Alla regler initieras/avslutas med samma typ av request/response-meddelanden över Kafka-topics.

_integration/kafka_ och _presentation/kafka_ innehåller DTO's och handlers för att konsumera och producera 
kafka-meddelanden på kanaler _regel-requests_ och _regel-responses_. <br>
Notera att kanalnamnen konfigureras till regel-specifika topic-namn i reglers _application.properties_.

## Integration med handläggning

Alla regler hämtar och uppdaterar handläggning-info med samma mekanismer.

_integration/handlaggning_ innehåller DTO's samt adapter med metoder för Get och Update av handläggning-info.

## Kogito och cloudevents

Vid all asynkron kommunikation med Kogito-processer (som t.ex. vid regel-initiering)
måste kafka-meddelandet innehålla cloud-event-data.<br>
_logic/entity/CloudEventData_ innehåller DTO som kan användas av alla regler vid kommunikation med Kogito-processer via kafka.

# test-base

## AbstractRegelTest

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