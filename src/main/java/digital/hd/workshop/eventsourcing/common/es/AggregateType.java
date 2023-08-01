package digital.hd.workshop.eventsourcing.common.es;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class AggregateType {

    private final String identifier;
    private final Map<EventType, Class<? extends Event>> eventTypes;
}
