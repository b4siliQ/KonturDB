package com.monolatte.kontur.model.SQL;

import java.util.List;

public interface ISQLDAOSearchable<T> extends ISQLDAO<T> {
    List<T> search(int searchType, String searchTerm);
}
