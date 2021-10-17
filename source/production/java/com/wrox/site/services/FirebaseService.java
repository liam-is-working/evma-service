package com.wrox.site.services;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.*;
import com.wrox.site.entities.Event;
import com.wrox.site.entities.UserProfile;
import com.wrox.site.repositories.EventRepository;
import com.wrox.site.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseService {

    @Inject
    EventRepository eventRepository;
    @Inject
    UserProfileRepository userProfileRepository;

    @PostConstruct()
    public void initialize() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream("C:\\Users\\ACER\\Desktop\\EVMA-FIx\\source\\production\\resources\\evma-fpt-firebase-adminsdk-nei30-a60a9f6e4b.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
    }

    public String followEvent(long userId, long targetId, FollowOperation operation) throws ExecutionException, InterruptedException {
        final Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("Users").document(String.valueOf(userId));

        //Limit number of follow events
//        if(operation == FollowOperation.FOLLOW_EVENT || operation == FollowOperation.FOLLOW_ORGANIZER){
//            List<Long> followedEvents = getFollow(userId, Issuer.EVENT);
//            List<Long> followedOrganizers = getFollow(userId, Issuer.ORGANIZER);
//            if(operation == FollowOperation.FOLLOW_EVENT && followedEvents!=null &&  followedEvents.size() >= 10)
//                return "Limit event";
//            if(operation == FollowOperation.FOLLOW_ORGANIZER && followedOrganizers!=null && followedOrganizers.size() >= 10)
//                return "Limit organizer";
//        }

        Map<String, Object> update = new Hashtable<>();
        switch (operation){
            case FOLLOW_EVENT:{
                update.put("followedEvents", FieldValue.arrayUnion(targetId));
                break;
            }
            case UNFOLLOW_EVENT:{
                update.put("followedEvents", FieldValue.arrayRemove(targetId));
                break;
            }
            case FOLLOW_ORGANIZER:{
                update.put("followedOrganizers", FieldValue.arrayUnion(targetId));
                break;
            }
            case UNFOLLOW_ORGANIZER:{
                update.put("followedOrganizers", FieldValue.arrayRemove(targetId));
                break;
            }
        }
        ApiFuture<WriteResult> future = docRef.set(update, SetOptions.merge());
        return future.get().getUpdateTime().toString();
    }

    public List<Long> getFollow(long userId, Issuer target) throws ExecutionException, InterruptedException {
        final Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("Users").document(String.valueOf(userId));

        ApiFuture<DocumentSnapshot> userDocFuture = docRef.get();
        DocumentSnapshot userDoc = userDocFuture.get();
        List<Long> followedEvents = (List<Long>) userDoc.get("followedEvents");
        List<Long> followedOrganizers = (List<Long>) userDoc.get("followedOrganizers");
        if(target == Issuer.EVENT)
            return followedEvents;
        if(target == Issuer.ORGANIZER)
            return followedOrganizers;
        return null;
    }
    public enum FollowOperation{
        UNFOLLOW_EVENT,
        UNFOLLOW_ORGANIZER,
        FOLLOW_EVENT,
        FOLLOW_ORGANIZER,

    }
    public enum NotificationTrigger{
        CHANGE_EVENT_TITLE(false),
        UPDATE_EVENT_DETAILS(false),
        ADD_NEW_POST(false),
        CANCEL_EVENT(false),
        DELETE_EVENT(false),
        ADD_EVENT(true),
        CHANGE_ORGANIZER_NAME(true),
        START_SOON(false);


        boolean organizerRelated;
        NotificationTrigger(boolean organizerRelated){
            this.organizerRelated = organizerRelated;
        }
    }
    public enum Issuer{
        ORGANIZER,
        EVENT
    }

    public String notify(long issuerId, NotificationTrigger trigger, Issuer issuer, String addition) throws ExecutionException, InterruptedException {
        final Firestore db = FirestoreClient.getFirestore();
        String role = null;
        if(issuer==Issuer.EVENT)
            role = "e";
        if(issuer==Issuer.ORGANIZER)
            role = "o";

        StringBuilder stringBuilder = new StringBuilder(String.valueOf(issuerId));
        String docName = stringBuilder.append("_").append(role).toString();
        stringBuilder.setLength(0);

        String collectionName = stringBuilder.
                append(LocalDate.now().getDayOfMonth()).
                append(".").
                append(LocalDate.now().getMonth().getValue()).toString();

        DocumentReference instantRef = null;
        DocumentReference storeNotiRef = null;
        instantRef = db.collection("InstantNotification").document(docName);
        storeNotiRef = db.collection(collectionName).document(docName);

        String message = this.buildNotificationMessage(trigger,issuerId,addition);

        Map<String, String>value = new Hashtable<>();
        value.put(Instant.now().toString(), message);

        instantRef.set(value);
        ApiFuture<WriteResult> result = storeNotiRef.set(value, SetOptions.merge());

        return result.get().getUpdateTime().toString();
    }

    private String buildNotificationMessage(NotificationTrigger trigger, long issuerId, String addition ){
        StringBuilder sb = new StringBuilder();
        if(trigger.organizerRelated){
            UserProfile org = userProfileRepository.findOne(issuerId);
            if(org==null)
                return null;
            String orgName = org.getName();
            switch (trigger){
                case ADD_EVENT:
                    return sb.append(orgName).append(" holds a new event").toString();
                case CHANGE_ORGANIZER_NAME:
                    return sb.append(addition).append(" changes its name to ").append(orgName).toString();
                default: return null;
            }
        }else{
            Event event = eventRepository.findOne(issuerId);
            if(event==null)
                return null;
            String eventTitle = event.getTitle();

            switch (trigger){
                case CHANGE_EVENT_TITLE:
                    return sb.append(addition).append(" changes its name to ").append(eventTitle).toString();
                case ADD_NEW_POST:
                    return sb.append(eventTitle).append(" added a post").toString();
                case UPDATE_EVENT_DETAILS:
                    return sb.append(eventTitle).append(" has been updated").toString();
                case CANCEL_EVENT:
                    return sb.append(eventTitle).append(" has been cancelled").toString();
                case DELETE_EVENT:
                    return sb.append(eventTitle).append(" has been deleted").toString();
                case START_SOON:{
                    int hour = event.getStartDate().atZone(ZoneOffset.UTC).getHour();
                    int minute = event.getStartDate().atZone(ZoneOffset.UTC).getMinute();
                    return sb.append(eventTitle).append(" will start soon at ")
                            .append(hour).append(":").append(minute).toString();
                }
                default: return null;
            }
        }
    }
}
