package com.library.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Author — модель")
class AuthorTest {

    private Author makeAuthor(int id) {
        return new Author(id, "Taras Shevchenko", "Ukraine", 1814);
    }

    // ── Конструктор ───────────────────────────────────────────────

    @Test
    @DisplayName("Повний конструктор заповнює всі поля")
    void fullConstructor_setsAllFields() {
        Author a = makeAuthor(1);
        assertAll(
                () -> assertEquals(1,                  a.getId()),
                () -> assertEquals("Taras Shevchenko", a.getFullName()),
                () -> assertEquals("Ukraine",          a.getCountry()),
                () -> assertEquals(1814,               a.getBirthYear())
        );
    }

    @Test
    @DisplayName("Порожній конструктор — значення за замовчуванням")
    void emptyConstructor_defaultValues() {
        Author a = new Author();
        assertAll(
                () -> assertEquals(0,    a.getId()),
                () -> assertNull(        a.getFullName()),
                () -> assertNull(        a.getCountry()),
                () -> assertEquals(0,    a.getBirthYear())
        );
    }

    // ── Setters ───────────────────────────────────────────────────

    @Test
    @DisplayName("Setters оновлюють значення")
    void setters_updateValues() {
        Author a = new Author();
        a.setId(10);
        a.setFullName("Ivan Franko");
        a.setCountry("Ukraine");
        a.setBirthYear(1856);

        assertAll(
                () -> assertEquals(10,           a.getId()),
                () -> assertEquals("Ivan Franko", a.getFullName()),
                () -> assertEquals("Ukraine",     a.getCountry()),
                () -> assertEquals(1856,          a.getBirthYear())
        );
    }

    // ── Граничні значення ─────────────────────────────────────────

    @Test
    @DisplayName("birthYear = 0 — допустимо (невідомий рік)")
    void birthYear_zero_isAccepted() {
        Author a = new Author(1, "Anonymous", null, 0);
        assertEquals(0, a.getBirthYear());
    }

    @Test
    @DisplayName("country може бути null")
    void country_canBeNull() {
        Author a = new Author(1, "Unknown", null, 1900);
        assertNull(a.getCountry());
    }

    @Test
    @DisplayName("Від'ємний рік народження зберігається як є")
    void birthYear_negative_storedAsIs() {
        Author a = new Author(1, "Julius Caesar", "Rome", -100);
        assertEquals(-100, a.getBirthYear());
    }
}
