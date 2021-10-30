package com.wrox.site.repositories;

import com.wrox.site.entities.Category;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long> {
    public Category getByName(String name);
    public Set<Category> getByStatus(boolean status);
    public Set<Category> getByIdInAndStatusIs(Iterable<Long> ids, boolean status);
}
