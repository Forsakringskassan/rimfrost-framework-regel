package se.fk.rimfrost.framework.regel.logic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableUnderlag;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableYrkande;
import se.fk.rimfrost.framework.handlaggning.model.ProduceratResultat;
import se.fk.rimfrost.framework.handlaggning.model.Underlag;
import se.fk.rimfrost.framework.handlaggning.model.Yrkande;

public class RegelUtils
{

   /**
   * Skapar nytt Yrkande-objekt där listan av producerade resultat har mergats så att endast den
   * senaste versionen av resultaten inkluderas.
   *
   * @param yrkande Ursprungligt yrkande
   * @param uppdateradeResultat De nya/förändrade resultat som ska mergas in i yrkandets resultat
   * @return Uppdaterat yrkande
   */
   public static Yrkande createYrkandeWithUpdatedProduceradeResultat(
         Yrkande yrkande, List<ProduceratResultat> uppdateradeResultat)
   {
      return ImmutableYrkande.builder()
            .from(yrkande)
            .produceradeResultat(
                  mergeProduceradeResultat(uppdateradeResultat, yrkande.produceradeResultat()))
            .build();
   }

   /**
   * Mergar två listor av producerade resultat.
   *
   * <p>Den mergade listan innehåller alla element ur uppdateradeResultat, samt de element ur
   * tidigareResultat som ej har motsvarande id i uppdateradeResultat.
   *
   * @param uppdateradeResultat Nya/förändrade resultat
   * @param tidigareResultat Ursprungliga resultat
   * @return Mergad lista av producerade resultat
   */
   public static List<ProduceratResultat> mergeProduceradeResultat(
         List<ProduceratResultat> uppdateradeResultat, List<ProduceratResultat> tidigareResultat)
   {
      Set<UUID> idsInUppdateradeResultat = uppdateradeResultat.stream().map(ProduceratResultat::id).collect(Collectors.toSet());
      List<ProduceratResultat> result = new ArrayList<>(uppdateradeResultat);
      tidigareResultat.stream()
            .filter(a -> !idsInUppdateradeResultat.contains(a.id()))
            .forEach(result::add);
      return result;
   }

   /**
   * @param typ Vilken typ av underlag
   * @param version Vilken version av underlaget
   * @param object Objektet som representerar underlaget
   * @param objectMapper Används för att mappa object till json
   * @return Ett underlag
   */
   public static Underlag createUnderlag(
         String typ, int version, Object object, ObjectMapper objectMapper)
   {
      try
      {
         return ImmutableUnderlag.builder()
               .typ(typ)
               .version(version)
               .data(objectMapper.writeValueAsString(object))
               .build();
      }
      catch (JsonProcessingException e)
      {
         throw new InternalError("Could not parse object to String", e);
      }
   }

   /***
   *
   * @param object Objekt som ska bli till json-format
   * @param objectMapper Används för att mappa object till json
   * @return object i json-format
   */
   public static String createProduceratResultatData(Object object, ObjectMapper objectMapper)
   {
      try
      {
         return objectMapper.writeValueAsString(object);
      }
      catch (JsonProcessingException e)
      {
         throw new InternalError("Could not parse object to String", e);
      }
   }
}
