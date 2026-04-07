package com.library.model;

import java.time.LocalDate;

public class Loan {
    private int       id;
    private int       bookId;
    private String    bookTitle;    // JOIN-поле
    private int       readerId;
    private String    readerName;   // JOIN-поле
    private LocalDate loanDate;
    private LocalDate dueDate;
    private String    status;

    public Loan() {}

    public Loan(int id, int bookId, String bookTitle, int readerId,
                String readerName, LocalDate loanDate,
                LocalDate dueDate, String status) {
        this.id = id; this.bookId = bookId; this.bookTitle = bookTitle;
        this.readerId = readerId; this.readerName = readerName;
        this.loanDate = loanDate; this.dueDate = dueDate; this.status = status;
    }

    public int       getId()         { return id; }
    public int       getBookId()     { return bookId; }
    public String    getBookTitle()  { return bookTitle; }
    public int       getReaderId()   { return readerId; }
    public String    getReaderName() { return readerName; }
    public LocalDate getLoanDate()   { return loanDate; }
    public LocalDate getDueDate()    { return dueDate; }
    public String    getStatus()     { return status; }

    public void setId(int id)                { this.id = id; }
    public void setBookId(int bookId)        { this.bookId = bookId; }
    public void setBookTitle(String t)       { this.bookTitle = t; }
    public void setReaderId(int readerId)    { this.readerId = readerId; }
    public void setReaderName(String n)      { this.readerName = n; }
    public void setLoanDate(LocalDate d)     { this.loanDate = d; }
    public void setDueDate(LocalDate d)      { this.dueDate = d; }
    public void setStatus(String status)     { this.status = status; }
}