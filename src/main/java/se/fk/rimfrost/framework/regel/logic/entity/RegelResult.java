package se.fk.rimfrost.framework.regel.logic.entity;

import org.immutables.value.Value;

import jakarta.validation.constraints.NotNull;
import se.fk.rimfrost.framework.regel.Utfall;

import java.util.List;

@Value.Immutable
public interface RegelResult
{

   @NotNull
   List<ErsattningData> ersattningar();

   @NotNull
   List<Underlag> underlag();

   @NotNull
   Utfall utfall();

}
