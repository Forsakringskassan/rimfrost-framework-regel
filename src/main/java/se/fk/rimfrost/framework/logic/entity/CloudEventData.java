package se.fk.rimfrost.framework.logic.entity;

import org.immutables.value.Value;
import java.util.UUID;

@Value.Immutable
public interface CloudEventData
{

   UUID id();

   UUID kogitorootprociid();

   UUID kogitoparentprociid();

   UUID kogitoprocinstanceid();

   String kogitorootprocid();

   String kogitoprocid();

   String kogitoprocist();

   String kogitoprocversion();

}
