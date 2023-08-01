package digital.hd.workshop.eventsourcing.common.es;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Component
public interface Journal {

    void appendEvents(AggregateType aggregateType, String aggregateId, Long aggregateVersion, List<Event> events);

    Stream<Event> loadEvents(AggregateType aggregateType, String aggregateId);

    Stream<ProjectionEvent> loadEvents(Collection<AggregateType> aggregateTypes, long startSequence);

}
