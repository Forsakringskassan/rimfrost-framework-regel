package se.fk.rimfrost.framework.regel.logic.dto;

import jakarta.annotation.Nullable;
import java.util.UUID;
import org.immutables.value.Value;

@Value.Immutable
public interface RegelDataRequest
{
   UUID id();

   UUID handlaggningId();

   UUID aktivitetId();

   @Nullable
   String processvyUrl();

   String kogitorootprocid();

   UUID kogitorootprociid();

   UUID kogitoparentprociid();

   String kogitoprocid();

   UUID kogitoprocinstanceid();

   String kogitoprocist();

   String kogitoprocversion();

   String type();

   String replyTo();
}
