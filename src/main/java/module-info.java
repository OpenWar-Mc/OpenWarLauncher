module com.openwar.charpy.openwarlauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.base;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires java.net.http;
    requires jdk.httpserver;
    requires org.json;
    requires org.jsoup;
    requires com.google.gson;
    requires jdk.management;

    opens com.openwar.charpy.openwarlauncher to javafx.fxml;
    exports com.openwar.charpy.openwarlauncher;
    exports com.openwar.charpy.openwarlauncher.controller;
    opens com.openwar.charpy.openwarlauncher.controller to javafx.fxml;
    exports com.openwar.charpy.openwarlauncher.utils;
    opens com.openwar.charpy.openwarlauncher.utils to javafx.fxml;
}