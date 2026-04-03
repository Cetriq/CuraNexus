package se.curanexus.encounter.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import se.curanexus.encounter.domain.Encounter;
import se.curanexus.encounter.domain.EncounterClass;
import se.curanexus.encounter.domain.EncounterStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Custom repository implementation using CriteriaBuilder.
 * Handles nullable enum parameters correctly with PostgreSQL.
 */
@Repository
public class EncounterRepositoryCustomImpl implements EncounterRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Encounter> searchEncounters(
            UUID patientId,
            EncounterStatus status,
            EncounterClass encounterClass,
            UUID responsibleUnitId,
            Instant fromDate,
            Instant toDate,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Main query for results
        CriteriaQuery<Encounter> query = cb.createQuery(Encounter.class);
        Root<Encounter> root = query.from(Encounter.class);

        List<Predicate> predicates = buildPredicates(cb, root, patientId, status,
                encounterClass, responsibleUnitId, fromDate, toDate);

        query.where(predicates.toArray(new Predicate[0]));

        // Apply sorting from pageable, default to plannedStartTime DESC
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : pageable.getSort()) {
                Path<?> path = root.get(sortOrder.getProperty());
                orders.add(sortOrder.isAscending() ? cb.asc(path) : cb.desc(path));
            }
            query.orderBy(orders);
        } else {
            query.orderBy(cb.desc(root.get("plannedStartTime")));
        }

        TypedQuery<Encounter> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Encounter> results = typedQuery.getResultList();

        // Count query for total
        long total = countEncounters(cb, patientId, status, encounterClass,
                responsibleUnitId, fromDate, toDate);

        return new PageImpl<>(results, pageable, total);
    }

    private long countEncounters(
            CriteriaBuilder cb,
            UUID patientId,
            EncounterStatus status,
            EncounterClass encounterClass,
            UUID responsibleUnitId,
            Instant fromDate,
            Instant toDate) {

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Encounter> countRoot = countQuery.from(Encounter.class);

        List<Predicate> predicates = buildPredicates(cb, countRoot, patientId, status,
                encounterClass, responsibleUnitId, fromDate, toDate);

        countQuery.select(cb.count(countRoot));
        countQuery.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<Encounter> root,
            UUID patientId,
            EncounterStatus status,
            EncounterClass encounterClass,
            UUID responsibleUnitId,
            Instant fromDate,
            Instant toDate) {

        List<Predicate> predicates = new ArrayList<>();

        if (patientId != null) {
            predicates.add(cb.equal(root.get("patientId"), patientId));
        }

        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        if (encounterClass != null) {
            predicates.add(cb.equal(root.get("encounterClass"), encounterClass));
        }

        if (responsibleUnitId != null) {
            predicates.add(cb.equal(root.get("responsibleUnitId"), responsibleUnitId));
        }

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("plannedStartTime"), fromDate));
        }

        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("plannedStartTime"), toDate));
        }

        return predicates;
    }
}
