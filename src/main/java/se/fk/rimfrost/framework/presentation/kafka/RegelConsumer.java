package se.fk.rimfrost.framework.presentation.kafka;

import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import se.fk.rimfrost.regel.common.RegelRequestMessagePayload;

@SuppressWarnings("unused")
@ApplicationScoped
public class RegelConsumer
{

   @Inject
   RegelMessageHandler handler;

   /**
    * Kafka / Reactive Messaging consumer method
    * Logical channel: regel-requests
    * Physical topic configured in application.properties
    */
   @Incoming("regel-requests")
   @Blocking
   public void onMessage(RegelRequestMessagePayload payload)
   {
      handler.consumeRegelRequest(payload);
   }

}
