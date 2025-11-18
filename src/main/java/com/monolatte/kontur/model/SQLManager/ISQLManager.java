package com.monolatte.kontur.model.SQLManager;

import java.util.List;

public interface ISQLManager<T> {

    void createTable();

    void dropTable();

    void addNote(T note);

    void deleteNote(int id);

    void updateNote(T note);

    List<T> getAllNotes();

}
