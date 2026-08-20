package se.fk.rimfrost.framework.regel.logic;

import java.util.List;
import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.regel.logic.dto.KompletteringUnderlag;

/**
 * Pre-flight completeness check included in all regel service contracts.
 *
 * <p>{@link DefaultKompletteringKontroll} provides the framework-registered CDI bean that
 * returns an empty list, meaning the yrkande is complete and the regel runs immediately.
 * Regel repos that require a completeness check register their own
 * {@code @Alternative @Priority(1)} implementation of this interface instead.
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
