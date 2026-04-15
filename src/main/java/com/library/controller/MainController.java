package com.library.controller;

import com.library.dao.AuthorDao;
import com.library.dao.BookDao;
import com.library.dao.impl.AuthorDaoImpl;
import com.library.dao.impl.BookDaoImpl;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Reader;
import javafx.beans.property.SimpleIntegerProperty;
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
 * Step 5 — Books CRUD
 */
public class MainController {

    // ── DAOs ─────────────────────────────────────────────────────
    private final BookDao   bookDao   = new BookDaoImpl();
    private final AuthorDao authorDao = new AuthorDaoImpl();

    // ── Status bar ─────────────────────────────────────────────
    @FXML private Label statusLabel;

    // ── Books ───────────────────────────────────────────────────
    @FXML private TableView<Book>            booksTable;
    @FXML private TableColumn<Book, Integer> bookIdCol;
    @FXML private TableColumn<Book, String>  bookTitleCol;
    @FXML private TableColumn<Book, String>  bookAuthorCol;
    @FXML private TableColumn<Book, String>  bookGenreCol;
    @FXML private TableColumn<Book, String>  bookIsbnCol;
    @FXML private TableColumn<Book, Integer> bookYearCol;
    @FXML private TableColumn<Book, Integer> bookCopiesCol;
    @FXML private TextField                  bookSearchField;

    private final ObservableList<Book> booksData = FXCollections.observableArrayList();

    // ── Authors ─────────────────────────────────────────────────
    @FXML private TableView<Author>            authorsTable;
    @FXML private TableColumn<Author, Integer> authorIdCol;
    @FXML private TableColumn<Author, String>  authorNameCol;
    @FXML private TableColumn<Author, String>  authorCountryCol;
    @FXML private TableColumn<Author, Integer> authorBirthYearCol;
    @FXML private TextField                    authorSearchField;

    // ── Readers ─────────────────────────────────────────────────
    @FXML private TableView<Reader>             readersTable;
    @FXML private TableColumn<Reader, Integer>  readerIdCol;
    @FXML private TableColumn<Reader, String>   readerNameCol;
    @FXML private TableColumn<Reader, String>   readerEmailCol;
    @FXML private TableColumn<Reader, String>   readerPhoneCol;
    @FXML private TableColumn<Reader, LocalDate> readerRegDateCol;
    @FXML private TextField                      readerSearchField;

    // ── Loans ───────────────────────────────────────────────────
    @FXML private TableView<Loan>             loansTable;
    @FXML private TableColumn<Loan, Integer>  loanIdCol;
    @FXML private TableColumn<Loan, String>   loanBookCol;
    @FXML private TableColumn<Loan, String>   loanReaderCol;
    @FXML private TableColumn<Loan, LocalDate> loanDateCol;
    @FXML private TableColumn<Loan, LocalDate> loanDueCol;
    @FXML private TableColumn<Loan, String>   loanStatusCol;
    @FXML private TextField                   loanSearchField;

    // ── Init ────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupBooksTable();
        loadBooks();
        setupBookSearch();
        statusLabel.setText("Connected to database  |  Step 5: Books CRUD");
        // Step 5: wire cellValueFactory + load data from DAO
    }

    // Books - Setup
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

    private void loadBooks() {
        booksData.setAll(bookDao.findAll());
    }

    private void setupBookSearch() {
        bookSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                booksData.setAll(bookDao.findAll());
            } else {
                booksData.setAll(bookDao.findByTitle(newVal.trim()));
            }
        });
    }

    // ── Book actions ────────────────────────────────────────────
    @FXML private void onAddBook()    {
//        showDialog.ifPresent(book -> {
//            bookDao.insert(book);
//            loadBooks();
//            setStatus("Book added.");
//        });
    }
    @FXML private void onEditBook()   {
//        Book selected = booksTable.getSelectedItem();
//        showBookDialog(selected).ifPresent(updated -> {
//            bookDao.update(updated);
//            loadBooks();
//            setStatus("Book updated.");
//        });
    }
    @FXML private void onDeleteBook() {
//        Book selected = booksTable.getSelectedItem();
//
//        Alert "Delete?"
//
//            if (yes) {
//                bookDao.delete(selected.getId());
//                loadBooks();
//                setStatus("Book deleted.");
//            }
    }

    // ════════════════════════════════════════════════════════════
    //  Books — Add / Edit dialog
    // ════════════════════════════════════════════════════════════
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
        TextField yearField   = field(isEdit ? str(existing.getPubYear())        : "", "e.g. 2024");
        TextField copiesField = field(isEdit ? str(existing.getCopies())         : "1", "≥ 1");

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
            sb.append("Title is required.");
        // if field isBlank / isNull
        // show message
        // add compare for year, copies

        return sb.toString();
    }

    // ── Author actions ──────────────────────────────────────────
    @FXML private void onAddAuthor()    { /* Step 6 */ }
    @FXML private void onEditAuthor()   { /* Step 6 */ }
    @FXML private void onDeleteAuthor() { /* Step 6 */ }

    // ── Reader actions ──────────────────────────────────────────
    @FXML private void onAddReader()    { /* Step 6 */ }
    @FXML private void onEditReader()   { /* Step 6 */ }
    @FXML private void onDeleteReader() { /* Step 6 */ }

    // ── Loan actions ────────────────────────────────────────────
    @FXML private void onAddLoan()    { /* Step 6 */ }
    @FXML private void onEditLoan()   { /* Step 6 */ }
    @FXML private void onDeleteLoan() { /* Step 6 */ }

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