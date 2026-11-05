package se.fk.rimfrost.framework.regel.integration.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
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

   public void sendRegelResponse(RegelResponse regelResponse)
   {
      var response = mapper.toRegelResponseMessagePayload(Objects.requireNonNull(regelResponse));

      LOGGER.info(
            "Sending RegelResponse for handlaggning {}",
            regelResponse.handlaggningId());
      regelResponseEmitter.send(response)
            .whenComplete((ignored, failure) -> {
               if (failure != null)
               {
                  LOGGER.error("Failed to send regel response for handlaggning. handlaggningId: {}, utfall: {}",
                        regelResponse.handlaggningId(), regelResponse.utfall(), failure);
               }
            });
   }
}
