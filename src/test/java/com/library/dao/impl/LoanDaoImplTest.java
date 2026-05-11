package com.library.dao.impl;

import com.library.model.Loan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.library.dao.impl.FakeJdbc.row;
import static org.junit.jupiter.api.Assertions.*;

class LoanDaoImplTest {
    private static final LocalDate LOAN_DATE = LocalDate.of(2025, 1, 10);
    private static final LocalDate DUE_DATE = LocalDate.of(2025, 1, 31);

    private FakeJdbc.Db db;
    private LoanDaoImpl dao;

    @BeforeEach
    void setUp() {
        db = FakeJdbc.install();
        dao = new LoanDaoImpl();
    }

    @AfterEach
    void tearDown() {
        FakeJdbc.uninstall();
    }

    private static Map<Object, Object> loanRow(int id, int bookId, String bookTitle,
                                               int readerId, String readerName,
                                               LocalDate loanDate, LocalDate dueDate,
                                               String status) {
        return row(
                "id", id,
                "book_id", bookId,
                "book_title", bookTitle,
                "reader_id", readerId,
                "reader_name", readerName,
                "loan_date", Date.valueOf(loanDate),
                "due_date", Date.valueOf(dueDate),
                "status", status
        );
    }

    @Test
    @DisplayName("findAll returns all loans from ResultSet")
    void findAll_returnsAllLoans() {
        db.rows(
                loanRow(1, 1, "Book A", 1, "Reader A", LOAN_DATE, DUE_DATE, "active"),
                loanRow(2, 2, "Book B", 2, "Reader B", LOAN_DATE, DUE_DATE, "returned")
        );

        List<Loan> result = dao.findAll();

        assertEquals(2, result.size());
        assertEquals("Book A", result.get(0).getBookTitle());
        assertEquals("Book B", result.get(1).getBookTitle());
    }

    @Test
    @DisplayName("findAll returns empty list when ResultSet is empty")
    void findAll_emptyResultSet_returnsEmpty() {
        db.emptyRows();

        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    @DisplayName("findAll returns empty list on SQL exception")
    void findAll_sqlException_returnsEmpty() {
        db.failQuery(new SQLException("fail"));

        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    @DisplayName("findById returns populated Optional when found")
    void findById_found_returnsLoan() {
        db.rows(loanRow(1, 10, "Kobzar", 20, "Ivan Petrenko", LOAN_DATE, DUE_DATE, "active"));

        Optional<Loan> result = dao.findById(1);

        assertTrue(result.isPresent());
        Loan loan = result.get();
        assertEquals(1, loan.getId());
        assertEquals(10, loan.getBookId());
        assertEquals("Kobzar", loan.getBookTitle());
        assertEquals(20, loan.getReaderId());
        assertEquals("Ivan Petrenko", loan.getReaderName());
        assertEquals(LOAN_DATE, loan.getLoanDate());
        assertEquals(DUE_DATE, loan.getDueDate());
        assertEquals("active", loan.getStatus());
        assertTrue(db.lastStatement().hasSetInt(1, 1));
    }

    @Test
    @DisplayName("findById returns empty Optional when not found")
    void findById_notFound_returnsEmpty() {
        db.emptyRows();

        assertTrue(dao.findById(999).isEmpty());
    }

    @Test
    @DisplayName("findById returns empty Optional on SQL exception")
    void findById_sqlException_returnsEmpty() {
        db.failQuery(new SQLException("fail"));

        assertTrue(dao.findById(1).isEmpty());
    }

    @Test
    @DisplayName("search numeric text triggers exact ID match")
    void search_numericText_bindsIdParam() {
        db.emptyRows();

        dao.search("42", null, null, null);

        FakeJdbc.StatementCall call = db.lastStatement();
        assertTrue(call.hasIntValue(42));
        assertEquals(0, call.countMethod("setString"));
    }

    @Test
    @DisplayName("search non-numeric text triggers LIKE on title and reader")
    void search_textString_bindsLikeParams() {
        db.emptyRows();

        dao.search("Kobzar", null, null, null);

        assertEquals(2, db.lastStatement().countStringsContaining("Kobzar"));
    }

    @Test
    @DisplayName("search blank text is ignored")
    void search_blankText_isIgnored() {
        db.emptyRows();

        dao.search("   ", null, null, null);

        FakeJdbc.StatementCall call = db.lastStatement();
        assertEquals(0, call.countMethod("setString"));
        assertEquals(0, call.countMethod("setInt"));
    }

    @Test
    @DisplayName("search status filter binds exact match param")
    void search_statusFilter_bindsExact() {
        db.emptyRows();

        dao.search(null, "active", null, null);

        assertTrue(db.lastStatement().hasStringValue("active"));
    }

    @Test
    @DisplayName("search date range binds two Date params")
    void search_dateRange_bindsBothDates() {
        db.emptyRows();
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);

        dao.search(null, null, from, to);

        assertTrue(db.lastStatement().hasDateValue(Date.valueOf(from)));
        assertTrue(db.lastStatement().hasDateValue(Date.valueOf(to)));
    }

    @Test
    @DisplayName("search null dates are not bound")
    void search_nullDates_notBound() {
        db.emptyRows();

        dao.search(null, null, null, null);

        assertEquals(0, db.lastStatement().countMethod("setDate"));
    }

    @Test
    @DisplayName("search all filters combined bind correct params")
    void search_allFilters_bindsAll() {
        db.emptyRows();
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 6, 30);

        dao.search("Kobzar", "active", from, to);

        FakeJdbc.StatementCall call = db.lastStatement();
        assertEquals(2, call.countStringsContaining("Kobzar"));
        assertTrue(call.hasStringValue("active"));
        assertTrue(call.hasDateValue(Date.valueOf(from)));
        assertTrue(call.hasDateValue(Date.valueOf(to)));
    }

