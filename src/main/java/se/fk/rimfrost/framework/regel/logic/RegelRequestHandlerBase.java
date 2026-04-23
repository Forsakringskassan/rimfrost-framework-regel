package se.fk.rimfrost.framework.regel.logic;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.regel.Utfall;
import se.fk.rimfrost.framework.regel.integration.config.RegelConfigProviderYaml;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelKafkaProducer;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.entity.*;

@SuppressWarnings("unused")
public abstract class RegelRequestHandlerBase
{
   @ConfigProperty(name = "kafka.source")
   private String kafkaSource;

   @ConfigProperty(name = "mp.messaging.outgoing.regel-responses.topic")
   private String responseTopic;

   @Inject
   protected RegelMapper regelMapper;

   @Inject
   protected HandlaggningAdapter handlaggningAdapter;

   @Inject
   protected RegelConfigProviderYaml regelConfigProvider;

   @Inject
   protected RegelKafkaProducer regelKafkaProducer;

   @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
   protected RegelConfig regelConfig;

   /*
   * Note: The name of the @PostConstruct method should if
   * possible be kept as init<classname> in order to avoid
   * being shadowed by any @PostConstruct methods in any
   * inheriting class that happens to have the same method
   * name.
   */
   @PostConstruct
   private void initRegelRequestHandlerBase()
   {
      this.regelConfig = regelConfigProvider.getConfig();
   }

   protected CloudEventData createCloudEvent(RegelDataRequest request)
   {
      return ImmutableCloudEventData.builder()
            .id(request.id())
            .kogitoparentprociid(request.kogitoparentprociid())
            .kogitoprocid(request.kogitoprocid())
            .kogitoprocinstanceid(request.kogitoprocinstanceid())
            .kogitoprocist(request.kogitoprocist())
            .kogitoprocversion(request.kogitoprocversion())
            .kogitorootprocid(request.kogitorootprocid())
            .kogitorootprociid(request.kogitorootprociid())
            .type(responseTopic)
            .source(kafkaSource)
            .build();
   }

   protected void sendResponse(UUID handlaggningId, CloudEventData cloudEventData, Utfall utfall)
   {
      var regelResponse = regelMapper.toRegelResponse(handlaggningId, cloudEventData, utfall);
      regelKafkaProducer.sendRegelResponse(regelResponse);
   }
}
