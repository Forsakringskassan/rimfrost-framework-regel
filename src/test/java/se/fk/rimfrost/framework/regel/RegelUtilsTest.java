package se.fk.rimfrost.framework.regel;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.handlaggning.model.*;
import se.fk.rimfrost.framework.regel.logic.RegelUtils;

public class RegelUtilsTest
{

   private ProduceratResultat resultat(UUID id, String yrkandestatus)
   {
      return ImmutableProduceratResultat.builder()
            .id(id)
            .version(1)
            .resultatFrom(OffsetDateTime.now())
            .resultatTom(OffsetDateTime.now())
            .yrkandeStatus(yrkandestatus)
            .typ("TestTyp")
            .data("TestData")
            .build();
   }

   private Yrkande yrkande(List<ProduceratResultat> resultat, String yrkandestatus)
   {
      return ImmutableYrkande.builder()
            .id(UUID.randomUUID())
            .version(1)
            .erbjudandeId(UUID.randomUUID().toString())
            .yrkandeDatum(OffsetDateTime.now())
            .yrkandeFrom(OffsetDateTime.now())
            .yrkandeTom(OffsetDateTime.now())
            .avsikt("TestAvsikt")
            .yrkandeStatus(yrkandestatus)
            .produceradeResultat(resultat)
            .build();
   }

   // =========================
   // mergeProduceradeResultat
   // =========================

   @Test
   @DisplayName("FRALL-FR-05.1: Merge inkluderar alla nya producerade resultat")
   void merge_shouldIncludeAllUpdatedResults()
   {
      UUID id1 = UUID.randomUUID();
      String yrkandestatus1 = UUID.randomUUID().toString();
      UUID id2 = UUID.randomUUID();
      String yrkandestatus2 = UUID.randomUUID().toString();

      var updated = List.of(resultat(id1, yrkandestatus1), resultat(id2, yrkandestatus2));
      List<ProduceratResultat> previous = List.of();

      var result = RegelUtils.mergeProduceradeResultat(updated, previous);

      assertEquals(2, result.size());
      assertTrue(result.stream().anyMatch(r -> r.id().equals(id1)));
      assertTrue(result.stream().anyMatch(r -> r.id().equals(id2)));
   }

   @Test
   @DisplayName("FRALL-FR-05.1: Merge bevarar tidigare resultat som inte ersätts av nya")
   void merge_shouldKeepPreviousIfNotOverridden()
   {
      UUID id1 = UUID.randomUUID(); // updated
      String yrkandestatus1 = UUID.randomUUID().toString();
      UUID id2 = UUID.randomUUID(); // only in previous
      String yrkandestatus2 = UUID.randomUUID().toString();

      var updated = List.of(resultat(id1, yrkandestatus1));
      var previous = List.of(resultat(id2, yrkandestatus2));

      var result = RegelUtils.mergeProduceradeResultat(updated, previous);

      assertEquals(2, result.size());
      assertTrue(result.stream().anyMatch(r -> r.id().equals(id1)));
      assertTrue(result.stream().anyMatch(r -> r.id().equals(id2)));
   }

   @Test
   @DisplayName("FRALL-FR-05.1: Nytt resultat med samma id ersätter tidigare vid merge")
   void merge_shouldPreferUpdatedOverPreviousWithSameId()
   {
      UUID id = UUID.randomUUID();
      String yrkandestatus1 = UUID.randomUUID().toString();
      String yrkandestatus2 = UUID.randomUUID().toString();

      var updated = List.of(resultat(id, yrkandestatus1));
      var previous = List.of(resultat(id, yrkandestatus2));

      var result = RegelUtils.mergeProduceradeResultat(updated, previous);

      assertEquals(1, result.size());
      assertEquals(id, result.getFirst().id());
      assertEquals(yrkandestatus1, result.getFirst().yrkandeStatus());
   }

   // ==========================================
   // createYrkandeWithUpdatedProduceradeResultat
   // ==========================================

   @Test
   @DisplayName("FRALL-FR-05.2: Nytt yrkande skapas med merge av befintliga och nya producerade resultat")
   void createYrkande_shouldMergeResultsIntoNewInstance()
   {
      UUID id1 = UUID.randomUUID(); // updated
      String yrkandestatus1 = UUID.randomUUID().toString();
      UUID id2 = UUID.randomUUID(); // existing
      String yrkandestatus2 = UUID.randomUUID().toString();

      var originalResults = List.of(resultat(id2, yrkandestatus2));
      var updatedResults = List.of(resultat(id1, yrkandestatus1));

      Yrkande original = yrkande(originalResults, yrkandestatus2);

      Yrkande updated = RegelUtils.createYrkandeWithUpdatedProduceradeResultat(original, updatedResults);

      assertEquals(2, updated.produceradeResultat().size());
      assertTrue(updated.produceradeResultat().stream().anyMatch(r -> r.id().equals(id1)));
      assertTrue(updated.produceradeResultat().stream().anyMatch(r -> r.id().equals(id2)));
   }
}
