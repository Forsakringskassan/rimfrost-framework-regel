package se.fk.rimfrost.framework.regel.presentation.kafka;

import se.fk.rimfrost.framework.regel.logic.dto.RegelResultRequest;

public interface RegelRequestHandlerInterface
{
   void handleRegelRequest(RegelResultRequest request);
}
