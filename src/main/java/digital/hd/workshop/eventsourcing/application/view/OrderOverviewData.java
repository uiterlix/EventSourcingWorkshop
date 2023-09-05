package digital.hd.workshop.eventsourcing.application.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderOverviewData {

    private String orderId;
    private Long itemCount;
    private String orderStatus;
}
