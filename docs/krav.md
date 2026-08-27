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

### FRALL-FR-06 — Kompletteringskontroll

- **FRALL-FR-06.1** Ramverket ska tillhandahålla ett interface (`KompletteringKontrollInterface`) med en defaultmetod `checkKomplettering()` som returnerar en tom lista, vilket innebär att yrkandet är komplett och regeln ska köras direkt. Regelimplementationer som kräver en fullständighetskontroll ska överskriva metoden.
- **FRALL-FR-06.2** Ramverket ska tillhandahålla en DTO (`KompletteringUnderlag`) som beskriver ett saknat attribut i yrkandet med ett maskinläsbart typidentifierare (`underlagTyp`) och en läsbar beskrivning (`beskrivning`). Typidentifieraren ska definieras som en lokal konstant i respektive regelrepo — aldrig i detta ramverk.
- **FRALL-FR-06.3** Ramverket ska tillhandahålla ett persistenslager (`KompletteringStorage`) för att lagra och hämta korrelationstillstånd per handlaggningsId under en pågående kompletteringsomgång.
- **FRALL-FR-06.4** `KompletteringStorage` ska returnera ett tomt värde (`Optional.empty()`) vid sökning på ett okänt handlaggningsId utan att kasta exception.
- **FRALL-FR-06.5** `KompletteringStorage` ska hantera borttagning av ett frånvarande handlaggningsId utan att kasta exception.
- **FRALL-FR-06.6** Ramverket ska tillhandahålla en operation (`handleKompletteringTimeout`) som avslutar den öppna OUL-uppgiften och tar bort korrelationstillståndet när kompletteringstimern löper ut.
- **FRALL-FR-06.7** `handleKompletteringTimeout` ska vara säker att anropa även om handläggaren redan avslutat kompletteringen (dvs. tillståndet redan är borttaget) — operationen ska logga och returnera utan exception.
- **FRALL-FR-06.8** Om avslutning av OUL-uppgiften misslyckas under timeout-hanteringen ska felet loggas och korrelationstillståndet ändå tas bort, utan att kasta exception.

### FRALL-FR-07 — Kompletteringsflöde via REST

- **FRALL-FR-07.1** Ramverket ska exponera `GET /{handlaggningId}/komplettering` som returnerar den information handläggaren behöver för att registrera kompletterande uppgifter. Informationen hämtas via regelns implementation av `KompletteringSvarServiceInterface`.
- **FRALL-FR-07.2** Ramverket ska exponera `PATCH /{handlaggningId}/komplettering` för registrering av kompletterande uppgifter via regelns implementation av `KompletteringSvarServiceInterface`.
- **FRALL-FR-07.3** Ramverket ska exponera `POST /{handlaggningId}/komplettering/done`. Vid anrop ska `checkKomplettering()` anropas för att verifiera att yrkandet nu är komplett. Om yrkandet fortfarande saknar uppgifter ska HTTP 422 returneras.
- **FRALL-FR-07.4** Om `checkKomplettering()` returnerar tom lista ska ramverket trigga regelkörningen med det lagrade korrelationstillståndet, vilket resulterar i att Kafka-svaret skickas till den ursprungliga `replyTo`-kanalen.
- **FRALL-FR-07.5** `POST /komplettering/done` ska returnera HTTP 409 om timeout redan har tömt korrelationstillståndet.
- **FRALL-FR-07.6** Om avslutning av OUL-uppgiften misslyckas under `POST /komplettering/done` ska felet loggas, regelkörningen ändå triggas och HTTP 207 returneras för att signalera att kompletteringen är accepterad men att OUL-uppgiften eventuellt fortfarande är öppen.
- **FRALL-FR-07.7** `KompletteringSvarServiceInterface` ska använda en enda typparameter för svarsdata: samma datastruktur används både som returvärde för `readSvarData` (GET) och som request body för `registerSvar` (PATCH). Regelrepos ska alltså inte behöva ange separata typer för GET-svar och PATCH-förfrågan.

---

## 2. Icke-funktionella krav

### FRALL-NFR-01 — Tillgänglighet och driftsäkerhet

- **FRALL-NFR-01.1** Ramverket ska exponera en healthcheck så att plattformen kan verifiera att tjänsten är operativ.

### FRALL-NFR-02 — Konfigurationshantering

- **FRALL-NFR-02.1** Sökvägen till konfigurationsfilen ska kunna definieras via miljövariabel utan ny driftsättning av applikationen.
- **FRALL-NFR-02.2** Konfigurationsfilen ska valideras vid uppstart och tjänsten ska vägra starta om konfigurationen är ogiltig.

### FRALL-NFR-03 — Observerbarhet

- **FRALL-NFR-03.1** Ramverket ska logga fel vid misslyckad sändning av regelsvar, inklusive handläggningsid och utfallsinformation.

---
