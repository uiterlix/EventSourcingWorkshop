package digital.hd.workshop.eventsourcing.application.view;

import digital.hd.workshop.eventsourcing.common.es.AggregateType;
import digital.hd.workshop.eventsourcing.common.es.Journal;
import digital.hd.workshop.eventsourcing.common.es.View;
import digital.hd.workshop.eventsourcing.common.es.ViewOffsetService;
import digital.hd.workshop.eventsourcing.domain.sales.OrderAR;
import digital.hd.workshop.eventsourcing.domain.sales.event.OrderCancelled;
import digital.hd.workshop.eventsourcing.domain.sales.event.OrderConfirmed;
import digital.hd.workshop.eventsourcing.domain.sales.event.OrderCreated;
import digital.hd.workshop.eventsourcing.domain.sales.event.OrderItemAdded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public class JPAOrderView extends View {
    private static final String VIEW_ID = "OrderOverviewJPA";
    private final OrderRepository orderRepository;

    protected JPAOrderView(@Autowired Journal journal,
                           @Autowired OrderRepository orderRepository,
                           @Autowired ViewOffsetService offsetService) {
        super(journal, offsetService);
        this.orderRepository = orderRepository;
    }

    @Override
    protected Collection<AggregateType> getAggregateTypes() {
        return Set.of(OrderAR.AGGREGATE_TYPE);
    }

    @Override
    public String getViewId() {
        return VIEW_ID;
    }

    public void handle(OrderCreated event) {
        Order order = Order.builder()
                        .orderId(event.getAggregateId())
                        .itemCount(0L)
                        .orderStatus("CREATED")
                        .build();
        orderRepository.save(order);
    }

    public void handle(OrderItemAdded event) {
        Order order = orderRepository.findById(event.getAggregateId()).orElseThrow();
        orderRepository.save(Order.builder()
                .orderId(event.getAggregateId())
                .itemCount(order.getItemCount() + event.getQuantity())
                .orderStatus(order.getOrderStatus())
                .build());
    }

    public void handle(OrderConfirmed event) {
        Order order = orderRepository.findById(event.getAggregateId()).orElseThrow();
        orderRepository.save(Order.builder()
                .orderId(event.getAggregateId())
                .itemCount(order.getItemCount())
                .orderStatus("CONFIRMED")
                .build());
    }

    public void handle(OrderCancelled event) {
        Order order = orderRepository.findById(event.getAggregateId()).orElseThrow();
        orderRepository.save(Order.builder()
                .orderId(event.getAggregateId())
                .itemCount(order.getItemCount())
                .orderStatus("CANCELLED")
                .build());
    }

    @Override
    protected void truncate() {
        orderRepository.deleteAll();
    }
}
