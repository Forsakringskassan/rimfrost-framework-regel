package se.fk.rimfrost.framework.regel.logic.storage.internal;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableKompletteringTillstand;
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
      var entity = new KompletteringTillstandEntity();
      entity.handlaggningId = handlaggningId;
      entity.oulUppgiftId = tillstand.oulUppgiftId();
      entity.replyTo = tillstand.replyTo();
      entity.cloudEventData = tillstand.cloudEventData();
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
            .replyTo(entity.replyTo)
            .cloudEventData(entity.cloudEventData)
            .build();
   }
}
