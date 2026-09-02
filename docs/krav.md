# Krav — rimfrost-framework-regel

## 1. Funktionella krav

### FRBAS-FR-01 — Mottagning av regelförfrågan

- **FRBAS-FR-01.1** Ramverket ska ta emot regelförfrågningar från Kafka-kanalen `regel-requests`.
- **FRBAS-FR-01.2** Ramverket ska delegera den mottagna förfrågan till den registrerade regelimplementationen för vidare behandling.

### FRBAS-FR-02 — Hantering av regelkonfiguration

- **FRBAS-FR-02.1** Ramverket ska ladda regelkonfiguration från en YAML-fil vars sökväg kan anges via miljövariabel eller konfigurationsproperty.
- **FRBAS-FR-02.2** Konfigurationsfilen ska innehålla uppgift, specifikation, regel och lagrum som beskriver den aktuella regelns metadata.
- **FRBAS-FR-02.3** Konfigurationsfilen ska valideras mot ett fastställt JSON Schema innan den tas i bruk.

### FRBAS-FR-03 — Regelkörning och utfallsrapportering

- **FRBAS-FR-03.1** Ramverket ska tillhandahålla ett kontrakt (`RegelRequestHandlerInterface`) som regelimplementationer implementerar för att utföra affärslogik och rapportera utfall.
- **FRBAS-FR-03.2** Ramverket ska stödja följande utfall: `JA`, `NEJ`, `UTREDNING` och `ERROR`.
- **FRBAS-FR-03.3** Vid normalt utfall ska ramverket skicka ett svar på den svarskanal (`replyTo`) som angavs i förfrågan.
- **FRBAS-FR-03.4** Vid felutfall ska svaret innehålla en felkod (från `rimfrost-framework-regel-error-codes`) och ett felmeddelande, och utfallet ska sättas till `ERROR`.

### FRBAS-FR-04 — Sändning av regelsvar

- **FRBAS-FR-04.1** Svarsmeddelandet ska vara ett CloudEvent-formaterat meddelande och innehålla handläggningsid, utfall samt Kogito-processkorrelationsdata.
- **FRBAS-FR-04.2** Svaret ska skickas dynamiskt till den topic som angavs i `replyTo`-fältet i förfrågan.
- **FRBAS-FR-04.3** Om sändning misslyckas ska felet loggas utan att orsaka ett exception som avbryter processen (fire-and-forget).

### FRBAS-FR-05 — Hjälpfunktioner för yrkande och resultat

- **FRBAS-FR-05.1** Ramverket ska tillhandahålla en hjälpfunktion för att merga producerade resultat från ett yrkande med nya resultat, där nyare resultat för samma id ersätter äldre.
- **FRBAS-FR-05.2** Ramverket ska tillhandahålla en hjälpfunktion för att skapa ett uppdaterat yrkande med mergade producerade resultat.

---

## 2. Icke-funktionella krav

### FRBAS-NFR-01 — Tillgänglighet och driftsäkerhet

- **FRBAS-NFR-01.1** Ramverket ska exponera en healthcheck så att plattformen kan verifiera att tjänsten är operativ.

### FRBAS-NFR-02 — Konfigurationshantering

- **FRBAS-NFR-02.1** Sökvägen till konfigurationsfilen ska kunna definieras via miljövariabel utan ny driftsättning av applikationen.
- **FRBAS-NFR-02.2** Konfigurationsfilen ska valideras vid uppstart och tjänsten ska vägra starta om konfigurationen är ogiltig.

### FRBAS-NFR-03 — Observerbarhet

- **FRBAS-NFR-03.1** Ramverket ska logga fel vid misslyckad sändning av regelsvar, inklusive handläggningsid och utfallsinformation.

---
