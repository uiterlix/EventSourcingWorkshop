package digital.hd.workshop.eventsourcing.common.es;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class EventType {
    private final String identifier;
    private final Long version;
}
