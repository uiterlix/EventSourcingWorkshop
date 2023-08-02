package digital.hd.workshop.eventsourcing.application.controller;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class LineItemDetails {

    private String productId;
    private int quantity;
}
