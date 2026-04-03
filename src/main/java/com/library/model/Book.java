package com.library.model;

import java.util.Objects;

public class Book {
    private int id;
    private String title;
    private int authorId;
    private String authorName;   // для відображення в таблиці
    private String genre;
    private String isbn;
    private Integer pubYear;     // ← Змінено з int на Integer (nullable)
    private int copies;

    // Constructors
    public Book() {}

    public Book(int id, String title, int authorId, String authorName,
                String genre, String isbn, Integer pubYear, int copies) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.authorName = authorName;
        this.genre = genre;
        this.isbn = isbn;
        this.pubYear = pubYear;
        this.copies = copies;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getPubYear() { return pubYear; }           // ← Integer
    public void setPubYear(Integer pubYear) { this.pubYear = pubYear; }

    public int getCopies() { return copies; }
    public void setCopies(int copies) { this.copies = copies; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return id == book.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return title + " (" + authorName + ")";
    }
}