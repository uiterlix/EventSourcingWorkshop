package digital.hd.workshop.eventsourcing.application;

import digital.hd.workshop.eventsourcing.common.es.EventSourcedRepository;
import digital.hd.workshop.eventsourcing.common.es.InvocationContext;
import digital.hd.workshop.eventsourcing.common.es.Journal;
import digital.hd.workshop.eventsourcing.domain.sales.OrderAR;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Initializer {

    private final EventSourcedRepository repository;
    private final Journal journal;

    public Initializer(@Autowired EventSourcedRepository repository,
                       @Autowired Journal journal) {
        this.repository = repository;
        this.journal = journal;
    }

    @PostConstruct
    public void init() {
        // try and save the aggregate
        OrderAR order = new OrderAR(UUID.randomUUID().toString());
        InvocationContext context = new InvocationContext("System");
        order.createOrder(context, "1");
        order.addLineItem(context, "GreenTea", 3);
        order.confirmOrder(context);
        repository.save(order);

        OrderAR loadedOrder = repository.load(order.getAggregateId(), OrderAR.class);
        // try and load the aggregate we just saved

        loadedOrder.cancelOrder(context);
        repository.save(loadedOrder);

        order = new OrderAR(UUID.randomUUID().toString());
        order.createOrder(context, "2");
        repository.save(order);

        for (int i = 0; i < 10; i++) {
            OrderAR o = new OrderAR(UUID.randomUUID().toString());
            o.createOrder(context, String.valueOf(i));
            repository.save(o);
        }
    }
}
