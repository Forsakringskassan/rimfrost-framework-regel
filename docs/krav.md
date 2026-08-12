# Krav — rimfrost-framework-regel

## 1. Funktionella krav

### FRALL-FR-01 — Mottagning av regelförfrågan

- **FRALL-FR-01.1** Ramverket ska ta emot regelförfrågningar från Kafka-kanalen `regel-requests`.
- **FRALL-FR-01.2** Ramverket ska delegera den mottagna förfrågan till den registrerade regelimplementationen för vidare behandling.

### FRALL-FR-02 — Hantering av regelkonfiguration

- **FRALL-FR-02.1** Ramverket ska ladda regelkonfiguration från en YAML-fil vars sökväg kan anges via miljövariabel eller konfigurationsproperty.
- **FRALL-FR-02.2** Konfigurationsfilen ska innehålla uppgift, specifikation, regel och lagrum som beskriver den aktuella regelns metadata.
- **FRALL-FR-02.3** Konfigurationsfilen ska valideras mot ett fastställt JSON Schema innan den tas i bruk.

### FRALL-FR-03 — Regelkörning och utfallsrapportering

- **FRALL-FR-03.1** Ramverket ska tillhandahålla ett kontrakt (`RegelRequestHandlerInterface`) som regelimplementationer implementerar för att utföra affärslogik och rapportera utfall.
- **FRALL-FR-03.2** Ramverket ska stödja följande utfall: `JA`, `NEJ`, `UTREDNING` och `ERROR`.
- **FRALL-FR-03.3** Vid normalt utfall ska ramverket skicka ett svar på den svarskanal (`replyTo`) som angavs i förfrågan.
- **FRALL-FR-03.4** Vid felutfall ska svaret innehålla en felkod (från `rimfrost-framework-regel-error-codes`) och ett felmeddelande, och utfallet ska sättas till `ERROR`.

### FRALL-FR-04 — Sändning av regelsvar

- **FRALL-FR-04.1** Svarsmeddelandet ska vara ett CloudEvent-formaterat meddelande och innehålla handläggningsid, utfall samt Kogito-processkorrelationsdata.
- **FRALL-FR-04.2** Svaret ska skickas dynamiskt till den topic som angavs i `replyTo`-fältet i förfrågan.
- **FRALL-FR-04.3** Om sändning misslyckas ska felet loggas utan att orsaka ett exception som avbryter processen (fire-and-forget).

### FRALL-FR-05 — Hjälpfunktioner för yrkande och resultat

- **FRALL-FR-05.1** Ramverket ska tillhandahålla en hjälpfunktion för att merga producerade resultat från ett yrkande med nya resultat, där nyare resultat för samma id ersätter äldre.
- **FRALL-FR-05.2** Ramverket ska tillhandahålla en hjälpfunktion för att skapa ett uppdaterat yrkande med mergade producerade resultat.

---

## 2. Icke-funktionella krav

### FRALL-NFR-01 — Tillgänglighet och driftsäkerhet

- **FRALL-NFR-01.1** Ramverket ska exponera en hälsokontroll så att plattformen kan verifiera att tjänsten är operativ.

### FRALL-NFR-02 — Konfigurationshantering

- **FRALL-NFR-02.1** Sökvägen till konfigurationsfilen ska kunna definieras via miljövariabel utan ny driftsättning av applikationen.
- **FRALL-NFR-02.2** Konfigurationsfilen ska valideras vid uppstart och tjänsten ska vägra starta om konfigurationen är ogiltig.

### FRALL-NFR-03 — Observerbarhet

- **FRALL-NFR-03.1** Ramverket ska logga fel vid misslyckad sändning av regelsvar, inklusive handläggningsid och utfallsinformation.

---
