package digital.hd.workshop.eventsourcing.application.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "ORDER_VIEW_JPA")
@Builder()
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class Order {

    @Id
    @Column
    private String orderId;

    @Column
    private Long itemCount;

    @Column
    private String orderStatus;
}
