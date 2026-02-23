package se.fk.rimfrost.framework.regel.logic.entity;

import jakarta.annotation.Nullable;
import org.immutables.value.Value;
import se.fk.rimfrost.framework.regel.logic.dto.FSSAinformation;
import se.fk.rimfrost.framework.regel.logic.dto.UppgiftStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value.Immutable
public interface UppgiftData
{
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
}
