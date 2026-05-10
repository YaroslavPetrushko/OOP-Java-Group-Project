package com.library.dao;

import com.library.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookDao {
    List<Book>     findAll();
    Optional<Book> findById(int id);

    /** Динамічний пошук: text шукає в title + author_name, genre/year — фільтри. */
    List<Book>     search(String text, String genre, Integer yearFrom, Integer yearTo);

    /** Унікальні жанри для ComboBox-фільтра. */
    List<String>   findAllGenres();

    void insert(Book book);
    void update(Book book);
    void delete(int id);
}
