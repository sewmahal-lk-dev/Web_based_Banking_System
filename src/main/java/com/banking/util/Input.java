package com.banking.util;

import java.math.BigDecimal;
import java.util.Set;

public final class Input {
    private Input() { }
    public static String text(String value, int max, String label) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > max || value.chars().anyMatch(c -> c < 32 && c != 10 && c != 13))
            throw new IllegalArgumentException("Enter a valid " + label + " (maximum " + max + " characters).");
        return value.trim();
    }
    public static String choice(String value, String... allowed) {
        if (value == null || !Set.of(allowed).contains(value)) throw new IllegalArgumentException("Select a valid option.");
        return value;
    }
    public static BigDecimal money(BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.compareTo(new BigDecimal("9999999999999.99")) > 0 || value.stripTrailingZeros().scale() > 2)
            throw new IllegalArgumentException("Enter a positive amount with at most two decimal places within the account limit.");
        return value.setScale(2);
    }
    public static int id(String value) {
        try { int id = Integer.parseInt(value); if (id > 0) return id; } catch (RuntimeException ignored) { }
        throw new IllegalArgumentException("Select a valid record.");
    }
    public static long longId(String value) {
        try { long id=Long.parseLong(value);if(id>0)return id; }catch(RuntimeException ignored){}
        throw new IllegalArgumentException("Select a valid record.");
    }
    public static String html(Object value) {
        return value == null ? "" : value.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
