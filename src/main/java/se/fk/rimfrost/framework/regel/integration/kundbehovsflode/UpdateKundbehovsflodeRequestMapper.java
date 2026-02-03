package se.fk.rimfrost.framework.regel.integration.kundbehovsflode;

import se.fk.rimfrost.framework.regel.integration.kundbehovsflode.dto.UpdateKundbehovsflodeRequest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.GetKundbehovsflodeResponse;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.PutKundbehovsflodeRequest;

@SuppressWarnings("unused")
public interface UpdateKundbehovsflodeRequestMapper
{
   PutKundbehovsflodeRequest toKundbehovsflodeRequest(UpdateKundbehovsflodeRequest request, GetKundbehovsflodeResponse apiResponse);
}
