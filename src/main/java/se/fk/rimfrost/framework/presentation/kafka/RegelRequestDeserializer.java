package se.fk.rimfrost.framework.presentation.kafka;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import se.fk.rimfrost.regel.common.RegelRequestMessagePayload;

@SuppressWarnings("unused")
public class RegelRequestDeserializer extends ObjectMapperDeserializer<RegelRequestMessagePayload>
{
   public RegelRequestDeserializer()
   {
      super(RegelRequestMessagePayload.class);
   }
}
