package se.fk.rimfrost.framework.regel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.framework.regel.integration.config.RegelConfigProviderYaml;

public class RegelConfigProviderYamlTest
{
   // REGEL_CONFIG_PATH (env var) uses the same YamlConfigLoader.loadFromFile() code path as
   // application.config.path. Testing the env var branch directly is not feasible since
   // System.getenv() cannot be set programmatically in Java without OS-level manipulation.
   @Test
   @DisplayName("FRALL-FR-02.1: Config laddas från sökväg angiven via konfigurationsproperty")
   void init_loadsConfigFromApplicationConfigPath() throws Exception
   {
      var configPath = Path.of(getClass().getClassLoader().getResource("config-path-test.yaml").toURI());
      RegelConfigProviderYaml provider = new RegelConfigProviderYaml();
      setField(provider, "applicationConfigPath", configPath.toString());

      invokeInit(provider);

      assertNotNull(provider.getConfig());
      assertEquals("ConfigPathTestAktivitet", provider.getConfig().getUppgift().getAktivitet());
   }

   @Test
   @DisplayName("FRALL-FR-02.3: Ramverket ska avvisa config som inte uppfyller JSON Schema")
   void init_shouldFailWhenConfigViolatesSchema() throws Exception
   {
      var configPath = Path.of(getClass().getClassLoader().getResource("config-schema-invalid-test.yaml").toURI());
      RegelConfigProviderYaml provider = new RegelConfigProviderYaml();
      setField(provider, "applicationConfigPath", configPath.toString());

      assertThrows(Exception.class, () -> invokeInit(provider));
   }

   @Test
   @DisplayName("FRALL-FR-02.2: Ramverket ska avvisa config där obligatoriska sektioner saknas")
   void init_shouldFailWhenRequiredSectionIsMissing() throws Exception
   {
      var configPath = Path.of(getClass().getClassLoader().getResource("config-missing-section-test.yaml").toURI());
      RegelConfigProviderYaml provider = new RegelConfigProviderYaml();
      setField(provider, "applicationConfigPath", configPath.toString());

      assertThrows(Exception.class, () -> invokeInit(provider));
   }

   private static void setField(Object target, String fieldName, String value) throws Exception
   {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
   }

   private static void invokeInit(RegelConfigProviderYaml provider) throws Exception
   {
      var method = RegelConfigProviderYaml.class.getDeclaredMethod("init");
      method.setAccessible(true);
      method.invoke(provider);
   }
}
