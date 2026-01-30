package se.fk.rimfrost.framework.regel.integration.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import se.fk.rimfrost.framework.regel.integration.kafka.dto.RegelResponse;
import se.fk.rimfrost.regel.common.RegelResponseMessagePayload;

@ApplicationScoped
public class RegelKafkaProducer
{
   @Inject
   RegelKafkaMapper mapper;

   @Inject
   @Channel("regel-responses")
   @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 1024)
   Emitter<RegelResponseMessagePayload> regelResponseEmitter;

   public void sendRegelResponse(RegelResponse regelResponse)
   {
      var response = mapper.toRegelResponseMessagePayload(regelResponse);
      regelResponseEmitter.send(response);
   }

}
