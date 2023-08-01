package digital.hd.workshop.eventsourcing.common.es;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@SuperBuilder
@Getter
@ToString
public abstract class Event {

    protected final String aggregateId;
    protected final String who;
    protected final Instant when;
    public abstract EventType getEventType();
}
