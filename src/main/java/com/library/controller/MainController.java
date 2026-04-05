package com.library.controller;

import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Reader;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

/**
 * Controller for MainView.fxml.
 *
 * Step 4 — UI shell: all @FXML fields declared, action stubs in place.
 * Step 5 — will add: cellValueFactory wiring, ObservableList, DAO calls.
 */
public class MainController {

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
        statusLabel.setText("Connected to database   |  Step 4: UI ready");
        // Step 5: wire cellValueFactory + load data from DAO
    }

    // ── Book actions ────────────────────────────────────────────
    @FXML private void onAddBook()    { /* Step 5 */ }
    @FXML private void onEditBook()   { /* Step 5 */ }
    @FXML private void onDeleteBook() { /* Step 5 */ }

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
