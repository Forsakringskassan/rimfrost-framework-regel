package se.fk.rimfrost.framework.regel.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.handlaggning.exception.HandlaggningException;
import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.handlaggning.model.HandlaggningUpdate;
import se.fk.rimfrost.framework.oul.adapter.OulAdapter;
import se.fk.rimfrost.framework.oul.exception.OulException;
import se.fk.rimfrost.framework.regel.RegelRequestMessagePayload;
import se.fk.rimfrost.framework.regel.logic.KompletteringKontrollInterface;
import se.fk.rimfrost.framework.regel.logic.KompletteringSvarServiceInterface;
import se.fk.rimfrost.framework.regel.logic.storage.KompletteringStorage;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelKafkaMapper;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;

/**
 * Abstract base controller for komplettering endpoints. Extend with the regel's own
 * OpenAPI-generated request and response types:
 *
 * <pre>
 * {@code @Path("/api/rtf")}
 * public class RtfKompletteringController
 *         extends KompletteringController<RtfKompletteringResponse, RtfPatchKompletteringRequest> {}
 * </pre>
 *
 * @param <T> GET response type — data shown to the handläggare
 * @param <Y> PATCH request type — the handläggare's registered svar
 */
public abstract class KompletteringController<T, Y>
{
   private static final Logger LOGGER = LoggerFactory.getLogger(KompletteringController.class);

   @Inject
   HandlaggningAdapter handlaggningAdapter;

   @Inject
   KompletteringSvarServiceInterface<T, Y> svarService;

   @Inject
   KompletteringKontrollInterface kompletteringKontroll;

   @Inject
   KompletteringStorage storage;

   @Inject
   OulAdapter oulAdapter;

   @Inject
   RegelRequestHandlerInterface regelRequestHandler;

   @Inject
   RegelKafkaMapper regelKafkaMapper;

   @Inject
   ObjectMapper objectMapper;

   /**
    * Returns the data the handläggare needs to register the sökande's svar.
    *
    * @param handlaggningId the handlaggning under komplettering
    * @return 200 with svar data, or 404/503 if handlaggning is unavailable
    */
   @GET
   @Path("/{handlaggningId}/komplettering")
   public Response getKomplettering(@PathParam("handlaggningId") UUID handlaggningId)
   {
      var handlaggning = fetchHandlaggning(handlaggningId);
      return Response.ok(svarService.readSvarData(handlaggning)).build();
   }

   /**
    * Registers the sökande's svar for the missing attributes.
    *
    * @param handlaggningId the handlaggning under komplettering
    * @param request        the handläggare's registered svar
    */
   @PATCH
   @Path("/{handlaggningId}/komplettering")
   public void patchKomplettering(@PathParam("handlaggningId") UUID handlaggningId,
         @Valid @NotNull Y request)
   {
      var handlaggning = fetchHandlaggning(handlaggningId);
      var update = svarService.registerSvar(handlaggning, request);
      updateHandlaggning(handlaggningId, update);
   }

   /**
    * Marks komplettering as complete.
    *
    * <p>Calls {@code checkKomplettering()} to verify the yrkande is now complete — returns
    * HTTP 422 if attributes are still missing. On success, ends the OUL task, triggers
    * the regel via {@code handleRegelRequest}, and clears correlation storage.
    * Returns HTTP 409 if the timeout has already cleared storage.
    *
    * @param handlaggningId the handlaggning whose komplettering is done
    */
   @POST
   @Path("/{handlaggningId}/komplettering/done")
   public void kompletteringDone(@PathParam("handlaggningId") UUID handlaggningId)
   {
      var tillstand = storage.getKompletteringTillstand(handlaggningId);
      if (tillstand.isEmpty())
      {
         throw new jakarta.ws.rs.WebApplicationException(Response.Status.CONFLICT);
      }

      var handlaggning = fetchHandlaggning(handlaggningId);
      var saknade = kompletteringKontroll.checkKomplettering(handlaggning);
      if (!saknade.isEmpty())
      {
         throw new jakarta.ws.rs.WebApplicationException(Response.status(422).build());
      }

      try
      {
         oulAdapter.endOperativUppgift(tillstand.get().oulUppgiftId(), "Komplettering klar");
      }
      catch (OulException e)
      {
         LOGGER.error(
               "Failed to end operativ uppgift on komplettering done. handlaggningId: {}, oulUppgiftId: {}",
               handlaggningId, tillstand.get().oulUppgiftId(), e);
      }

      try
      {
         var payload = objectMapper.readValue(tillstand.get().cloudEventData(), RegelRequestMessagePayload.class);
         var regelRequest = regelKafkaMapper.toRegelDataRequest(payload);
         regelRequestHandler.handleRegelRequest(regelRequest);
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to trigger regel after komplettering done. handlaggningId: {}", handlaggningId, e);
         throw new jakarta.ws.rs.WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }

      storage.deleteKompletteringTillstand(handlaggningId);
   }

   private Handlaggning fetchHandlaggning(UUID handlaggningId)
   {
      try
      {
         return handlaggningAdapter.readHandlaggning(handlaggningId);
      }
      catch (HandlaggningException e)
      {
         throw new jakarta.ws.rs.WebApplicationException(toHttpStatus(e));
      }
   }

   private void updateHandlaggning(UUID handlaggningId, HandlaggningUpdate update)
   {
      try
      {
         handlaggningAdapter.updateHandlaggning(update);
      }
      catch (HandlaggningException e)
      {
         LOGGER.error("Failed to update handlaggning. handlaggningId: {}", handlaggningId, e);
         throw new jakarta.ws.rs.WebApplicationException(toHttpStatus(e));
      }
   }

   private Response.Status toHttpStatus(HandlaggningException e)
   {
      return switch (e.getErrorType())
      {
         case NOT_FOUND -> Response.Status.NOT_FOUND;
         case BAD_REQUEST -> Response.Status.BAD_REQUEST;
         case SERVICE_UNAVAILABLE -> Response.Status.SERVICE_UNAVAILABLE;
         default -> Response.Status.INTERNAL_SERVER_ERROR;
      };
   }
}
