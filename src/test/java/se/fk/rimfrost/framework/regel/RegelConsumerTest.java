package se.fk.rimfrost.framework.regel;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelMessageHandler;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static se.fk.rimfrost.framework.regel.RegelTestData.newRegelRequestMessagePayload;

@QuarkusTest
public class RegelConsumerTest extends RegelTestBase
{
   @InjectMock
   RegelMessageHandler regelMessageHandler;

   @ConfigProperty(name = "mp.messaging.outgoing.regel-responses.topic")
   String responseTopic;

   @BeforeEach
   public void regelResetState()
   {
      super.regelResetState();
   }

   @Test
   public void regel_consumer_should_call_regel_message_handler_on_incoming_request() throws InterruptedException
   {
      var handlaggningId = UUID.randomUUID().toString();
      var expectedPayload = newRegelRequestMessagePayload(handlaggningId, responseTopic);
      regelKafkaConnector.sendRegelRequest(handlaggningId, responseTopic);
      TimeUnit.SECONDS.sleep(1);
      Mockito.verify(regelMessageHandler, Mockito.times(1)).consumeRegelRequest(expectedPayload);
   }
}
