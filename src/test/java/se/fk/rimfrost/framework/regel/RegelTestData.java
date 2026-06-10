package se.fk.rimfrost.framework.regel;

import static se.fk.rimfrost.framework.regel.RegelKafkaConnector.regelRequestsChannel;

public class RegelTestData
{
   public static RegelRequestMessagePayload newRegelRequestMessagePayload(
         String handlaggningId, String aktivitetId, String type, String replyTo)
   {
      RegelRequestMessagePayload payload = new RegelRequestMessagePayload();
      RegelRequestMessagePayloadData data = new RegelRequestMessagePayloadData();
      data.setHandlaggningId(handlaggningId);
      data.setAktivitetId(aktivitetId);
      data.setReplyTo(replyTo);
      payload.setSpecversion(SpecVersion.V1);
      payload.setId("99994567-89ab-4cde-9012-3456789abcde");
      payload.setSource("TestSource-001");
      payload.setType(type);
      payload.setKogitoprocid("234567");
      payload.setKogitorootprocid("123456");
      payload.setKogitorootprociid("77774567-89ab-4cde-9012-3456789abcde");
      payload.setKogitoparentprociid("88884567-89ab-4cde-9012-3456789abcde");
      payload.setKogitoprocinstanceid("66664567-89ab-4cde-9012-3456789abcde");
      payload.setKogitoprocist("345678");
      payload.setKogitoprocversion("111");
      payload.setKogitoproctype(Kogitoproctype.BPMN);
      payload.setKogitoprocrefid("56789");
      payload.setData(data);
      return payload;
   }

   public static RegelRequestMessagePayload newRegelRequestMessagePayload(String handlaggningId, String replyTo)
   {
      return newRegelRequestMessagePayload(
            handlaggningId, "9b9d8261-559b-48db-b8bb-cbf61401c0ae", regelRequestsChannel, replyTo);
   }
}
