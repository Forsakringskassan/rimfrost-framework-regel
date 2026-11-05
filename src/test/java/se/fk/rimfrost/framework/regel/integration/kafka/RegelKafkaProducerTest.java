package se.fk.rimfrost.framework.regel.integration.kafka;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logmanager.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.regel.RegelResponseMessagePayload;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.integration.kafka.dto.RegelResponse;

public class RegelKafkaProducerTest
{
   private final Logger logger = Logger.getLogger(RegelKafkaProducer.class.getName());
   private final List<LogRecord> records = new ArrayList<>();
   private final Handler handler = new Handler()
   {
      @Override
      public void publish(LogRecord record)
      {
         records.add(record);
      }

      @Override
      public void flush()
      {
      }

      @Override
      public void close()
      {
      }
   };

   private RegelKafkaProducer producer;

   @SuppressWarnings("unchecked")
   @BeforeEach
   void setUp()
   {
      logger.addHandler(handler);
      producer = new RegelKafkaProducer();
      producer.mapper = mock(RegelKafkaMapper.class);
      producer.regelResponseEmitter = mock(Emitter.class);
   }

   @AfterEach
   void tearDown()
   {
      logger.removeHandler(handler);
   }

   @Test
   @DisplayName("FRALL-FR-04.3, FRALL-NFR-03.1: Regelsvar som Kafka nekar loggas med handläggningsid och utfall utan att ett exception kastas")
   void should_log_when_kafka_rejects_regel_response()
   {
      var handlaggningId = UUID.randomUUID();
      var regelResponse = mock(RegelResponse.class);
      when(regelResponse.handlaggningId()).thenReturn(handlaggningId);
      when(regelResponse.utfall()).thenReturn(Utfall.JA);
      var payload = new RegelResponseMessagePayload();
      when(producer.mapper.toRegelResponseMessagePayload(regelResponse)).thenReturn(payload);
      when(producer.regelResponseEmitter.send(payload))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("nack")));

      assertDoesNotThrow(() -> producer.sendRegelResponse(regelResponse));

      var errors = records.stream().filter(r -> r.getLevel().intValue() >= Level.SEVERE.intValue()).toList();
      assertEquals(1, errors.size());
      var message = String.format(errors.getFirst().getMessage().replace("{}", "%s"), errors.getFirst().getParameters());
      assertTrue(message.contains(handlaggningId.toString()));
      assertTrue(message.contains(Utfall.JA.toString()));
   }
}
