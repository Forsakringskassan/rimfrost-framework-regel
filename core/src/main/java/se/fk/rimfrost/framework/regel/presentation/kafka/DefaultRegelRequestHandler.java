package se.fk.rimfrost.framework.regel.presentation.kafka;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;

@ApplicationScoped
@DefaultBean
public class DefaultRegelRequestHandler implements RegelRequestHandlerInterface
{

   @Override
   public void handleRegelRequest(RegelDataRequest request)
   {
   }
}
