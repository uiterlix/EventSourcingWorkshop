package digital.hd.workshop.eventsourcing.common.es;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class EventSourcedAggregateRoot {

    private final List<Event> pendingEvents = new ArrayList<>();
    protected final String aggregateId; // represents order state
    private final Map<Class<? extends Event>, Method> METHOD_CACHE = new HashMap<>();
    private long version;

    public EventSourcedAggregateRoot(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public void reconstituteFromEvents(Stream<? extends Event> events) {
        events.forEach(this::handleEvent);
    }

    protected void handleEvent(Event event) {
        /* Reflectively find and call the appropriate apply method */
        try {
            Class<? extends Event> eventClass = event.getClass();
            Method applyMethod = METHOD_CACHE.get(eventClass);
            if (applyMethod == null) {
                applyMethod = this.getClass().getDeclaredMethod("handle", eventClass);
                applyMethod.setAccessible(true);
                METHOD_CACHE.put(eventClass, applyMethod);
            }
            try {
                applyMethod.invoke(this, event);
                version++;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Missing handle method for " + event.getClass().getSimpleName());
        }
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public <E extends Event> void applyChange(E event) {
        handleEvent(event);
        pendingEvents.add(event);
    }

    public List<Event> getPendingEvents() {
        return Collections.unmodifiableList(pendingEvents);
    }

    public long getVersion() {
        return version;
    }

    public abstract AggregateType getAggregateType();

}
