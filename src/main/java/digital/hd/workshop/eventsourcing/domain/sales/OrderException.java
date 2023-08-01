package digital.hd.workshop.eventsourcing.domain.sales;

public class OrderException extends RuntimeException {

    public OrderException(String message) {
        super(message);
    }
}
