package se.fk.rimfrost.framework.logic.config;

import java.util.UUID;

@SuppressWarnings("unused")
public class Regel
{

   private UUID id;
   private String version;
   private String namn;
   private String beskrivning;

   public Regel()
   {
      // required by SnakeYAML
   }

   public UUID getId()
   {
      return id;
   }

   public void setId(UUID id)
   {
      this.id = id;
   }

   public String getVersion()
   {
      return version;
   }

   public void setVersion(String version)
   {
      this.version = version;
   }

   public String getNamn()
   {
      return namn;
   }

   public void setNamn(String namn)
   {
      this.namn = namn;
   }

   public String getBeskrivning()
   {
      return beskrivning;
   }

   public void setBeskrivning(String beskrivning)
   {
      this.beskrivning = beskrivning;
   }

}
