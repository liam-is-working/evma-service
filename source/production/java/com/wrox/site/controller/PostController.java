package com.wrox.site.controller;

import com.wrox.config.annotation.RestEndpoint;
import com.wrox.site.entities.Event;
import com.wrox.site.entities.Post;
import com.wrox.site.entities.UserPrincipal;
import com.wrox.site.services.EventService;
import com.wrox.site.services.FirebaseService;
import com.wrox.site.services.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutionException;

@RestEndpoint
public class PostController {
    @Inject
    PostService postService;
    @Inject
    EventService eventService;
    @Inject
    FirebaseService firebaseService;

    @RequestMapping(value = "posts/{eventId}", method = RequestMethod.GET)
    public ResponseEntity<PageEntity<Post>> fetchEventPosts(@PathVariable long eventId,
                                                           @PageableDefault(page = 0,size = 5)Pageable p){
        Page<Post> postPage = postService.getEventPosts(eventId, p);
        return new ResponseEntity<>(new PageEntity<>(postPage), HttpStatus.OK);
    }

    @RequestMapping(value = "posts", method = RequestMethod.POST)
    public ResponseEntity<Post> createEventPost(@RequestBody PostForm postForm,
                                                @AuthenticationPrincipal UserPrincipal principal) throws ExecutionException, InterruptedException {
        Event event = eventService.getEventDetail(postForm.eventId);
        if(principal==null || principal.isEnabled() == false|| event==null || event.getUserProfileId()!= principal.getId()){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        Post newPost = new Post();
        newPost.setCreatedDate(postForm.createdDate);

        if(postForm.createdDate==null){
            ZoneId hcmZoneId = ZoneId.of("Asia/Ho_Chi_Minh");
            newPost.setCreatedDate(LocalDateTime.now(hcmZoneId).toInstant(ZoneOffset.UTC));
        }
        newPost.setContent(postForm.content);
        newPost.setEventId(postForm.eventId);
        newPost = postService.save(newPost);
        firebaseService.notify(event.getId(), FirebaseService.NotificationTrigger.ADD_NEW_POST, FirebaseService.Issuer.EVENT,null);
        return new ResponseEntity<>(newPost, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "posts/{postId}", method = RequestMethod.PUT)
    public ResponseEntity<Post> editEventPost(@RequestBody PostForm postForm,
                                                @PathVariable(value = "postId") long postId,
                                              @AuthenticationPrincipal UserPrincipal principal){
        Post editedPost = postService.getPost(postId);
        Event event = eventService.getEventDetail(postForm.eventId);
        if(editedPost==null || event==null){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        if(principal==null || principal.isEnabled() == false||event.getUserProfileId() != principal.getId())
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);

        editedPost.setCreatedDate(postForm.createdDate);
        editedPost.setContent(postForm.content);
        return new ResponseEntity<>(postService.save(editedPost), HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "posts/{postId}", method = RequestMethod.DELETE)
    public ResponseEntity deleteEventPost(@AuthenticationPrincipal UserPrincipal principal,
                                @PathVariable long postId){
        Post post = postService.getPost(postId);

        if(post == null)
            return new ResponseEntity(HttpStatus.NOT_FOUND);

        if(principal == null || principal.isEnabled() == false||
        eventService.getEventDetail(post.getEventId()).getUserProfileId()!= principal.getId())
            return new ResponseEntity(HttpStatus.FORBIDDEN);

        postService.deletePost(postId);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    public static class PostForm implements Serializable{
        @Size(max = 2500, message = "Content < 2500")
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
