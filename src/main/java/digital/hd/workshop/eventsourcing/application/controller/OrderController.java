package digital.hd.workshop.eventsourcing.application.controller;

import digital.hd.workshop.eventsourcing.common.es.EventSourcedRepository;
import digital.hd.workshop.eventsourcing.common.es.InvocationContext;
import digital.hd.workshop.eventsourcing.domain.sales.OrderAR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final EventSourcedRepository repository;

    public OrderController(@Autowired EventSourcedRepository repository) {
        this.repository = repository;
    }

    @PostMapping("create/{id}")
    public void createOrder(@PathVariable("id") String id,
                            @RequestParam String tableNumber) {
        InvocationContext context = new InvocationContext("System");
        OrderAR order = new OrderAR(id);
        order.createOrder(context, tableNumber);
        repository.save(order);
    }

}
