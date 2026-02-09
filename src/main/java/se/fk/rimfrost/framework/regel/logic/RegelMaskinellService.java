package se.fk.rimfrost.framework.regel.logic;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.regel.integration.config.RegelConfigProviderYaml;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelKafkaProducer;
import se.fk.rimfrost.framework.regel.integration.kundbehovsflode.KundbehovsflodeAdapter;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.entity.*;
import se.fk.rimfrost.framework.regel.presentation.kafka.RegelRequestHandlerInterface;

@SuppressWarnings("unused")
public abstract class RegelMaskinellService implements RegelRequestHandlerInterface
{
   private static final Logger LOGGER = LoggerFactory.getLogger(RegelMaskinellService.class);

   @ConfigProperty(name = "mp.messaging.outgoing.regel-responses.topic")
   String responseTopic;

   @ConfigProperty(name = "kafka.source")
   String kafkaSource;

   @Inject
   protected RegelMaskinellMapper regelMapper;

   @Inject
   protected KundbehovsflodeAdapter kundbehovsflodeAdapter;

   @Inject
   protected RegelConfigProviderYaml regelConfigProvider;

   @Inject
   protected RegelKafkaProducer regelKafkaProducer;

   protected RegelConfig regelConfig;

   public abstract void handleRegelRequest(RegelDataRequest request);

   @PostConstruct
   void init()
   {
      this.regelConfig = regelConfigProvider.getConfig();
   }
}
