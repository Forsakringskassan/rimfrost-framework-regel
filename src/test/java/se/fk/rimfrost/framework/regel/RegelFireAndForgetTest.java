package se.fk.rimfrost.framework.regel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelKafkaProducer;
import se.fk.rimfrost.framework.regel.logic.entity.CloudEventData;
import se.fk.rimfrost.framework.regel.logic.entity.ImmutableCloudEventData;

@QuarkusTest
public class RegelFireAndForgetTest extends RegelTestBase
{
   @InjectMock
   RegelKafkaProducer regelKafkaProducer;

   @Inject
   RegelRequestHandlerBaseTest.TestRegelRequestHandler testRegelRequestHandler;

   @ConfigProperty(name = "mp.messaging.outgoing.regel-responses.topic")
   String responseTopic;

   @Test
   @DisplayName("FRALL-FR-04.3: Misslyckad Kafka-sändning loggas utan att ett exception kastas")
   void send_regel_response_should_not_throw_when_producer_fails()
   {
      Mockito.doThrow(new IllegalStateException("Kafka unavailable"))
            .when(regelKafkaProducer).sendRegelResponse(Mockito.any(), Mockito.any());

      var handlaggningId = UUID.randomUUID();
      assertDoesNotThrow(
            () -> testRegelRequestHandler.sendResponse(handlaggningId, createCloudEventData(), Utfall.JA, responseTopic));
   }

   private static CloudEventData createCloudEventData()
   {
      return ImmutableCloudEventData.builder()
            .id(UUID.randomUUID())
            .kogitorootprociid(UUID.randomUUID())
            .kogitoparentprociid(UUID.randomUUID())
            .kogitoprocinstanceid(UUID.randomUUID())
            .kogitorootprocid(UUID.randomUUID().toString())
            .kogitoprocid(UUID.randomUUID().toString())
            .kogitoprocist(UUID.randomUUID().toString())
            .kogitoprocversion(UUID.randomUUID().toString())
            .type("test")
            .source("test")
            .build();
   }
}
