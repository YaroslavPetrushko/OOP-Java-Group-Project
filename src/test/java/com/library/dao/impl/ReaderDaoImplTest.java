package com.library.dao.impl;

import com.library.model.Reader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.library.dao.impl.FakeJdbc.row;
import static org.junit.jupiter.api.Assertions.*;

class ReaderDaoImplTest {
    private static final LocalDate REG_DATE = LocalDate.of(2024, 3, 15);

    private FakeJdbc.Db db;
    private ReaderDaoImpl dao;

    @BeforeEach
    void setUp() {
        db = FakeJdbc.install();
        dao = new ReaderDaoImpl();
    }

    @AfterEach
    void tearDown() {
        FakeJdbc.uninstall();
    }

    private static Map<Object, Object> readerRow(int id, String fullName, String email,
                                                 String phone, LocalDate regDate) {
        return row(
                "id", id,
                "full_name", fullName,
                "email", email,
                "phone", phone,
                "reg_date", regDate == null ? null : Date.valueOf(regDate)
        );
    }

    @Test
    @DisplayName("findAll returns all readers from ResultSet")
    void findAll_returnsAllReaders() {
        db.rows(
                readerRow(1, "Reader A", "a@a.com", "111", REG_DATE),
                readerRow(2, "Reader B", "b@b.com", "222", REG_DATE)
        );

        List<Reader> result = dao.findAll();

        assertEquals(2, result.size());
        assertEquals("Reader A", result.get(0).getFullName());
        assertEquals("Reader B", result.get(1).getFullName());
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
    void findById_found_returnsReader() {
        db.rows(readerRow(1, "Ivan Petrenko", "ivan@example.com", "+380501234567", REG_DATE));

        Optional<Reader> result = dao.findById(1);

        assertTrue(result.isPresent());
        Reader reader = result.get();
        assertEquals(1, reader.getId());
        assertEquals("Ivan Petrenko", reader.getFullName());
        assertEquals("ivan@example.com", reader.getEmail());
        assertEquals("+380501234567", reader.getPhone());
        assertEquals(REG_DATE, reader.getRegDate());
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
    @DisplayName("mapRow null reg_date falls back to today")
    void mapRow_nullRegDate_fallsBackToToday() {
        db.rows(readerRow(1, "No Date", null, null, null));

        Optional<Reader> result = dao.findById(1);

        assertTrue(result.isPresent());
        assertNotNull(result.get().getRegDate());
        assertFalse(result.get().getRegDate().isAfter(LocalDate.now()));
    }

    @Test
    @DisplayName("search no filters returns all rows")
    void search_noFilters_returnsAll() {
        db.rows(readerRow(1, "Ivan Petrenko", "ivan@example.com", "+380501234567", REG_DATE));

        List<Reader> result = dao.search(null, null, null);

        assertEquals(1, result.size());
        assertEquals("Ivan Petrenko", result.get(0).getFullName());
    }

    @Test
    @DisplayName("search text filter binds three LIKE params")
    void search_textFilter_bindsThreeParams() {
        db.emptyRows();

        dao.search("Ivan", null, null);

        assertEquals(3, db.lastStatement().countStringsContaining("Ivan"));
    }

    @Test
    @DisplayName("search blank text is ignored")
    void search_blankText_isIgnored() {
        db.emptyRows();

        dao.search("  ", null, null);

        assertEquals(0, db.lastStatement().countMethod("setString"));
    }

    @Test
    @DisplayName("search date range binds both dates")
    void search_dateRange_bindsBoth() {
        db.emptyRows();
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        dao.search(null, from, to);

        assertTrue(db.lastStatement().hasDateValue(Date.valueOf(from)));
        assertTrue(db.lastStatement().hasDateValue(Date.valueOf(to)));
    }

    @Test
    @DisplayName("search null dates are not bound")
    void search_nullDates_notBound() {
        db.emptyRows();

        dao.search(null, null, null);

        assertEquals(0, db.lastStatement().countMethod("setDate"));
    }

    @Test
    @DisplayName("search all filters combined bind correct params")
    void search_allFilters_bindsAll() {
        db.emptyRows();
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        dao.search("Ivan", from, to);

        FakeJdbc.StatementCall call = db.lastStatement();
        assertEquals(3, call.countStringsContaining("Ivan"));
        assertTrue(call.hasDateValue(Date.valueOf(from)));
        assertTrue(call.hasDateValue(Date.valueOf(to)));
    }

    @Test
    @DisplayName("insert binds all params correctly")
    void insert_bindsAllParams() {
        dao.insert(new Reader(0, "Olena Kovalchuk", "olena@test.com", "+380661234567", REG_DATE));

        FakeJdbc.StatementCall call = db.lastStatement();
        assertTrue(call.hasSetString(1, "Olena Kovalchuk"));
        assertTrue(call.hasSetString(2, "olena@test.com"));
        assertTrue(call.hasSetString(3, "+380661234567"));
        assertTrue(call.hasSetDate(4, Date.valueOf(REG_DATE)));
        assertTrue(call.updateExecuted());
    }

    @Test
    @DisplayName("insert null email and phone set SQL NULL")
    void insert_nullEmailPhone_setsNull() {
        dao.insert(new Reader(0, "No Contact", null, null, REG_DATE));

        FakeJdbc.StatementCall call = db.lastStatement();
        assertTrue(call.hasSetNull(2, Types.VARCHAR));
        assertTrue(call.hasSetNull(3, Types.VARCHAR));
    }

    @Test
    @DisplayName("insert null regDate falls back to today")
    void insert_nullRegDate_fallsBackToToday() {
        dao.insert(new Reader(0, "Late Reg", null, null, null));

        assertEquals(1, db.lastStatement().countMethod("setDate"));
    }

    @Test
    @DisplayName("insert throws RuntimeException on SQL exception")
    void insert_sqlException_throwsRuntime() {
        db.failUpdate(new SQLException("constraint"));

        Reader reader = new Reader(0, "Test", null, null, REG_DATE);
        assertThrows(RuntimeException.class, () -> dao.insert(reader));
    }

    @Test
    @DisplayName("update binds all params including id at position 5")
    void update_bindsAllParams() {
        dao.update(new Reader(77, "Updated Name", "upd@test.com", "+11", REG_DATE));

        FakeJdbc.StatementCall call = db.lastStatement();
        assertTrue(call.hasSetString(1, "Updated Name"));
        assertTrue(call.hasSetString(2, "upd@test.com"));
        assertTrue(call.hasSetString(3, "+11"));
        assertTrue(call.hasSetDate(4, Date.valueOf(REG_DATE)));
        assertTrue(call.hasSetInt(5, 77));
        assertTrue(call.updateExecuted());
    }

    @Test
    @DisplayName("update throws RuntimeException on SQL exception")
    void update_sqlException_throwsRuntime() {
        db.failUpdate(new SQLException("fail"));

        Reader reader = new Reader(1, "Name", null, null, REG_DATE);
        assertThrows(RuntimeException.class, () -> dao.update(reader));
    }

    @Test
    @DisplayName("delete binds id and executes update")
    void delete_executesUpdate() {
        dao.delete(5);

        assertTrue(db.lastStatement().hasSetInt(1, 5));
        assertTrue(db.lastStatement().updateExecuted());
    }

    @Test
    @DisplayName("delete FK violation throws descriptive RuntimeException")
    void delete_fkViolation_throwsDescriptiveException() {
        db.failUpdate(new SQLException("fk violation", "23503"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> dao.delete(1));
        assertTrue(ex.getMessage().contains("Cannot delete this reader"));
    }

    @Test
    @DisplayName("delete non-FK SQL exception throws generic RuntimeException")
    void delete_genericSqlException_throwsRuntime() {
        db.failUpdate(new SQLException("some error", "99999"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> dao.delete(1));
        assertTrue(ex.getMessage().contains("Failed to delete reader"));
    }
}
