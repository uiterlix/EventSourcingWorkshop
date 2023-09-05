package digital.hd.workshop.eventsourcing.application.controller;

import digital.hd.workshop.eventsourcing.application.view.JDBCOrderView;
import digital.hd.workshop.eventsourcing.application.view.OrderOverviewData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order-view")
public class OrderViewController {

    private final JDBCOrderView orderView;

    public OrderViewController(@Autowired JDBCOrderView orderView) {
        this.orderView = orderView;
    }

    @GetMapping()
    public List<OrderOverviewData> listOrders() {
        return orderView.getOrders();
    }

    @PostMapping("/replay")
    public void replay() {
        orderView.replay();
    }

}
