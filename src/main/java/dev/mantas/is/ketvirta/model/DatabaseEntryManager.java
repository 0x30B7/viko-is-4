package dev.mantas.is.ketvirta.model;

import dev.mantas.is.ketvirta.model.database.DatabaseEntry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DatabaseEntryManager {

    private final Map<String, DatabaseEntry> registeredEntries = new HashMap<>();

    public void updateEntries(Collection<DatabaseEntry> entries) {
        this.registeredEntries.clear();
        this.registeredEntries.putAll(entries.stream().collect(Collectors.toMap(
                entry -> entry.getTitle().toLowerCase(Locale.ROOT),
                entry -> entry
        )));
    }

    public void registerEntry(DatabaseEntry entry) {
        registeredEntries.put(entry.getTitle().toLowerCase(Locale.ROOT), entry);
    }

    public void unregisterEntry(DatabaseEntry entry) {
        registeredEntries.remove(entry.getTitle().toLowerCase(Locale.ROOT));
    }

    public DatabaseEntry getEntryByTitle(String title) {
        return registeredEntries.get(title.toLowerCase(Locale.ROOT));
    }

    public Collection<DatabaseEntry> getEntries() {
        return registeredEntries.values();
    }

}
