package com.wrox.site.services;

import com.wrox.site.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    public Page<Post> getEventPosts(long eventId, Pageable p);
    public Post save(Post post);
    public Post getPost(long postId);
}