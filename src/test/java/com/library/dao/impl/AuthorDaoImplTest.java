package com.library.dao.impl;

import com.library.model.Author;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.library.dao.impl.FakeJdbc.row;
import static org.junit.jupiter.api.Assertions.*;

class AuthorDaoImplTest {
    private FakeJdbc.Db db;
    private AuthorDaoImpl dao;

    @BeforeEach
    void setUp() {
        db = FakeJdbc.install();
        dao = new AuthorDaoImpl();
    }

    @AfterEach
    void tearDown() {
        FakeJdbc.uninstall();
    }

    private static Map<Object, Object> authorRow(int id, String name, String country, int birthYear) {
        return row(
                "id", id,
                "full_name", name,
                "country", country,
                "birth_year", birthYear
        );
    }

    @Test
    @DisplayName("findAll returns all authors from ResultSet")
    void findAll_returnsAllAuthors() {
        db.rows(
                authorRow(1, "Author A", "UA", 1800),
                authorRow(2, "Author B", "PL", 1900)
        );

        List<Author> result = dao.findAll();

        assertEquals(2, result.size());
        assertEquals("Author A", result.get(0).getFullName());
        assertEquals("Author B", result.get(1).getFullName());
    }

    @Test
    @DisplayName("findAll returns empty list when ResultSet is empty")
    void findAll_emptyResultSet_returnsEmptyList() {
        db.emptyRows();

        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    @DisplayName("findAll returns empty list on SQL exception")
    void findAll_sqlException_returnsEmptyList() {
        db.failQuery(new SQLException("DB error"));

        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    @DisplayName("findById returns Optional with author when found")
    void findById_found_returnsOptional() {
        db.rows(authorRow(1, "Taras Shevchenko", "Ukraine", 1814));

        Optional<Author> result = dao.findById(1);

        assertTrue(result.isPresent());
        Author author = result.get();
        assertEquals(1, author.getId());
        assertEquals("Taras Shevchenko", author.getFullName());
        assertEquals("Ukraine", author.getCountry());
        assertEquals(1814, author.getBirthYear());
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
    @DisplayName("findAllCountries returns distinct country list")
    void findAllCountries_returnsCountries() {
        db.rows(row(1, "Poland"), row(1, "Ukraine"));

        List<String> countries = dao.findAllCountries();

        assertEquals(List.of("Poland", "Ukraine"), countries);
    }

    @Test
    @DisplayName("findAllCountries returns empty list on SQL exception")
    void findAllCountries_sqlException_returnsEmpty() {
        db.failQuery(new SQLException("fail"));

        assertTrue(dao.findAllCountries().isEmpty());
    }

    @Test
    @DisplayName("search no filters returns all rows")
    void search_noFilters_returnsAll() {
        db.rows(authorRow(1, "Ivan Franko", "Ukraine", 1856));

        List<Author> result = dao.search(null, null, null, null);

        assertEquals(1, result.size());
        assertEquals("Ivan Franko", result.get(0).getFullName());
    }

    @Test
    @DisplayName("search text filter binds three LIKE params")
    void search_textFilter_bindsThreeParams() {
        db.emptyRows();

        dao.search("Shevchenko", null, null, null);

        assertEquals(3, db.lastStatement().countStringsContaining("Shevchenko"));
    }

    @Test
    @DisplayName("search country filter binds exact match param")
    void search_countryFilter_bindsExact() {
        db.emptyRows();

        dao.search(null, "Ukraine", null, null);

        assertTrue(db.lastStatement().hasStringValue("Ukraine"));
    }

    @Test
    @DisplayName("search year range binds two int params")
    void search_yearRange_bindsBothBounds() {
        db.emptyRows();

        dao.search(null, null, 1800, 1900);

        assertTrue(db.lastStatement().hasIntValue(1800));
        assertTrue(db.lastStatement().hasIntValue(1900));
    }

    @Test
    @DisplayName("search zero year values are ignored")
    void search_zeroYears_areIgnored() {
        db.emptyRows();

        dao.search(null, null, 0, 0);

        assertEquals(0, db.lastStatement().countMethod("setInt"));
    }

    @Test
    @DisplayName("search blank text is ignored")
    void search_blankText_isIgnored() {
        db.emptyRows();

        dao.search("   ", null, null, null);

        assertEquals(0, db.lastStatement().countMethod("setString"));
    }

    @Test
    @DisplayName("insert binds fullName, country, birthYear correctly")
    void insert_bindsAllParams() {
        dao.insert(new Author(0, "Lesia Ukrainka", "Ukraine", 1871));

        FakeJdbc.StatementCall call = db.lastStatement();
        assertTrue(call.hasSetString(1, "Lesia Ukrainka"));
        assertTrue(call.hasSetString(2, "Ukraine"));
        assertTrue(call.hasSetInt(3, 1871));
        assertTrue(call.updateExecuted());
    }

    @Test
    @DisplayName("insert null country sets SQL NULL")
    void insert_nullCountry_setsNull() {
        dao.insert(new Author(0, "Unknown Author", null, 0));

        assertTrue(db.lastStatement().hasSetNull(2, Types.VARCHAR));
        assertTrue(db.lastStatement().hasSetNull(3, Types.SMALLINT));
    }

    @Test
    @DisplayName("insert birthYear 0 sets SQL NULL")
    void insert_zeroBirthYear_setsNull() {
        dao.insert(new Author(0, "Some Author", "France", 0));

        assertTrue(db.lastStatement().hasSetNull(3, Types.SMALLINT));
    }

    @Test
    @DisplayName("insert SQL exception is swallowed")
    void insert_sqlException_doesNotThrow() {
        db.failUpdate(new SQLException("fail"));

        assertDoesNotThrow(() -> dao.insert(new Author(0, "Test", "UA", 1900)));
    }

    @Test
    @DisplayName("update binds all params including id at position 4")
    void update_bindsAllParams() {
        dao.update(new Author(7, "Updated Name", "Germany", 1950));

        FakeJdbc.StatementCall call = db.lastStatement();
        assertTrue(call.hasSetString(1, "Updated Name"));
        assertTrue(call.hasSetString(2, "Germany"));
        assertTrue(call.hasSetInt(3, 1950));
        assertTrue(call.hasSetInt(4, 7));
        assertTrue(call.updateExecuted());
    }

    @Test
    @DisplayName("update SQL exception is swallowed")
    void update_sqlException_doesNotThrow() {
        db.failUpdate(new SQLException("fail"));

        assertDoesNotThrow(() -> dao.update(new Author(1, "Name", "UA", 1900)));
    }

    @Test
    @DisplayName("delete binds id and executes update")
    void delete_executesUpdate() {
        dao.delete(42);

        assertTrue(db.lastStatement().hasSetInt(1, 42));
        assertTrue(db.lastStatement().updateExecuted());
    }

    @Test
    @DisplayName("delete SQL exception is swallowed")
    void delete_sqlException_doesNotThrow() {
        db.failUpdate(new SQLException("fail"));

        assertDoesNotThrow(() -> dao.delete(1));
    }
}
