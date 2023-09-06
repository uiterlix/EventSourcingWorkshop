package digital.hd.workshop.eventsourcing.common.es;

public class AggregateNotFoundException extends RuntimeException {
    private final String aggregateId;

    public AggregateNotFoundException(String aggregateId) {
        super("Aggregate not found: %s".formatted(aggregateId));
        this.aggregateId = aggregateId;
    }

    public String getAggregateId() {
        return aggregateId;
    }
}
