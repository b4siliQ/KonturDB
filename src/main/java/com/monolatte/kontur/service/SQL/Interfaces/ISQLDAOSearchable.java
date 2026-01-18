package com.monolatte.kontur.service.SQL.Interfaces;

import com.monolatte.kontur.model.Notes.BaseNote;

import java.util.List;

public interface ISQLDAOSearchable<T extends BaseNote> extends ISQLDAO<T> {
    List<T> search(String columnDescription, String searchTerm);
}
