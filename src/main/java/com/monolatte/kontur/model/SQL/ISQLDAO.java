package com.monolatte.kontur.model.SQL;

import com.monolatte.kontur.model.Notes.BaseNote;

import java.util.List;

public interface ISQLDAO<T extends BaseNote> extends ISQLDAOBase {

    void addNote(T note);

    void deleteNote(long id);

    void updateNote(T note);

    List<T> getAllNotes();

}
