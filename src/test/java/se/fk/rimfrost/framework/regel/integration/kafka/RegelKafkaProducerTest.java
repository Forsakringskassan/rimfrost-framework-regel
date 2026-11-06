package se.fk.rimfrost.framework.regel.integration.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
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
   private UUID handlaggningId;
   private RegelResponse regelResponse;
   private RegelResponseMessagePayload payload;

   @SuppressWarnings("unchecked")
   @BeforeEach
   void setUp()
   {
      logger.addHandler(handler);
      producer = new RegelKafkaProducer();
      producer.mapper = mock(RegelKafkaMapper.class);
      producer.regelResponseEmitter = mock(MutinyEmitter.class);
      producer.publishTimeout = Duration.ofMillis(200);

      handlaggningId = UUID.randomUUID();
      regelResponse = mock(RegelResponse.class);
      when(regelResponse.handlaggningId()).thenReturn(handlaggningId);
      when(regelResponse.utfall()).thenReturn(Utfall.JA);
      payload = new RegelResponseMessagePayload();
      when(producer.mapper.toRegelResponseMessagePayload(regelResponse)).thenReturn(payload);
   }

   @AfterEach
   void tearDown()
   {
      logger.removeHandler(handler);
   }

   @Test
   @DisplayName("FRALL-FR-04.3, FRALL-NFR-03.1: Regelsvar som Kafka nekar loggas med handläggningsid och utfall och ger exception")
   void should_log_and_throw_when_kafka_rejects_regel_response()
   {
      when(producer.regelResponseEmitter.send(payload))
            .thenReturn(Uni.createFrom().failure(new RuntimeException("nack")));

      assertThrows(RegelResponseException.class, () -> producer.sendRegelResponse(regelResponse));

      var errors = records.stream().filter(r -> r.getLevel().intValue() >= Level.SEVERE.intValue()).toList();
      assertEquals(1, errors.size());
      var message = String.format(errors.getFirst().getMessage().replace("{}", "%s"), errors.getFirst().getParameters());
      assertTrue(message.contains(handlaggningId.toString()));
      assertTrue(message.contains(Utfall.JA.toString()));
   }

   @Test
   @DisplayName("FRALL-FR-04.4: Regelsvar som aldrig kvitteras ger exception efter konfigurerad tid")
   void should_throw_when_regel_response_is_not_acknowledged_in_time()
   {
      when(producer.regelResponseEmitter.send(payload)).thenReturn(Uni.createFrom().nothing());

      assertThrows(RegelResponseException.class, () -> producer.sendRegelResponse(regelResponse));
   }
}
