package se.fk.rimfrost.framework.regel.logic;

import se.fk.rimfrost.framework.handlaggning.model.ImmutableYrkande;
import se.fk.rimfrost.framework.handlaggning.model.ProduceratResultat;
import se.fk.rimfrost.framework.handlaggning.model.Yrkande;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RegelProduceradeResultat
{

   public static Yrkande createYrkandeWithUpdatedProduceradeResultat(Yrkande yrkande,
         List<ProduceratResultat> uppdateradeResultat)
   {
      return ImmutableYrkande.builder()
            .from(yrkande)
            .produceradeResultat(
                  mergeProduceradeResultat(
                        uppdateradeResultat,
                        yrkande.produceradeResultat()))
            .build();
   }

   private static List<ProduceratResultat> mergeProduceradeResultat(List<ProduceratResultat> uppdateradeResultat,
         List<ProduceratResultat> tidigareResultat)
   {
      Set<UUID> idsInUppdateradeResultat = uppdateradeResultat.stream()
            .map(ProduceratResultat::id)
            .collect(Collectors.toSet());
      List<ProduceratResultat> result = new ArrayList<>(uppdateradeResultat);
      tidigareResultat.stream()
            .filter(a -> !idsInUppdateradeResultat.contains(a.id()))
            .forEach(result::add);
      return result;
   }

}
