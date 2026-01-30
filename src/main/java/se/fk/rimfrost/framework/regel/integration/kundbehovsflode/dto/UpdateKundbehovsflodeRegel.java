package se.fk.rimfrost.framework.regel.integration.kundbehovsflode.dto;

import java.util.UUID;

import org.immutables.value.Value;

@Value.Immutable
public interface UpdateKundbehovsflodeRegel
{

   UUID id();

   String version();

   String namn();

   String beskrivning();

   UpdateKundbehovsflodeLagrum lagrum();

}
