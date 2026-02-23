package se.fk.rimfrost.framework.regel.integration.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;

@SuppressWarnings("unused")
@ApplicationScoped
@Startup
public class RegelConfigProviderYaml implements RegelConfigProvider
{

   private static final String ENV_CONFIG_PATH = "REGEL_CONFIG_PATH";

   private static final Logger LOGGER = LoggerFactory.getLogger(RegelConfigProviderYaml.class);

   private RegelConfig config;

   @ConfigProperty(name = "application.config.path")
   String applicationConfigPath;

   @PostConstruct
   void init()
   {
      String configPath = System.getenv(ENV_CONFIG_PATH);

      if (configPath == null || configPath.isEmpty())
      {
         configPath = applicationConfigPath;
      }

      if (configPath != null && !configPath.isEmpty())
      {
         try
         {
            this.config = YamlConfigLoader.loadFromFile(Path.of(configPath), RegelConfig.class);
         }
         catch (FileNotFoundException e)
         {
            LOGGER.warn("Config file {} not found, falling back to classpath discovery", configPath);

            // Set configPath to null to fall back to classpath loading
            configPath = null;
         }
      }

      if (configPath == null || configPath.isEmpty())
      {
         this.config = YamlConfigLoader.loadFromClasspath(
               "config.yaml",
               RegelConfig.class);
      }
   }

   @SuppressFBWarnings("EI_EXPOSE_REP")
   @Override
   public RegelConfig getConfig()
   {
      return config;
   }
}
