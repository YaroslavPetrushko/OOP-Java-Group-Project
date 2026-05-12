package com.library;

import com.library.db.DBConnection;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Bootstrap class for the Library Management System.
 *
 * <p>Performs pre-launch setup before handing off to the JavaFX runtime:
 * <ol>
 *   <li>Redirects {@code System.out} to UTF-8 so that Ukrainian and other
 *       non-ASCII characters are printed correctly on all platforms.</li>
 *   <li>Warms up the database connection via {@link DBConnection#getInstance()}
 *       so that any credential errors are visible in the console <em>before</em>
 *       the window opens.</li>
 *   <li>Delegates to {@link AppLauncher#main(String[])} which starts the
 *       JavaFX application lifecycle via {@link LibraryApp}.</li>
 * </ol>
 *
 * @see AppLauncher
 * @see LibraryApp
 * @see DBConnection
 */
public class Main {

    /**
     * Application entry point.
     *
     * @param args command-line arguments (forwarded to JavaFX)
     */
    public static void main(String[] args) {
        // Ensure UTF-8 output on Windows consoles (e.g. Ukrainian text)
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        System.out.println("===========================================");
        System.out.println("  Library Management System — v1.0");
        System.out.println("  Project structure: OK ✅");
        System.out.println("  Java version: " + System.getProperty("java.version"));

        // Warm up the DB connection before the window opens;
        // prints success or error message to the console
        DBConnection.getInstance();

        System.out.println("  Launching UI... ✅");
        System.out.println("===========================================");

        AppLauncher.main(args);
    }
}