package se.fk.rimfrost.framework.regel.integration.kafka;

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.regel.RegelResponseMessagePayload;
import se.fk.rimfrost.framework.regel.integration.kafka.dto.RegelResponse;

import java.util.Objects;

@ApplicationScoped
public class RegelKafkaProducer
{
   private static final Logger LOGGER = LoggerFactory.getLogger(RegelKafkaProducer.class);

   @Inject
   RegelKafkaMapper mapper;

   @Inject
   @Channel("regel-responses")
   @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 1024)
   Emitter<RegelResponseMessagePayload> regelResponseEmitter;

   public void sendRegelResponse(RegelResponse regelResponse, String topic)
   {
      var response = mapper.toRegelResponseMessagePayload(regelResponse);

      var metadata = OutgoingKafkaRecordMetadata.builder()
            .withTopic(Objects.requireNonNull(topic))
            .build();

      var message = Message.of(response).addMetadata(metadata);

      LOGGER.info(
            "Sending RegelResponse for handlaggning {} to topic {}",
            regelResponse.handlaggningId(),
            topic);
      regelResponseEmitter.send(message);
   }
}
