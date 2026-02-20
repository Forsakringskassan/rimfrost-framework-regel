package se.fk.rimfrost.framework.regel.logic;

import se.fk.rimfrost.framework.regel.logic.dto.ProcessRegelRequest;
import se.fk.rimfrost.framework.regel.logic.entity.RegelResult;

public interface RegelServiceInterface
{

   RegelResult processRegel(ProcessRegelRequest regelResult);

}
