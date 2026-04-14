package com.library.controller;

import com.library.dao.BookDao;
import com.library.dao.impl.BookDaoImpl;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Reader;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

/**
 * Controller for MainView.fxml.
 *
 * Step 5 — Books CRUD
 */
public class MainController {

    // ── DAOs ─────────────────────────────────────────────────────
    private final BookDao   bookDao   = new BookDaoImpl();

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
}
