package se.fk.rimfrost.framework.regel.storage;

import se.fk.rimfrost.framework.regel.storage.entity.RegelCommonData;
import java.util.UUID;

public interface RegelCommonDataStorage
{
   RegelCommonData getRegelCommonData(UUID handlaggningId);

   void setRegelCommonData(UUID handlaggningId, RegelCommonData regelCommonData);

   void deleteRegelCommonData(UUID handlaggningId);
}
