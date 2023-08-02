package digital.hd.workshop.eventsourcing.common.es;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class EventSourcedRepository {

    private final Journal journal;

    public EventSourcedRepository(@Autowired Journal journal) {
        this.journal = journal;
    }

    public <T extends EventSourcedAggregateRoot> void save(T aggregate) {
        long expectedVersion = aggregate.getVersion() - aggregate.getPendingEvents().size();
        journal.appendEvents(aggregate.getAggregateType(), aggregate.getAggregateId(), expectedVersion, aggregate.getPendingEvents());
    }

    @SneakyThrows
    public <T extends EventSourcedAggregateRoot> T load(String aggregateId, Class<T> aggregateType) {
        T aggregate = aggregateType.getDeclaredConstructor(String.class).newInstance(aggregateId);
        try (Stream<Event> events = journal.loadEvents(aggregate.getAggregateType(), aggregateId)) {
            aggregate.reconstituteFromEvents(events);
        }
        return aggregate;
    }
}
