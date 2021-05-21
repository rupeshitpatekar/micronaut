package com.springernature.sndeals.repository;

import com.springernature.sndeals.domain.Comment;


import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.transaction.annotation.TransactionalAdvice;


/**
 * Micronaut Data  repository for the Comment entity.
 */
@SuppressWarnings("unused")
@Repository
@TransactionalAdvice
public interface CommentRepository extends JpaRepository<Comment, Long>{

}
