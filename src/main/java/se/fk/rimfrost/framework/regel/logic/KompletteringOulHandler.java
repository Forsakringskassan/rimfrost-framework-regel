package se.fk.rimfrost.framework.regel.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import se.fk.rimfrost.framework.oul.adapter.OulAdapter;
import se.fk.rimfrost.framework.oul.exception.OulException;
import se.fk.rimfrost.framework.oul.model.Erbjudande;
import se.fk.rimfrost.framework.oul.model.ImmutableCreateOperativUppgiftRequest;
import se.fk.rimfrost.framework.oul.model.ImmutableProcessInfo;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableKompletteringTillstand;
import se.fk.rimfrost.framework.regel.logic.storage.KompletteringStorage;

/**
 * Creates the komplettering OUL task and persists correlation state.
 *
 * <p>Called by the maskinell and manuell request handlers when
 * {@code checkKomplettering()} returns a non-empty list. All OUL task metadata
 * ({@code regel}, {@code beskrivning}, {@code roll}, {@code url},
 * {@code verksamhetslogik}) is derived from the regel's own {@link RegelConfig} —
 * no separate komplettering configuration is required in regel repos.
 *
 * <p>Missing attribute details are served via
 * {@code GET /{handlaggningId}/komplettering} (pull model), not embedded in the OUL task.
 */
@ApplicationScoped
public class KompletteringOulHandler
{
   @ConfigProperty(name = "kafka.subtopic")
   String subTopic;

   @Inject
   OulAdapter oulAdapter;

   @Inject
   KompletteringStorage storage;

   /**
    * Creates the komplettering OUL task and stores the full correlation state.
    * Returns without sending a Kafka reply — the BPMN process instance keeps waiting.
    *
    * @param handlaggningId       the handlaggning being processed
    * @param cloudEventData       original CloudEvent payload as raw JSON, re-published on done
    * @param cloudEventAttributes CloudEvent attributes extracted from the incoming event
    * @param replyTo              Kafka reply topic from the original CloudEvent
    * @param regelConfig          the regel's own config; used to derive all OUL task metadata
    * @param erbjudande           the erbjudande associated with the handlaggning
    * @throws OulException if the OUL task creation fails
    */
   public void initiate(UUID handlaggningId,
         String cloudEventData,
         Map<String, String> cloudEventAttributes,
         String replyTo,
         RegelConfig regelConfig,
         Erbjudande erbjudande) throws OulException
   {
      var oulRequest = ImmutableCreateOperativUppgiftRequest.builder()
            .handlaggningId(handlaggningId)
            .version("1")
            .regel("Hantera komplettering för " + regelConfig.getSpecifikation().getNamn())
            .beskrivning("Kompletteringsuppgift för att hantera saknade uppgifter i yrkandet.")
            .verksamhetslogik(regelConfig.getSpecifikation().getVerksamhetslogik())
            .roll(regelConfig.getSpecifikation().getRoll())
            .url(regelConfig.getUppgift().getPath() + "/komplettering")
            .subTopic(subTopic)
            .erbjudande(erbjudande)
            .processInfo(ImmutableProcessInfo.builder()
                  .replyTopic(replyTo)
                  .cloudeventAttributes(cloudEventAttributes)
                  .build())
            .build();

      var operativUppgift = oulAdapter.createOperativUppgift(oulRequest);

      var tillstand = ImmutableKompletteringTillstand.builder()
            .oulUppgiftId(operativUppgift.getUppgiftId())
            .replyTo(replyTo)
            .cloudEventData(cloudEventData)
            .build();

      storage.setKompletteringTillstand(handlaggningId, tillstand);
   }
}
