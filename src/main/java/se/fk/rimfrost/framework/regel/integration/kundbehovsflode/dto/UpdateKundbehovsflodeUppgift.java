package se.fk.rimfrost.framework.regel.integration.kundbehovsflode.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;
import se.fk.rimfrost.framework.regel.logic.dto.UppgiftStatus;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.FSSAinformation;

@Value.Immutable
public interface UpdateKundbehovsflodeUppgift
{
   UUID id();

   String version();

   OffsetDateTime skapadTs();

   @Nullable
   OffsetDateTime utfordTs();

   @Nullable
   OffsetDateTime planeradTs();

   @Nullable
   UUID utforarId();

   UppgiftStatus uppgiftStatus();

   String aktivitet();

   FSSAinformation fsSAinformation();

   UpdateKundbehovsflodeSpecifikation specifikation();
}
