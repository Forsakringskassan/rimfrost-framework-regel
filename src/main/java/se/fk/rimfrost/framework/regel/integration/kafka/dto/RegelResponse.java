package se.fk.rimfrost.framework.regel.integration.kafka.dto;

import java.util.UUID;
import org.immutables.value.Value;
import se.fk.rimfrost.framework.regel.Utfall;

@Value.Immutable
public interface RegelResponse
{

   UUID id();

   UUID handlaggningId();

   String kogitorootprocid();

   UUID kogitorootprociid();

   UUID kogitoparentprociid();

   String kogitoprocid();

   UUID kogitoprocinstanceid();

   String kogitoprocist();

   String kogitoprocversion();

   Utfall utfall();

   String type();

   String source();
}
