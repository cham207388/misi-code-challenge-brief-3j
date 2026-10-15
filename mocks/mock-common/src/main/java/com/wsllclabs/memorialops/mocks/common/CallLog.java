package com.wsllclabs.memorialops.mocks.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class CallLog {

    public record CallRecord(Instant at, String method, String path, int status) {}

    private final CopyOnWriteArrayList<CallRecord> records = new CopyOnWriteArrayList<>();

    public void add(String method, String path, int status) {
        records.add(new CallRecord(Instant.now(), method, path, status));
    }

    public List<CallRecord> all() {
        return List.copyOf(records);
    }

    public long count() {
        return records.stream()
                .filter(record -> !record.path().startsWith("/__admin"))
                .count();
    }

    public void reset() {
        records.clear();
    }

    public List<CallRecord> recent(int limit) {
        List<CallRecord> snapshot = all();
        int from = Math.max(0, snapshot.size() - limit);
        return new ArrayList<>(snapshot.subList(from, snapshot.size()));
    }
}
