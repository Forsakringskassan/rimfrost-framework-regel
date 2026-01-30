package se.fk.rimfrost.framework.regel.integration.kundbehovsflode.dto;

import java.util.UUID;

import org.immutables.value.Value;

import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Roll;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Verksamhetslogik;

@Value.Immutable
public interface UpdateKundbehovsflodeSpecifikation
{

   UUID id();

   String version();

   String namn();

   String uppgiftsbeskrivning();

   Verksamhetslogik verksamhetslogik();

   Roll roll();

   String applikationsId();

   String applikationsversion();

   String url();

   UpdateKundbehovsflodeRegel regel();
}
