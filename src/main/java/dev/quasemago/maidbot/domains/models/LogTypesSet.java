package dev.quasemago.maidbot.domains.models;

import java.util.*;

public final class LogTypesSet extends AbstractSet<LogTypes> {
    private final long rawValue;

    private LogTypesSet (long rawValue) {
        this.rawValue = rawValue;
    }

    public static LogTypesSet of(long rawValue) {
        return new LogTypesSet(rawValue);
    }

    public EnumSet<LogTypes> asEnumSet() {
        EnumSet<LogTypes> types = EnumSet.allOf(LogTypes.class);
        types.removeIf((type) -> !this.contains(type));
        return types;
    }

    public long getRawValue() {
        return this.rawValue;
    }

    public boolean contains(Object o) {
        return o instanceof LogTypes && (((LogTypes)o).getValue() & this.rawValue) > 0L;
    }

    public Iterator<LogTypes> iterator() {
        return Collections.unmodifiableSet(this.asEnumSet()).iterator();
    }

    public int size() {
        return Long.bitCount(this.rawValue);
    }
}
