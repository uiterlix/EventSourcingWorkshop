package digital.hd.workshop.eventsourcing.application.view;

import digital.hd.workshop.eventsourcing.common.es.AggregateType;
import digital.hd.workshop.eventsourcing.common.es.Journal;
import digital.hd.workshop.eventsourcing.common.es.View;
import digital.hd.workshop.eventsourcing.common.es.ViewOffsetService;
import digital.hd.workshop.eventsourcing.domain.sales.OrderAR;
import digital.hd.workshop.eventsourcing.domain.sales.event.OrderCancelled;
import digital.hd.workshop.eventsourcing.domain.sales.event.OrderConfirmed;
import digital.hd.workshop.eventsourcing.domain.sales.event.OrderCreated;
import digital.hd.workshop.eventsourcing.domain.sales.event.OrderItemAdded;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
public class JDBCOrderView extends View {
    private static final String VIEW_ID = "OrderOverview";
    private final JdbcTemplate jdbcTemplate;

    protected JDBCOrderView(@Autowired Journal journal,
                            @Autowired JdbcTemplate jdbcTemplate,
                            @Autowired ViewOffsetService offsetService) {
        super(journal, offsetService);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected Collection<AggregateType> getAggregateTypes() {
        return Set.of(OrderAR.AGGREGATE_TYPE);
    }

    @Override
    public String getViewId() {
        return VIEW_ID;
    }

    public void handle(OrderCreated event) {
        jdbcTemplate.update("INSERT INTO ORDER_OVERVIEW(ORDER_ID, ITEM_COUNT, ORDER_STATUS) VALUES(?, ?, ?)",
                event.getAggregateId(), 0, "CREATED");
    }

    public void handle(OrderItemAdded event) {
        jdbcTemplate.update("UPDATE ORDER_OVERVIEW SET ITEM_COUNT = ITEM_COUNT + ? WHERE ORDER_ID = ?",
                event.getQuantity(),
                event.getAggregateId());
    }

    public void handle(OrderConfirmed event) {
        jdbcTemplate.update("UPDATE ORDER_OVERVIEW SET ORDER_STATUS = ? WHERE ORDER_ID = ?",
                "CONFIRMED",
                event.getAggregateId());
    }

    public void handle(OrderCancelled event) {
        jdbcTemplate.update("UPDATE ORDER_OVERVIEW SET ORDER_STATUS = ? WHERE ORDER_ID = ?",
                "CANCELLED",
                event.getAggregateId());
    }

    public List<OrderOverviewData> getOrders() {
        return jdbcTemplate.query("SELECT * FROM ORDER_OVERVIEW", (rs, rowNum) -> OrderOverviewData.builder()
                .orderId(rs.getString("ORDER_ID"))
                .itemCount(rs.getLong("ITEM_COUNT"))
                .orderStatus(rs.getString("ORDER_STATUS"))
                .build());
    }

    @Override
    protected void truncate() {
        jdbcTemplate.update("TRUNCATE TABLE ORDER_OVERVIEW");
    }
}
