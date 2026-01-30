package se.fk.rimfrost.framework.regel.presentation.kafka;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import se.fk.rimfrost.framework.regel.RegelRequestMessagePayload;

@SuppressWarnings("unused")
public class RegelRequestDeserializer extends ObjectMapperDeserializer<RegelRequestMessagePayload>
{
   public RegelRequestDeserializer()
   {
      super(RegelRequestMessagePayload.class);
   }
}
