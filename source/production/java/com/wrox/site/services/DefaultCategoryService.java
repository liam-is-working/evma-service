package com.wrox.site.services;

import com.wrox.site.entities.Category;
import com.wrox.site.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

@Service
public class DefaultCategoryService implements CategoryService{
    @Inject
    CategoryRepository categories;
    @Override
    public Category getById(long id) {
        return categories.findOne(id);
    }

    @Override
    public Set<Category> getAll() {
        Iterable<Category> result = categories.findAll();
        Set<Category> list = new HashSet<>();
        for(Category c : result){
            list.add(c);
        }
        return list;
    }

    @Override
    public Set<Category> getByIds(Iterable<Long> ids, boolean enable) {
        return categories.getByIdInAndStatusIs(ids, enable);
    }


    @Override
    public Category getByName(String catName) {
        return categories.getByName(catName);
    }

    @Override
    public Set<Category> getAvailable() {
        return categories.getByStatus(true);
    }

    @Override
    public Category save(Category c) {
        return categories.save(c);
    }
}
