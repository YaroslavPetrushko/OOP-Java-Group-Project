package com.library.dao;

import com.library.model.Author;
import java.util.List;
import java.util.Optional;

public interface AuthorDao {
    List<Author>     findAll();
    Optional<Author> findById(int id);

    /** text шукає в full_name + country + birth_year; country/year — додаткові фільтри. */
    List<Author>  search(String text, String country, Integer yearFrom, Integer yearTo);

    /** Унікальні країни для ComboBox-фільтра. */
    List<String>  findAllCountries();

    void insert(Author author);
    void update(Author author);
    void delete(int id);
}
