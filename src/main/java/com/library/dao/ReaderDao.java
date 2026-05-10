package com.library.dao;

import com.library.model.Reader;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReaderDao {
    List<Reader>     findAll();
    Optional<Reader> findById(int id);

    /** text шукає в full_name + email + phone; regFrom/regTo — діапазон дати реєстрації. */
    List<Reader> search(String text, LocalDate regFrom, LocalDate regTo);

    void insert(Reader reader);
    void update(Reader reader);
    void delete(int id);
}
