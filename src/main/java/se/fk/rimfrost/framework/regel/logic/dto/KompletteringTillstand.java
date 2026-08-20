package se.fk.rimfrost.framework.regel.logic.dto;

import java.util.UUID;
import org.immutables.value.Value;

/**
 * Correlation state persisted by {@code KompletteringStorage} while a komplettering OUL task
 * is open. Cleared on {@code POST /komplettering/done} or on timeout.
 */
@Value.Immutable
public interface KompletteringTillstand
{
   /** ID of the OUL task created for this komplettering. */
   UUID oulUppgiftId();

   /** Kafka topic to send the reply to when the komplettering is completed. */
   String replyTo();

   /** Original CloudEvent payload — re-published to retrigger the regel on done. */
   String cloudEventData();
}
