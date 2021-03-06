package com.springernature.sndeals.repository;

import com.springernature.sndeals.domain.User;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.annotation.EntityGraph;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.TransactionalAdvice;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

/**
 * Micronaut Data JPA repository for the {@link User} entity.
 */
@Repository
@TransactionalAdvice
public interface UserRepository extends JpaRepository<User, Long> {



    public Optional<User> findOneByActivationKey(String activationKey);

    public List<User> findAllByActivatedFalseAndCreatedDateBefore(Instant dateTime);

    public Optional<User> findOneByResetKey(String resetKey);


    public Optional<User> findOneByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "authorities")
    public Optional<User> findOneById(Long id);

    @EntityGraph(attributePaths = "authorities")
    public Optional<User> findOneByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    public Optional<User> findOneByEmail(String email);

    public Page<User> findAllByLoginNot(String login, Pageable pageable);

    public void update(@Id Long id, Instant createdDate);
}
