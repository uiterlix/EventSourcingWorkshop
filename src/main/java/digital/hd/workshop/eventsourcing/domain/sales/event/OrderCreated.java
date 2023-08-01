package digital.hd.workshop.eventsourcing.domain.sales.event;

import digital.hd.workshop.eventsourcing.common.es.Event;
import digital.hd.workshop.eventsourcing.common.es.EventType;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder
@Jacksonized
@Getter
@ToString(callSuper = true)
public class OrderCreated extends Event {

    public static final EventType EVENT_TYPE = EventType.builder().identifier("OrderCreated").version(1L).build();

    private final String tableNumber;

    @Override
    public EventType getEventType() {
        return EVENT_TYPE;
    }
}
