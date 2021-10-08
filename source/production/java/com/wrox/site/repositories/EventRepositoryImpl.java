package com.wrox.site.repositories;

import com.wrox.site.Criterion;
import com.wrox.site.SearchCriteria;
import com.wrox.site.entities.Category;
import com.wrox.site.entities.Event;
import com.wrox.site.entities.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

public class EventRepositoryImpl implements CustomEventRepository{
    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Page<Event> searchEvent(SearchCriteria criteria, Pageable p) {
        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
        Root<Event> countRoot = countCriteria.from(Event.class);
        long total = this.entityManager.createQuery(
                countCriteria.select(builder.count(countRoot))
                        .where(toPredicates(criteria, countRoot, builder))
        ).getSingleResult();

        CriteriaQuery<Event> pageCriteria = builder.createQuery(Event.class);
        Root<Event> pageRoot = pageCriteria.from(Event.class);
        List<Event> list = this.entityManager.createQuery(
                        pageCriteria.select(pageRoot)
                                .where(toPredicates(criteria, pageRoot, builder))
                                .orderBy(toOrders(p.getSort(), pageRoot, builder))
                ).setFirstResult(p.getOffset())
                .setMaxResults(p.getPageSize())
                .getResultList();

        return new PageImpl<>(new ArrayList<>(list), p, total);
    }

    @Override
    public Page<Event> searchEvent(String title, Set<Category> categorySet, Set<String> nameSet,
                                   Set<String> tagSet, Instant startDate, Instant endDate, Pageable p,
                                   EventStatus publishedStatus) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Event> cq= cb.createQuery(Event.class);
        Root<Event> root = cq.from(Event.class);

        List<Predicate> allPredicates = new ArrayList<>();

        //get tags
        Expression<Collection<String>> tags = root.get("tags");
        //get categories
        Expression<Collection<Category>> categories = root.get("categories");
        //get organizer names
        Expression<Collection<String>> organizerNames = root.get("organizerNames");

        if(categorySet!=null){
            Set<Predicate> categoryPredicates = new HashSet<>();
            for (Category cat : categorySet){
                categoryPredicates.add(cb.isMember(cat, categories));
            }
            if(categoryPredicates.size()!=0)
                allPredicates.add(cb.or(toPredicates(categoryPredicates)));
        }

        if(tagSet!=null){
            Set<Predicate> tagPredicates = new HashSet<>();
            for (String tag : tagSet){
                tagPredicates.add(cb.isMember(tag, tags));
            }
            if(tagPredicates.size()!=0)
                allPredicates.add(cb.or(toPredicates(tagPredicates)));
        }

        if(nameSet!=null){
            Set<Predicate> organizerNamePredicates = new HashSet<>();
            for (String name : nameSet){
                organizerNamePredicates.add(cb.isMember(name, organizerNames));
            }
            if(organizerNamePredicates.size()!=0)
                allPredicates.add(cb.or(toPredicates(organizerNamePredicates)));
        }

        if(startDate!=null || endDate!=null){
            if(startDate != null && endDate != null){
                allPredicates.add(cb.between(root.get("startDate"),
                        startDate.truncatedTo(ChronoUnit.HOURS),
                        endDate.truncatedTo(ChronoUnit.HOURS)));
            }else if(startDate == null){
                allPredicates.add(cb.between(root.get("endDate"),
                        endDate.truncatedTo(ChronoUnit.HOURS),
                        endDate.plus(1,ChronoUnit.DAYS)));
            }else {
                allPredicates.add(cb.between(root.get("startDate"),
                        startDate.truncatedTo(ChronoUnit.HOURS),
                        startDate.truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.DAYS)));
            }
        }

        if(title!=null)
            allPredicates.add(cb.like(cb.upper(root.get("title")), "%"+title.toUpperCase()+"%"));

        //hardcoded with status
        allPredicates.add(cb.equal(root.get("status"), publishedStatus));

        List<Event> list = this.entityManager.createQuery(
                        cq.select(root)
                                .where(toPredicates(allPredicates))
                                .orderBy(toOrders(p.getSort(), root, cb))
                ).setFirstResult(p.getOffset())
                .setMaxResults(p.getPageSize())
                .getResultList();

        return new PageImpl<Event>(new ArrayList<>(list), p, 0);

    }

    private static Predicate[] toPredicates(Collection<Predicate> predicates){
        Predicate[] returnVal = new Predicate[predicates.size()];
        int i = 0;
        for(Predicate p : predicates){
            returnVal[i++] = p;
        }
        return returnVal;
    }

    private static Predicate[] toPredicates(SearchCriteria criteria, Root<?> root,
                                            CriteriaBuilder builder)
    {
        Predicate[] predicates = new Predicate[criteria.size()];
        int i = 0;
        for(Criterion c : criteria)
            predicates[i++] = c.getOperator().toPredicate(c, root, builder);
        return predicates;
    }
}
