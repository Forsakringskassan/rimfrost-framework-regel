package se.fk.rimfrost.framework.logic;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.integration.kafka.dto.ImmutableRegelResponse;
import se.fk.rimfrost.framework.integration.kafka.dto.RegelResponse;
import se.fk.rimfrost.framework.logic.entity.CloudEventData;
import se.fk.rimfrost.regel.common.Utfall;

import java.util.UUID;

@SuppressWarnings("unused")
@ApplicationScoped
public class RegelMapper
{

   public RegelResponse toRegelResponse(UUID kundbehovsflodeId, CloudEventData cloudevent, Utfall utfall)
   {
      return ImmutableRegelResponse.builder()
            .id(cloudevent.id())
            .kundbehovsflodeId(kundbehovsflodeId)
            .kogitoparentprociid(cloudevent.kogitoparentprociid())
            .kogitorootprociid(cloudevent.kogitorootprociid())
            .kogitoprocid(cloudevent.kogitoprocid())
            .kogitorootprocid(cloudevent.kogitorootprocid())
            .kogitoprocinstanceid(cloudevent.kogitoprocinstanceid())
            .kogitoprocist(cloudevent.kogitoprocist())
            .kogitoprocversion(cloudevent.kogitoprocversion())
            .utfall(utfall)
            .type(cloudevent.type())
            .source(cloudevent.source())
            .build();
   }
}
