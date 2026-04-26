package com.library.dao;

import com.library.model.Author;
import java.util.List;
import java.util.Optional;

public interface AuthorDao {
    List<Author>     findAll();
    List<Author>     findByName(String name);
    Optional<Author> findById(int id);
    void             insert(Author author);
    void             update(Author author);
    void             delete(int id);
}