package digital.hd.workshop.eventsourcing.common.es;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProjectionEvent {

    private final Long sequenceNumber;
    private final Event event;
}
