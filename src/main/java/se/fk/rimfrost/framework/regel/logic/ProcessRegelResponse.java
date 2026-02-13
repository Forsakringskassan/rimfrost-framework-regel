package se.fk.rimfrost.framework.regel.logic;

import java.util.List;

import org.immutables.value.Value;

import se.fk.rimfrost.framework.regel.logic.entity.ErsattningData;
import se.fk.rimfrost.framework.regel.logic.entity.Underlag;

@Value.Immutable
public interface ProcessRegelResponse {
    
    List<ErsattningData> ersattningar();

    List<Underlag> underlag();

}
