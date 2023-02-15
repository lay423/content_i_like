package com.content_i_like.service.saveoptions;

import java.util.List;
import java.util.Set;

public interface DBSaveOption<T> {

  List<T> saveNewRowsAllUnique(Set<T> entities);
}
