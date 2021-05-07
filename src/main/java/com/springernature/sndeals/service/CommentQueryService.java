package com.springernature.sndeals.service;

import com.springernature.sndeals.domain.Comment;
import com.springernature.sndeals.domain.Comment_;
import com.springernature.sndeals.repository.CommentRepository;
import com.springernature.sndeals.service.dto.CommentCriteria;
import com.springernature.sndeals.service.dto.CommentDTO;
import com.springernature.sndeals.service.mapper.CommentMapper;
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
public class CommentQueryService extends QueryService<Comment> {

    private final Logger log = LoggerFactory.getLogger(CommentQueryService.class);

    private final CommentRepository commentRepository;

    private final CommentMapper commentMapper;

    private final EntityManager entityManager;

    public CommentQueryService(CommentRepository commentRepository, CommentMapper commentMapper, EntityManager entityManager) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.entityManager = entityManager;
    }

    /**
     * Return a {@link List} of {@link CommentDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @ReadOnly
    @Transactional
    public List<CommentDTO> findByCriteria(CommentCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        return commentMapper.toDto(getCommentsByCriteria(criteria,null));
    }

    /**
     * Return a {@link Page} of {@link CommentDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @ReadOnly
    @Transactional
    public Page<CommentDTO> findByCriteria(CommentCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        List<Comment> comments = getCommentsByCriteria(criteria, null);
        return Page.of(comments.stream().map(commentMapper::toDto).collect(Collectors.toList()), page, comments.size());
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @ReadOnly
    @Transactional
    public long countByCriteria(CommentCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        return getCommentsByCriteria(criteria,null).size();
    }

    protected List<Comment> getCommentsByCriteria(CommentCriteria criteria, Pageable page) {
        StringBuilder specification = new StringBuilder("SELECT * FROM COMMENT ");
        if (criteria != null) {
            if (criteria.getPostId() != null) {
                specification.append("LEFT JOIN POST ON COMMENT.POST_ID = POST.ID ");
                specification.append(" WHERE COMMENT.POST_ID ="+ criteria.getPostId());
//                specification = specification.and(buildSpecification(criteria.getPostId(),
//                    root -> root.join(Comment_.post, JoinType.LEFT).get(Post_.id)));
            }else{
                specification.append("WHERE 1=1 ");
            }
            if (criteria.getId() != null) {
                specification.append(" AND "+ Comment_.ID +"=" + criteria.getId());
            }
            if (criteria.getComment() != null) {
                specification.append(" AND "+ Comment_.comment +"=" + criteria.getComment());
            }
            if(page != null){
                specification.append(" limit="+ page.getSize());
                specification.append(" offset="+ (page.getNumber() - 1) * page.getSize());
            }

        }
        return entityManager.createNativeQuery(specification.toString(), Comment.class).getResultList();
    }
}
