package com.springernature.sndeals.repository;

import com.springernature.sndeals.domain.Post;


import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.transaction.annotation.TransactionalAdvice;


/**
 * Micronaut Data  repository for the Post entity.
 */
@SuppressWarnings("unused")
@Repository
@TransactionalAdvice
public interface PostRepository extends JpaRepository<Post, Long> {

}
