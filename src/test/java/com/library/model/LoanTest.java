package com.library.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Loan — модель")
class LoanTest {

    private static final LocalDate LOAN_DATE = LocalDate.of(2025, 4, 1);
    private static final LocalDate DUE_DATE  = LocalDate.of(2025, 4, 15);

    private Loan makeLoan(int id) {
        return new Loan(id, 10, "Clean Code", 3, "Olena Kovalenko",
                LOAN_DATE, DUE_DATE, "active");
    }

    // ── Конструктор ───────────────────────────────────────────────

    @Test
    @DisplayName("Повний конструктор заповнює всі поля")
    void fullConstructor_setsAllFields() {
        Loan l = makeLoan(1);
        assertAll(
                () -> assertEquals(1,                  l.getId()),
                () -> assertEquals(10,                 l.getBookId()),
                () -> assertEquals("Clean Code",       l.getBookTitle()),
                () -> assertEquals(3,                  l.getReaderId()),
                () -> assertEquals("Olena Kovalenko",  l.getReaderName()),
                () -> assertEquals(LOAN_DATE,          l.getLoanDate()),
                () -> assertEquals(DUE_DATE,           l.getDueDate()),
                () -> assertEquals("active",           l.getStatus())
        );
    }

    @Test
    @DisplayName("Порожній конструктор — значення за замовчуванням")
    void emptyConstructor_defaultValues() {
        Loan l = new Loan();
        assertAll(
                () -> assertEquals(0,    l.getId()),
                () -> assertEquals(0,    l.getBookId()),
                () -> assertNull(        l.getBookTitle()),
                () -> assertEquals(0,    l.getReaderId()),
                () -> assertNull(        l.getReaderName()),
                () -> assertNull(        l.getLoanDate()),
                () -> assertNull(        l.getDueDate()),
                () -> assertNull(        l.getStatus())
        );
    }

    // ── Setters ───────────────────────────────────────────────────

    @Test
    @DisplayName("Setters оновлюють значення")
    void setters_updateValues() {
        Loan l = new Loan();
        l.setId(7);
        l.setBookId(20);
        l.setBookTitle("Refactoring");
        l.setReaderId(5);
        l.setReaderName("Ivan Franko");
        l.setLoanDate(LOAN_DATE);
        l.setDueDate(DUE_DATE);
        l.setStatus("returned");

        assertAll(
                () -> assertEquals(7,             l.getId()),
                () -> assertEquals(20,            l.getBookId()),
                () -> assertEquals("Refactoring", l.getBookTitle()),
                () -> assertEquals(5,             l.getReaderId()),
                () -> assertEquals("Ivan Franko", l.getReaderName()),
                () -> assertEquals(LOAN_DATE,     l.getLoanDate()),
                () -> assertEquals(DUE_DATE,      l.getDueDate()),
                () -> assertEquals("returned",    l.getStatus())
        );
    }

    // ── Статуси ───────────────────────────────────────────────────

    @Test
    @DisplayName("Статус 'active' зберігається без змін")
    void status_active() {
        Loan l = makeLoan(1);
        assertEquals("active", l.getStatus());
    }

    @Test
    @DisplayName("Статус можна змінити на 'returned'")
    void status_canBeSetToReturned() {
        Loan l = makeLoan(1);
        l.setStatus("returned");
        assertEquals("returned", l.getStatus());
    }

    @Test
    @DisplayName("Статус можна змінити на 'overdue'")
    void status_canBeSetToOverdue() {
        Loan l = makeLoan(1);
        l.setStatus("overdue");
        assertEquals("overdue", l.getStatus());
    }

    // ── Дати ─────────────────────────────────────────────────────

    @Test
    @DisplayName("dueDate пізніше loanDate (бізнес-логіка)")
    void dueDate_isAfterLoanDate() {
        Loan l = makeLoan(1);
        assertTrue(l.getDueDate().isAfter(l.getLoanDate()),
                "Дата повернення має бути пізніше дати видачі");
    }

    @Test
    @DisplayName("Однакові дати видачі та повернення — допустимо (1 день)")
    void sameLoanAndDueDate_isAccepted() {
        Loan l = new Loan(1, 1, "Book", 1, "Reader",
                LOAN_DATE, LOAN_DATE, "active");
        assertEquals(l.getLoanDate(), l.getDueDate());
    }
}
