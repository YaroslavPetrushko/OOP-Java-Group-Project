package com.library.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reader — модель")
class ReaderTest {

    private static final LocalDate REG = LocalDate.of(2024, 1, 15);

    private Reader makeReader(int id) {
        return new Reader(id, "Olena Kovalenko", "olena@example.com", "+380501234567", REG);
    }

    // ── Конструктор ───────────────────────────────────────────────

    @Test
    @DisplayName("Повний конструктор заповнює всі поля")
    void fullConstructor_setsAllFields() {
        Reader r = makeReader(5);
        assertAll(
                () -> assertEquals(5,                      r.getId()),
                () -> assertEquals("Olena Kovalenko",      r.getFullName()),
                () -> assertEquals("olena@example.com",    r.getEmail()),
                () -> assertEquals("+380501234567",         r.getPhone()),
                () -> assertEquals(REG,                    r.getRegDate())
        );
    }

    @Test
    @DisplayName("Порожній конструктор — значення за замовчуванням")
    void emptyConstructor_defaultValues() {
        Reader r = new Reader();
        assertAll(
                () -> assertEquals(0,    r.getId()),
                () -> assertNull(        r.getFullName()),
                () -> assertNull(        r.getEmail()),
                () -> assertNull(        r.getPhone()),
                () -> assertNull(        r.getRegDate())
        );
    }

    // ── Setters ───────────────────────────────────────────────────

    @Test
    @DisplayName("Setters оновлюють значення")
    void setters_updateValues() {
        Reader r = new Reader();
        LocalDate date = LocalDate.of(2025, 3, 1);
        r.setId(99);
        r.setFullName("Mykola Petrenko");
        r.setEmail("mykola@lib.ua");
        r.setPhone("+380661112233");
        r.setRegDate(date);

        assertAll(
                () -> assertEquals(99,                  r.getId()),
                () -> assertEquals("Mykola Petrenko",   r.getFullName()),
                () -> assertEquals("mykola@lib.ua",     r.getEmail()),
                () -> assertEquals("+380661112233",      r.getPhone()),
                () -> assertEquals(date,                r.getRegDate())
        );
    }

    // ── Граничні значення ─────────────────────────────────────────

    @Test
    @DisplayName("email та phone можуть бути null")
    void optionalFields_canBeNull() {
        Reader r = new Reader(1, "NoEmail Reader", null, null, REG);
        assertNull(r.getEmail());
        assertNull(r.getPhone());
    }

    @Test
    @DisplayName("regDate може бути null")
    void regDate_canBeNull() {
        Reader r = new Reader(1, "Test", "t@t.ua", "123", null);
        assertNull(r.getRegDate());
    }

    @Test
    @DisplayName("regDate зберігає точну дату")
    void regDate_exactDate() {
        LocalDate date = LocalDate.of(2000, 12, 31);
        Reader r = new Reader(1, "Test", null, null, date);
        assertEquals(LocalDate.of(2000, 12, 31), r.getRegDate());
    }
}
