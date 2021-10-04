package com.wrox.site.services;

import com.wrox.site.entities.Post;
import com.wrox.site.repositories.EventRepository;
import com.wrox.site.repositories.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class DefaultPostService implements PostService{
    @Inject
    PostRepository posts;

    @Override
    public Page<Post> getEventPosts(long eventId, Pageable p) {
        return posts.getPostByEventId(eventId, p);
    }

    @Override
    public Post save(Post post) {
        posts.save(post);
        if(post.getImageURL()==null)
            post.setImageURL("postImg_"+post.getId());
        return post;
    }

    @Override
    public Post getPost(long postId) {
        return posts.findOne(postId);
    }
}
