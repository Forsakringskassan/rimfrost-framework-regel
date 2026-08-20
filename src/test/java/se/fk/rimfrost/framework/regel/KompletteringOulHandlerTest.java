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
import se.fk.rimfrost.framework.regel.logic.storage.KompletteringStorage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@QuarkusTest
class KompletteringOulHandlerTest
{
   @InjectMock
   OulAdapter oulAdapter;

   @Inject
   KompletteringOulHandler handler;

   @Inject
   KompletteringStorage storage;

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

      handler.initiate(handlaggningId, "{}", cloudEventAttributes, "reply-topic", regelConfig(), erbjudande);

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

      handler.initiate(handlaggningId, "{original}", Map.of(), "reply-topic", regelConfig(), erbjudande);

      var stored = storage.getKompletteringTillstand(handlaggningId);
      assertTrue(stored.isPresent());
      assertEquals(oulUppgiftId, stored.get().oulUppgiftId());
      assertEquals("reply-topic", stored.get().replyTo());
      assertEquals("{original}", stored.get().cloudEventData());
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
            () -> handler.initiate(handlaggningId, "{}", Map.of(), "reply-topic", regelConfig(), erbjudande));

      assertFalse(storage.getKompletteringTillstand(handlaggningId).isPresent());
   }
}
