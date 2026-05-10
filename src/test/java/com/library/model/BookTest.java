package com.library.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Book — модель")
class BookTest {

    private Book makeBook(int id) {
        return new Book(id, "Clean Code", 1, "Robert Martin",
                "Programming", "978-0132350884", 2008, 3);
    }

    // ── Конструктор ───────────────────────────────────────────────

    @Test
    @DisplayName("Повний конструктор заповнює всі поля")
    void fullConstructor_setsAllFields() {
        Book b = makeBook(1);
        assertAll(
                () -> assertEquals(1,                    b.getId()),
                () -> assertEquals("Clean Code",         b.getTitle()),
                () -> assertEquals(1,                    b.getAuthorId()),
                () -> assertEquals("Robert Martin",      b.getAuthorName()),
                () -> assertEquals("Programming",        b.getGenre()),
                () -> assertEquals("978-0132350884",     b.getIsbn()),
                () -> assertEquals(2008,                 b.getPubYear()),
                () -> assertEquals(3,                    b.getCopies())
        );
    }

    @Test
    @DisplayName("Порожній конструктор — всі поля за замовчуванням")
    void emptyConstructor_defaultValues() {
        Book b = new Book();
        assertAll(
                () -> assertEquals(0,    b.getId()),
                () -> assertNull(        b.getTitle()),
                () -> assertNull(        b.getGenre()),
                () -> assertNull(        b.getIsbn()),
                () -> assertNull(        b.getAuthorName()),
                () -> assertEquals(0,    b.getPubYear()),
                () -> assertEquals(0,    b.getCopies())
        );
    }

    // ── Setters ───────────────────────────────────────────────────

    @Test
    @DisplayName("Setters оновлюють значення")
    void setters_updateValues() {
        Book b = new Book();
        b.setId(42);
        b.setTitle("Refactoring");
        b.setAuthorId(5);
        b.setAuthorName("Martin Fowler");
        b.setGenre("Engineering");
        b.setIsbn("978-0201485677");
        b.setPubYear(1999);
        b.setCopies(10);

        assertAll(
                () -> assertEquals(42,                   b.getId()),
                () -> assertEquals("Refactoring",        b.getTitle()),
                () -> assertEquals(5,                    b.getAuthorId()),
                () -> assertEquals("Martin Fowler",      b.getAuthorName()),
                () -> assertEquals("Engineering",        b.getGenre()),
                () -> assertEquals("978-0201485677",     b.getIsbn()),
                () -> assertEquals(1999,                 b.getPubYear()),
                () -> assertEquals(10,                   b.getCopies())
        );
    }

    // ── equals / hashCode ─────────────────────────────────────────

    @Test
    @DisplayName("equals: однаковий id → рівні")
    void equals_sameId_isEqual() {
        Book b1 = makeBook(7);
        Book b2 = makeBook(7);
        b2.setTitle("Different Title");        // інша назва — не важливо
        assertEquals(b1, b2);
    }

    @Test
    @DisplayName("equals: різний id → не рівні")
    void equals_differentId_notEqual() {
        assertNotEquals(makeBook(1), makeBook(2));
    }

    @Test
    @DisplayName("equals: той самий об'єкт → рівний")
    void equals_sameReference_isEqual() {
        Book b = makeBook(5);
        assertEquals(b, b);
    }

    @Test
    @DisplayName("equals: null → не рівний")
    void equals_null_notEqual() {
        assertNotEquals(makeBook(1), null);
    }

    @Test
    @DisplayName("equals: інший тип → не рівний")
    void equals_differentType_notEqual() {
        assertNotEquals(makeBook(1), "not a book");
    }

    @Test
    @DisplayName("hashCode: однаковий id → однаковий hash")
    void hashCode_sameId_sameHash() {
        assertEquals(makeBook(3).hashCode(), makeBook(3).hashCode());
    }

    @Test
    @DisplayName("hashCode: різний id → різний hash (з великою імовірністю)")
    void hashCode_differentId_differentHash() {
        assertNotEquals(makeBook(1).hashCode(), makeBook(2).hashCode());
    }

    // ── toString ──────────────────────────────────────────────────

    @Test
    @DisplayName("toString містить назву та автора")
    void toString_containsTitleAndAuthor() {
        Book b = makeBook(1);
        String s = b.toString();
        assertTrue(s.contains("Clean Code"));
        assertTrue(s.contains("Robert Martin"));
    }
}
