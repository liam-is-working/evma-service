package com.wrox.site.repositories;

import com.wrox.site.entities.Event;
import com.wrox.site.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends PagingAndSortingRepository<Post, Long> {
    public Page<Post> getPostByEventId(long eventId, Pageable p);
}
