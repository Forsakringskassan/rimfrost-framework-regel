package se.fk.rimfrost.framework.integration.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import se.fk.rimfrost.framework.integration.kafka.dto.RegelResponse;
import se.fk.rimfrost.regel.common.RegelResponseMessagePayload;

import java.util.UUID;

@ApplicationScoped
public class RegelKafkaProducer
{
   @Inject
   RegelKafkaMapper mapper;

   @Inject
   @Channel("regel-responses")
   @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 1024)
   Emitter<RegelResponseMessagePayload> regelResponseEmitter;

   public void sendRegelResponse(RegelResponse regelResponse, String source)
   {
      var response = mapper.toRegelResponseMessagePayload(regelResponse, source);
      regelResponseEmitter.send(response);
   }

}
