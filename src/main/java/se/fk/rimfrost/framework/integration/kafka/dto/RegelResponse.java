package se.fk.rimfrost.framework.integration.kafka.dto;

import org.immutables.value.Value;
import se.fk.rimfrost.regel.common.Utfall;

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

}
