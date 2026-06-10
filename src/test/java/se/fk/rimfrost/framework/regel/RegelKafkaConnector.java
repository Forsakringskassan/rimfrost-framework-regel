package se.fk.rimfrost.framework.regel;

import io.smallrye.reactive.messaging.memory.InMemoryConnector;

/**
 * Specialized Kafka test connector for rule-engine (Regel) messaging flows.
 *
 * <p>Provides helper methods for sending rule requests, verifying produced responses, and
 * validating response payload content for tests.</p>
 */
@SuppressWarnings("unused")
public class RegelKafkaConnector extends KafkaConnector
{

   /** Input channel for sending Regel requests. */
   public static final String regelRequestsChannel = "regel-requests";

   /** Output channel where Regel responses are published. */
   public static final String regelResponsesChannel = "regel-responses";

   /**
   * Creates a new {@code RegelKafkaConnector}.
   *
   * @param inMemoryConnector connector managing in-memory message sources and sinks
   */
   public RegelKafkaConnector(InMemoryConnector inMemoryConnector)
   {
      super(inMemoryConnector);
   }

   /**
   * Clears all previously received Regel response messages.
   *
   * <p>Useful for resetting test state between scenarios.</p>
   */
   public void clear()
   {
      inMemoryConnector.sink(regelResponsesChannel).clear();
   }

   /**
   * Sends a Regel request message for the provided handläggning identifier.
   *
   * @param handlaggningId identifier for the case being processed
   */
   public void sendRegelRequest(String handlaggningId, String replyTo)
   {
      var payload = RegelTestData.newRegelRequestMessagePayload(handlaggningId, replyTo);
      inMemoryConnector.source(regelRequestsChannel).send(payload);
   }

   /**
   * Waits for regel response and returns payload
   *
   * @return RegelResponseMessagePayload content of regel response
   */
   public RegelResponseMessagePayload waitForRegelResponse()
   {
      return (RegelResponseMessagePayload) waitForMessages(regelResponsesChannel).getFirst().getPayload();
   }
}
