package com.springernature.sndeals.repository;

import com.springernature.sndeals.domain.Authority;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.transaction.annotation.TransactionalAdvice;

/**
 * Micronaut Data repository for the {@link Authority} entity.
 */
@Repository
@TransactionalAdvice
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
