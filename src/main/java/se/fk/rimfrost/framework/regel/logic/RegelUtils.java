package se.fk.rimfrost.framework.regel.logic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableSakfragaStallningstagande;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableUppgiftsdatakopia;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableUppgiftsdatakoppling;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableYrkande;
import se.fk.rimfrost.framework.handlaggning.model.SakfragaStallningstagande;
import se.fk.rimfrost.framework.handlaggning.model.Uppgiftsdatakopia;
import se.fk.rimfrost.framework.handlaggning.model.Uppgiftsdatakoppling;
import se.fk.rimfrost.framework.handlaggning.model.Yrkande;

public class RegelUtils
{

   /**
   * Skapar en {@link Uppgiftsdatakopia} med serialiserat JSON-innehåll.
   *
   * @param informationsobjektId ID för informationsobjektet
   * @param version Version av informationsobjektet
   * @param informationsobjekttyp Typ av informationsobjekt
   * @param object Objektet som ska serialiseras som data
   * @param objectMapper Används för att serialisera object till JSON
   * @return En {@link Uppgiftsdatakopia}
   */
   public static Uppgiftsdatakopia createUppgiftsdatakopia(
         String informationsobjektId,
         int version,
         String informationsobjekttyp,
         Object object,
         ObjectMapper objectMapper)
   {
      try
      {
         return ImmutableUppgiftsdatakopia.builder()
               .informationsobjektId(informationsobjektId)
               .informationsobjektversion(version)
               .informationsobjekttyp(informationsobjekttyp)
               .data(objectMapper.writeValueAsString(object))
               .build();
      }
      catch (JsonProcessingException e)
      {
         throw new InternalError("Could not serialize object to JSON", e);
      }
   }

   /**
   * Skapar en {@link Uppgiftsdatakoppling} som refererar till en specifik version av ett informationsobjekt.
   *
   * @param informationsobjektId ID för informationsobjektet
   * @param version Version av informationsobjektet
   * @return En {@link Uppgiftsdatakoppling}
   */
   public static Uppgiftsdatakoppling createUppgiftsdatakoppling(
         String informationsobjektId, int version)
   {
      return ImmutableUppgiftsdatakoppling.builder()
            .informationsobjektId(informationsobjektId)
            .informationsobjektversion(version)
            .build();
   }

   /**
   * Skapar ett nytt {@link SakfragaStallningstagande} med serialiserat JSON-innehåll.
   *
   * @param id Unikt ID för sakfrågan/ställningstagandet
   * @param objektTypId Typ av objekt
   * @param object Objektet som ska serialiseras som data
   * @param objectMapper Används för att serialisera object till JSON
   * @return Ett {@link SakfragaStallningstagande}
   */
   public static SakfragaStallningstagande createSakfragaStallningstagande(
         UUID id, String objektTypId, Object object, ObjectMapper objectMapper)
   {
      try
      {
         return ImmutableSakfragaStallningstagande.builder()
               .id(id)
               .objektTypId(objektTypId)
               .data(objectMapper.writeValueAsString(object))
               .build();
      }
      catch (JsonProcessingException e)
      {
         throw new InternalError("Could not serialize object to JSON", e);
      }
   }

   /**
   * Skapar ett nytt {@link Yrkande} där listan av sakfrågor/ställningstaganden har mergats så att
   * endast den senaste versionen av varje element inkluderas.
   *
   * @param yrkande Ursprungligt yrkande
   * @param uppdaterade De nya/förändrade sakfrågor/ställningstaganden som ska mergas in
   * @return Uppdaterat yrkande
   */
   public static Yrkande createYrkandeWithUpdatedSakfragorStallningstaganden(
         Yrkande yrkande, List<SakfragaStallningstagande> uppdaterade)
   {
      return ImmutableYrkande.builder()
            .from(yrkande)
            .sakfragorStallningstaganden(
                  mergeSakfragorStallningstaganden(uppdaterade, yrkande.sakfragorStallningstaganden()))
            .build();
   }

   /**
   * Mergar två listor av sakfrågor/ställningstaganden.
   *
   * <p>Den mergade listan innehåller alla element ur {@code uppdaterade}, samt de element ur
   * {@code tidigare} som ej har motsvarande id i {@code uppdaterade}.</p>
   *
   * @param uppdaterade Nya/förändrade sakfrågor/ställningstaganden
   * @param tidigare Ursprungliga sakfrågor/ställningstaganden
   * @return Mergad lista
   */
   public static List<SakfragaStallningstagande> mergeSakfragorStallningstaganden(
         List<SakfragaStallningstagande> uppdaterade, List<SakfragaStallningstagande> tidigare)
   {
      Set<UUID> idsInUppdaterade = uppdaterade.stream()
            .map(SakfragaStallningstagande::id)
            .collect(Collectors.toSet());
      List<SakfragaStallningstagande> result = new ArrayList<>(uppdaterade);
      tidigare.stream()
            .filter(s -> !idsInUppdaterade.contains(s.id()))
            .forEach(result::add);
      return result;
   }

   /**
   * Serialiserar ett objekt till JSON-sträng, lämplig att använda som {@code data}-fält i en {@link Uppgiftsdatakopia}.
   *
   * @param object Objektet som ska serialiseras
   * @param objectMapper Används för att serialisera object till JSON
   * @return JSON-representation av object
   */
   public static String toJson(Object object, ObjectMapper objectMapper)
   {
      try
      {
         return objectMapper.writeValueAsString(object);
      }
      catch (JsonProcessingException e)
      {
         throw new InternalError("Could not serialize object to JSON", e);
      }
   }
}
