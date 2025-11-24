package com.monolatte.kontur.model.SQLManager;

import java.util.List;

public interface ISQLManagerSearchable<T> extends ISQLManager<T> {
    List<T> search(int searchType, String searchTerm);
}
