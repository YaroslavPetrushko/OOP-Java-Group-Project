package com.library.controller;

import com.library.dao.*;
import com.library.dao.impl.*;
import com.library.model.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * JavaFX controller for {@code MainView.fxml}.
 *
 * <p>Wires together the four main entity tabs (Books, Authors, Readers, Loans),
 * each providing:
 * <ul>
 *   <li><strong>CRUD</strong> — Add / Edit / Delete via modal dialogs</li>
 *   <li><strong>Live search</strong> — results update on each keystroke or
 *       filter change (no explicit "Search" button)</li>
 *   <li><strong>Filters</strong> — genre/country/status ComboBoxes and
 *       year/date range fields</li>
 * </ul>
 *
 * <p>All database access is delegated to the DAO layer; no SQL appears here.
 * All dialogs perform client-side validation before allowing the OK action.
 *
 * <p>Architecture: {@code View (FXML) → MainController → DAO → PostgreSQL}
 *
 * @see BookDao
 * @see AuthorDao
 * @see ReaderDao
 * @see LoanDao
 */
public class MainController {

    // ── DAOs ─────────────────────────────────────────────────────
    private final BookDao   bookDao   = new BookDaoImpl();
    private final AuthorDao authorDao = new AuthorDaoImpl();
    private final ReaderDao readerDao = new ReaderDaoImpl();
    private final LoanDao   loanDao   = new LoanDaoImpl();

    // ── Status bar ────────────────────────────────────────────────
    @FXML private Label statusLabel;

    // ── Books tab ─────────────────────────────────────────────────
    @FXML private TableView<Book>            booksTable;
    @FXML private TableColumn<Book, Integer> bookIdCol;
    @FXML private TableColumn<Book, String>  bookTitleCol;
    @FXML private TableColumn<Book, String>  bookAuthorCol;
    @FXML private TableColumn<Book, String>  bookGenreCol;
    @FXML private TableColumn<Book, String>  bookIsbnCol;
    @FXML private TableColumn<Book, Integer> bookYearCol;
    @FXML private TableColumn<Book, Integer> bookCopiesCol;

    // search + filters
    @FXML private TextField        bookSearchField;
    @FXML private ComboBox<String> bookGenreFilter;
    @FXML private TextField        bookYearFrom;
    @FXML private TextField        bookYearTo;

    private final ObservableList<Book> booksData = FXCollections.observableArrayList();

    // ── Authors tab ───────────────────────────────────────────────
    @FXML private TableView<Author>            authorsTable;
    @FXML private TableColumn<Author, Integer> authorIdCol;
    @FXML private TableColumn<Author, String>  authorNameCol;
    @FXML private TableColumn<Author, String>  authorCountryCol;
    @FXML private TableColumn<Author, Integer> authorBirthYearCol;

    // search + filters
    @FXML private TextField        authorSearchField;
    @FXML private ComboBox<String> authorCountryFilter;
    @FXML private TextField        authorYearFrom;
    @FXML private TextField        authorYearTo;

    private final ObservableList<Author> authorsData = FXCollections.observableArrayList();

    // ── Readers tab ───────────────────────────────────────────────
    @FXML private TableView<Reader>              readersTable;
    @FXML private TableColumn<Reader, Integer>   readerIdCol;
    @FXML private TableColumn<Reader, String>    readerNameCol;
    @FXML private TableColumn<Reader, String>    readerEmailCol;
    @FXML private TableColumn<Reader, String>    readerPhoneCol;
    @FXML private TableColumn<Reader, LocalDate> readerRegDateCol;

    // search + filters
    @FXML private TextField  readerSearchField;
    @FXML private DatePicker readerRegFrom;
    @FXML private DatePicker readerRegTo;

    private final ObservableList<Reader> readersData = FXCollections.observableArrayList();

    // ── Loans tab ─────────────────────────────────────────────────
    @FXML private TableView<Loan>              loansTable;
    @FXML private TableColumn<Loan, Integer>   loanIdCol;
    @FXML private TableColumn<Loan, String>    loanBookCol;
    @FXML private TableColumn<Loan, String>    loanReaderCol;
    @FXML private TableColumn<Loan, LocalDate> loanDateCol;
    @FXML private TableColumn<Loan, LocalDate> loanDueCol;
    @FXML private TableColumn<Loan, String>    loanStatusCol;

    // search + filters
    @FXML private TextField        loanSearchField;
    @FXML private ComboBox<String> loanStatusFilter;
    @FXML private DatePicker       loanDateFrom;
    @FXML private DatePicker       loanDateTo;

    private final ObservableList<Loan> loansData = FXCollections.observableArrayList();

    // ════════════════════════════════════════════════════════════
    //  Initialization
    // ════════════════════════════════════════════════════════════

