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

/**
 * Controller for MainView.fxml.
 *
 * Step 5 — Books CRUD.
 * Step 6 — Authors / Readers / Loans CRUD.
 * Step 7 — Multi-field search + filter bars for all four tabs.
 */
public class MainController {

    // ── DAOs ─────────────────────────────────────────────────────
    private final BookDao   bookDao   = new BookDaoImpl();
    private final AuthorDao authorDao = new AuthorDaoImpl();
    private final ReaderDao readerDao = new ReaderDaoImpl();
    private final LoanDao   loanDao   = new LoanDaoImpl();

    // ── Status bar ────────────────────────────────────────────────
    @FXML private Label statusLabel;

    // ── Books ─────────────────────────────────────────────────────
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

    // ── Authors ──────────────────────────────────────────
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

    // ── Readers ──────────────────────────────────────────
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

    // ── Loans ────────────────────────────────────────────
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
    //  Init
    // ════════════════════════════════════════════════════════════
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

        setStatus("Connected ✅  |  CRUD ready");
    }

    // ════════════════════════════════════════════════════════════
    //  Books — table setup
    // ════════════════════════════════════════════════════════════
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
    //  BOOKS — filters init & apply
    // ════════════════════════════════════════════════════════════
    private void initBookFilters() {
        // Genre ComboBox
        bookGenreFilter.getItems().add("");          // sentinel for "all"
        bookGenreFilter.getItems().addAll(bookDao.findAllGenres());
        bookGenreFilter.setValue("");

        // Listeners - react to changes
        bookSearchField.textProperty() .addListener((o, p, n) -> applyBooksFilter());
        bookGenreFilter.valueProperty().addListener((o, p, n) -> applyBooksFilter());
        bookYearFrom   .textProperty() .addListener((o, p, n) -> applyBooksFilter());
        bookYearTo     .textProperty() .addListener((o, p, n) -> applyBooksFilter());
    }

    // Read all books controls
    private void applyBooksFilter() {
        String text  = bookSearchField.getText();
        String genre = bookGenreFilter.getValue();
        Integer yearFrom = parseIntOrZero(bookYearFrom.getText());
        Integer yearTo = parseIntOrZero(bookYearTo.getText());

        booksData.setAll(bookDao.search(text, genre, yearFrom, yearTo));
        setStatus("Books: " + booksData.size() + " record(s) found.");
    }

    @FXML
    private void onClearBooksFilter() {
        bookSearchField.clear();
        bookGenreFilter.setValue("");
        bookYearFrom.clear();
        bookYearTo.clear();
    }

    // ════════════════════════════════════════════════════════════
    //  Books — CRUD actions
    // ════════════════════════════════════════════════════════════
    @FXML
    private void onAddBook() {
        showBookDialog(null).ifPresent(book -> {
            bookDao.insert(book);
            applyBooksFilter();
            setStatus("Book \"" + book.getTitle() + "\" added.");
        });
    }

    @FXML
    private void onEditBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No selection",
                    "Please select a book to edit.");
            return;
        }
        showBookDialog(selected).ifPresent(updated -> {
            updated.setId(selected.getId());
            bookDao.update(updated);
            applyBooksFilter();
            setStatus("Book \"" + updated.getTitle() + "\" updated.");
        });
    }

    @FXML
    private void onDeleteBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No selection",
                    "Please select a book to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selected.getTitle() + "\"?\nThis action cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm deletion");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                bookDao.delete(selected.getId());
                applyBooksFilter();
                setStatus("Book deleted.");
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    //  Books — Add / Edit dialog
    // ════════════════════════════════════════════════════════════
    /**
     * @param existing null → Add mode; non-null → Edit mode (pre-fills fields)
     */

    //refactor
    private Optional<Book> showBookDialog(Book existing) {
        boolean isEdit = existing != null;

        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Book" : "Add Book");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        // ── Form ─────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(18, 24, 8, 24));

        TextField titleField  = field(isEdit ? existing.getTitle()              : "", "Book title *");
        TextField genreField  = field(isEdit ? existing.getGenre()              : "", "Genre");
        TextField isbnField   = field(isEdit ? existing.getIsbn()               : "", "ISBN");
        TextField yearField   = field(isEdit ? str(existing.getPubYear())       : "", "e.g. 2024");
        TextField copiesField = field(isEdit ? str(existing.getCopies())        : "1", "≥ 1");

        // Author dropdown
        List<Author> authors = authorDao.findAll();
        ComboBox<Author> authorBox = new ComboBox<>(
                FXCollections.observableArrayList(authors));
        authorBox.setConverter(new StringConverter<>() {
            @Override public String toString(Author a)   { return a == null ? "" : a.getFullName(); }
            @Override public Author fromString(String s) { return null; }
        });
        authorBox.setPromptText("Select author *");
        authorBox.setPrefWidth(220);
        if (isEdit) {
            authors.stream()
                    .filter(a -> a.getId() == existing.getAuthorId())
                    .findFirst()
                    .ifPresent(authorBox::setValue);
        }

        grid.add(label("Title *"),   0, 0); grid.add(titleField,  1, 0);
        grid.add(label("Author *"),  0, 1); grid.add(authorBox,   1, 1);
        grid.add(label("Genre"),     0, 2); grid.add(genreField,  1, 2);
        grid.add(label("ISBN"),      0, 3); grid.add(isbnField,   1, 3);
        grid.add(label("Pub. year"), 0, 4); grid.add(yearField,   1, 4);
        grid.add(label("Copies"),    0, 5); grid.add(copiesField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // ── Validation on OK ──────────────────────────────────────
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            String errors = validateBookForm(titleField, authorBox, yearField, copiesField);
            if (!errors.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation error", errors);
                ev.consume();         // keep dialog open
            }
        });

        // ── Result converter ──────────────────────────────────────
        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            Book b = new Book();
            b.setTitle(titleField.getText().trim());
            Author a = authorBox.getValue();
            b.setAuthorId(a.getId());
            b.setAuthorName(a.getFullName());
            b.setGenre(coalesce(genreField.getText()));
            b.setIsbn(coalesce(isbnField.getText()));
            b.setPubYear(parseIntOrZero(yearField.getText()));
            b.setCopies(Math.max(1, parseIntOrZero(copiesField.getText())));
            return b;
        });

        return dialog.showAndWait();
    }

    // ── Dialog validation ─────────────────────────────────────────
    private String validateBookForm(TextField titleField,
                                    ComboBox<Author> authorBox,
                                    TextField yearField,
                                    TextField copiesField) {
        StringBuilder sb = new StringBuilder();
        if (titleField.getText().isBlank())
            sb.append("• Title is required.\n");
        if (authorBox.getValue() == null)
            sb.append("• Author is required.\n");
        String yr = yearField.getText().trim();
        if (!yr.isEmpty() && !yr.matches("\\d{1,4}"))
            sb.append("• Year must be a 1–4 digit number.\n");
        String cp = copiesField.getText().trim();
        if (!cp.isEmpty() && !cp.matches("\\d+"))
            sb.append("• Copies must be a positive integer.\n");
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════════
    //  AUTHORS — table setup
    // ════════════════════════════════════════════════════════════
    private void setupAuthorsTable() {
        authorIdCol       .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        authorNameCol     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        authorCountryCol  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCountry()));
        authorBirthYearCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getBirthYear()).asObject());
        authorsTable.setItems(authorsData);
    }

    // ════════════════════════════════════════════════════════════
    //  AUTHORS — filters init & apply
    // ════════════════════════════════════════════════════════════
    private void initAuthorFilters() {
        authorCountryFilter.getItems().add("");
        authorCountryFilter.getItems().addAll(authorDao.findAllCountries());
        authorCountryFilter.setValue("");

        authorSearchField   .textProperty() .addListener((o, p, n) -> applyAuthorsFilter());
        authorCountryFilter .valueProperty().addListener((o, p, n) -> applyAuthorsFilter());
        authorYearFrom      .textProperty() .addListener((o, p, n) -> applyAuthorsFilter());
        authorYearTo        .textProperty() .addListener((o, p, n) -> applyAuthorsFilter());
    }

    private void applyAuthorsFilter() {
        String  text    = authorSearchField.getText();
        String  country = authorCountryFilter.getValue();
        Integer yFrom   = parseIntOrZero(authorYearFrom.getText());
        Integer yTo     = parseIntOrZero(authorYearTo.getText());

        authorsData.setAll(authorDao.search(text, country, yFrom, yTo));
        setStatus("Authors: " + authorsData.size() + " record(s) found.");
    }

    @FXML
    private void onClearAuthorsFilter() {
        authorSearchField.clear();
        authorCountryFilter.setValue("");
        authorYearFrom.clear();
        authorYearTo.clear();
    }
    // ════════════════════════════════════════════════════════════
    //  AUTHORS — CRUD
    // ════════════════════════════════════════════════════════════
    @FXML private void onAddAuthor()    {
        showAuthorDialog(null).ifPresent(author -> {
            authorDao.insert(author);
            applyAuthorsFilter();
            setStatus("Author \"" + author.getFullName() + "\" added.");
        });
    }

    @FXML private void onEditAuthor()   {
        Author selected = authorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No selection",
                    "Select an author to edit.");
            return;
        }
        showAuthorDialog(selected).ifPresent(updated -> {
            updated.setId(selected.getId());
            authorDao.update(updated);
            applyAuthorsFilter();
            setStatus("Author \"" + updated.getFullName() + "\" updated.");
        });
    }

    @FXML private void onDeleteAuthor() {
        Author selected = authorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No selection",
                    "Please select an author to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selected.getFullName() + "\"?\nThis action cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm deletion");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                authorDao.delete(selected.getId());
                applyAuthorsFilter();
                setStatus("Author deleted.");
            }
        });
    }

    // refactor
    private Optional<Author> showAuthorDialog(Author existing) {
        boolean isEdit = existing != null;

        Dialog<Author> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Author" : "Add Author");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        // ── Form ─────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(18, 24, 8, 24));

        TextField nameField  = field(isEdit ? existing.getFullName()        : "", "Full name *");
        TextField countryField = field(isEdit ? existing.getCountry()         : "", "Country");
        TextField yearField  = field(isEdit ? str(existing.getBirthYear())  : "", "e.g. 1950");

        grid.add(label("Full name *"),  0, 0); grid.add(nameField,  1, 0);
        grid.add(label("Country"),      0, 1); grid.add(countryField, 1, 1);
        grid.add(label("Birth year"),   0, 2); grid.add(yearField,  1, 2);

        dialog.getDialogPane().setContent(grid);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            String errors = validateAuthorForm(nameField, countryField, yearField);
            if (!errors.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation error", errors);
                ev.consume();         // keep dialog open
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            Author a = new Author();
            a.setFullName(nameField.getText().trim());
            a.setCountry(countryField.getText());
            a.setBirthYear(parseIntOrZero(yearField.getText()));
            return a;
        });

        return dialog.showAndWait();
    }

    private String validateAuthorForm(TextField nameField,
                                      TextField countryField,
                                      TextField yearField) {
        StringBuilder sb = new StringBuilder();
        if (nameField.getText().isBlank())
            sb.append("• Name is required.\n");
        String yr = yearField.getText().trim();
        if (!yr.isEmpty() && !yr.matches("\\d{1,4}"))
            sb.append("• Year must be a 1–4 digit number.\n");
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════════
    //  READERS — table setup
    // ════════════════════════════════════════════════════════════
    private void setupReadersTable() {
        readerIdCol     .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        readerNameCol   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        readerEmailCol  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        readerPhoneCol  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        readerRegDateCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getRegDate()));
        readersTable.setItems(readersData);
    }

    // ════════════════════════════════════════════════════════════
    //  READERS — filters init & apply
    // ════════════════════════════════════════════════════════════
    private void initReaderFilters() {
        readerSearchField.textProperty()   .addListener((o, p, n) -> applyReadersFilter());
        readerRegFrom    .valueProperty()  .addListener((o, p, n) -> applyReadersFilter());
        readerRegTo      .valueProperty()  .addListener((o, p, n) -> applyReadersFilter());
    }

    private void applyReadersFilter() {
        String    text  = readerSearchField.getText();
        LocalDate from  = readerRegFrom.getValue();
        LocalDate to    = readerRegTo.getValue();

        readersData.setAll(readerDao.search(text, from, to));
        setStatus("Readers: " + readersData.size() + " record(s) found.");
    }

    @FXML
    private void onClearReadersFilter() {
        readerSearchField.clear();
        readerRegFrom.setValue(null);
        readerRegTo.setValue(null);
    }

    // ════════════════════════════════════════════════════════════
    //  READERS — CRUD
    // ════════════════════════════════════════════════════════════
    @FXML private void onAddReader()    {
        showReaderDialog(null).ifPresent(reader -> {
            readerDao.insert(reader);
            applyReadersFilter();
            setStatus("Reader \"" + reader.getFullName() + "\" added.");
        });
    }

    @FXML private void onEditReader()   {
        Reader selected = readersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No selection",
                    "Please select a reader to edit.");
            return;
        }
        showReaderDialog(selected).ifPresent(updated -> {
            updated.setId(selected.getId());
            readerDao.update(updated);
            applyReadersFilter();
            setStatus("Reader \"" + updated.getFullName() + "\" updated.");
        });

    }
    @FXML private void onDeleteReader() {
        Reader selected = readersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No selection",
                    "Please select a reader to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selected.getFullName() + "\"?\nThis action cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm deletion");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                readerDao.delete(selected.getId());
                applyReadersFilter();
                setStatus("Reader deleted.");
            }
        });
    }

    // refactor
    private Optional<Reader> showReaderDialog(Reader existing) {
        boolean isEdit = existing != null;

        Dialog<Reader> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Reader" : "Add Reader");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        // ── Form ─────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(18, 24, 8, 24));

        TextField nameField  = field(isEdit ? existing.getFullName()              : "", "Full Name*");
        TextField emailField  = field(isEdit ? existing.getEmail()              : "", "email@example.com");
        TextField phoneField   = field(isEdit ? existing.getPhone()               : "", "+380...");
        DatePicker registerDate  = new DatePicker(isEdit ? existing.getRegDate() : LocalDate.now());
        registerDate.setPrefWidth(220);

        grid.add(label("Full Name *"),   0, 0); grid.add(nameField,  1, 0);
        grid.add(label("Email"),  0, 1); grid.add(emailField,   1, 1);
        grid.add(label("Phone"),     0, 2); grid.add(phoneField,  1, 2);
        grid.add(label("Reg. date"),      0, 3); grid.add(registerDate,   1, 3);

        dialog.getDialogPane().setContent(grid);

        // ── Validation on OK ──────────────────────────────────────
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            String errors = validateReaderForm(nameField, emailField, phoneField, registerDate);
            if (!errors.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation error", errors);
                ev.consume();         // keep dialog open
            }
        });

        // ── Result converter ──────────────────────────────────────
        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            Reader r = new Reader();
            r.setFullName(nameField.getText().trim());
            r.setEmail(coalesce(emailField.getText()));
            r.setPhone(coalesce(phoneField.getText()));
            r.setRegDate(registerDate.getValue());
            return r;
        });

        return dialog.showAndWait();
    }

    // ── Dialog validation ─────────────────────────────────────────
    private String validateReaderForm(TextField nameField,
                                    TextField emailField,
                                    TextField phoneField,
                                      DatePicker registerDate) {
        StringBuilder sb = new StringBuilder();
        if (nameField.getText().isBlank())
            sb.append("• Name is required.\n");
        if (registerDate.getValue() == null)
            sb.append("• Registration date is required.\n");
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════════
    //  LOANS — table setup
    // ════════════════════════════════════════════════════════════
    private void setupLoansTable() {
        loanIdCol    .setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        loanBookCol  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBookTitle()));
        loanReaderCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReaderName()));
        loanDateCol  .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getLoanDate()));
        loanDueCol   .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDueDate()));
        loanStatusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        // colour-code status
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
    //  LOANS — filters init & apply
    // ════════════════════════════════════════════════════════════
    private void initLoanFilters() {
        loanStatusFilter.getItems().addAll("", "active", "returned", "overdue");
        loanStatusFilter.setValue("");

        loanSearchField .textProperty() .addListener((o, p, n) -> applyLoansFilter());
        loanStatusFilter.valueProperty().addListener((o, p, n) -> applyLoansFilter());
        loanDateFrom    .valueProperty().addListener((o, p, n) -> applyLoansFilter());
        loanDateTo      .valueProperty().addListener((o, p, n) -> applyLoansFilter());
    }

    private void applyLoansFilter() {
        String    text   = loanSearchField.getText();
        String    status = loanStatusFilter.getValue();
        LocalDate from   = loanDateFrom.getValue();
        LocalDate to     = loanDateTo.getValue();

        loansData.setAll(loanDao.search(text, status, from, to));
        setStatus("Loans: " + loansData.size() + " record(s) found.");
    }

    @FXML
    private void onClearLoansFilter() {
        loanSearchField.clear();
        loanStatusFilter.setValue("");
        loanDateFrom.setValue(null);
        loanDateTo.setValue(null);
    }

    // ════════════════════════════════════════════════════════════
    //  LOANS — CRUD
    // ════════════════════════════════════════════════════════════
    @FXML private void onAddLoan()    {
        showLoanDialog(null).ifPresent(loan -> {
            loanDao.insert(loan);
            applyLoansFilter();
            setStatus("Loan added.");
        });
    }
    @FXML private void onEditLoan()   {
        Loan selected = loansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No selection",
                    "Please select a loan to edit.");
            return;
        }
        showLoanDialog(selected).ifPresent(updated -> {
            updated.setId(selected.getId());
            loanDao.update(updated);
            applyLoansFilter();
            setStatus("Book updated.");
        });
    }
    @FXML private void onDeleteLoan() {
        Loan selected = loansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No selection",
                    "Please select a loan to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selected.getId() + "\"?\nThis action cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm deletion");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                loanDao.delete(selected.getId());
                applyLoansFilter();
                setStatus("Loan deleted.");
            }
        });
    }

    private Optional<Loan> showLoanDialog(Loan existing) {
        boolean isEdit = existing != null;

        Dialog<Loan> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Loan" : "Add Loan");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        // ── Form ─────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(18, 24, 8, 24));

        List<Book> books = bookDao.findAll();
        ComboBox<Book> bookBox = new ComboBox<>(FXCollections.observableArrayList(books));
        bookBox.setConverter(new StringConverter<>() {
                                 @Override public String toString(Book b)   { return b == null ? "" : b.getTitle(); }
                                 @Override public Book fromString(String s) { return null; }
                             }
        );
        bookBox.setPromptText("Select book *");
        bookBox.setPrefWidth(220);
        if (isEdit) {
            books.stream()
                    .filter(b -> b.getId() == existing.getBookId())
                    .findFirst()
                    .ifPresent(bookBox::setValue);
        }

        List <Reader> readers = readerDao.findAll();
        ComboBox<Reader> readerBox = new ComboBox<>(FXCollections.observableArrayList(readers));
        readerBox.setConverter(new StringConverter<>() {
            @Override public String toString(Reader reader)   { return reader == null ? "" : reader.getFullName(); }
            @Override public Reader fromString(String s) { return null; }
        });
        readerBox.setPromptText("Select reader *");
        readerBox.setPrefWidth(220);
        if (isEdit) {
            readers.stream()
                    .filter(r -> r.getId() == existing.getReaderId())
                    .findFirst()
                    .ifPresent(readerBox::setValue);
        }

        DatePicker loanDate = new DatePicker(isEdit ? existing.getLoanDate():LocalDate.now());
        DatePicker dueDate = new DatePicker(isEdit ? existing.getDueDate(): LocalDate.now().plusDays(21));
        loanDate.setPrefWidth(220);
        dueDate.setPrefWidth(220);

        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("active", "returned", "overdue"));
        statusBox.setValue(isEdit ? existing.getStatus() : "active");
        statusBox.setPrefWidth(220);

        grid.add(label("Book *"),   0, 0); grid.add(bookBox,  1, 0);
        grid.add(label("Reader *"),  0, 1); grid.add(readerBox,   1, 1);
        grid.add(label("Loan date"),     0, 2); grid.add(loanDate,  1, 2);
        grid.add(label("Due date"),      0, 3); grid.add(dueDate,   1, 3);
        grid.add(label("Status"),      0, 4); grid.add(statusBox,   1, 4);
        dialog.getDialogPane().setContent(grid);

        // ── Validation on OK ──────────────────────────────────────
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            String errors = validateLoanForm(bookBox, readerBox, loanDate, dueDate);
            if (!errors.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation error", errors);
                ev.consume();         // keep dialog open
            }
        });

        // ── Result converter ──────────────────────────────────────
        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            Book b = bookBox.getValue();
            Reader r = readerBox.getValue();
            Loan l = new Loan();
            l.setBookId(b.getId());
            l.setBookTitle(b.getTitle());
            l.setReaderId(r.getId());
            l.setReaderName(r.getFullName());
            l.setLoanDate(loanDate.getValue());
            l.setDueDate(dueDate.getValue());
            l.setStatus(statusBox.getValue());

            return l;
        });

        return dialog.showAndWait();
    }

    // ── Dialog validation ─────────────────────────────────────────
    private String validateLoanForm(ComboBox bookBox,
                                      ComboBox readerBox,
                                      DatePicker loanDate,
                                      DatePicker dueDate) {
        StringBuilder sb = new StringBuilder();
        if (bookBox.getValue()==null)
            sb.append("• Book is required.\n");
        if (readerBox.getValue()==null)
            sb.append("• Reader is required.\n");
        if (dueDate.getValue() == null)
            sb.append("• Due date is required.\n");
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════════
    //  Utilities
    // ════════════════════════════════════════════════════════════
    private void setStatus(String msg) { statusLabel.setText(msg); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private static TextField field(String value, String prompt) {
        TextField tf = new TextField(value);
        tf.setPromptText(prompt);
        tf.setPrefWidth(220);
        return tf;
    }

    private static Label label(String text) {
        Label l = new Label(text);
        l.setMinWidth(80);
        return l;
    }

    private static String str(int v)          { return v == 0 ? "" : String.valueOf(v); }
    private static int    parseIntOrZero(String s) {
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
    }
    private static String coalesce(String s)  { return (s == null || s.isBlank()) ? null : s.trim(); }
}