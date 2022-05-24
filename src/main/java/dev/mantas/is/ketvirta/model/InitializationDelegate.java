package dev.mantas.is.ketvirta.model;

import dev.mantas.is.ketvirta.util.InnerPasswordCodec;

public interface InitializationDelegate {

    void setDatabasePath(String path);

    void setMasterPassword(InnerPasswordCodec.InnerPassword password);

}
