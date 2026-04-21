package se.fk.rimfrost.framework.regel.presentation.kafka;

import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;

public interface RegelRequestHandlerInterface
{
   void handleRegelRequest(RegelDataRequest request);
}
