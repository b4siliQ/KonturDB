package com.monolatte.kontur.model.SQLManager;

import java.util.List;

public interface ISQLManagerSearchable<T> {
    List<T> searcher(int searchType, String searchTerm);
}
