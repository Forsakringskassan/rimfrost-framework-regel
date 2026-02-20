package se.fk.rimfrost.framework.regel.integration.kafka.dto;

import org.immutables.value.Value;
import se.fk.rimfrost.framework.regel.Utfall;

import java.util.UUID;

@Value.Immutable
public interface RegelResponse
{

   UUID id();

   UUID kundbehovsflodeId();

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
