package com.springernature.sndeals.repository;

import com.springernature.sndeals.domain.Attachment;


import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.transaction.annotation.TransactionalAdvice;


/**
 * Micronaut Data  repository for the Attachment entity.
 */
@SuppressWarnings("unused")
@Repository
@TransactionalAdvice
public interface AttachmentRepository extends JpaRepository<Attachment, Long>{

}
