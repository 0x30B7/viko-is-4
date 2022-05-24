module dev.mantas.is {
    requires static lombok;

    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires de.mkammerer.argon2;
    requires de.mkammerer.argon2.nolibs;

    requires com.sun.jna;

    opens dev.mantas.is.ketvirta to javafx.fxml;
    opens dev.mantas.is.ketvirta.controller to javafx.fxml;
    opens dev.mantas.is.ketvirta.model.database to de.mkammerer.argon2.nolibs;

    exports dev.mantas.is.ketvirta;
    exports dev.mantas.is.ketvirta.model;
    exports dev.mantas.is.ketvirta.model.database;
    exports dev.mantas.is.ketvirta.util;
    exports dev.mantas.is.ketvirta.controller;
}