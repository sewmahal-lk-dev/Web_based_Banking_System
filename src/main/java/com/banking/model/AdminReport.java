package com.banking.model;

import java.io.Serializable;
import java.time.*;
import java.util.*;

/** One bounded, immutable report snapshot shared by preview and PDF. */
public record AdminReport(String type, String title, List<String> headers, List<List<String>> rows,
        Map<String,String> filters, String generatedBy, OffsetDateTime generatedAt, String token) implements Serializable {
    public String generatedLabel() {
        return generatedAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx"));
    }
    public AdminReport {
        headers = List.copyOf(headers);
        rows = rows.stream().map(List::copyOf).toList();
        filters = Collections.unmodifiableMap(new LinkedHashMap<>(filters));
    }
}
