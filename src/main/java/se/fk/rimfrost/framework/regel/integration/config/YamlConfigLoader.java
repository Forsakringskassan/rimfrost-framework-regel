package se.fk.rimfrost.framework.regel.integration.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public final class YamlConfigLoader
{

   private static final String SCHEMA_CLASSPATH = "schema/regel_schema.yaml";

   private YamlConfigLoader()
   {
   } // utility class

   /**
    * Loads a typed config object from a YAML file, validating it against the bundled JSON Schema
    * before parsing.
    *
    * @param path  path to the YAML file
    * @param clazz target type
    * @param <T>   target type
    * @return parsed config object
    * @throws FileNotFoundException if the file does not exist
    */
   public static <T> T loadFromFile(Path path, Class<T> clazz) throws FileNotFoundException
   {
      if (!Files.exists(path))
      {
         throw new FileNotFoundException("YAML config not found: " + path);
      }

      try
      {
         byte[] bytes = Files.readAllBytes(path);
         validateAgainstSchema(bytes);

         LoaderOptions loaderOptions = new LoaderOptions();
         Constructor constructor = new Constructor(clazz, loaderOptions);
         Yaml yaml = new Yaml(constructor);
         return yaml.load(new ByteArrayInputStream(bytes));
      }
      catch (FileNotFoundException | IllegalStateException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to load YAML config: " + path, e);
      }
   }

   /**
    * Loads a typed config object from a classpath resource, validating it against the bundled JSON
    * Schema before parsing.
    *
    * @param resource classpath resource name
    * @param type     target type
    * @param <T>      target type
    * @return parsed config object
    */
   public static <T> T loadFromClasspath(String resource, Class<T> type)
   {
      try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource))
      {
         if (is == null)
         {
            throw new IllegalStateException("YAML config not found on classpath: " + resource);
         }
         byte[] bytes = is.readAllBytes();
         validateAgainstSchema(bytes);

         return new Yaml().loadAs(new ByteArrayInputStream(bytes), type);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to load YAML config: " + resource, e);
      }
   }

   /**
    * Validates raw YAML bytes against the bundled JSON Schema.
    *
    * @param yamlBytes raw YAML content
    * @throws IllegalStateException if schema violations are found
    * @throws RuntimeException      if the schema itself cannot be loaded
    */
   private static void validateAgainstSchema(byte[] yamlBytes)
   {
      ObjectMapper mapper = new ObjectMapper();
      Object rawYaml = new Yaml().load(new ByteArrayInputStream(yamlBytes));
      JsonNode configNode = mapper.valueToTree(rawYaml);

      try (InputStream schemaStream = YamlConfigLoader.class.getClassLoader()
            .getResourceAsStream(SCHEMA_CLASSPATH))
      {
         if (schemaStream == null)
         {
            throw new IllegalStateException("Config schema not found on classpath: " + SCHEMA_CLASSPATH);
         }

         JsonNode schemaNode = mapper.valueToTree(new Yaml().load(schemaStream));
         JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
         JsonSchema schema = factory.getSchema(schemaNode);

         Set<ValidationMessage> violations = schema.validate(configNode);
         if (!violations.isEmpty())
         {
            String details = violations.stream()
                  .map(ValidationMessage::getMessage)
                  .collect(Collectors.joining("; "));
            throw new IllegalStateException("Config violates JSON Schema: " + details);
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to load config schema", e);
      }
   }
}
