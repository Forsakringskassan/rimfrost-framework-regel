package se.fk.rimfrost.framework.regel.integration.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import se.fk.rimfrost.framework.regel.integration.kafka.dto.RegelResponse;
import se.fk.rimfrost.framework.regel.RegelResponseMessagePayload;

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

   @ConfigProperty(name = "mp.messaging.outgoing.regel-responses.topic")
   String topic;

   public void sendRegelResponse(RegelResponse regelResponse)
   {
      var response = mapper.toRegelResponseMessagePayload(regelResponse);
      LOGGER.info("Sending RegelResponse for handlaggning {} to topic {}", regelResponse.handlaggningId(), topic);
      regelResponseEmitter.send(response);
   }

}
