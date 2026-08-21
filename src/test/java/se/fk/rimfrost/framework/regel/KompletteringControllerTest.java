package se.fk.rimfrost.framework.regel;

import io.quarkus.arc.DefaultBean;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.handlaggning.model.Handlaggning;
import se.fk.rimfrost.framework.handlaggning.model.HandlaggningUpdate;
import se.fk.rimfrost.framework.oul.adapter.OulAdapter;
import se.fk.rimfrost.framework.oul.exception.OulException;
import se.fk.rimfrost.framework.regel.logic.KompletteringKontrollInterface;
import se.fk.rimfrost.framework.regel.logic.KompletteringSvarServiceInterface;
import se.fk.rimfrost.framework.regel.logic.dto.KompletteringUnderlag;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableKompletteringTillstand;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableKompletteringUnderlag;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.storage.KompletteringStorage;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;
import se.fk.rimfrost.framework.regel.presentation.rest.KompletteringController;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@QuarkusTest
class KompletteringControllerTest
{
   /** Minimal concrete subclass registered as a JAX-RS resource for this test. */
   @Path("/api/test")
   public static class TestKompletteringController extends KompletteringController<Object, Object>
   {
   }

   /** Stub CDI bean satisfying the KompletteringKontrollInterface injection point; replaced by @InjectMock at test time. */
   @ApplicationScoped
   @DefaultBean
   public static class TestKompletteringKontroll implements KompletteringKontrollInterface
   {
      @Override
      public List<KompletteringUnderlag> checkKomplettering(Handlaggning handlaggning)
      {
         return List.of();
      }
   }

   /** Stub CDI bean satisfying the generic svarService injection point; unused by the done endpoint. */
   @ApplicationScoped
   @DefaultBean
   public static class TestSvarService implements KompletteringSvarServiceInterface<Object, Object>
   {
      @Override
      public Object readSvarData(Handlaggning handlaggning)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public HandlaggningUpdate registerSvar(Handlaggning handlaggning, Object request)
      {
         throw new UnsupportedOperationException();
      }
   }

   @InjectMock
   OulAdapter oulAdapter;

   @InjectMock
   HandlaggningAdapter handlaggningAdapter;

   @InjectMock
   KompletteringKontrollInterface kompletteringKontroll;

   @InjectMock
   RegelRequestHandlerInterface regelRequestHandler;

   @Inject
   KompletteringStorage storage;

   private RegelDataRequest regelDataRequest(UUID handlaggningId)
   {
      return ImmutableRegelDataRequest.builder()
            .id(UUID.randomUUID())
            .handlaggningId(handlaggningId)
            .aktivitetId(UUID.randomUUID())
            .replyTo("reply-topic")
            .type("test-type")
            .kogitorootprocid("root-proc-id")
            .kogitorootprociid(UUID.randomUUID())
            .kogitoparentprociid(UUID.randomUUID())
            .kogitoprocid("proc-id")
            .kogitoprocinstanceid(UUID.randomUUID())
            .kogitoprocist("proc-ist")
            .kogitoprocversion("1.0")
            .build();
   }

   @Test
   @DisplayName("kompletteringDone calls handleRegelRequest with the stored RegelDataRequest")
   void should_call_handle_regel_request_with_stored_regel_data_request() throws Exception
   {
      var handlaggningId = UUID.randomUUID();
      var oulUppgiftId = UUID.randomUUID();
      var regelDataRequest = regelDataRequest(handlaggningId);
      storage.setKompletteringTillstand(handlaggningId, ImmutableKompletteringTillstand.builder()
            .oulUppgiftId(oulUppgiftId)
            .regelDataRequest(regelDataRequest)
            .build());

      var handlaggning = Mockito.mock(Handlaggning.class);
      Mockito.when(handlaggningAdapter.readHandlaggning(handlaggningId)).thenReturn(handlaggning);
      Mockito.when(kompletteringKontroll.checkKomplettering(handlaggning)).thenReturn(List.of());

      RestAssured.given()
            .post("/api/test/" + handlaggningId + "/komplettering/done")
            .then()
            .statusCode(204);

      verify(oulAdapter).endOperativUppgift(oulUppgiftId, "Komplettering klar");
      verify(regelRequestHandler).handleRegelRequest(regelDataRequest);
      assertFalse(storage.getKompletteringTillstand(handlaggningId).isPresent());
   }

