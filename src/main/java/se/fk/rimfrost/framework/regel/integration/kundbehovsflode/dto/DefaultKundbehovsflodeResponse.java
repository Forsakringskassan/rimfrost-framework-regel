package se.fk.rimfrost.framework.regel.integration.kundbehovsflode.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;

@Value.Immutable
public interface DefaultKundbehovsflodeResponse
{

   UUID kundbehovsflodeId();

   String personnummer();

   String formanstyp();

   List<Ersattning> ersattning();

   @Value.Immutable
   public interface Ersattning
   {

      UUID ersattningsId();

      String ersattningsTyp();

      int omfattningsProcent();

      int belopp();

      int berakningsgrund();

      @Nullable
      String beslutsutfall();

      LocalDate franOchMed();

      LocalDate tillOchMed();
   }

}
