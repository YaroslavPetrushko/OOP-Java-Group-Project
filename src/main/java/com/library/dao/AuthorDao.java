package com.library.dao;

import com.library.model.Author;
import java.util.List;

public interface AuthorDao {
    List<Author> findAll();
}