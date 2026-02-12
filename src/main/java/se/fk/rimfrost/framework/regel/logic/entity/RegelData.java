package se.fk.rimfrost.framework.regel.logic.entity;

import jakarta.annotation.Nullable;
import org.immutables.value.Value;

import se.fk.rimfrost.framework.regel.logic.dto.FSSAinformation;
import se.fk.rimfrost.framework.regel.logic.dto.UppgiftStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Value.Immutable
public interface RegelData
{

   UUID kundbehovsflodeId();

   UUID cloudeventId();

   @Nullable
   UUID uppgiftId();

   @Nullable
   UUID utforarId();

   OffsetDateTime skapadTs();

   @Nullable
   OffsetDateTime utfordTs();

   @Nullable
   OffsetDateTime planeradTs();

   UppgiftStatus uppgiftStatus();

   FSSAinformation fssaInformation();

   List<ErsattningData> ersattningar();

   List<Underlag> underlag();

}
