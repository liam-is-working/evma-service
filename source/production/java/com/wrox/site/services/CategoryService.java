package com.wrox.site.services;

import com.wrox.site.entities.Category;

import java.util.List;

public interface CategoryService {
     public Category getById(long id);
     public List<Category> getAll();
}
