package se.fk.rimfrost.framework.regel.logic;

import java.util.List;
import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.regel.logic.dto.KompletteringUnderlag;

/**
 * Pre-flight completeness check included in all regel service contracts.
 *
 * <p>Regel repos must register an {@code @ApplicationScoped} bean implementing this interface.
 * The default method returns an empty list — no override is required unless the regel needs
 * a completeness check. Regel repos that do override return one {@link KompletteringUnderlag}
 * per missing attribute.
 *
 * <p>The check inspects {@code handlaggning.yrkande()} and its nested fields — not stored
 * underlag, which are not available on {@link Handlaggning}.
 *
 * <p>This interface is extended by {@code RegelMaskinellServiceInterface} and
 * {@code RegelManuellServiceInterface}.
 */
public interface KompletteringKontrollInterface
{
   /**
    * Returns the list of attributes missing from the yrkande, or an empty list if the
    * yrkande is complete.
    *
    * @param handlaggning the current handlaggning; inspect {@code handlaggning.yrkande()}
    * @return missing attributes, one entry per gap; empty means proceed with the regel
    */
   default List<KompletteringUnderlag> checkKomplettering(Handlaggning handlaggning)
   {
      return List.of();
   }
}