    @Test
    @DisplayName("insert binds all params correctly")
    void insert_bindsAllParams() {
        dao.insert(new Loan(0, 10, "Kobzar", 20, "Ivan", LOAN_DATE, DUE_DATE, "active"));

        FakeJdbc.StatementCall call = db.lastStatement();
        assertTrue(call.hasSetInt(1, 10));
        assertTrue(call.hasSetInt(2, 20));
        assertTrue(call.hasSetDate(3, Date.valueOf(LOAN_DATE)));
        assertTrue(call.hasSetDate(4, Date.valueOf(DUE_DATE)));
        assertTrue(call.hasSetString(5, "active"));
        assertTrue(call.updateExecuted());
    }

    @Test
    @DisplayName("insert throws RuntimeException on SQL exception")
    void insert_sqlException_throwsRuntime() {
        db.failUpdate(new SQLException("fail"));

        Loan loan = new Loan(0, 1, "B", 1, "R", LOAN_DATE, DUE_DATE, "active");
        assertThrows(RuntimeException.class, () -> dao.insert(loan));
    }

    @Test
    @DisplayName("update binds all params including id at position 6")
    void update_bindsAllParams() {
        dao.update(new Loan(55, 10, "Kobzar", 20, "Ivan", LOAN_DATE, DUE_DATE, "returned"));

        FakeJdbc.StatementCall call = db.lastStatement();
        assertTrue(call.hasSetInt(1, 10));
        assertTrue(call.hasSetInt(2, 20));
        assertTrue(call.hasSetDate(3, Date.valueOf(LOAN_DATE)));
        assertTrue(call.hasSetDate(4, Date.valueOf(DUE_DATE)));
        assertTrue(call.hasSetString(5, "returned"));
        assertTrue(call.hasSetInt(6, 55));
        assertTrue(call.updateExecuted());
    }

    @Test
    @DisplayName("update throws RuntimeException on SQL exception")
    void update_sqlException_throwsRuntime() {
        db.failUpdate(new SQLException("fail"));

        Loan loan = new Loan(1, 1, "B", 1, "R", LOAN_DATE, DUE_DATE, "active");
        assertThrows(RuntimeException.class, () -> dao.update(loan));
    }

    @Test
    @DisplayName("delete binds id and executes update")
    void delete_executesUpdate() {
        dao.delete(7);

        assertTrue(db.lastStatement().hasSetInt(1, 7));
        assertTrue(db.lastStatement().updateExecuted());
    }

    @Test
    @DisplayName("delete throws RuntimeException on SQL exception")
    void delete_sqlException_throwsRuntime() {
        db.failUpdate(new SQLException("fail"));

        assertThrows(RuntimeException.class, () -> dao.delete(1));
    }

    @Test
    @DisplayName("mapRow converts loanDate and dueDate to LocalDate")
    void mapRow_dates_convertedCorrectly() {
        db.rows(loanRow(1, 10, "Kobzar", 20, "Ivan Petrenko", LOAN_DATE, DUE_DATE, "active"));

        Optional<Loan> result = dao.findById(1);

        assertTrue(result.isPresent());
        assertEquals(LOAN_DATE, result.get().getLoanDate());
        assertEquals(DUE_DATE, result.get().getDueDate());
    }
}
