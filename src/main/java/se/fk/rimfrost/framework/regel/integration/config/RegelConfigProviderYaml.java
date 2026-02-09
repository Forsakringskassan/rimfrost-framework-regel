package se.fk.rimfrost.framework.regel.integration.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import se.fk.rimfrost.framework.regel.logic.config.RegelConfig;
import java.nio.file.Path;

@SuppressWarnings("unused")
@ApplicationScoped
@Startup
public class RegelConfigProviderYaml implements RegelConfigProvider
{

   private static final String ENV_CONFIG_PATH = "REGEL_CONFIG_PATH";

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

      this.config = YamlConfigLoader.loadFromFile(Path.of(configPath), RegelConfig.class);
   }

   @SuppressFBWarnings("EI_EXPOSE_REP")
   @Override
   public RegelConfig getConfig()
   {
      return config;
   }
}
