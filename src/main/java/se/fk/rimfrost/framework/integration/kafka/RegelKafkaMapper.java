package se.fk.rimfrost.framework.integration.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.framework.integration.kafka.dto.RegelResponse;
import se.fk.rimfrost.regel.common.*;

@SuppressWarnings("unused")
@ApplicationScoped
public class RegelKafkaMapper
{

   public RegelResponseMessagePayload toRegelResponseMessagePayload(RegelResponse regelResponse, String source)
   {
      var data = new RegelResponseMessagePayloadData();
      data.setKundbehovsflodeId(regelResponse.kundbehovsflodeId().toString());
      data.setUtfall(regelResponse.utfall());

      var response = new RegelResponseMessagePayload();
      response.setId(regelResponse.id().toString());
      response.setKogitorootprocid(regelResponse.kogitorootprocid());
      response.setKogitorootprociid(regelResponse.kogitorootprociid().toString());
      response.setKogitoparentprociid(regelResponse.kogitoparentprociid().toString());
      response.setKogitoprocid(regelResponse.kogitoprocid());
      response.setKogitoprocinstanceid(regelResponse.kogitoprocinstanceid().toString());
      response.setKogitoprocrefid(regelResponse.kogitoprocinstanceid().toString());
      response.setKogitoprocist(regelResponse.kogitoprocist());
      response.setKogitoprocversion(regelResponse.kogitoprocversion());
      response.setSpecversion(SpecVersion.NUMBER_1_DOT_0);
      response.setSource(source);
      response.setType("regel-responses");
      response.setKogitoproctype(KogitoProcType.BPMN);
      response.setData(data);

      return response;
   }

}
