package se.fk.rimfrost.framework.regel;

import static org.junit.jupiter.api.Assertions.*;
import static se.fk.rimfrost.framework.regel.RegelTestData.newRegelRequestMessagePayload;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelKafkaMapper;

public class RegelKafkaMapperTest
{
   private final RegelKafkaMapper mapper = new RegelKafkaMapper();

   @Test
   @DisplayName("FRALL-FR-06.1: processvyUrl läses från regelanropet tillsammans med aktivitetId")
   public void should_map_processvy_url_and_aktivitet_id()
   {
      var payload = newRegelRequestMessagePayload(UUID.randomUUID().toString(), "regel-responses");
      payload.getData().setProcessvyUrl("/process/vah");

      RegelDataRequest request = mapper.toRegelDataRequest(payload);

      assertEquals("/process/vah", request.processvyUrl());
      assertEquals(UUID.fromString(payload.getData().getAktivitetId()), request.aktivitetId());
   }

   @Test
   @DisplayName("FRALL-FR-06.2: Ett regelanrop utan processvyUrl är giltigt")
   public void should_accept_request_without_processvy_url()
   {
      var payload = newRegelRequestMessagePayload(UUID.randomUUID().toString(), "regel-responses");

      RegelDataRequest request = mapper.toRegelDataRequest(payload);

      assertNull(request.processvyUrl());
      assertEquals(UUID.fromString(payload.getData().getAktivitetId()), request.aktivitetId());
   }

   @Test
   @DisplayName("FRALL-FR-06.2: Ett regelanrop utan aktivitetId är felaktigt")
   public void should_reject_request_without_aktivitet_id()
   {
      var payload = newRegelRequestMessagePayload(UUID.randomUUID().toString(), null, "regel-requests", "regel-responses");
      payload.getData().setProcessvyUrl("/process/vah");

      assertThrows(RuntimeException.class, () -> mapper.toRegelDataRequest(payload));
   }
}
