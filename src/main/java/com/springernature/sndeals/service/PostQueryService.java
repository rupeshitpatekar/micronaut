package com.springernature.sndeals.service;

import com.springernature.sndeals.domain.Post;
import com.springernature.sndeals.domain.Post_;
import com.springernature.sndeals.repository.PostRepository;
import com.springernature.sndeals.service.dto.PostCriteria;
import com.springernature.sndeals.service.dto.PostDTO;
import com.springernature.sndeals.service.mapper.PostMapper;
import io.github.jhipster.service.QueryService;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.ReadOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@ReadOnly
@Transactional
public class PostQueryService extends QueryService<Post> {

    private final Logger log = LoggerFactory.getLogger(PostQueryService.class);

    private final PostRepository postRepository;

    private final PostMapper postMapper;

    private final EntityManager entityManager;

    public PostQueryService(PostRepository postRepository, PostMapper postMapper, EntityManager entityManager) {
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.entityManager = entityManager;
    }

    /**
     * Return a {@link List} of {@link PostDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @ReadOnly
    @Transactional
    public List<PostDTO> findByCriteria(PostCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        return postMapper.toDto(getPostByCriteria(criteria, null));
    }

    /**
     * Return a {@link Page} of {@link PostDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @ReadOnly
    @Transactional
    public Page<PostDTO> findByCriteria(PostCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        List<Post> posts = getPostByCriteria(criteria, null);
        return Page.of(posts.stream().map(postMapper::toDto).collect(Collectors.toList()), page, posts.size());
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @ReadOnly
    @Transactional
    public long countByCriteria(PostCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        return getPostByCriteria(criteria, null).size();
    }

    protected List<Post> getPostByCriteria(PostCriteria criteria, Pageable page) {
        StringBuilder specification = new StringBuilder("SELECT * FROM POST ");
        if (criteria != null) {
            if (criteria.getCategoryId() != null) {
                specification.append(" LEFT JOIN CATEGORY WHERE POST.category_id = CATEGORY.ID");
                specification.append(" WHERE POST.category_id = "+ criteria.getCategoryId());
            }else {
                specification.append(" WHERE 1=1 ");
            }

            if (criteria.getId() != null) {
                specification.append(" AND "+ Post_.id +"=" + criteria.getId());
            }
            if (criteria.getTitle() != null) {
                specification.append(" AND "+ Post_.title +"=" + criteria.getTitle());
            }
            if (criteria.getDescription() != null) {
                specification.append(" AND "+ Post_.description +"=" + criteria.getDescription());
            }
            if (criteria.getLocation() != null) {
                specification.append(" AND "+ Post_.location +"=" + criteria.getLocation());
            }
            if (criteria.getStatus() != null) {
                specification.append(" AND "+ Post_.status +"=" + criteria.getStatus());
            }
            if(page != null){
                specification.append(" limit="+ page.getSize());
                specification.append(" offset="+ (page.getNumber() - 1) * page.getSize());
            }

        }
        return entityManager.createNativeQuery(specification.toString(), Post.class).getResultList();
    }
}
