package com.monolatte.kontur.model.SQL;

import com.monolatte.kontur.model.Notes.BaseNote;

import java.util.List;

public interface ISQLDAOSearchable<T extends BaseNote> extends ISQLDAO<T> {
    List<T> search(int searchType, String searchTerm);
}
