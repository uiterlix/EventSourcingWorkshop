package digital.hd.workshop.eventsourcing.infrastructure;

import digital.hd.workshop.eventsourcing.common.es.ViewOffsetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DBViewOffsetService implements ViewOffsetService {
    private final JdbcTemplate jdbcTemplate;

    public DBViewOffsetService(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void setOffset(String viewId, Long offset) {
        jdbcTemplate.update("MERGE INTO VIEW_OFFSETS(VIEW_NAME, VIEW_OFFSET) VALUES(?, ?)",
                viewId, offset);
    }

    @Override
    public Long getOffset(String viewId) {
        return jdbcTemplate.query("SELECT VIEW_OFFSET FROM VIEW_OFFSETS WHERE VIEW_NAME = ?",
                r -> {
                    if (r.next()) {
                        return r.getLong(1);
                    } else {
                        return 0L;
                    }
                }, viewId);
    }

    @Override
    public void resetOffset(String viewId) {
        jdbcTemplate.update("MERGE INTO VIEW_OFFSETS(VIEW_NAME, VIEW_OFFSET) VALUES(?, ?)",
                viewId, 0L);
    }
}
