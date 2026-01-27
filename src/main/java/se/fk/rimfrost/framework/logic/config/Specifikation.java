package se.fk.rimfrost.framework.logic.config;

import java.util.UUID;

@SuppressWarnings("unused")
public class Specifikation
{
   private UUID id;
   private String version;
   private String namn;
   private String uppgiftbeskrivning;
   private String verksamhetslogik;
   private String roll;
   private String applikationsId;
   private String applikationsversion;

   public Specifikation()
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

   public String getUppgiftbeskrivning()
   {
      return uppgiftbeskrivning;
   }

   public void setUppgiftbeskrivning(String uppgiftbeskrivning)
   {
      this.uppgiftbeskrivning = uppgiftbeskrivning;
   }

   public String getVerksamhetslogik()
   {
      return verksamhetslogik;
   }

   public void setVerksamhetslogik(String verksamhetslogik)
   {
      this.verksamhetslogik = verksamhetslogik;
   }

   public String getRoll()
   {
      return roll;
   }

   public void setRoll(String roll)
   {
      this.roll = roll;
   }

   public String getApplikationsId()
   {
      return applikationsId;
   }

   public void setApplikationsId(String applikationsId)
   {
      this.applikationsId = applikationsId;
   }

   public String getApplikationsversion()
   {
      return applikationsversion;
   }

   public void setApplikationsversion(String applikationsversion)
   {
      this.applikationsversion = applikationsversion;
   }
}
