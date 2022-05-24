package dev.mantas.is.ketvirta.model.database;

import dev.mantas.is.ketvirta.model.DatabaseLoader;
import dev.mantas.is.ketvirta.util.InnerPasswordCodec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class DatabaseSerializer {

    public static void serialize(DatabaseLoader loader, Collection<DatabaseEntry> entries,
                                 InnerPasswordCodec.InnerPassword masterPassword) throws Exception {
        StringBuilder rawData = new StringBuilder(128 * entries.size());

        for (DatabaseEntry entry : entries) {
            rawData.append(entry.getTitle()).append(",");
            rawData.append(entry.getUsername()).append(",");
            rawData.append(entry.getDescription()).append(",");
            rawData.append(InnerPasswordCodec.serialize(entry.getPassword())).append("\n");
        }

        System.out.println("rawData");
        System.out.println(rawData);

        String unencryptedData = rawData.toString();
        byte[] encryptedData = DatabaseEncryption.encrypt(rawData.toString(), masterPassword);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] unencryptedDataHash = digest.digest(unencryptedData.getBytes(StandardCharsets.UTF_8));

        loader.save(MessageFormat.format("hash,{0},data,{1}",
                Base64.getEncoder().encodeToString(unencryptedDataHash),
                Base64.getEncoder().encodeToString(encryptedData)));
    }

    public static DatabaseReadResult deserialize(DatabaseLoader loader, InnerPasswordCodec.InnerPassword masterPassword) {
        String input;

        try {
            input = loader.load();
        } catch (Exception ex) {
            return DatabaseReadResult.failed(ex);
        }

        if (input.isEmpty()) {
            return DatabaseReadResult.success(Collections.emptyList());
        }

        Scanner scanner = new Scanner(input);
        scanner.useDelimiter(",");

        String rawHash = "", rawData = "";
        while (scanner.hasNext()) {
            String type = scanner.next();

            if (type.equals("hash")) {
                rawHash = scanner.next();
            } else if (type.equals("data")) {
                rawData = scanner.next();
            }
        }

        String decryptedDBData;

        try {
            decryptedDBData = DatabaseEncryption.decrypt(rawData, masterPassword);

            // Verify
            byte[] hash = Base64.getDecoder().decode(rawHash);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] dataHash = digest.digest(decryptedDBData.getBytes(StandardCharsets.UTF_8));

            if (!Arrays.equals(hash, dataHash)) {
                return DatabaseReadResult.failed(new Exception("Invalid credentials"));
            }
        } catch (Exception ex) {
            return DatabaseReadResult.failed(new Exception("Decryption failed", ex));
        }

        List<DatabaseEntry> passwordEntries = new ArrayList<>();

        Scanner dbColumnScanner = new Scanner(decryptedDBData);
        dbColumnScanner.useDelimiter("\n");
        while (dbColumnScanner.hasNext()) {
            String column = dbColumnScanner.next();

            Scanner dbRowScanner = new Scanner(column);
            dbRowScanner.useDelimiter(",");
            while (dbRowScanner.hasNext()) {
                passwordEntries.add(new DatabaseEntry(
                        dbRowScanner.next(),
                        dbRowScanner.next(),
                        dbRowScanner.next(),
                        InnerPasswordCodec.deserialize(dbRowScanner.next())
                ));
            }
        }

        return DatabaseReadResult.success(passwordEntries);
    }

}
