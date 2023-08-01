package digital.hd.workshop.eventsourcing.infrastructure;

import digital.hd.workshop.eventsourcing.common.es.EventType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;

public abstract class JacksonEventIgnoreMixin {
    @JsonIgnore
    public String aggregateId;
    @JsonIgnore
    public String who;
    @JsonIgnore
    public Instant when;
    @JsonIgnore
    public abstract EventType getEventType();
}
