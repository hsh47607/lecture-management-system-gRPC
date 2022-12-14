package repository;

import config.HibernateFactory;
import exception.DatabaseException;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class Repository<T, ID> {
    private final HibernateFactory hibernateFactory = HibernateFactory.getInstance();
    private final EntityManager em = hibernateFactory.entityManager();
    private final Class<T> domainClass;
    public Repository(Class<T> domain) {
        this.domainClass = domain;
    }

    public List<T> findAll() {
        String query = "SELECT a FROM " + domainClass.getName() + " a";

        Session session = em.unwrap(Session.class);

        List<T> valueList = session.createQuery(query, domainClass).getResultList();

        valueList.forEach(v -> {
            if(!em.contains(v)) {
                em.persist(v);
            }
        });

        return valueList;
    }

    public Optional<T> findById(ID id) {
        T value = em.find(domainClass, id);

        if(value == null) {
            Session session = em.unwrap(Session.class);

            value = session.load(domainClass, (Serializable) id);

            em.persist(value);
        }

        return Optional.ofNullable(value);
    }

    public Optional<T> findByStringCondition(String fieldName, String condition) {
        String query = String.format(
                "SELECT a FROM %s a WHERE a.%s = :condition",
                domainClass.getName(), fieldName
        );
        Session session = em.unwrap(Session.class);

        Optional<T> value = session.createQuery(query, domainClass)
                .setParameter("condition", condition)
                .getResultList().stream().findAny();

        value.ifPresent(v -> {
            if(!em.contains(v)) {
                em.persist(v);
            }
        });

        return value;
    }

    public <S extends T> S save(S entity) {
        em.getTransaction().begin();
        try {

            em.merge(entity);
            em.flush();

            em.getTransaction().commit();

        } catch (RuntimeException e) {
            em.getTransaction().rollback();
        }
        return entity;
    }

    public void delete(T entity) throws DatabaseException {
        if(entity == null) {
            throw new DatabaseException.NotExistException("cannot delete this entity doesn't exist.");
        }

        em.getTransaction().begin();

        try {

            ID id = (ID) em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
            T existing = em.find(domainClass, id);
            if(existing == null) {
                throw new DatabaseException.NotExistException("cannot delete this entity doesn't exist.");
            }

            em.remove(em.contains(entity) ? entity : em.merge(entity));
            em.flush();

            em.getTransaction().commit();

        } catch (RuntimeException e) {
            em.getTransaction().rollback();
            throw new DatabaseException("occurred internal database error.");
        }
    }

    public void deleteById(ID id) {
        T entity = em.find(domainClass, id);

        entity = entity == null ? findById(id).orElse(null) : entity;

        delete(entity);
    }

    public void deleteByStringCondition(String fieldName, String condition) {
        T entity = findByStringCondition(fieldName, condition).orElse(null);

        delete(entity);
    }


}
