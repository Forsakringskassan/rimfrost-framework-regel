package se.fk.rimfrost.framework.regel;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableSakfragaStallningstagande;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableYrkande;
import se.fk.rimfrost.framework.handlaggning.model.SakfragaStallningstagande;
import se.fk.rimfrost.framework.handlaggning.model.Yrkande;
import se.fk.rimfrost.framework.regel.logic.RegelUtils;

public class RegelUtilsTest
{
   private static final ObjectMapper MAPPER = new ObjectMapper();

   private SakfragaStallningstagande sakfraga(UUID id, String objektTypId)
   {
      return ImmutableSakfragaStallningstagande.builder()
            .id(id)
            .objektTypId(objektTypId)
            .data("{}")
            .build();
   }

   private Yrkande yrkande(List<SakfragaStallningstagande> sakfragor)
   {
      return ImmutableYrkande.builder()
            .id(UUID.randomUUID())
            .version(1)
            .ingangtypId("TestIngangtypId")
            .yrkandeDatum(OffsetDateTime.now())
            .yrkandeFrom(OffsetDateTime.now())
            .yrkandeTom(OffsetDateTime.now())
            .avsikt("TestAvsikt")
            .yrkandeStatus("AKTIV")
            .sakfragorStallningstaganden(sakfragor)
            .build();
   }

   // ======================================
   // mergeSakfragorStallningstaganden
   // ======================================

   @Test
   @DisplayName("FRALL-FR-05.1: Merge inkluderar alla nya sakfrågor/ställningstaganden")
   void merge_shouldIncludeAllUpdated()
   {
      UUID id1 = UUID.randomUUID();
      UUID id2 = UUID.randomUUID();

      var updated = List.of(sakfraga(id1, "typ1"), sakfraga(id2, "typ2"));
      var previous = List.<SakfragaStallningstagande> of();

      var result = RegelUtils.mergeSakfragorStallningstaganden(updated, previous);

      assertEquals(2, result.size());
      assertTrue(result.stream().anyMatch(s -> s.id().equals(id1)));
      assertTrue(result.stream().anyMatch(s -> s.id().equals(id2)));
   }

   @Test
   @DisplayName("FRALL-FR-05.1: Merge bevarar tidigare sakfrågor som inte ersätts av nya")
   void merge_shouldKeepPreviousIfNotOverridden()
   {
      UUID id1 = UUID.randomUUID();
      UUID id2 = UUID.randomUUID();

      var updated = List.of(sakfraga(id1, "typ1"));
      var previous = List.of(sakfraga(id2, "typ2"));

      var result = RegelUtils.mergeSakfragorStallningstaganden(updated, previous);

      assertEquals(2, result.size());
      assertTrue(result.stream().anyMatch(s -> s.id().equals(id1)));
      assertTrue(result.stream().anyMatch(s -> s.id().equals(id2)));
   }

   @Test
   @DisplayName("FRALL-FR-05.1: Ny sakfråga med samma id ersätter tidigare vid merge")
   void merge_shouldPreferUpdatedOverPreviousWithSameId()
   {
      UUID id = UUID.randomUUID();

      var updated = List.of(sakfraga(id, "nyTyp"));
      var previous = List.of(sakfraga(id, "gammalTyp"));

      var result = RegelUtils.mergeSakfragorStallningstaganden(updated, previous);

      assertEquals(1, result.size());
      assertEquals("nyTyp", result.getFirst().objektTypId());
   }

   // ===========================================================
   // createYrkandeWithUpdatedSakfragorStallningstaganden
   // ===========================================================

   @Test
   @DisplayName("FRALL-FR-05.2: Nytt yrkande skapas med merge av befintliga och nya sakfrågor/ställningstaganden")
   void createYrkande_shouldMergeSakfragorIntoNewInstance()
   {
      UUID id1 = UUID.randomUUID();
      UUID id2 = UUID.randomUUID();

      Yrkande original = yrkande(List.of(sakfraga(id2, "gammal")));
      var uppdaterade = List.of(sakfraga(id1, "ny"));

      Yrkande updated = RegelUtils.createYrkandeWithUpdatedSakfragorStallningstaganden(original, uppdaterade);

      assertEquals(2, updated.sakfragorStallningstaganden().size());
      assertTrue(updated.sakfragorStallningstaganden().stream().anyMatch(s -> s.id().equals(id1)));
      assertTrue(updated.sakfragorStallningstaganden().stream().anyMatch(s -> s.id().equals(id2)));
   }

   // ======================================
   // createSakfragaStallningstagande
   // ======================================

   @Test
   @DisplayName("FRALL-FR-05.3: createSakfragaStallningstagande sätter id, objektTypId och serialiserat data")
   void createSakfragaStallningstagande_shouldSetAllFields() throws Exception
   {
      UUID id = UUID.randomUUID();
      var payload = new TestPayload("hej");

      var result = RegelUtils.createSakfragaStallningstagande(id, "testTyp", payload, MAPPER);

      assertEquals(id, result.id());
      assertEquals("testTyp", result.objektTypId());
      assertEquals(MAPPER.writeValueAsString(payload), result.data());
   }

   // ======================================
   // createUppgiftsdatakopia
   // ======================================

   @Test
   @DisplayName("FRALL-FR-05.4: createUppgiftsdatakopia sätter alla fält och serialiserar data")
   void createUppgiftsdatakopia_shouldSetAllFields() throws Exception
   {
      var payload = new TestPayload("data");

      var result = RegelUtils.createUppgiftsdatakopia("obj-1", 3, "TypA", payload, MAPPER);

      assertEquals("obj-1", result.informationsobjektId());
      assertEquals(3, result.informationsobjektversion());
      assertEquals("TypA", result.informationsobjekttyp());
      assertEquals(MAPPER.writeValueAsString(payload), result.data());
   }

   // ======================================
   // createUppgiftsdatakoppling
   // ======================================

   @Test
   @DisplayName("FRALL-FR-05.5: createUppgiftsdatakoppling sätter informationsobjektId och version")
   void createUppgiftsdatakoppling_shouldSetAllFields()
   {
      var result = RegelUtils.createUppgiftsdatakoppling("obj-2", 7);

      assertEquals("obj-2", result.informationsobjektId());
      assertEquals(7, result.informationsobjektversion());
   }

   // ======================================
   // toJson
   // ======================================

   @Test
   @DisplayName("FRALL-FR-05.6: toJson serialiserar objekt till JSON-sträng")
   void toJson_shouldSerializeObject() throws Exception
   {
      var payload = new TestPayload("värde");

      var result = RegelUtils.toJson(payload, MAPPER);

      assertEquals(MAPPER.writeValueAsString(payload), result);
   }

   record TestPayload(String value)
   {
   }
}
