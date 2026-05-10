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
        // listeners trigger applyBooksFilter automatically
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

    private Optional<Book> showBookDialog(Book existing) {
        boolean edit = existing != null;
        Dialog<Book> dlg = dialog(edit ? "Edit Book" : "Add Book");

        GridPane g = grid();
        TextField titleF  = field(edit ? existing.getTitle()        : "", "Book title *");
        TextField genreF  = field(edit ? existing.getGenre()        : "", "Genre");
        TextField isbnF   = field(edit ? existing.getIsbn()         : "", "ISBN");
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
        Integer yearFrom   = parseIntOrZero(authorYearFrom.getText());
        Integer yearTo     = parseIntOrZero(authorYearTo.getText());

        authorsData.setAll(authorDao.search(text, country, yearFrom, yearTo));
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
    //  Utilities
    // ════════════════════════════════════════════════════════════

    // ── Shared UI helpers ───────────────────────────────────────────
    private <T> Dialog<T> dialog(String title) {
        Dialog<T> dlg = new Dialog<>();
        dlg.setTitle(title);
        dlg.setHeaderText(null);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        return dlg;
    }

    private GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10);
        g.setPadding(new Insets(18, 24, 8, 24));
        return g;
    }

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

    private ComboBox<Author> authorComboBox() {
        ComboBox<Author> box = new ComboBox<>(
                FXCollections.observableArrayList(authorDao.findAll()));
        box.setConverter(strConv(a -> a == null ? "" : a.getFullName()));
        box.setPromptText("Select author *");
        box.setPrefWidth(220);
        return box;
    }

    /** Компактний StringConverter через лямбду (тільки toString). */
    private <T> StringConverter<T> strConv(java.util.function.Function<T, String> fn) {
        return new StringConverter<>() {
            @Override public String toString(T v)       { return fn.apply(v); }
            @Override public T fromString(String s)     { return null; }
        };
    }

    private void setStatus(String msg) { statusLabel.setText(msg); }

    private void warn(String msg) { showAlert(Alert.AlertType.WARNING, "No selection", msg); }

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirm"); a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ── Field factories ───────────────────────────────────────────
    private static TextField field(String value, String prompt) {
        TextField tf = new TextField(value == null ? "" : value);
        tf.setPromptText(prompt);
        tf.setPrefWidth(220);
        return tf;
    }

    private static Label label(String text) {
        Label l = new Label(text);
        l.setMinWidth(90);
        return l;
    }

    // ── Value helpers ─────────────────────────────────────────────
    private static String  str(int v)         { return v == 0 ? "" : String.valueOf(v); }
    private static int     toInt(String s)    { try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; } }
    private static Integer parseIntOrZero(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }
    private static String coalesce(String s)  { return (s == null || s.isBlank()) ? null : s.trim(); }

    // ── Validators ────────────────────────────────────────────────
    private static boolean validYear(String s) { return s.isBlank() || s.trim().matches("\\d{1,4}"); }
    private static boolean validInt (String s) { return s.isBlank() || s.trim().matches("\\d+"); }
}