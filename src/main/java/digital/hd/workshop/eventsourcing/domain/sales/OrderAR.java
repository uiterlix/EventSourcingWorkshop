package digital.hd.workshop.eventsourcing.domain.sales;

import digital.hd.workshop.eventsourcing.common.es.AggregateType;
import digital.hd.workshop.eventsourcing.common.es.EventSourcedAggregateRoot;
import digital.hd.workshop.eventsourcing.common.es.InvocationContext;
import digital.hd.workshop.eventsourcing.domain.sales.event.OrderCancelled;
import digital.hd.workshop.eventsourcing.domain.sales.event.OrderConfirmed;
import digital.hd.workshop.eventsourcing.domain.sales.event.OrderCreated;
import digital.hd.workshop.eventsourcing.domain.sales.event.OrderItemAdded;
import digital.hd.workshop.eventsourcing.domain.sales.state.OrderItem;
import digital.hd.workshop.eventsourcing.domain.sales.state.OrderStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OrderAR extends EventSourcedAggregateRoot {

    public static final AggregateType AGGREGATE_TYPE = AggregateType.builder()
            .identifier("Order")
            .eventTypes(Map.of(
                    OrderCreated.EVENT_TYPE, OrderCreated.class,
                    OrderItemAdded.EVENT_TYPE, OrderItemAdded.class,
                    OrderConfirmed.EVENT_TYPE, OrderConfirmed.class,
                    OrderCancelled.EVENT_TYPE, OrderCancelled.class))
            .build();

    private OrderStatus status;
    private final List<OrderItem> items = new ArrayList<>();

    public OrderAR(String aggregateId) {
        super(aggregateId);
    }

    void handle(OrderCreated event) {
        this.status = OrderStatus.CREATED;
        // simply not interested in tableNumber here
    }

    void handle(OrderItemAdded event) {
        items.add(new OrderItem(event.getProductId(), event.getQuantity()));
    }

    void handle(OrderConfirmed event) {
        this.status = OrderStatus.CONFIRMED;
    }

    void handle(OrderCancelled event) {
        this.status = OrderStatus.CANCELLED;
    }

    public void createOrder(InvocationContext context, String tableNumber) {
        applyChange(OrderCreated.builder()
                .aggregateId(aggregateId)
                .when(Instant.now())
                .who(context.getUser())
                .tableNumber(tableNumber)
                .build());
    }

    public void addLineItem(InvocationContext context, String productId, int quantity) {
        applyChange(OrderItemAdded.builder()
                .aggregateId(aggregateId)
                .when(Instant.now())
                .who(context.getUser())
                .productId(productId)
                .quantity(quantity)
                .build());
    }

    public void confirmOrder(InvocationContext context) {
        if (status.equals(OrderStatus.CONFIRMED)) {
            throw new OrderException("Order already confirmed");
        }
        if (items.size() == 0) {
            throw new OrderException("Order has no items");
        }
        applyChange(OrderConfirmed.builder()
                .aggregateId(aggregateId)
                .when(Instant.now())
                .who(context.getUser())
                .build());
    }

    public void cancelOrder(InvocationContext context) {
        applyChange(OrderCancelled.builder()
                .aggregateId(aggregateId)
                .when(Instant.now())
                .who(context.getUser())
                .build());
    }

    @Override
    public AggregateType getAggregateType() {
        return AGGREGATE_TYPE;
    }

    // Getters, mainly for testing purposes
    public String getAggregateId() {
        return aggregateId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

}
