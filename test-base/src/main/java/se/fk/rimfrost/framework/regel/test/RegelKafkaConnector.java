package se.fk.rimfrost.framework.regel.test;

import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import org.junit.jupiter.api.Assertions;
import se.fk.rimfrost.framework.regel.RegelResponseMessagePayload;
import se.fk.rimfrost.framework.regel.Utfall;
import static se.fk.rimfrost.framework.regel.test.RegelTestData.newRegelRequestMessagePayload;

/**
 * Specialized Kafka test connector for rule-engine
 * (Regel) messaging flows.
 *
 * <p>Provides helper methods for sending rule requests,
 * verifying produced responses, and validating response
 * payload content for tests.</p>
 */
@SuppressWarnings("unused")
public class RegelKafkaConnector extends KafkaConnector
{

   /**
    * Input channel for sending Regel requests.
    */
   public static final String regelRequestsChannel = "regel-requests";

   /**
    * Output channel where Regel responses are published.
    */
   public static final String regelResponsesChannel = "regel-responses";

   /**
    * Creates a new {@code RegelKafkaConnector}.
    *
    * @param inMemoryConnector connector managing in-memory
    *                          message sources and sinks
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
    * Sends a Regel request message for the provided
    * handläggning identifier.
    *
    * @param handlaggningId identifier for the case being processed
    */
   public void sendRegelRequest(String handlaggningId)
   {
      var payload = newRegelRequestMessagePayload(handlaggningId);
      inMemoryConnector.source(regelRequestsChannel).send(payload);
   }

   /**
    * Verifies that exactly one Regel response message
    * was produced.
    */
   public void verifyRegelResponseProduced()
   {
      Assertions.assertEquals(1, waitForMessages(regelResponsesChannel).size());
   }

   /**
    * Verifies the content of the produced Regel response.
    *
    * <p>Checks both the handläggning identifier and
    * the resulting {@link Utfall}.</p>
    *
    * @param handlaggningId expected handläggning identifier
    * @param utfall expected rule evaluation outcome
    */
   public void verifyRegelResponseContent(String handlaggningId, Utfall utfall)
   {
      var msg = (RegelResponseMessagePayload) waitForMessages(regelResponsesChannel)
            .getFirst()
            .getPayload();
      Assertions.assertEquals(handlaggningId, msg.getData().getHandlaggningId());
      Assertions.assertEquals(utfall, msg.getData().getUtfall());
   }
}
