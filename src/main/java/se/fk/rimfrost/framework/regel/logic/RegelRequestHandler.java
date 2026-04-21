package se.fk.rimfrost.framework.regel.logic;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;

/**
 * Default runtime implementation of RegelRequestHandlerInterface.
 *
 * <p>This bean is required so that CDI can resolve the dependency used by {@link
 * se.fk.rimfrost.framework.regel.presentation.kafka.RegelMessageHandler}.
 *
 * <p>Actual rule-specific behavior can be implemented here or delegated to domain services.
 */
@SuppressWarnings("unused")
@ApplicationScoped
public class RegelRequestHandler implements RegelRequestHandlerInterface
{

   @Override
   public void handleRegelRequest(RegelDataRequest request)
   {
      // TODO implement rule execution logic
      // This is currently a placeholder to satisfy CDI wiring
   }
}
