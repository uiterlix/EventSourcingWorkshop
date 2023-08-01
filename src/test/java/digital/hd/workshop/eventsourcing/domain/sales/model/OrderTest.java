package digital.hd.workshop.eventsourcing.domain.sales.model;

import digital.hd.workshop.eventsourcing.common.es.InvocationContext;
import digital.hd.workshop.eventsourcing.domain.sales.OrderAR;
import digital.hd.workshop.eventsourcing.domain.sales.OrderException;
import digital.hd.workshop.eventsourcing.domain.sales.state.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderTest {

    @Test
    void testSimpleOrder() {
        String user = "John";
        InvocationContext context = new InvocationContext(user);

        OrderAR order = new OrderAR(UUID.randomUUID().toString());
        order.createOrder(context, "1");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getVersion()).isEqualTo(1L);

        order.addLineItem(context, "GreenTea", 1);
        assertThat(order.getItems().size()).isEqualTo(1);
        assertThat(order.getVersion()).isEqualTo(2L);
    }

    @Test
    void testConfirmWithoutItems() {
        String user = "John";
        InvocationContext context = new InvocationContext(user);

        OrderAR order = new OrderAR(UUID.randomUUID().toString());
        order.createOrder(context, "1");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

        assertThatThrownBy(() -> order.confirmOrder(context))
                .isInstanceOf(OrderException.class)
                .hasMessage("Order has no items");
    }

    @Test
    void testOrderAlreadyConfirmed() {
        String user = "John";
        InvocationContext context = new InvocationContext(user);

        OrderAR order = new OrderAR(UUID.randomUUID().toString());
        order.createOrder(context, "1");
        order.addLineItem(context, "GreenTea", 1);
        order.confirmOrder(context);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        assertThatThrownBy(() -> order.confirmOrder(context))
                .isInstanceOf(OrderException.class)
                .hasMessage("Order already confirmed");
    }
}
