package se.fk.rimfrost.framework.regel.logic.storage.internal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import se.fk.rimfrost.framework.regel.logic.dto.KompletteringTillstand;
import se.fk.rimfrost.framework.regel.logic.storage.KompletteringStorage;

/**
 * Panache-backed implementation of {@link KompletteringStorage}.
 */
@ApplicationScoped
@Transactional
public class PanacheKompletteringStorage implements KompletteringStorage
{
   @Inject
   KompletteringTillstandRepository repository;

   @Inject
   KompletteringTillstandMapper mapper;

   @Override
   public void setKompletteringTillstand(UUID handlaggningId, KompletteringTillstand tillstand)
   {
      var existing = repository.findByIdOptional(handlaggningId);
      if (existing.isPresent())
      {
         var updated = mapper.toEntity(handlaggningId, tillstand);
         updated.version = existing.get().version;
         updated.createdAt = existing.get().createdAt;
         repository.getEntityManager().merge(updated);
      }
      else
      {
         repository.persist(mapper.toEntity(handlaggningId, tillstand));
      }
   }

   @Override
   public Optional<KompletteringTillstand> getKompletteringTillstand(UUID handlaggningId)
   {
      return repository.findByIdOptional(handlaggningId)
            .map(mapper::toDomain);
   }

   @Override
   public void deleteKompletteringTillstand(UUID handlaggningId)
   {
      repository.deleteById(handlaggningId);
   }
}
