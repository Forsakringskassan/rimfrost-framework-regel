package se.fk.rimfrost.framework.regel.logic.entity;

import jakarta.annotation.Nullable;
import org.immutables.value.Value;
import se.fk.rimfrost.framework.regel.logic.dto.Beslutsutfall;
import java.util.UUID;

@Value.Immutable
public interface ErsattningData
{

   UUID id();

   @Nullable
   Beslutsutfall beslutsutfall();

   @Nullable
   String avslagsanledning();

}
