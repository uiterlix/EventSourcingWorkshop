package digital.hd.workshop.eventsourcing.infrastructure;

import digital.hd.workshop.eventsourcing.common.es.AggregateType;
import digital.hd.workshop.eventsourcing.common.es.Event;
import digital.hd.workshop.eventsourcing.common.es.EventType;
import digital.hd.workshop.eventsourcing.common.es.Journal;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import digital.hd.workshop.eventsourcing.common.es.JournalListener;
import digital.hd.workshop.eventsourcing.common.es.ProjectionEvent;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DBJournal implements Journal {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .addModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .addMixIn(Event.class, JacksonEventIgnoreMixin.class)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .build();

    private final JdbcTemplate jdbcTemplate;
    private final List<JournalListener> listeners;

    public DBJournal(@Autowired JdbcTemplate jdbcTemplate,
                     @Autowired List<JournalListener> listeners) {
        this.jdbcTemplate = jdbcTemplate;
        this.listeners = listeners;
    }

    @Override
    public void appendEvents(AggregateType aggregateType, String aggregateId, Long aggregateVersion, List<Event> events) {
        doAppendEvents(aggregateType, aggregateId, aggregateVersion, events);
        jdbcTemplate.query("SELECT SEQ FROM JOURNAL ORDER BY SEQ DESC LIMIT 1", rs -> {
            notifyListeners(aggregateType, rs.getLong("SEQ"));
        });
    }

    private void notifyListeners(AggregateType aggregateType, long seq) {
        listeners.forEach(l -> l.notifyEventsAdded(aggregateType.getIdentifier(), seq));
    }

    @Transactional
    public void doAppendEvents(AggregateType aggregateType, String aggregateId, Long aggregateVersion, List<Event> events) {
        long lastSequence = getLastAggregateSequence(aggregateType, aggregateId);
        if (lastSequence > aggregateVersion) {
            throw new RuntimeException("Aggregate version mismatch");
        }

        long eventSeq = aggregateVersion;
        for (Event event : events) {
            final long aggregateInstanceSeq = eventSeq;
            EventType eventType = event.getEventType();
            jdbcTemplate.execute("""
                INSERT INTO JOURNAL(AGGREGATE_ID, AGGREGATE_TYPE, WHO, TS, EVENT_TYPE, EVENT_VERSION, EVENT_SEQ, EVENT_DATA)
                VALUES(?, ? ,? ,? ,? ,?, ?, ?)
                    """, (PreparedStatementCallback<Void>) ps ->
            {
                ps.setString(1, aggregateId);
                ps.setString(2, aggregateType.getIdentifier());
                ps.setString(3, event.getWho());
                ps.setTimestamp(4, Timestamp.from(event.getWhen()));
                ps.setString(5, eventType.getIdentifier());
                ps.setLong(6, eventType.getVersion());
                ps.setLong(7, aggregateInstanceSeq);
                ps.setString(8, serialize(event));
                ps.execute();
                return null;
            });
            eventSeq ++;
        }
    }

    private long getLastAggregateSequence(AggregateType aggregateType, String aggregateId) {
        List<Long> eventSeqs = jdbcTemplate.query("""
            SELECT EVENT_SEQ FROM JOURNAL
            WHERE AGGREGATE_TYPE = ?
                AND AGGREGATE_ID = ?
            ORDER BY EVENT_SEQ DESC
            LIMIT 1""",
                ps -> {
                    ps.setString(1, aggregateType.getIdentifier());
                    ps.setString(2, aggregateId);
                    ps.execute();
                }, (rs, rn) -> rs.getLong("EVENT_SEQ"));
        return eventSeqs.isEmpty() ? 0 : eventSeqs.get(0);
    }

    @Override
    public Stream<Event> loadEvents(AggregateType aggregateType, String aggregateId) {
        return jdbcTemplate.queryForStream("""
            SELECT AGGREGATE_TYPE, AGGREGATE_ID, EVENT_TYPE, EVENT_VERSION, WHO, TS, EVENT_DATA
            FROM JOURNAL
            WHERE AGGREGATE_TYPE = ?
                AND AGGREGATE_ID = ?
            ORDER BY EVENT_SEQ
            """, ps -> {
            ps.setString(1, aggregateType.getIdentifier());
            ps.setString(2, aggregateId);
        }, (rs, rn) -> createEventFromResult(aggregateType, rs));
    }

    @Override
    public Stream<ProjectionEvent> loadEvents(Collection<AggregateType> aggregateTypes, long startSequence) {
        Map<String, AggregateType> aggregateTypeMap = aggregateTypes.stream().collect(Collectors.toMap(AggregateType::getIdentifier, Function.identity()));
        String atPlaceHolders = aggregateTypes.stream().map(at -> "?").reduce((s1, s2) -> s1 + ", " + s2)
                .orElseThrow(() -> new RuntimeException("No aggregate types provided"));
        return jdbcTemplate.queryForStream("""
                SELECT SEQ, AGGREGATE_TYPE, AGGREGATE_ID, EVENT_TYPE, EVENT_VERSION, WHO, TS, EVENT_DATA
                FROM JOURNAL
                WHERE AGGREGATE_TYPE IN (%s)
                    AND SEQ > ?
                ORDER BY SEQ
                """.formatted(atPlaceHolders), ps -> {
            int i = 1;
            for (AggregateType aggregateType : aggregateTypes) {
                ps.setString(i++, aggregateType.getIdentifier());
            }
            ps.setLong(i++, startSequence);
        }, (rs, rn) -> {
            String aggregateTypeIdentifier = rs.getString("AGGREGATE_TYPE");
            AggregateType aggregateType = aggregateTypeMap.get(aggregateTypeIdentifier);
            Event event = createEventFromResult(aggregateType, rs);
            return ProjectionEvent.builder()
                    .event(event)
                    .sequenceNumber(rs.getLong("SEQ"))
                    .build();
        });
    }

    @SneakyThrows
    private String serialize(Event event) {
        return JSON_MAPPER.writeValueAsString(event);
    }

    private Event createEventFromResult(AggregateType aggregateType, ResultSet rs) throws SQLException {
        String aggregateId = rs.getString("AGGREGATE_ID");
        String eventTypeIdentifier = rs.getString("EVENT_TYPE");
        Long eventVersion = rs.getLong("EVENT_VERSION");
        String eventJson = rs.getString("EVENT_DATA");
        String who = rs.getString("WHO");
        Instant when = rs.getTimestamp("TS").toInstant();
        Class<? extends Event> eventClass = aggregateType.getEventTypes().get(EventType.builder().identifier(eventTypeIdentifier).version(eventVersion).build());
        return createEvent(eventJson, aggregateId, who, when, eventClass);
    }

    @SneakyThrows
    private Event createEvent(String eventJson, String aggregateId, String who,
                              Instant when, Class<? extends Event> eventClass) {
        ObjectNode jsonNode = (ObjectNode) JSON_MAPPER.readTree(eventJson);
        jsonNode.put("aggregateId", aggregateId);
        jsonNode.put("who", who);
        jsonNode.put("when", when.toString());
        return JSON_MAPPER.treeToValue(jsonNode, eventClass);
    }
}
