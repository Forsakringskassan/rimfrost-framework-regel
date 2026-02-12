package se.fk.rimfrost.framework.regel.logic;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.KundbehovsflodeAdapter;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.KundbehovsflodeMapper;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.integration.config.RegelConfigProviderYaml;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelKafkaProducer;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.entity.*;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;

@SuppressWarnings("unused")
public class RegelService implements RegelRequestHandlerInterface
{
   private static final Logger LOGGER = LoggerFactory.getLogger(RegelService.class);

   @ConfigProperty(name = "mp.messaging.outgoing.regel-responses.topic")
   protected String responseTopic;

   @ConfigProperty(name = "kafka.source")
   protected String kafkaSource;

   @Inject
   protected RegelMapper regelMapper;

   @Inject
   protected KundbehovsflodeAdapter kundbehovsflodeAdapter;

   @Inject
   protected RegelConfigProviderYaml regelConfigProvider;

   @Inject
   protected RegelKafkaProducer regelKafkaProducer;

   protected RegelConfig regelConfig;

   public void handleRegelRequest(RegelDataRequest request)
   {

   }

   @PostConstruct
   void init()
   {
      this.regelConfig = regelConfigProvider.getConfig();
   }

   public void sendResponse(RegelDataRequest request, Utfall utfall)
   {
      var cloudevent = ImmutableCloudEventData.builder()
            .id(request.id())
            .kogitoparentprociid(request.kogitoparentprociid())
            .kogitoprocid(request.kogitoprocid())
            .kogitoprocinstanceid(request.kogitoprocinstanceid())
            .kogitoprocist(request.kogitoprocist())
            .kogitoprocversion(request.kogitoprocversion())
            .kogitorootprocid(request.kogitorootprocid())
            .kogitorootprociid(request.kogitorootprociid())
            .type(request.type())
            .source(kafkaSource)
            .build();
      var regelResponse = regelMapper.toRegelResponse(request.kundbehovsflodeId(), cloudevent, utfall);
      regelKafkaProducer.sendRegelResponse(regelResponse);
   }
}