   @Test
   @DisplayName("kompletteringDone returns 409 when no tillstand is in storage")
   void should_return_409_when_no_tillstand()
   {
      RestAssured.given()
            .post("/api/test/" + UUID.randomUUID() + "/komplettering/done")
            .then()
            .statusCode(409);

      verifyNoInteractions(regelRequestHandler);
   }

   @Test
   @DisplayName("kompletteringDone returns 422 when yrkande is still incomplete")
   void should_return_422_when_komplettering_incomplete() throws Exception
   {
      var handlaggningId = UUID.randomUUID();
      storage.setKompletteringTillstand(handlaggningId, ImmutableKompletteringTillstand.builder()
            .oulUppgiftId(UUID.randomUUID())
            .regelDataRequest(regelDataRequest(handlaggningId))
            .build());

      var handlaggning = Mockito.mock(Handlaggning.class);
      Mockito.when(handlaggningAdapter.readHandlaggning(handlaggningId)).thenReturn(handlaggning);
      Mockito.when(kompletteringKontroll.checkKomplettering(handlaggning))
            .thenReturn(List.of(ImmutableKompletteringUnderlag.builder()
                  .underlagTyp("MISSING_FIELD")
                  .beskrivning("Field is missing")
                  .build()));

      RestAssured.given()
            .post("/api/test/" + handlaggningId + "/komplettering/done")
            .then()
            .statusCode(422);

      verifyNoInteractions(regelRequestHandler);
   }

   @Test
   @DisplayName("FRALL-FR-07.6: kompletteringDone returns 207 and still triggers regel when endOperativUppgift fails")
   void should_return_207_and_trigger_regel_when_oul_end_fails() throws Exception
   {
      var handlaggningId = UUID.randomUUID();
      var oulUppgiftId = UUID.randomUUID();
      var regelDataRequest = regelDataRequest(handlaggningId);
      storage.setKompletteringTillstand(handlaggningId, ImmutableKompletteringTillstand.builder()
            .oulUppgiftId(oulUppgiftId)
            .regelDataRequest(regelDataRequest)
            .build());

      var handlaggning = Mockito.mock(Handlaggning.class);
      Mockito.when(handlaggningAdapter.readHandlaggning(handlaggningId)).thenReturn(handlaggning);
      Mockito.when(kompletteringKontroll.checkKomplettering(handlaggning)).thenReturn(List.of());
      doThrow(new OulException(OulException.ErrorType.SERVICE_UNAVAILABLE, "OUL down"))
            .when(oulAdapter).endOperativUppgift(any(), any());

      RestAssured.given()
            .post("/api/test/" + handlaggningId + "/komplettering/done")
            .then()
            .statusCode(207);

      verify(regelRequestHandler).handleRegelRequest(regelDataRequest);
      assertFalse(storage.getKompletteringTillstand(handlaggningId).isPresent());
   }

   @Test
   @DisplayName("kompletteringDone returns 500 when handleRegelRequest throws")
   void should_return_500_when_handle_regel_request_throws() throws Exception
   {
      var handlaggningId = UUID.randomUUID();
      storage.setKompletteringTillstand(handlaggningId, ImmutableKompletteringTillstand.builder()
            .oulUppgiftId(UUID.randomUUID())
            .regelDataRequest(regelDataRequest(handlaggningId))
            .build());

      var handlaggning = Mockito.mock(Handlaggning.class);
      Mockito.when(handlaggningAdapter.readHandlaggning(handlaggningId)).thenReturn(handlaggning);
      Mockito.when(kompletteringKontroll.checkKomplettering(handlaggning)).thenReturn(List.of());
      doThrow(new RuntimeException("Kafka unavailable"))
            .when(regelRequestHandler).handleRegelRequest(any());

      RestAssured.given()
            .post("/api/test/" + handlaggningId + "/komplettering/done")
            .then()
            .statusCode(500);
   }
}
