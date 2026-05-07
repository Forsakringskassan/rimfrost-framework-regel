package se.fk.rimfrost.framework.regel;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelMessageHandler;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;

import java.util.UUID;

import static se.fk.rimfrost.framework.regel.RegelTestData.newRegelRequestMessagePayload;

@QuarkusTest
public class RegelMessageHandlerTest extends RegelTestBase
{
   @InjectMock
   RegelRequestHandlerInterface regelRequestHandler;

   @Inject
   RegelMessageHandler regelMessageHandler;

   @Test
   public void consume_regel_request_should_call_regel_request_handler()
   {
      var handlaggningId = UUID.randomUUID().toString();
      var payload = newRegelRequestMessagePayload(handlaggningId);

      regelMessageHandler.consumeRegelRequest(payload);
      Mockito.verify(regelRequestHandler, Mockito.times(1)).handleRegelRequest(createRegelDataRequest(payload));
   }

   private RegelDataRequest createRegelDataRequest(RegelRequestMessagePayload payload)
   {
      return ImmutableRegelDataRequest.builder()
            .id(UUID.fromString(payload.getId()))
            .handlaggningId(UUID.fromString(payload.getData().getHandlaggningId()))
            .aktivitetId(UUID.fromString(payload.getData().getAktivitetId()))
            .kogitorootprocid(payload.getKogitorootprocid())
            .kogitorootprociid(UUID.fromString(payload.getKogitorootprociid()))
            .kogitoparentprociid(UUID.fromString(payload.getKogitoparentprociid()))
            .kogitoprocid(payload.getKogitoprocid())
            .kogitoprocinstanceid(UUID.fromString(payload.getKogitoprocinstanceid()))
            .kogitoprocist(payload.getKogitoprocist())
            .kogitoprocversion(payload.getKogitoprocversion())
            .type(payload.getType())
            .build();
   }
}
