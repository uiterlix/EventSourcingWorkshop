package digital.hd.workshop.eventsourcing.domain.sales;

import digital.hd.workshop.eventsourcing.common.es.EventSourcedRepository;
import digital.hd.workshop.eventsourcing.common.es.InvocationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderCommandHandler {

    private final EventSourcedRepository repository;

    public OrderCommandHandler(@Autowired EventSourcedRepository repository) {
        this.repository = repository;
    }

    public void createOrder(String user, String orderId, String tableNumber) {
        InvocationContext context = new InvocationContext(user);
        OrderAR order = new OrderAR(orderId);
        order.createOrder(context, tableNumber);
        repository.save(order);
    }

    public void addItemToOrder(String user, String orderId, String productId, int quantity) {
        InvocationContext context = new InvocationContext(user);
        OrderAR order = repository.load(orderId, OrderAR.class);
        order.addLineItem(context, productId, quantity);
        repository.save(order);
    }

    public void confirmOrder(String user, String orderId) {
        InvocationContext context = new InvocationContext(user);
        OrderAR order = repository.load(orderId, OrderAR.class);
        order.confirmOrder(context);
        repository.save(order);
    }

    public void cancelOrder(String user, String orderId) {
        InvocationContext context = new InvocationContext(user);
        OrderAR order = repository.load(orderId, OrderAR.class);
        order.cancelOrder(context);
        repository.save(order);
    }
}
