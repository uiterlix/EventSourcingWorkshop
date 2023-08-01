package digital.hd.workshop.eventsourcing.common.es;

public interface ViewOffsetService {

    void setOffset(String viewId, Long offset);

    Long getOffset(String viewId);

    void resetOffset(String viewId);
}
