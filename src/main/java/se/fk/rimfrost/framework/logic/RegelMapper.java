package se.fk.rimfrost.framework.logic;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.integration.kafka.dto.ImmutableRegelResponse;
import se.fk.rimfrost.framework.integration.kafka.dto.RegelResponse;
import se.fk.rimfrost.framework.logic.entity.CloudEventData;
import java.util.UUID;

@SuppressWarnings("unused")
@ApplicationScoped
public class RegelMapper
{

   public RegelResponse toRegelResponse(UUID kundbehovsflodeId, CloudEventData cloudevent, boolean rattTillForsakring)
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
            .rattTillForsakring(rattTillForsakring)
            .build();
   }

}
