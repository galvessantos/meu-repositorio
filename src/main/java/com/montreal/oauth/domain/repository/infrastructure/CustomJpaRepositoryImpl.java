package com.montreal.oauth.domain.repository.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.util.Collection;

public class CustomJpaRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> implements CustomJpaRepository<T, ID> {

    private EntityManager manager;

    public CustomJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);

        this.manager = entityManager;
    }

    @Override
    @Transactional
    public void detach(T entity) {
        this.manager.detach(entity);
    }

    @Override
    @Transactional
    public void refresh(T t) {
        manager.refresh(t);
    }

    @Override
    @Transactional
    public void refresh(Collection<T> s) {
        for (T t: s){
            manager.refresh(t);
        }
    }

    @Override
    @Transactional
    public void flush(){
        manager.flush();
    }

}
