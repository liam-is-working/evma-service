package com.wrox.site.controller;

import com.wrox.config.annotation.RestEndpoint;
import com.wrox.exception.ResourceNotFoundException;
import com.wrox.site.entities.Event;
import com.wrox.site.entities.Post;
import com.wrox.site.entities.UserPrincipal;
import com.wrox.site.services.EventService;
import com.wrox.site.services.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.Serializable;
import java.time.Instant;

@RestEndpoint
public class PostController {
    @Inject
    PostService postService;
    @Inject
    EventService eventService;

    @RequestMapping(value = "posts/{eventId}", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<Post>> fetchEventPost(@PathVariable long eventId,
                                                           @PageableDefault(size = 5)Pageable p){
        Page<Post> postPage = postService.getEventPosts(eventId, p);
        return new ResponseEntity<>(new PageEntity<>(postPage), HttpStatus.OK);
    }

    @RequestMapping(value = "posts", method = RequestMethod.POST)
    public ResponseEntity<Post> createEventPost(@RequestBody PostForm postForm,
                                                @AuthenticationPrincipal UserPrincipal principal){
        Event event = eventService.getEventDetail(postForm.eventId);
        if(principal==null || event==null || event.getUserProfileId()!= principal.getId()){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        Post newPost = new Post();
        newPost.setCreatedDate(postForm.createdDate);
        newPost.setContent(postForm.content);
        newPost.setEventId(postForm.eventId);
        return new ResponseEntity<>(postService.save(newPost), HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "posts/{postId}", method = RequestMethod.PUT)
    public ResponseEntity<Post> editEventPost(@RequestBody PostForm postForm,
                                                @PathVariable(value = "postId") long postId,
                                              @AuthenticationPrincipal UserPrincipal principal){
        Post editedPost = postService.getPost(postId);
        Event event = eventService.getEventDetail(editedPost.getEventId());
        if(principal==null || event==null || event.getUserProfileId() != principal.getId()){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        editedPost.setCreatedDate(postForm.createdDate);
        editedPost.setContent(postForm.content);
        editedPost.setEventId(postForm.eventId);
        return new ResponseEntity<>(postService.save(editedPost), HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "post/{postId}", method = RequestMethod.DELETE)
    public ResponseEntity deleteEventPost(@AuthenticationPrincipal UserPrincipal principal,
                                @PathVariable long postId){
        Post post = postService.getPost(postId);

        if(post == null)
            throw new ResourceNotFoundException();

        if(principal == null ||
        eventService.getEventDetail(post.getEventId()).getUserProfileId()!= principal.getId())
            return new ResponseEntity(HttpStatus.FORBIDDEN);

        postService.deletePost(postId);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    public static class PostForm implements Serializable{
        String content;
        Instant createdDate;
        long eventId;

        public PostForm() {
        }

        public long getEventId() {
            return eventId;
        }

        public void setEventId(long eventId) {
            this.eventId = eventId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Instant getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(Instant createdDate) {
            this.createdDate = createdDate;
        }

    }
}
