package com.library;

import com.library.db.DBConnection;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Entry point of the Library Management System.
 *
 * Step 1 — console smoke-test.
 * JavaFX launch will be moved to AppLauncher on the next step.
 */
public class Main {

    public static void main(String[] args) {
        // UTF-8 Encoding for emojis in console
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        System.out.println("===========================================");
        System.out.println("  Library Management System — v0.2");
        System.out.println("  Project structure: OK ✅");
        System.out.println("  Java version: " + System.getProperty("java.version"));
        DBConnection.getInstance();
        System.out.println("===========================================");
    }
}