package se.fk.rimfrost.framework.regel.logic.storage.internal;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableKompletteringTillstand;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.KompletteringTillstand;

/**
 * Maps between {@link KompletteringTillstand} (domain) and {@link KompletteringTillstandEntity} (JPA).
 */
@ApplicationScoped
public class KompletteringTillstandMapper
{
   /**
    * Maps a domain object to a new JPA entity keyed on the given {@code handlaggningId}.
    *
    * @param handlaggningId the primary key
    * @param tillstand      the domain object to map
    * @return a new entity ready for persistence
    */
   public KompletteringTillstandEntity toEntity(UUID handlaggningId, KompletteringTillstand tillstand)
   {
      var request = tillstand.regelDataRequest();
      var entity = new KompletteringTillstandEntity();
      entity.handlaggningId = handlaggningId;
      entity.oulUppgiftId = tillstand.oulUppgiftId();
      entity.regelRequestId = request.id();
      entity.aktivitetId = request.aktivitetId();
      entity.replyTo = request.replyTo();
      entity.type = request.type();
      entity.kogitorootprocid = request.kogitorootprocid();
      entity.kogitorootprociid = request.kogitorootprociid();
      entity.kogitoparentprociid = request.kogitoparentprociid();
      entity.kogitoprocid = request.kogitoprocid();
      entity.kogitoprocinstanceid = request.kogitoprocinstanceid();
      entity.kogitoprocist = request.kogitoprocist();
      entity.kogitoprocversion = request.kogitoprocversion();
      return entity;
   }

   /**
    * Maps a JPA entity to a domain object.
    *
    * @param entity the entity to map
    * @return the corresponding domain object
    */
   public KompletteringTillstand toDomain(KompletteringTillstandEntity entity)
   {
      return ImmutableKompletteringTillstand.builder()
            .oulUppgiftId(entity.oulUppgiftId)
            .regelDataRequest(ImmutableRegelDataRequest.builder()
                  .id(entity.regelRequestId)
                  .handlaggningId(entity.handlaggningId)
                  .aktivitetId(entity.aktivitetId)
                  .replyTo(entity.replyTo)
                  .type(entity.type)
                  .kogitorootprocid(entity.kogitorootprocid)
                  .kogitorootprociid(entity.kogitorootprociid)
                  .kogitoparentprociid(entity.kogitoparentprociid)
                  .kogitoprocid(entity.kogitoprocid)
                  .kogitoprocinstanceid(entity.kogitoprocinstanceid)
                  .kogitoprocist(entity.kogitoprocist)
                  .kogitoprocversion(entity.kogitoprocversion)
                  .build())
            .build();
   }
}
