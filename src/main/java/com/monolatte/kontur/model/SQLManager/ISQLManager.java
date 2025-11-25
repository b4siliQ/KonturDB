package com.monolatte.kontur.model.SQLManager;

import java.util.List;

public interface ISQLManager<T> extends ISQLManagerBase {

    void addNote(T note);

    void deleteNote(int id);

    void updateNote(T note);

    List<T> getAllNotes();

}
