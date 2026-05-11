// ── AppLauncher.java ──────────────────────────────────────────────────────────
package com.library;

import javafx.application.Application;

/**
 * Application entry-point wrapper.
 *
 * <p>JavaFX requires that the class containing {@code main()} must <em>not</em>
 * be a subclass of {@link Application} when running from the module path without
 * a proper JavaFX launcher. This thin wrapper delegates to {@link LibraryApp}
 * via {@link Application#launch} to satisfy that constraint.
 *
 * @see LibraryApp
 */
public class AppLauncher {

    /**
     * Delegates to {@link Application#launch(Class, String[])} with {@link LibraryApp}.
     *
     * @param args command-line arguments (passed through unchanged)
     */
    public static void main(String[] args) {
        Application.launch(LibraryApp.class, args);
    }
}