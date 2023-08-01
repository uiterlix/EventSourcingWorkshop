package digital.hd.workshop.eventsourcing.common.es;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ViewUpdater implements JournalListener {

    private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);
    private final List<View> views;

    public ViewUpdater(@Lazy @Autowired List<View> views) {
        this.views = views;
    }

    public void notifyEventsAdded(String aggregateTypeId, long sequence) {
        views.stream().filter(p -> p.getAggregateTypes().stream().map(AggregateType::getIdentifier).anyMatch(aggregateTypeId::equals))
                .forEach(p -> EXECUTOR.submit(() -> {
                    try {
                        p.handleEventAdded(sequence);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
    }
}
