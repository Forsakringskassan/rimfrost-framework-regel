package se.fk.rimfrost.framework.regel.logic;

import se.fk.rimfrost.framework.kundbehovsflode.adapter.dto.KundbehovsflodeResponse;

public interface RegelServiceInterface {
    
    ProcessRegelResponse processRegel(KundbehovsflodeResponse regelData);

}
