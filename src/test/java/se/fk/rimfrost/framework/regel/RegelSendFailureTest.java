package se.fk.rimfrost.framework.regel;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelKafkaProducer;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelResponseException;
import se.fk.rimfrost.framework.regel.logic.entity.CloudEventData;
import se.fk.rimfrost.framework.regel.logic.entity.ImmutableCloudEventData;

@QuarkusTest
public class RegelSendFailureTest extends RegelTestBase
{
   @InjectMock
   RegelKafkaProducer regelKafkaProducer;

   @Inject
   RegelRequestHandlerBaseTest.TestRegelRequestHandler testRegelRequestHandler;

   @Test
   @DisplayName("FRALL-FR-04.3: Misslyckad Kafka-sändning kastas vidare till anroparen")
   void send_regel_response_should_throw_when_producer_fails()
   {
      Mockito.doThrow(new RegelResponseException("Kafka unavailable", new RuntimeException("nack")))
            .when(regelKafkaProducer).sendRegelResponse(Mockito.any());

      var handlaggningId = UUID.randomUUID();
      assertThrows(RegelResponseException.class,
            () -> testRegelRequestHandler.sendResponse(handlaggningId, createCloudEventData(), Utfall.JA));
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
