package se.fk.rimfrost.framework.regel.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.oul.adapter.OulAdapter;
import se.fk.rimfrost.framework.oul.exception.OulException;
import se.fk.rimfrost.framework.regel.logic.storage.KompletteringStorage;

/**
 * Handles the komplettering timeout triggered by the BPMN event-based gateway timer branch.
 */
@ApplicationScoped
public class KompletteringService
{
   private static final Logger LOGGER = LoggerFactory.getLogger(KompletteringService.class);

   @Inject
   KompletteringStorage storage;

   @Inject
   OulAdapter oulAdapter;

   /**
    * Ends the open OUL task and clears storage when the komplettering timer fires.
    *
    * <p>Safe to call if the handläggare completed just before the timeout fired —
    * logs and returns without throwing if storage is already empty.
    *
    * @param handlaggningId the handlaggning whose komplettering round timed out
    */
   public void handleKompletteringTimeout(UUID handlaggningId)
   {
      var tillstand = storage.getKompletteringTillstand(handlaggningId);
      if (tillstand.isEmpty())
      {
         LOGGER.info("Komplettering already resolved for handlaggningId: {}, skipping timeout handling",
               handlaggningId);
         return;
      }

      try
      {
         oulAdapter.endOperativUppgift(tillstand.get().oulUppgiftId(), "Timeout");
      }
      catch (OulException e)
      {
         LOGGER.error(
               "Failed to end operativ uppgift on komplettering timeout. handlaggningId: {}, oulUppgiftId: {}",
               handlaggningId, tillstand.get().oulUppgiftId(), e);
      }

      storage.deleteKompletteringTillstand(handlaggningId);
   }
}
