package se.fk.rimfrost.framework.regel.logic;

import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.handlaggning.model.HandlaggningUpdate;

/**
 * Regel-specific extension point for handling the handläggare's svar during komplettering.
 *
 * <p>Regel repos implement this interface with their own OpenAPI-generated types:
 *
 * <pre>
 * {@code @ApplicationScoped}
 * public class RtfKompletteringSvarService
 *         implements KompletteringSvarServiceInterface<RtfKompletteringResponse, RtfPatchKompletteringRequest> { ... }
 * </pre>
 *
 * @param <T> response type for {@code GET /{handlaggningId}/komplettering}
 * @param <Y> request type for {@code PATCH /{handlaggningId}/komplettering}
 */
public interface KompletteringSvarServiceInterface<T, Y>
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
   HandlaggningUpdate registerSvar(Handlaggning handlaggning, Y request);
}
