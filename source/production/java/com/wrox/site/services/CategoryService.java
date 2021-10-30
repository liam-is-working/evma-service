package com.wrox.site.services;

import com.wrox.site.entities.Category;

import java.util.List;
import java.util.Set;

public interface CategoryService {
     public Category getById(long id);
     public Set<Category> getAll();
     public Set<Category> getByIds(Iterable<Long> ids, boolean enable);
     public Category getByName (String catName);
     public Set<Category> getAvailable();
     public Category save(Category c);
}
