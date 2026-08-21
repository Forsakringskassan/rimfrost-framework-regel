package se.fk.rimfrost.framework.regel;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.oul.adapter.OulAdapter;
import se.fk.rimfrost.framework.oul.exception.OulException;
import se.fk.rimfrost.framework.regel.logic.KompletteringService;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableKompletteringTillstand;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.storage.KompletteringStorage;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@QuarkusTest
class KompletteringServiceTest
{
   @InjectMock
   OulAdapter oulAdapter;

   @Inject
   KompletteringService service;

   @Inject
   KompletteringStorage storage;

   private ImmutableKompletteringTillstand.Builder tillstandBuilder(UUID oulUppgiftId)
   {
      return ImmutableKompletteringTillstand.builder()
            .oulUppgiftId(oulUppgiftId)
            .regelDataRequest(ImmutableRegelDataRequest.builder()
                  .id(UUID.randomUUID())
                  .handlaggningId(UUID.randomUUID())
                  .aktivitetId(UUID.randomUUID())
                  .replyTo("reply-topic")
                  .type("test-type")
                  .kogitorootprocid("root-proc-id")
                  .kogitorootprociid(UUID.randomUUID())
                  .kogitoparentprociid(UUID.randomUUID())
                  .kogitoprocid("proc-id")
                  .kogitoprocinstanceid(UUID.randomUUID())
                  .kogitoprocist("proc-ist")
                  .kogitoprocversion("1.0")
                  .build());
   }

   @Test
   @DisplayName("FRALL-FR-06.7: Timeout on empty storage returns without calling OUL")
   void should_return_without_oul_call_when_storage_empty()
   {
      service.handleKompletteringTimeout(UUID.randomUUID());

      verifyNoInteractions(oulAdapter);
   }

   @Test
   @DisplayName("FRALL-FR-06.6: Timeout ends OUL task and clears storage")
   void should_end_oul_task_and_clear_storage_on_timeout() throws OulException
   {
      var handlaggningId = UUID.randomUUID();
      var oulUppgiftId = UUID.randomUUID();
      storage.setKompletteringTillstand(handlaggningId, tillstandBuilder(oulUppgiftId).build());

      service.handleKompletteringTimeout(handlaggningId);

      verify(oulAdapter).endOperativUppgift(oulUppgiftId, "Timeout");
      assertFalse(storage.getKompletteringTillstand(handlaggningId).isPresent());
   }

   @Test
   @DisplayName("FRALL-FR-06.8: OulException on timeout does not prevent storage cleanup")
   void should_clear_storage_even_when_oul_end_fails() throws OulException
   {
      var handlaggningId = UUID.randomUUID();
      var oulUppgiftId = UUID.randomUUID();
      storage.setKompletteringTillstand(handlaggningId, tillstandBuilder(oulUppgiftId).build());
      doThrow(new OulException(OulException.ErrorType.SERVICE_UNAVAILABLE, "OUL down"))
            .when(oulAdapter).endOperativUppgift(oulUppgiftId, "Timeout");

      service.handleKompletteringTimeout(handlaggningId);

      assertFalse(storage.getKompletteringTillstand(handlaggningId).isPresent());
   }
}
