package se.fk.rimfrost.framework.regel;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.fk.rimfrost.framework.oul.adapter.OulAdapter;
import se.fk.rimfrost.framework.oul.exception.OulException;
import se.fk.rimfrost.framework.oul.model.CreateOperativUppgiftRequest;
import se.fk.rimfrost.framework.oul.model.ImmutableErbjudande;
import se.fk.rimfrost.framework.oul.model.ImmutableOperativUppgift;
import se.fk.rimfrost.framework.oul.model.ImmutableProcessInfo;
import se.fk.rimfrost.framework.regel.logic.KompletteringOulHandler;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import se.fk.rimfrost.framework.regel.logic.config.Specifikation;
import se.fk.rimfrost.framework.regel.logic.config.Uppgift;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.KompletteringTillstand;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.storage.KompletteringStorage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@QuarkusTest
class KompletteringOulHandlerTest
{
   @InjectMock
   OulAdapter oulAdapter;

   @InjectMock
   KompletteringStorage storage;

   @Inject
   KompletteringOulHandler handler;

   private RegelConfig regelConfig()
   {
      var specifikation = new Specifikation();
      specifikation.setNamn("Min Regel");
      specifikation.setVerksamhetslogik("C");
      specifikation.setRoll("ANSVARIG_HANDLAGGARE");

      var uppgift = new Uppgift();
      uppgift.setPath("/api/min-regel");

      var config = new RegelConfig();
      config.setSpecifikation(specifikation);
      config.setUppgift(uppgift);
      return config;
   }

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

   private void stubOulAdapter(UUID handlaggningId, UUID oulUppgiftId) throws OulException
   {
      Mockito.when(oulAdapter.createOperativUppgift(any()))
            .thenReturn(ImmutableOperativUppgift.builder()
                  .uppgiftId(oulUppgiftId)
                  .handlaggningId(handlaggningId)
                  .status("OPEN")
                  .processInfo(ImmutableProcessInfo.builder()
                        .replyTopic("reply-topic")
                        .cloudeventAttributes(Map.of())
                        .build())
                  .build());
   }

   @Test
   @DisplayName("OUL request is built with metadata derived from RegelConfig")
   void should_build_oul_request_from_regel_config() throws OulException
   {
      var handlaggningId = UUID.randomUUID();
      var oulUppgiftId = UUID.randomUUID();
      stubOulAdapter(handlaggningId, oulUppgiftId);

      var erbjudande = ImmutableErbjudande.builder().id("erbjudande-1").namn("Erbjudande Ett").build();
      var cloudEventAttributes = Map.of("id", UUID.randomUUID().toString(), "type", "regel.request");

      handler.initiate(regelDataRequest(handlaggningId), cloudEventAttributes, regelConfig(), erbjudande);

      var captor = ArgumentCaptor.forClass(CreateOperativUppgiftRequest.class);
      verify(oulAdapter).createOperativUppgift(captor.capture());
      var request = captor.getValue();

      assertEquals(handlaggningId, request.getHandlaggningId());
      assertEquals("Hantera komplettering för Min Regel", request.getRegel());
      assertEquals("/api/min-regel/komplettering", request.getUrl());
      assertEquals("C", request.getVerksamhetslogik());
      assertEquals("ANSVARIG_HANDLAGGARE", request.getRoll());
      assertEquals("reply-topic", request.getProcessInfo().getReplyTopic());
   }

   @Test
   @DisplayName("Tillstand is stored with oulUppgiftId from OUL response after successful initiate")
   void should_store_tillstand_after_successful_oul_call() throws OulException
   {
      var handlaggningId = UUID.randomUUID();
      var oulUppgiftId = UUID.randomUUID();
      stubOulAdapter(handlaggningId, oulUppgiftId);

      var erbjudande = ImmutableErbjudande.builder().id("erbjudande-1").namn("Erbjudande Ett").build();
      var regelDataRequest = regelDataRequest(handlaggningId);

      handler.initiate(regelDataRequest, Map.of(), regelConfig(), erbjudande);

      var captor = ArgumentCaptor.forClass(KompletteringTillstand.class);
      verify(storage).setKompletteringTillstand(eq(handlaggningId), captor.capture());
      var stored = captor.getValue();
      assertEquals(oulUppgiftId, stored.oulUppgiftId());
      assertEquals(regelDataRequest, stored.regelDataRequest());
   }

