# rimfrost-framework-regel
Ramverkskomponenter som är gemensamma för alla typer av regler (både maskinella och manuella).

## Konfiguration av regelns verksamhetsdata

Reglers verksamhetsdata kan konfigureras i yaml-fil som (default) läses från regelns _src/main/resources/config.yaml_.
Där förväntas regler specificera t.ex:
- version
- namn
- uppgiftbeskrivning
- lagrum
- etc...

Implementation av inläsning från config.yaml finns i katalogen _integration/config_.

Exempel på config.yaml i template: <br>
https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell/blob/main/src/main/resources/config.yaml

## Regel-initiering via Kafka

Alla regler initieras/avslutas med samma typ av request/response-meddelanden över Kafka-topics.

_integration/kafka_ och _presentation/kafka_ innehåller DTO's och handlers för att konsumera och producera 
kafka-meddelanden på kanaler _regel-requests_ och _regel-responses_. <br>
Notera att kanalnamnen konfigureras till regel-specifika topic-namn i reglers _application.properties_.

## Integration med kundbehovsflöde

Alla regler hämtar och uppdaterar kundbehovsflöde-info med samma mekanismer.

_integration/kundbehovsflode_ innehåller DTO's samt adapter med metoder för Get och Update av kundbehovsflödes-info.

## Kogito och cloudevents

Vid all asynkron kommunikation med Kogito-processer (som t.ex. vid regel-initiering)
måste kafka-meddelandet innehålla cloud-event-data.<br>
_logic/entity/CloudEventData_ innehåller DTO som kan användas av alla regler vid kommunikation med Kogito-processer via kafka.