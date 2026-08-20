package se.fk.rimfrost.framework.regel.logic;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.regel.logic.dto.KompletteringUnderlag;

/**
 * Default implementation of {@link KompletteringKontrollInterface} that always returns an empty
 * list, meaning the yrkande is considered complete and the regel runs immediately.
 *
 * <p>Regel repos that require a completeness check should register their own
 * {@code @Alternative @Priority(1)} implementation instead of overriding this bean.
 */
@ApplicationScoped
public class DefaultKompletteringKontroll implements KompletteringKontrollInterface
{
   @Override
   public List<KompletteringUnderlag> checkKomplettering(Handlaggning handlaggning)
   {
      return List.of();
   }
}
