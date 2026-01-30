package se.fk.rimfrost.framework.regel.integration.kundbehovsflode;

import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.GetKundbehovsflodeResponse;

@SuppressWarnings("unused")
public interface KundbehovsflodeResponseMapper<T>
{
   T toKundbehovsflodeResponse(GetKundbehovsflodeResponse apiResponse);
}
