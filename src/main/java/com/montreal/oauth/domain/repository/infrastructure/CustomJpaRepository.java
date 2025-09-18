package com.montreal.oauth.domain.repository.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Collection;

@NoRepositoryBean
public interface CustomJpaRepository<T, ID> extends JpaRepository<T, ID> {

	void detach(T entity);

	void refresh(T t);

	void refresh(Collection<T> s);

	void flush();

}