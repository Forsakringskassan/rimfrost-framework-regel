package se.fk.rimfrost.framework.regel.presentation.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelResultRequest;
import se.fk.rimfrost.framework.regel.logic.dto.RegelResultRequest;
import se.fk.rimfrost.framework.regel.RegelRequestMessagePayload;
import java.util.UUID;

@ApplicationScoped
public class RegelKafkaMapper
{
   public RegelResultRequest toRegelResultRequest(RegelRequestMessagePayload rtfRequest)
   {
      return ImmutableRegelResultRequest.builder()
            .id(UUID.fromString(rtfRequest.getId()))
            .kogitorootprociid(UUID.fromString(rtfRequest.getKogitorootprociid()))
            .kogitorootprocid(rtfRequest.getKogitorootprocid())
            .kogitoparentprociid(UUID.fromString(rtfRequest.getKogitoparentprociid()))
            .kogitoprocid(rtfRequest.getKogitoprocid())
            .kogitoprocinstanceid(UUID.fromString(rtfRequest.getKogitoprocinstanceid()))
            .kogitoprocist(rtfRequest.getKogitoprocist())
            .kogitoprocversion(rtfRequest.getKogitoprocversion())
            .kundbehovsflodeId(UUID.fromString(rtfRequest.getData().getKundbehovsflodeId()))
            .type(rtfRequest.getType())
            .build();
   }

}
