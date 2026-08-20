package se.fk.rimfrost.framework.regel;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableKompletteringTillstand;
import se.fk.rimfrost.framework.regel.logic.storage.KompletteringStorage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class KompletteringStorageTest
{
   @Inject
   KompletteringStorage storage;

   private ImmutableKompletteringTillstand.Builder tillstandBuilder()
   {
      return ImmutableKompletteringTillstand.builder()
            .oulUppgiftId(UUID.randomUUID())
            .replyTo("reply-topic")
            .cloudEventData("{}");
   }

   @Test
   @DisplayName("FRALL-FR-06.3: Stored tillstand can be retrieved by handlaggningId")
   void should_return_stored_tillstand()
   {
      var handlaggningId = UUID.randomUUID();
      var tillstand = tillstandBuilder().build();

      storage.setKompletteringTillstand(handlaggningId, tillstand);

      var result = storage.getKompletteringTillstand(handlaggningId);
      assertTrue(result.isPresent());
      assertEquals(tillstand, result.get());
   }

   @Test
   @DisplayName("FRALL-FR-06.4: get returns empty for unknown handlaggningId")
   void should_return_empty_for_unknown_handlaggning_id()
   {
      assertFalse(storage.getKompletteringTillstand(UUID.randomUUID()).isPresent());
   }

   @Test
   @DisplayName("FRALL-FR-06.3, FRALL-FR-06.5: Deleted tillstand is no longer retrievable")
   void should_return_empty_after_delete()
   {
      var handlaggningId = UUID.randomUUID();
      storage.setKompletteringTillstand(handlaggningId, tillstandBuilder().build());

      storage.deleteKompletteringTillstand(handlaggningId);

      assertFalse(storage.getKompletteringTillstand(handlaggningId).isPresent());
   }

   @Test
   @DisplayName("FRALL-FR-06.5: delete on absent handlaggningId does not throw")
   void should_not_throw_on_delete_of_absent_entry()
   {
      storage.deleteKompletteringTillstand(UUID.randomUUID());
   }
}
