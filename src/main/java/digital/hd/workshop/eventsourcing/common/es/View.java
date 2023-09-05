package digital.hd.workshop.eventsourcing.common.es;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class View {
    private final Map<Class<? extends Event>, Method> METHOD_CACHE = new HashMap<>();
    private final Journal journal;
    private final ViewOffsetService offsetService;

    protected View(Journal journal, ViewOffsetService offsetService) {
        this.journal = journal;
        this.offsetService = offsetService;
    }

    protected abstract String getViewId();

    protected abstract Collection<AggregateType> getAggregateTypes();

    protected abstract void truncate();

    public void replay() {
        truncate();
        try (Stream<ProjectionEvent> stream = journal.loadEvents(getAggregateTypes(), 0L)) {
            stream.forEach(this::handleEvent);
        }
    }

    private void handleEvent(ProjectionEvent projectionEvent) {
        Event event = projectionEvent.getEvent();
        getApplyMethod(event.getClass()).ifPresent(applyMethod -> {
            try {
                applyMethod.invoke(this, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
        offsetService.setOffset(getViewId(), projectionEvent.getSequenceNumber());
    }

    private Optional<Method> getApplyMethod(Class<? extends Event> eventClass) {
        /* Reflectively find appropriate apply method */
        try {
            Method applyMethod = METHOD_CACHE.get(eventClass);
            if (applyMethod == null) {
                applyMethod = this.getClass().getDeclaredMethod("handle", eventClass);
                applyMethod.setAccessible(true);
                METHOD_CACHE.put(eventClass, applyMethod);
            }
            return Optional.of(applyMethod);
        } catch (NoSuchMethodException ne) {
            return Optional.empty();
        }
    }

    public synchronized void handleEventAdded(Long sequence) {
        Long currentOffset = offsetService.getOffset(getViewId());
        if (currentOffset < sequence) {
            try (Stream<ProjectionEvent> stream = journal.loadEvents(getAggregateTypes(), currentOffset)) {
                stream.forEach(this::handleEvent);
            }
        }
    }
}
