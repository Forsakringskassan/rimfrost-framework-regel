package se.fk.rimfrost.framework.regel.logic;

import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.handlaggning.model.HandlaggningUpdate;

/**
 * Regel-specific extension point for handling the handläggare's svar during komplettering.
 *
 * <p>The same type {@code T} is used for both the GET response body ({@code readSvarData}) and the
 * PATCH request body ({@code registerSvar}), as required by FRALL-FR-07.7. Regel repos implement
 * this interface with their own OpenAPI-generated type:
 *
 * <pre>
 * {@code @ApplicationScoped}
 * public class RtfKompletteringSvarService
 *         implements KompletteringSvarServiceInterface<RtfKompletteringData> { ... }
 * </pre>
 *
 * @param <T> shared data type for {@code GET /{handlaggningId}/komplettering} response and
 *            {@code PATCH /{handlaggningId}/komplettering} request body
 */
public interface KompletteringSvarServiceInterface<T>
{
   /**
    * Returns the data the handläggare needs to register the sökande's svar.
    *
    * @param handlaggning the current handlaggning
    * @return response body for the komplettering GET endpoint
    */
   T readSvarData(Handlaggning handlaggning);

   /**
    * Applies the svar to the handlaggning and returns an update ready to be persisted.
    *
    * @param handlaggning the current handlaggning
    * @param request      the handläggare's registered svar
    * @return the handlaggning update to persist
    */
   HandlaggningUpdate registerSvar(Handlaggning handlaggning, T request);
}
