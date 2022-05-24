package dev.mantas.is.ketvirta.model.database;

import dev.mantas.is.ketvirta.util.InnerPasswordCodec;

public class DatabaseEntry {

    private final String title;
    private String username;
    private String description;
    private InnerPasswordCodec.InnerPassword password;

    public DatabaseEntry(String title, String username,  String description, InnerPasswordCodec.InnerPassword password) {
        this.title = title;
        this.username = username;
        this.description = description;
        this.password = password;
    }

    public String getTitle() {
        return title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InnerPasswordCodec.InnerPassword getPassword() {
        return password;
    }

    public void setPassword(InnerPasswordCodec.InnerPassword password) {
        this.password = password;
    }

}
