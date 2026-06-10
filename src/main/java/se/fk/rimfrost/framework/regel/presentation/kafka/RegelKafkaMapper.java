package se.fk.rimfrost.framework.regel.presentation.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import se.fk.rimfrost.framework.regel.RegelRequestMessagePayload;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;

@ApplicationScoped
public class RegelKafkaMapper
{
   public RegelDataRequest toRegelDataRequest(RegelRequestMessagePayload request)
   {
      return ImmutableRegelDataRequest.builder()
            .id(UUID.fromString(request.getId()))
            .kogitorootprociid(UUID.fromString(request.getKogitorootprociid()))
            .kogitorootprocid(request.getKogitorootprocid())
            .kogitoparentprociid(UUID.fromString(request.getKogitoparentprociid()))
            .kogitoprocid(request.getKogitoprocid())
            .kogitoprocinstanceid(UUID.fromString(request.getKogitoprocinstanceid()))
            .kogitoprocist(request.getKogitoprocist())
            .kogitoprocversion(request.getKogitoprocversion())
            .handlaggningId(UUID.fromString(request.getData().getHandlaggningId()))
            .aktivitetId(UUID.fromString(request.getData().getAktivitetId()))
            .replyTo(request.getData().getReplyTo())
            .type(request.getType())
            .build();
   }
}
