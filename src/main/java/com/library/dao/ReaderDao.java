package com.library.dao;

import com.library.model.Reader;
import java.util.List;
import java.util.Optional;

public interface ReaderDao {
    List<Reader>     findAll();
    List<Reader>     findByName(String name);
    Optional<Reader> findById(int id);
    void             insert(Reader reader);
    void             update(Reader reader);
    void             delete(int id);
}