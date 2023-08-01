package digital.hd.workshop.eventsourcing.common.es;

public interface JournalListener {
    void notifyEventsAdded(String aggregateTypeId, long sequence);
}
