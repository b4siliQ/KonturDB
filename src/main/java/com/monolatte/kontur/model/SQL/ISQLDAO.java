package com.monolatte.kontur.model.SQL;

import java.util.List;

public interface ISQLDAO<T> extends ISQLDAOBase {

    void addNote(T note);

    void deleteNote(long id);

    void updateNote(T note);

    List<T> getAllNotes();

}