   @Test
   @DisplayName("OulException from createOperativUppgift propagates and tillstand is not stored")
   void should_propagate_oul_exception_without_storing_tillstand() throws OulException
   {
      var handlaggningId = UUID.randomUUID();
      Mockito.when(oulAdapter.createOperativUppgift(any()))
            .thenThrow(new OulException(OulException.ErrorType.SERVICE_UNAVAILABLE, "OUL down"));

      var erbjudande = ImmutableErbjudande.builder().id("erbjudande-1").namn("Erbjudande Ett").build();

      assertThrows(OulException.class,
            () -> handler.initiate(regelDataRequest(handlaggningId), Map.of(), regelConfig(), erbjudande));

      verify(storage, never()).setKompletteringTillstand(any(), any());
   }

   @Test
   @DisplayName("FRALL-FR-06.9: storage failure triggers best-effort endOperativUppgift on the created OUL task")
   void should_end_oul_task_when_storage_fails() throws OulException
   {
      var handlaggningId = UUID.randomUUID();
      var oulUppgiftId = UUID.randomUUID();
      stubOulAdapter(handlaggningId, oulUppgiftId);

      var storageEx = new RuntimeException("db down");
      doThrow(storageEx).when(storage).setKompletteringTillstand(eq(handlaggningId), any());

      var erbjudande = ImmutableErbjudande.builder().id("erbjudande-1").namn("Erbjudande Ett").build();

      var thrown = assertThrows(RuntimeException.class,
            () -> handler.initiate(regelDataRequest(handlaggningId), Map.of(), regelConfig(), erbjudande));
      assertSame(storageEx, thrown);

      verify(oulAdapter).endOperativUppgift(eq(oulUppgiftId), anyString());
   }

   @Test
   @DisplayName("FRALL-FR-06.10: cleanup failure is swallowed and original storage exception still propagates")
   void should_swallow_cleanup_failure_and_rethrow_storage_exception() throws OulException
   {
      var handlaggningId = UUID.randomUUID();
      var oulUppgiftId = UUID.randomUUID();
      stubOulAdapter(handlaggningId, oulUppgiftId);

      var storageEx = new RuntimeException("db down");
      doThrow(storageEx).when(storage).setKompletteringTillstand(eq(handlaggningId), any());
      Mockito.when(oulAdapter.endOperativUppgift(eq(oulUppgiftId), anyString()))
            .thenThrow(new OulException(OulException.ErrorType.SERVICE_UNAVAILABLE, "OUL also down"));

      var erbjudande = ImmutableErbjudande.builder().id("erbjudande-1").namn("Erbjudande Ett").build();

      var thrown = assertThrows(RuntimeException.class,
            () -> handler.initiate(regelDataRequest(handlaggningId), Map.of(), regelConfig(), erbjudande));
      assertSame(storageEx, thrown);
   }

   @Test
   @DisplayName("FRALL-FR-06.9: endOperativUppgift is not called on happy path")
   void should_not_end_oul_task_on_happy_path() throws OulException
   {
      var handlaggningId = UUID.randomUUID();
      var oulUppgiftId = UUID.randomUUID();
      stubOulAdapter(handlaggningId, oulUppgiftId);

      var erbjudande = ImmutableErbjudande.builder().id("erbjudande-1").namn("Erbjudande Ett").build();

      handler.initiate(regelDataRequest(handlaggningId), Map.of(), regelConfig(), erbjudande);

      verify(oulAdapter, never()).endOperativUppgift(any(), anyString());
   }
}
