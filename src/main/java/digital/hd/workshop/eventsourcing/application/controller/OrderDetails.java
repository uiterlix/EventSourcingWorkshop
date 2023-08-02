package digital.hd.workshop.eventsourcing.application.controller;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class OrderDetails {

    private String tableNumber;
}
