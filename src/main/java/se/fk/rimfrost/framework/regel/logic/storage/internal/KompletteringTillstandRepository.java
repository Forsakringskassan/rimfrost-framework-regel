package se.fk.rimfrost.framework.regel.logic.storage.internal;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

/**
 * Panache repository for {@link KompletteringTillstandEntity}.
 */
@ApplicationScoped
public class KompletteringTillstandRepository
      implements PanacheRepositoryBase<KompletteringTillstandEntity, UUID>
{
}