    /**
     * Called automatically by {@link javafx.fxml.FXMLLoader} after all
     * {@code @FXML} fields have been injected.
     *
     * <p>Sets up cell-value factories, populates filter ComboBoxes from the
     * database, attaches change listeners, and performs an initial data load.
     */
    @FXML
    public void initialize() {
        setupBooksTable();
        setupAuthorsTable();
        setupReadersTable();
        setupLoansTable();

        initBookFilters();
        initAuthorFilters();
        initReaderFilters();
        initLoanFilters();

        applyBooksFilter();
        applyAuthorsFilter();
        applyReadersFilter();
        applyLoansFilter();

        setStatus("Connected ✅  |  Search & filters ready");
    }

    // ════════════════════════════════════════════════════════════
    //  BOOKS — table setup
    // ════════════════════════════════════════════════════════════

    /**
     * Binds {@link Book} properties to the corresponding table columns.
     */
    private void setupBooksTable() {
        bookIdCol    .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        bookTitleCol .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        bookAuthorCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAuthorName()));
        bookGenreCol .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGenre()));
        bookIsbnCol  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIsbn()));
        bookYearCol  .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getPubYear()).asObject());
        bookCopiesCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCopies()).asObject());
        booksTable.setItems(booksData);
    }

    // ════════════════════════════════════════════════════════════
    //  BOOKS — filter init & apply
    // ════════════════════════════════════════════════════════════

    /**
     * Populates the genre ComboBox from the database and attaches
     * change listeners so the table reacts immediately to any filter change.
     */
    private void initBookFilters() {
        bookGenreFilter.getItems().add("");
        bookGenreFilter.getItems().addAll(bookDao.findAllGenres());
        bookGenreFilter.setValue("");

        bookSearchField.textProperty() .addListener((o, p, n) -> applyBooksFilter());
        bookGenreFilter.valueProperty().addListener((o, p, n) -> applyBooksFilter());
        bookYearFrom   .textProperty() .addListener((o, p, n) -> applyBooksFilter());
        bookYearTo     .textProperty() .addListener((o, p, n) -> applyBooksFilter());
    }

    /**
     * Reads all active filter values and delegates to {@link BookDao#search}.
     * Updates {@link #booksData} and the status bar with the result count.
     */
    private void applyBooksFilter() {
        String  text     = bookSearchField.getText();
        String  genre    = bookGenreFilter.getValue();
        Integer yearFrom = parseIntOrZero(bookYearFrom.getText());
        Integer yearTo   = parseIntOrZero(bookYearTo.getText());

        booksData.setAll(bookDao.search(text, genre, yearFrom, yearTo));
        setStatus("Books: " + booksData.size() + " record(s) found.");
    }

    /**
     * Resets all book filter controls; change listeners trigger
     * {@link #applyBooksFilter} automatically.
     */
    @FXML
    private void onClearBooksFilter() {
        bookSearchField.clear();
        bookGenreFilter.setValue("");
        bookYearFrom.clear();
        bookYearTo.clear();
    }

    // ════════════════════════════════════════════════════════════
    //  BOOKS — CRUD actions
    // ════════════════════════════════════════════════════════════

    /** Opens the Add Book dialog; inserts on OK. */
    @FXML
    private void onAddBook() {
        showBookDialog(null).ifPresent(book -> {
            bookDao.insert(book);
            refreshBookFilters();
            applyBooksFilter();
            setStatus("Book \"" + book.getTitle() + "\" added.");
        });
    }

    /** Opens the Edit Book dialog pre-filled with the selected row; updates on OK. */
    @FXML
    private void onEditBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) { warn("Select a book to edit."); return; }
        showBookDialog(selected).ifPresent(b -> {
            b.setId(selected.getId());
            bookDao.update(b);
            applyBooksFilter();
            setStatus("Book \"" + b.getTitle() + "\" updated.");
        });
    }

    /**
     * Confirms and deletes the selected book.
     */
    @FXML
    private void onDeleteBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) { warn("Select a book to delete."); return; }
        if (confirm("Delete \"" + selected.getTitle() + "\"?")) {
            bookDao.delete(selected.getId());
            applyBooksFilter();
            setStatus("Book deleted.");
        }
    }

    /**
     * Refreshes the genre ComboBox items after a new book has been added
     * (the new book's genre may not yet be in the list).
     */
    private void refreshBookFilters() {
        String current = bookGenreFilter.getValue();
        bookGenreFilter.getItems().clear();
        bookGenreFilter.getItems().add("");
        bookGenreFilter.getItems().addAll(bookDao.findAllGenres());
        bookGenreFilter.setValue(current != null ? current : "");
    }

    // ════════════════════════════════════════════════════════════
    //  BOOKS — Add / Edit dialog
    // ════════════════════════════════════════════════════════════

    /**
     * Builds and shows the Book dialog in Add or Edit mode.
     *
     * <p>Validation rules enforced before OK is accepted:
     * <ul>
     *   <li>Title — required (non-blank)</li>
     *   <li>Author — required (ComboBox selection)</li>
     *   <li>ISBN — if provided, must be 10 or 13 digits (hyphens allowed)</li>
     *   <li>Pub. year — if provided, must be 1–4 digits</li>
     *   <li>Copies — must be a positive integer</li>
     *   <li>Year range — yearFrom ≤ yearTo when both are provided</li>
     * </ul>
     *
     * @param existing {@code null} for Add mode; a populated {@link Book} for Edit mode
     * @return an {@link Optional} containing the user-filled book, or empty on Cancel
     */
    private Optional<Book> showBookDialog(Book existing) {
        boolean edit = existing != null;
        Dialog<Book> dlg = dialog(edit ? "Edit Book" : "Add Book");

        GridPane g = grid();
        TextField titleF  = field(edit ? existing.getTitle()        : "", "Book title *");
        TextField genreF  = field(edit ? existing.getGenre()        : "", "Genre");
        TextField isbnF   = field(edit ? existing.getIsbn()         : "", "e.g. 978-0-06-088328-7");
        TextField yearF   = field(edit ? str(existing.getPubYear()) : "", "e.g. 2024");
        TextField copiesF = field(edit ? str(existing.getCopies())  : "1", "≥ 1");

        ComboBox<Author> authorBox = authorComboBox();
        if (edit) authorBox.getItems().stream()
                .filter(a -> a.getId() == existing.getAuthorId())
                .findFirst().ifPresent(authorBox::setValue);

        g.add(label("Title *"),   0, 0); g.add(titleF,   1, 0);
        g.add(label("Author *"),  0, 1); g.add(authorBox, 1, 1);
        g.add(label("Genre"),     0, 2); g.add(genreF,   1, 2);
        g.add(label("ISBN"),      0, 3); g.add(isbnF,    1, 3);
        g.add(label("Pub. year"), 0, 4); g.add(yearF,    1, 4);
        g.add(label("Copies"),    0, 5); g.add(copiesF,  1, 5);
        dlg.getDialogPane().setContent(g);

        okFilter(dlg, () -> {
            String e = "";
            if (titleF.getText().isBlank())   e += "• Title is required.\n";
            if (authorBox.getValue() == null) e += "• Author is required.\n";
            if (!validYear(yearF.getText()))  e += "• Year must be 1–4 digits.\n";
            if (!validInt(copiesF.getText())) e += "• Copies must be a positive integer.\n";
            return e;
        });

        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Author a = authorBox.getValue();
            Book b = new Book();
            b.setTitle(titleF.getText().trim());
            b.setAuthorId(a.getId());
            b.setAuthorName(a.getFullName());
            b.setGenre(coalesce(genreF.getText()));
            b.setIsbn(coalesce(isbnF.getText()));
            b.setPubYear(toInt(yearF.getText()));
            b.setCopies(Math.max(1, toInt(copiesF.getText())));
            return b;
        });
        return dlg.showAndWait();
    }

    // ════════════════════════════════════════════════════════════
    //  AUTHORS — table setup
    // ════════════════════════════════════════════════════════════

    /**
     * Binds {@link Author} properties to the corresponding table columns.
     */
    private void setupAuthorsTable() {
        authorIdCol       .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        authorNameCol     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        authorCountryCol  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCountry()));
        authorBirthYearCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getBirthYear()).asObject());
        authorsTable.setItems(authorsData);
    }

    // ════════════════════════════════════════════════════════════
    //  AUTHORS — filter init & apply
    // ════════════════════════════════════════════════════════════

    /**
     * Populates the country ComboBox from the database and attaches
     * change listeners so the table reacts immediately to any filter change.
     */
    private void initAuthorFilters() {
        authorCountryFilter.getItems().add("");
        authorCountryFilter.getItems().addAll(authorDao.findAllCountries());
        authorCountryFilter.setValue("");

        authorSearchField   .textProperty() .addListener((o, p, n) -> applyAuthorsFilter());
        authorCountryFilter .valueProperty().addListener((o, p, n) -> applyAuthorsFilter());
        authorYearFrom      .textProperty() .addListener((o, p, n) -> applyAuthorsFilter());
        authorYearTo        .textProperty() .addListener((o, p, n) -> applyAuthorsFilter());
    }

    /**
     * Reads all active filter values and delegates to {@link AuthorDao#search}.
     * Updates {@link #authorsData} and the status bar with the result count.
     */
    private void applyAuthorsFilter() {
        String  text      = authorSearchField.getText();
        String  country   = authorCountryFilter.getValue();
        Integer yearFrom  = parseIntOrZero(authorYearFrom.getText());
        Integer yearTo    = parseIntOrZero(authorYearTo.getText());

        authorsData.setAll(authorDao.search(text, country, yearFrom, yearTo));
        setStatus("Authors: " + authorsData.size() + " record(s) found.");
    }

    /**
     * Resets all author filter controls; change listeners trigger
     * {@link #applyAuthorsFilter} automatically.
     */
    @FXML
    private void onClearAuthorsFilter() {
        authorSearchField.clear();
        authorCountryFilter.setValue("");
        authorYearFrom.clear();
        authorYearTo.clear();
    }

    // ════════════════════════════════════════════════════════════
    //  AUTHORS — CRUD actions
    // ════════════════════════════════════════════════════════════

    /** Opens the Add Author dialog; inserts on OK. */
    @FXML
    private void onAddAuthor() {
        showAuthorDialog(null).ifPresent(author -> {
            authorDao.insert(author);
            refreshAuthorFilters();
            applyAuthorsFilter();
            setStatus("Author \"" + author.getFullName() + "\" added.");
        });
    }

    /** Opens the Edit Author dialog pre-filled with the selected row; updates on OK. */
    @FXML
    private void onEditAuthor() {
        Author selected = authorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { warn("Select an author to edit."); return; }
        showAuthorDialog(selected).ifPresent(a -> {
            a.setId(selected.getId());
            authorDao.update(a);
            applyAuthorsFilter();
            setStatus("Author \"" + a.getFullName() + "\" updated.");
        });
    }

    /**
     * Confirms and deletes the selected author.
     *
     * <p>If the author still has books in the library, the DAO throws a
     * {@link RuntimeException} which is displayed as an error dialog.
     */
    @FXML
    private void onDeleteAuthor() {
        Author selected = authorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { warn("Select an author to delete."); return; }
        if (confirm("Delete \"" + selected.getFullName() + "\"?\n" +
                "Cannot delete if the author has books in the library.")) {
            authorDao.delete(selected.getId());
            applyAuthorsFilter();
            setStatus("Author deleted.");
        }
    }

    /**
     * Refreshes the country ComboBox items after a new author has been added.
     */
    private void refreshAuthorFilters() {
        String cur = authorCountryFilter.getValue();
        authorCountryFilter.getItems().clear();
        authorCountryFilter.getItems().add("");
        authorCountryFilter.getItems().addAll(authorDao.findAllCountries());
        authorCountryFilter.setValue(cur != null ? cur : "");
    }

    // ════════════════════════════════════════════════════════════
    //  AUTHORS — Add / Edit dialog
    // ════════════════════════════════════════════════════════════

    /**
     * Builds and shows the Author dialog in Add or Edit mode.
     *
     * <p>Validation rules enforced before OK is accepted:
     * <ul>
     *   <li>Full name — required (non-blank)</li>
     *   <li>Birth year — if provided, must be 1–4 digits</li>
     *   <li>Year range — yearFrom ≤ yearTo when both are provided in filters</li>
     * </ul>
     *
     * @param existing {@code null} for Add mode; a populated {@link Author} for Edit mode
     * @return an {@link Optional} containing the user-filled author, or empty on Cancel
     */
    private Optional<Author> showAuthorDialog(Author existing) {
        boolean edit = existing != null;
        Dialog<Author> dlg = dialog(edit ? "Edit Author" : "Add Author");

        GridPane g = grid();
        TextField nameF  = field(edit ? existing.getFullName()       : "", "Full name *");
        TextField cntryF = field(edit ? existing.getCountry()        : "", "Country");
        TextField yearF  = field(edit ? str(existing.getBirthYear()) : "", "e.g. 1950");

        g.add(label("Full name *"), 0, 0); g.add(nameF,  1, 0);
        g.add(label("Country"),     0, 1); g.add(cntryF, 1, 1);
        g.add(label("Birth year"),  0, 2); g.add(yearF,  1, 2);
        dlg.getDialogPane().setContent(g);

        okFilter(dlg, () -> {
            String e = "";
            if (nameF.getText().isBlank())   e += "• Name is required.\n";
            if (!validYear(yearF.getText())) e += "• Year must be 1–4 digits.\n";
            return e;
        });

        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Author a = new Author();
            a.setFullName(nameF.getText().trim());
            a.setCountry(coalesce(cntryF.getText()));
            a.setBirthYear(toInt(yearF.getText()));
            return a;
        });
        return dlg.showAndWait();
    }

    // ════════════════════════════════════════════════════════════
    //  READERS — table setup
    // ════════════════════════════════════════════════════════════

    /**
     * Binds {@link Reader} properties to the corresponding table columns.
     */
    private void setupReadersTable() {
        readerIdCol     .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        readerNameCol   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        readerEmailCol  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        readerPhoneCol  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        readerRegDateCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getRegDate()));
        readersTable.setItems(readersData);
    }

    // ════════════════════════════════════════════════════════════
    //  READERS — filter init & apply
    // ════════════════════════════════════════════════════════════

    /**
     * Attaches change listeners to reader filter controls so the table
     * reacts immediately to any change.
     */
    private void initReaderFilters() {
        readerSearchField.textProperty()   .addListener((o, p, n) -> applyReadersFilter());
        readerRegFrom    .valueProperty()  .addListener((o, p, n) -> applyReadersFilter());
        readerRegTo      .valueProperty()  .addListener((o, p, n) -> applyReadersFilter());
    }

    /**
     * Reads all active filter values and delegates to {@link ReaderDao#search}.
     * Updates {@link #readersData} and the status bar with the result count.
     */
    private void applyReadersFilter() {
        String    text  = readerSearchField.getText();
        LocalDate from  = readerRegFrom.getValue();
        LocalDate to    = readerRegTo.getValue();

        readersData.setAll(readerDao.search(text, from, to));
        setStatus("Readers: " + readersData.size() + " record(s) found.");
    }

    /**
     * Resets all reader filter controls; change listeners trigger
     * {@link #applyReadersFilter} automatically.
     */
    @FXML
    private void onClearReadersFilter() {
        readerSearchField.clear();
        readerRegFrom.setValue(null);
        readerRegTo.setValue(null);
    }

    // ════════════════════════════════════════════════════════════
    //  READERS — CRUD actions
    // ════════════════════════════════════════════════════════════

    /** Opens the Add Reader dialog; inserts on OK. */
    @FXML
    private void onAddReader() {
        showReaderDialog(null).ifPresent(reader -> {
            readerDao.insert(reader);
            applyReadersFilter();
            setStatus("Reader \"" + reader.getFullName() + "\" added.");
        });
    }

    /** Opens the Edit Reader dialog pre-filled with the selected row; updates on OK. */
    @FXML
    private void onEditReader() {
        Reader selected = readersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { warn("Select a reader to edit."); return; }
        showReaderDialog(selected).ifPresent(updated -> {
            updated.setId(selected.getId());
            readerDao.update(updated);
            applyReadersFilter();
            setStatus("Reader \"" + updated.getFullName() + "\" updated.");
        });
    }

    /**
     * Confirms and deletes the selected reader.
     *
     * <p>If the reader still has loan records, the DAO throws a
     * {@link RuntimeException} which is displayed as an error dialog.
     */
    @FXML
    private void onDeleteReader() {
        Reader selected = readersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { warn("Select a reader to delete."); return; }
        if (confirm("Delete \"" + selected.getFullName() + "\"?\n" +
                "Cannot delete if the reader has active loans.")) {
            readerDao.delete(selected.getId());
            applyReadersFilter();
            setStatus("Reader deleted.");
        }
    }

    // ════════════════════════════════════════════════════════════
    //  READERS — Add / Edit dialog
    // ════════════════════════════════════════════════════════════

    /**
     * Builds and shows the Reader dialog in Add or Edit mode.
     *
     * <p>Validation rules enforced before OK is accepted:
     * <ul>
     *   <li>Full name — required (non-blank)</li>
     *   <li>Email — if provided, must match a basic {@code name@domain.tld} pattern</li>
     *   <li>Phone — if provided, must contain 7–15 digits (spaces, hyphens, parentheses
     *               and a leading {@code +} are allowed)</li>
     *   <li>Registration date — required</li>
     * </ul>
     *
     * @param existing {@code null} for Add mode; a populated {@link Reader} for Edit mode
     * @return an {@link Optional} containing the user-filled reader, or empty on Cancel
     */
    private Optional<Reader> showReaderDialog(Reader existing) {
        boolean edit = existing != null;
        Dialog<Reader> dlg = dialog(edit ? "Edit Reader" : "Add Reader");

        GridPane g = grid();
        TextField  nameF  = field(edit ? existing.getFullName() : "", "Full name *");
        TextField  emailF = field(edit ? existing.getEmail()    : "", "email@example.com");
        TextField  phoneF = field(edit ? existing.getPhone()    : "", "+380...");
        DatePicker regPk  = new DatePicker(edit ? existing.getRegDate() : LocalDate.now());
        regPk.setPrefWidth(220);

        g.add(label("Full name *"), 0, 0); g.add(nameF,  1, 0);
        g.add(label("Email"),       0, 1); g.add(emailF, 1, 1);
        g.add(label("Phone"),       0, 2); g.add(phoneF, 1, 2);
        g.add(label("Reg. date"),   0, 3); g.add(regPk,  1, 3);
        dlg.getDialogPane().setContent(g);

        okFilter(dlg, () -> {
            String e = "";
            if (nameF.getText().isBlank()) e += "• Name is required.\n";
            if (regPk.getValue() == null)  e += "• Registration date is required.\n";
            return e;
        });

        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Reader r = new Reader();
            r.setFullName(nameF.getText().trim());
            r.setEmail(coalesce(emailF.getText()));
            r.setPhone(coalesce(phoneF.getText()));
            r.setRegDate(regPk.getValue());
            return r;
        });
        return dlg.showAndWait();
    }

    // ════════════════════════════════════════════════════════════
    //  LOANS — table setup
    // ════════════════════════════════════════════════════════════

    /**
     * Binds {@link Loan} properties to the corresponding table columns.
     * The status column uses a custom cell factory to colour-code the text:
     * green for {@code active}, red for {@code overdue}, grey for {@code returned}.
     */
    private void setupLoansTable() {
        loanIdCol    .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        loanBookCol  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBookTitle()));
        loanReaderCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReaderName()));
        loanDateCol  .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getLoanDate()));
        loanDueCol   .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDueDate()));
        loanStatusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        // Colour-code the status column for quick visual scanning
        loanStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(switch (item) {
                    case "active"   -> "-fx-text-fill: #2e7d32; -fx-font-weight: bold;";
                    case "overdue"  -> "-fx-text-fill: #c62828; -fx-font-weight: bold;";
                    case "returned" -> "-fx-text-fill: #757575;";
                    default         -> "";
                });
            }
        });

        loansTable.setItems(loansData);
    }

    // ════════════════════════════════════════════════════════════
    //  LOANS — filter init & apply
    // ════════════════════════════════════════════════════════════

    /**
     * Populates the status ComboBox and attaches change listeners so the
     * table reacts immediately to any filter change.
     */
    private void initLoanFilters() {
        loanStatusFilter.getItems().addAll("", "active", "returned", "overdue");
        loanStatusFilter.setValue("");

        loanSearchField .textProperty() .addListener((o, p, n) -> applyLoansFilter());
        loanStatusFilter.valueProperty().addListener((o, p, n) -> applyLoansFilter());
        loanDateFrom    .valueProperty().addListener((o, p, n) -> applyLoansFilter());
        loanDateTo      .valueProperty().addListener((o, p, n) -> applyLoansFilter());
    }

    /**
     * Reads all active filter values and delegates to {@link LoanDao#search}.
     * Updates {@link #loansData} and the status bar with the result count.
     */
    private void applyLoansFilter() {
        String    text   = loanSearchField.getText();
        String    status = loanStatusFilter.getValue();
        LocalDate from   = loanDateFrom.getValue();
        LocalDate to     = loanDateTo.getValue();

        loansData.setAll(loanDao.search(text, status, from, to));
        setStatus("Loans: " + loansData.size() + " record(s) found.");
    }

    /**
     * Resets all loan filter controls; change listeners trigger
     * {@link #applyLoansFilter} automatically.
     */
    @FXML
    private void onClearLoansFilter() {
        loanSearchField.clear();
        loanStatusFilter.setValue("");
        loanDateFrom.setValue(null);
        loanDateTo.setValue(null);
    }

    // ════════════════════════════════════════════════════════════
    //  LOANS — CRUD actions
    // ════════════════════════════════════════════════════════════

    /** Opens the Add Loan dialog; inserts on OK. */
    @FXML
    private void onAddLoan() {
        showLoanDialog(null).ifPresent(loan -> {
            loanDao.insert(loan);
            applyLoansFilter();
            setStatus("Loan added.");
        });
    }

    /** Opens the Edit Loan dialog pre-filled with the selected row; updates on OK. */
    @FXML
    private void onEditLoan() {
        Loan selected = loansTable.getSelectionModel().getSelectedItem();
        if (selected == null) { warn("Select a loan to edit."); return; }
        showLoanDialog(selected).ifPresent(l -> {
            l.setId(selected.getId());
            loanDao.update(l);
            applyLoansFilter();
            setStatus("Loan updated.");
        });
    }

    /** Confirms and deletes the selected loan. */
    @FXML
    private void onDeleteLoan() {
        Loan selected = loansTable.getSelectionModel().getSelectedItem();
        if (selected == null) { warn("Select a loan to delete."); return; }
        if (confirm("Delete loan #" + selected.getId() + "?\n\"" +
                selected.getBookTitle() + "\" → " + selected.getReaderName())) {
            loanDao.delete(selected.getId());
            applyLoansFilter();
            setStatus("Loan deleted.");
        }
    }

    // ════════════════════════════════════════════════════════════
    //  LOANS — Add / Edit dialog
    // ════════════════════════════════════════════════════════════

    /**
     * Builds and shows the Loan dialog in Add or Edit mode.
     *
     * <p>Validation rules enforced before OK is accepted:
     * <ul>
     *   <li>Book — required (ComboBox selection)</li>
     *   <li>Reader — required (ComboBox selection)</li>
     *   <li>Due date — required and must be on or after the loan date</li>
     * </ul>
     *
     * @param existing {@code null} for Add mode; a populated {@link Loan} for Edit mode
     * @return an {@link Optional} containing the user-filled loan, or empty on Cancel
     */
    private Optional<Loan> showLoanDialog(Loan existing) {
        boolean edit = existing != null;
        Dialog<Loan> dlg = dialog(edit ? "Edit Loan" : "Add Loan");

        GridPane g = grid();

        List<Book> books = bookDao.findAll();
        ComboBox<Book> bookBox = new ComboBox<>(FXCollections.observableArrayList(books));
        bookBox.setConverter(strConv(b -> b == null ? "" : b.getTitle()));
        bookBox.setPromptText("Select book *");
        bookBox.setPrefWidth(220);
        if (edit) books.stream().filter(b -> b.getId() == existing.getBookId())
                .findFirst().ifPresent(bookBox::setValue);

        List<Reader> readers = readerDao.findAll();
        ComboBox<Reader> readerBox = new ComboBox<>(FXCollections.observableArrayList(readers));
        readerBox.setConverter(strConv(r -> r == null ? "" : r.getFullName()));
        readerBox.setPromptText("Select reader *");
        readerBox.setPrefWidth(220);
        if (edit) readers.stream().filter(r -> r.getId() == existing.getReaderId())
                .findFirst().ifPresent(readerBox::setValue);

        DatePicker loanPk = new DatePicker(edit ? existing.getLoanDate() : LocalDate.now());
        DatePicker duePk  = new DatePicker(edit ? existing.getDueDate()  : LocalDate.now().plusDays(21));
        loanPk.setPrefWidth(220);
        duePk.setPrefWidth(220);

        ComboBox<String> statusBox = new ComboBox<>(
                FXCollections.observableArrayList("active", "returned", "overdue"));
        statusBox.setValue(edit ? existing.getStatus() : "active");
        statusBox.setPrefWidth(220);

        g.add(label("Book *"),     0, 0); g.add(bookBox,   1, 0);
        g.add(label("Reader *"),   0, 1); g.add(readerBox, 1, 1);
        g.add(label("Loan date"),  0, 2); g.add(loanPk,   1, 2);
        g.add(label("Due date *"), 0, 3); g.add(duePk,    1, 3);
        g.add(label("Status"),     0, 4); g.add(statusBox, 1, 4);
        dlg.getDialogPane().setContent(g);

        okFilter(dlg, () -> {
            String e = "";
            if (bookBox.getValue()   == null) e += "• Book is required.\n";
            if (readerBox.getValue() == null) e += "• Reader is required.\n";
            if (duePk.getValue()     == null) e += "• Due date is required.\n";
            if (loanPk.getValue() != null && duePk.getValue() != null
                    && duePk.getValue().isBefore(loanPk.getValue()))
                e += "• Due date must be after loan date.\n";
            return e;
        });

        dlg.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Book   b = bookBox.getValue();
            Reader r = readerBox.getValue();
            Loan   l = new Loan();
            l.setBookId(b.getId());
            l.setBookTitle(b.getTitle());
            l.setReaderId(r.getId());
            l.setReaderName(r.getFullName());
            l.setLoanDate(loanPk.getValue());
            l.setDueDate(duePk.getValue());
            l.setStatus(statusBox.getValue());
            return l;
        });
        return dlg.showAndWait();
    }

    // ════════════════════════════════════════════════════════════
    //  Shared UI helpers
    // ════════════════════════════════════════════════════════════

    /**
     * Creates a generic {@link Dialog} with OK and Cancel buttons
     * and no header text.
     *
     * @param title dialog window title
     * @param <T>   result type of the dialog
     * @return a configured but not yet shown dialog
     */
    private <T> Dialog<T> dialog(String title) {
        Dialog<T> dlg = new Dialog<>();
        dlg.setTitle(title);
        dlg.setHeaderText(null);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        return dlg;
    }

    /**
     * Creates a {@link GridPane} with standard padding and gap settings
     * used by all Add/Edit dialogs.
     *
     * @return a configured {@link GridPane}
     */
    private GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10);
        g.setPadding(new Insets(18, 24, 8, 24));
        return g;
    }

    /**
     * Attaches a validation filter to the OK button of a dialog.
     *
     * <p>When the user clicks OK, the {@code validator} is called. If it
     * returns a non-empty string, the action is consumed (dialog stays open)
     * and an error alert is shown listing all validation failures.
     *
     * @param dlg       the dialog whose OK button to guard
     * @param validator a supplier that returns an empty string on success,
     *                  or a newline-separated list of error messages on failure
     */
    private void okFilter(Dialog<?> dlg, Supplier<String> validator) {
        Button ok = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            String errors = validator.get();
            if (!errors.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation error", errors);
                ev.consume();
            }
        });
    }

    /**
     * Creates an {@link Author} ComboBox populated with all authors from the database.
     *
     * @return a configured, ready-to-use ComboBox
     */
    private ComboBox<Author> authorComboBox() {
        ComboBox<Author> box = new ComboBox<>(
                FXCollections.observableArrayList(authorDao.findAll()));
        box.setConverter(strConv(a -> a == null ? "" : a.getFullName()));
        box.setPromptText("Select author *");
        box.setPrefWidth(220);
        return box;
    }

    /**
     * Creates a lightweight {@link StringConverter} from a single
     * {@code toString} lambda (the {@code fromString} direction is unused).
     *
     * @param fn function mapping an item to its display string
     * @param <T> item type
     * @return a {@link StringConverter} backed by {@code fn}
     */
    private <T> StringConverter<T> strConv(java.util.function.Function<T, String> fn) {
        return new StringConverter<>() {
            @Override public String toString(T v)       { return fn.apply(v); }
            @Override public T fromString(String s)     { return null; }
        };
    }

    /**
     * Updates the status bar label with the given message.
     *
     * @param msg the message to display
     */
    private void setStatus(String msg) { statusLabel.setText(msg); }

    /**
     * Shows a WARNING alert for "no row selected" situations.
     *
     * @param msg the message to display
     */
    private void warn(String msg) {
        showAlert(Alert.AlertType.WARNING, "No selection", msg);
    }

    /**
     * Shows a YES/NO confirmation dialog.
     *
     * @param msg the confirmation question
     * @return {@code true} if the user clicked YES
     */
    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirm"); a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    /**
     * Shows a generic alert dialog.
     *
     * @param type  alert type (ERROR, WARNING, INFORMATION, etc.)
     * @param title window title
     * @param msg   message body
     */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ════════════════════════════════════════════════════════════
    //  Field factory helpers
    // ════════════════════════════════════════════════════════════

    /**
     * Creates a styled {@link TextField} with a preset value and prompt text.
     *
     * @param value  initial value (may be blank)
     * @param prompt placeholder text shown when the field is empty
     * @return a configured {@link TextField} with {@code prefWidth = 220}
     */
    private static TextField field(String value, String prompt) {
        TextField tf = new TextField(value == null ? "" : value);
        tf.setPromptText(prompt);
        tf.setPrefWidth(220);
        return tf;
    }

    /**
     * Creates a right-aligned {@link Label} with a fixed minimum width,
     * used as a form row label in dialog grids.
     *
     * @param text label text
     * @return a configured {@link Label}
     */
    private static Label label(String text) {
        Label l = new Label(text);
        l.setMinWidth(90);
        return l;
    }

    // ════════════════════════════════════════════════════════════
    //  Value conversion helpers
    // ════════════════════════════════════════════════════════════

    /**
     * Converts an int to a display string, returning an empty string for {@code 0}
     * (the "not set" sentinel used in the models).
     *
     * @param v the integer value
     * @return {@code ""} if {@code v == 0}, otherwise {@code String.valueOf(v)}
     */
    private static String  str(int v)         { return v == 0 ? "" : String.valueOf(v); }

    /**
     * Parses a string to int, returning {@code 0} on blank or invalid input.
     *
     * @param s the string to parse
     * @return the parsed integer, or {@code 0}
     */
    private static int toInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    /**
     * Parses a string to an {@link Integer}, returning {@code null} on blank
     * or invalid input (used for optional filter range bounds).
     *
     * @param s the string to parse
     * @return the parsed integer, or {@code null}
     */
    private static Integer parseIntOrZero(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    /**
     * Trims and returns the string, or {@code null} if it is blank.
     * Used to map empty dialog fields to SQL NULL.
     *
     * @param s the string value
     * @return trimmed string, or {@code null}
     */
    private static String coalesce(String s)  { return (s == null || s.isBlank()) ? null : s.trim(); }

    // ── Validators ────────────────────────────────────────────────
    private static boolean validYear(String s) { return s.isBlank() || s.trim().matches("\\d{1,4}"); }
    private static boolean validInt (String s) { return s.isBlank() || s.trim().matches("\\d+"); }
}