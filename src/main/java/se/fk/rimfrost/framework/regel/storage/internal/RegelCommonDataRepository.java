package se.fk.rimfrost.framework.regel.storage.internal;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
class RegelCommonDataRepository implements PanacheRepositoryBase<RegelCommonDataEntity, UUID>
{
}
