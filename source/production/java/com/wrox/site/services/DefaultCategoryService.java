package com.wrox.site.services;

import com.wrox.site.entities.Category;
import com.wrox.site.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultCategoryService implements CategoryService{
    @Inject
    CategoryRepository categories;
    @Override
    public Category getById(long id) {
        return categories.findOne(id);
    }

    @Override
    public List<Category> getAll() {
        Iterable<Category> result = categories.findAll();
        List<Category> list = new ArrayList<>();
        for(Category c : result){
            list.add(c);
        }
        return list;
    }
}
