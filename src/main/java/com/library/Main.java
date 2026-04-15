package com.library;

import com.library.db.DBConnection;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Entry point of the Library Management System.
 *
 * Step 3 - launches JavaFX window via AppLauncher -> LibraryApp
 */
public class Main {

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        System.out.println("===========================================");
        System.out.println("  Library Management System — v0.5");
        System.out.println("  Project structure: OK ✅");
        System.out.println("  Java version: " + System.getProperty("java.version"));

        // Warm up the DB connection before the window opens
        DBConnection.getInstance();

        System.out.println("  Launching UI... ✅");
        System.out.println("===========================================");

        AppLauncher.main(args);
    }
}