package se.fk.rimfrost.framework.regel.presentation.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import se.fk.github.logging.callerinfo.model.MDCKeys;
import se.fk.rimfrost.regel.common.RegelRequestMessagePayload;

@ApplicationScoped
public class RegelMessageHandler
{

   private static final Logger LOGGER = LoggerFactory.getLogger(RegelMessageHandler.class);

   @Inject
   RegelKafkaMapper mapper;

   @Inject
   RegelRequestHandlerInterface regelRequestHandlerInterface;

   /**
    * Handle a received RegelRequestMessagePayload
    */
   public void consumeRegelRequest(RegelRequestMessagePayload payload)
   {
      try
      {
         // Put process ID in MDC for logging
         MDC.put(MDCKeys.PROCESSID.name(), payload.getData().getKundbehovsflodeId());

         LOGGER.info(
               "RegelRequestMessagePayload received with KundbehovsflodeId: {}",
               payload.getData().getKundbehovsflodeId());

         // Map to service request and delegate
         var request = mapper.toRegelDataRequest(payload);
         regelRequestHandlerInterface.handleRegelRequest(request);

      }
      finally
      {
         // Always clear MDC to avoid leaking to other threads
         MDC.clear();
      }
   }

}
