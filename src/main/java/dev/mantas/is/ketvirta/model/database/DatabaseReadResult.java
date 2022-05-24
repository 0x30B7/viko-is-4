package dev.mantas.is.ketvirta.model.database;

import java.util.Collections;
import java.util.List;

public class DatabaseReadResult {

    public static DatabaseReadResult success(List<DatabaseEntry> entries) {
        return new DatabaseReadResult(true, entries, null);
    }

    public static DatabaseReadResult failed(Exception exception) {
        return new DatabaseReadResult(false, Collections.emptyList(), exception);
    }

    private DatabaseReadResult(boolean success, List<DatabaseEntry> entries, Exception exception) {
        this.success = success;
        this.entries = entries;
        this.exception = exception;
    }

    private final boolean success;
    private final List<DatabaseEntry> entries;
    private final Exception exception;

    public boolean isSuccessful() {
        return success;
    }

    public List<DatabaseEntry> contents() {
        return entries;
    }

    public Exception exception() {
        return exception;
    }

}
