package digital.hd.workshop.eventsourcing.application.controller;

import digital.hd.workshop.eventsourcing.domain.sales.OrderCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderCommandHandler commandHandler;

    public OrderController(@Autowired OrderCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @PostMapping("{id}")
    public void createOrder(@PathVariable("id") String id,
                            @RequestBody OrderDetails orderDetails) {
        commandHandler.createOrder("web-user", id, orderDetails.getTableNumber());
    }

    @PostMapping("{id}/add-item")
    public void addItemToOrder(@PathVariable("id") String id,
                               @RequestBody LineItemDetails orderItem) {
        commandHandler.addItemToOrder("web-user", id, orderItem.getProductId(), orderItem.getQuantity());
    }

    @PostMapping("{id}/confirm")
    public void confirmOrder(@PathVariable("id") String id) {
        commandHandler.confirmOrder("web-user", id);
    }

    @PostMapping("{id}/cancel")
    public void cancelOrder(@PathVariable("id") String id) {
        commandHandler.cancelOrder("web-user", id);
    }
}
