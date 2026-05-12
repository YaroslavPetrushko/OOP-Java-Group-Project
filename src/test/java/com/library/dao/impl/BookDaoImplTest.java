package com.library.dao.impl;

import com.library.model.Book;
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

class BookDaoImplTest {
    private FakeJdbc.Db db;
    private BookDaoImpl dao;

    @BeforeEach
    void setUp() {
        db = FakeJdbc.install();
        dao = new BookDaoImpl();
    }

    @AfterEach
    void tearDown() {
        FakeJdbc.uninstall();
    }

    private static Map<Object, Object> bookRow(int id, String title, int authorId, String authorName,
                                               String genre, String isbn, int pubYear, int copies) {
        return row(
                "id", id,
                "title", title,
                "author_id", authorId,
                "author_name", authorName,
                "genre", genre,
                "isbn", isbn,
                "pub_year", pubYear,
                "copies", copies
        );
    }

    @Test
    @DisplayName("findAll returns all books from ResultSet")
    void findAll_returnsAllBooks() {
        db.rows(
                bookRow(1, "Book A", 1, "Author A", "Fiction", "111", 2000, 3),
                bookRow(2, "Book B", 2, "Author B", "Drama", "222", 2010, 7)
        );

        List<Book> result = dao.findAll();

        assertEquals(2, result.size());
        assertEquals("Book A", result.get(0).getTitle());
        assertEquals("Book B", result.get(1).getTitle());
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
        db.failQuery(new SQLException("DB down"));

        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    @DisplayName("findById returns populated Optional when book found")
    void findById_found_returnsBook() {
        db.rows(bookRow(1, "Kobzar", 10, "Taras Shevchenko", "Poetry",
                "978-0000000001", 1840, 5));

        Optional<Book> result = dao.findById(1);

        assertTrue(result.isPresent());
        Book book = result.get();
        assertEquals(1, book.getId());
        assertEquals("Kobzar", book.getTitle());
        assertEquals(10, book.getAuthorId());
        assertEquals("Taras Shevchenko", book.getAuthorName());
        assertEquals("Poetry", book.getGenre());
        assertEquals("978-0000000001", book.getIsbn());
        assertEquals(1840, book.getPubYear());
        assertEquals(5, book.getCopies());
        assertTrue(db.lastStatement().hasSetInt(1, 1));
    }

    @Test
    @DisplayName("findById returns empty Optional when book not found")
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
    @DisplayName("findAllGenres returns distinct genre list")
    void findAllGenres_returnsGenres() {
        db.rows(row(1, "Fiction"), row(1, "Poetry"));

        List<String> genres = dao.findAllGenres();

        assertEquals(List.of("Fiction", "Poetry"), genres);
    }

    @Test
    @DisplayName("findAllGenres returns empty list on SQL exception")
    void findAllGenres_sqlException_returnsEmpty() {
        db.failQuery(new SQLException("fail"));

        assertTrue(dao.findAllGenres().isEmpty());
    }

    @Test
    @DisplayName("search no filters returns all rows")
    void search_noFilters_returnsAll() {
        db.rows(bookRow(1, "Kobzar", 1, "Shevchenko", "Poetry", "000", 1840, 2));

        List<Book> result = dao.search(null, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("search text filter binds three LIKE params (title, author, isbn)")
    void search_textFilter_bindsThreeLikeParams() {
        db.emptyRows();
        dao.search("Kobzar", null, null, null);
        assertEquals(3, db.lastStatement().countStringsContaining("Kobzar"));
    }

    @Test
    @DisplayName("search genre filter binds exact match param")
    void search_genreFilter_bindsExact() {
        db.emptyRows();

        dao.search(null, "Poetry", null, null);

        assertTrue(db.lastStatement().hasStringValue("Poetry"));
    }

    @Test
    @DisplayName("search year range binds both bounds")
    void search_yearRange_bindsBoth() {
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

        dao.search("  ", null, null, null);

        assertEquals(0, db.lastStatement().countMethod("setString"));
    }

    @Test
    @DisplayName("search all filters combined bind correct params")
    void search_allFilters_bindsAll() {
        db.emptyRows();

        dao.search("Kobzar", "Poetry", 1800, 1900);

        FakeJdbc.StatementCall call = db.lastStatement();
        assertEquals(3, call.countStringsContaining("Kobzar"));
        assertTrue(call.hasStringValue("Poetry"));
        assertTrue(call.hasIntValue(1800));
        assertTrue(call.hasIntValue(1900));
    }

    @Test
    @DisplayName("insert binds all params correctly")
    void insert_bindsAllParams() {
        dao.insert(new Book(0, "Test Book", 5, "Test Author", "Drama", "978-1", 2020, 3));

        FakeJdbc.StatementCall call = db.lastStatement();
        assertTrue(call.hasSetString(1, "Test Book"));
        assertTrue(call.hasSetInt(2, 5));
        assertTrue(call.hasSetString(3, "Drama"));
        assertTrue(call.hasSetString(4, "978-1"));
        assertTrue(call.hasSetInt(5, 2020));
        assertTrue(call.hasSetInt(6, 3));
        assertTrue(call.updateExecuted());
    }

    @Test
    @DisplayName("insert null genre and isbn set SQL NULL")
    void insert_nullGenreIsbn_setsNull() {
        dao.insert(new Book(0, "Title", 1, "Author", null, null, 0, 1));

        FakeJdbc.StatementCall call = db.lastStatement();
        assertTrue(call.hasSetNull(3, Types.VARCHAR));
        assertTrue(call.hasSetNull(4, Types.VARCHAR));
        assertTrue(call.hasSetNull(5, Types.SMALLINT));
    }

    @Test
    @DisplayName("insert throws RuntimeException on SQL exception")
    void insert_sqlException_throwsRuntime() {
        db.failUpdate(new SQLException("constraint"));

        Book book = new Book(0, "Bad Book", 1, "A", null, null, 0, 1);
        assertThrows(RuntimeException.class, () -> dao.insert(book));
    }

    @Test
    @DisplayName("update binds all params including id at position 7")
    void update_bindsAllParams() {
        dao.update(new Book(99, "Updated", 3, "Auth", "Fiction", "000-1", 2021, 10));

        FakeJdbc.StatementCall call = db.lastStatement();
        assertTrue(call.hasSetString(1, "Updated"));
        assertTrue(call.hasSetInt(2, 3));
        assertTrue(call.hasSetString(3, "Fiction"));
        assertTrue(call.hasSetString(4, "000-1"));
        assertTrue(call.hasSetInt(5, 2021));
        assertTrue(call.hasSetInt(6, 10));
        assertTrue(call.hasSetInt(7, 99));
        assertTrue(call.updateExecuted());
    }

    @Test
    @DisplayName("update throws RuntimeException on SQL exception")
    void update_sqlException_throwsRuntime() {
        db.failUpdate(new SQLException("fail"));

        Book book = new Book(1, "T", 1, "A", null, null, 0, 1);
        assertThrows(RuntimeException.class, () -> dao.update(book));
    }

    @Test
    @DisplayName("delete binds id and executes update")
    void delete_executesUpdate() {
        dao.delete(7);

        assertTrue(db.lastStatement().hasSetInt(1, 7));
        assertTrue(db.lastStatement().updateExecuted());
    }

    @Test
    @DisplayName("delete FK violation throws descriptive RuntimeException")
    void delete_fkViolation_throwsDescriptiveException() {
        db.failUpdate(new SQLException("fk violation", "23503"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> dao.delete(1));
        assertTrue(ex.getMessage().contains("Cannot delete this book"));
    }

    @Test
    @DisplayName("delete non-FK SQL exception throws generic RuntimeException")
    void delete_genericSqlException_throwsRuntime() {
        db.failUpdate(new SQLException("some error", "99999"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> dao.delete(1));
        assertTrue(ex.getMessage().contains("Failed to delete book"));
    }
}
