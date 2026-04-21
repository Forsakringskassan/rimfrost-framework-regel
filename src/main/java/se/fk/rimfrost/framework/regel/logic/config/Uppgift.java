package se.fk.rimfrost.framework.regel.logic.config;

import java.util.UUID;

@SuppressWarnings("unused")
public class Uppgift
{
   private UUID id;
   private int version;
   private String path;
   private String aktivitet;

   public Uppgift()
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

   public int getVersion()
   {
      return version;
   }

   public void setVersion(int version)
   {
      this.version = version;
   }

   public String getAktivitet()
   {
      return aktivitet;
   }

   public void setAktivitet(String aktivitet)
   {
      this.aktivitet = aktivitet;
   }

   public String getPath()
   {
      return path;
   }

   public void setPath(String path)
   {
      this.path = path;
   }
}
