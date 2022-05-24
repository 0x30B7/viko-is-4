package dev.mantas.is.ketvirta.model;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Getter
@Setter
public class DatabaseLoader {

    private Path databaseFilePath;

    public String checkPreviousPath() throws IOException {
        File lastDbCacheFile = new File("./.lastdb");

        if (!lastDbCacheFile.exists()) {
           return null;
        }

        return Files.readString(lastDbCacheFile.toPath());
    }

    public String load() throws IOException {
        File lastDbCacheFile = new File("./.lastdb");
        boolean lastDbCacheNeedsUpdate = true;
        Path databaseFilePath = this.databaseFilePath;

        if (databaseFilePath == null) {
            if (lastDbCacheFile.exists()) {
                try {
                    String path = Files.readString(lastDbCacheFile.toPath());
                    lastDbCacheNeedsUpdate = false;
                    databaseFilePath = Path.of(path);
                } catch (Exception ex) {
                    System.out.println("Could not load last database path info");
                    ex.printStackTrace();
                }
            }
        }

        if (databaseFilePath == null) {
            throw new IllegalStateException("Missing database path");
        }

        this.databaseFilePath = databaseFilePath;

        if (lastDbCacheNeedsUpdate) {
            Files.writeString(lastDbCacheFile.toPath(), databaseFilePath.toAbsolutePath().toString(),
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }

        if (!Files.exists(databaseFilePath)) {
            return "";
        }

        return Files.readString(databaseFilePath);
    }

    public void save(String dbData) throws IOException {
        Files.writeString(databaseFilePath, dbData, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
