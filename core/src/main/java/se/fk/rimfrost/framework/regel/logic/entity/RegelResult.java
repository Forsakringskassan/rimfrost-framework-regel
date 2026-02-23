package se.fk.rimfrost.framework.regel.logic.entity;

import org.immutables.value.Value;

import jakarta.validation.constraints.NotNull;
import se.fk.rimfrost.framework.regel.Utfall;
import jakarta.annotation.Nullable;

import se.fk.rimfrost.framework.regel.logic.dto.FSSAinformation;
import se.fk.rimfrost.framework.regel.logic.dto.UppgiftStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Value.Immutable
public interface RegelResult
{
   @NotNull
   UppgiftData uppgiftData();

   @NotNull
   List<ErsattningData> ersattningar();

   @NotNull
   List<Underlag> underlag();

   @NotNull
   Utfall utfall();

}
