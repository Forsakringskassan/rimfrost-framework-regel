package se.fk.rimfrost.framework.regel.integration.kafka;

import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.regel.RegelResponseMessagePayload;
import se.fk.rimfrost.framework.regel.integration.kafka.dto.RegelResponse;

import java.time.Duration;
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
   MutinyEmitter<RegelResponseMessagePayload> regelResponseEmitter;

   @ConfigProperty(name = "rimfrost.regel.publish-timeout", defaultValue = "10s")
   Duration publishTimeout;

   public void sendRegelResponse(RegelResponse regelResponse)
   {
      var response = mapper.toRegelResponseMessagePayload(Objects.requireNonNull(regelResponse));

      LOGGER.info(
            "Sending RegelResponse for handlaggning {}",
            regelResponse.handlaggningId());
      try
      {
         regelResponseEmitter.send(response).await().atMost(publishTimeout);
      }
      catch (RuntimeException e)
      {
         LOGGER.error("Failed to send regel response for handlaggning. handlaggningId: {}, utfall: {}",
               regelResponse.handlaggningId(), regelResponse.utfall(), e);
         throw new RegelResponseException(
               "Failed to send regel response for handlaggning " + regelResponse.handlaggningId(), e);
      }
   }
}
