package se.fk.rimfrost.framework.presentation.kafka;

import se.fk.rimfrost.framework.logic.dto.RegelDataRequest;

public interface RegelRequestHandlerInterface
{
   void handleRegelRequest(RegelDataRequest request);
}
