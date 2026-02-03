package se.fk.rimfrost.framework.regel.integration.kundbehovsflode;

import se.fk.rimfrost.framework.regel.integration.kundbehovsflode.dto.KundbehovsflodeResponse;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.GetKundbehovsflodeResponse;

@SuppressWarnings("unused")
public interface KundbehovsflodeResponseMapper
{
   KundbehovsflodeResponse toKundbehovsflodeResponse(GetKundbehovsflodeResponse apiResponse);
}
