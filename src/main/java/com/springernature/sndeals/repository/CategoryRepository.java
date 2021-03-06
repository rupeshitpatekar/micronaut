package com.springernature.sndeals.repository;

import com.springernature.sndeals.domain.Category;


import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.transaction.annotation.TransactionalAdvice;


/**
 * Micronaut Data  repository for the Category entity.
 */
@SuppressWarnings("unused")
@Repository
@TransactionalAdvice
public interface CategoryRepository extends JpaRepository<Category, Long> {


}
