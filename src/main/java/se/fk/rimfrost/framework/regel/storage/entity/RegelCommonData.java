package se.fk.rimfrost.framework.regel.storage.entity;

import org.immutables.value.Value;
import se.fk.rimfrost.framework.handlaggning.model.Uppgift;
import jakarta.annotation.Nullable;
import java.util.UUID;

@Value.Immutable
public interface RegelCommonData
{
   Uppgift uppgift();

   @Nullable
   UUID oulUppgiftId();
}
