package se.fk.rimfrost.framework.regel.integration.kundbehovsflode.dto;

import java.util.List;
import java.util.UUID;

import org.immutables.value.Value;

@Value.Immutable
public interface DefaultUpdateKundbehovsflodeRequest extends UpdateKundbehovsflodeRequest
{
   UUID kundbehovsflodeId();

   UpdateKundbehovsflodeUppgift uppgift();

   List<UpdateKundbehovsflodeErsattning> ersattningar();

   List<UpdateKundbehovsflodeUnderlag> underlag();

}
