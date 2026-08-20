package se.fk.rimfrost.framework.regel.logic.storage;

import java.util.Optional;
import java.util.UUID;
import se.fk.rimfrost.framework.regel.logic.dto.KompletteringTillstand;

/**
 * Stores pending komplettering correlation state between OUL task creation and
 * handläggare svar.
 */
public interface KompletteringStorage
{
   /**
    * Persists state for a new komplettering round. Overwrites any existing state for the
    * same {@code handlaggningId}.
    *
    * @param handlaggningId the handlaggning being processed
    * @param tillstand      the correlation state to store
    */
   void setKompletteringTillstand(UUID handlaggningId, KompletteringTillstand tillstand);

   /**
    * Returns the correlation state, or empty if none exists (already resolved or timed out).
    *
    * @param handlaggningId the handlaggning to look up
    * @return the stored tillstand, or {@code Optional.empty()} if absent
    */
   Optional<KompletteringTillstand> getKompletteringTillstand(UUID handlaggningId);

   /**
    * Removes the correlation state. Safe to call even if already absent.
    *
    * @param handlaggningId the handlaggning whose state to remove
    */
   void deleteKompletteringTillstand(UUID handlaggningId);
}
