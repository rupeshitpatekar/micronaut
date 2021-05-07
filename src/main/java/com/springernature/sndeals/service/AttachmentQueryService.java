package com.springernature.sndeals.service;

import com.springernature.sndeals.domain.Attachment;
import com.springernature.sndeals.domain.Attachment_;
import com.springernature.sndeals.repository.AttachmentRepository;
import com.springernature.sndeals.service.dto.AttachmentCriteria;
import com.springernature.sndeals.service.dto.AttachmentDTO;
import com.springernature.sndeals.service.mapper.AttachmentMapper;
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
public class AttachmentQueryService extends QueryService<Attachment> {

    private final Logger log = LoggerFactory.getLogger(AttachmentQueryService.class);

    private final AttachmentRepository attachmentRepository;

    private final AttachmentMapper attachmentMapper;

    private final EntityManager entityManager;

    public AttachmentQueryService(AttachmentRepository attachmentRepository, AttachmentMapper attachmentMapper, EntityManager entityManager) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentMapper = attachmentMapper;
        this.entityManager = entityManager;
    }

    /**
     * Return a {@link List} of {@link AttachmentDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @ReadOnly
    @Transactional
    public List<AttachmentDTO> findByCriteria(AttachmentCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        return attachmentMapper.toDto(getAttachmentByCriteria(criteria, null));
    }

    /**
     * Return a {@link Page} of {@link AttachmentDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @ReadOnly
    @Transactional
    public Page<AttachmentDTO> findByCriteria(AttachmentCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        List<Attachment> attachments = getAttachmentByCriteria(criteria, null);
        return Page.of(attachments.stream().map(attachmentMapper::toDto).collect(Collectors.toList()), page, attachments.size());
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @ReadOnly
    @Transactional
    public long countByCriteria(AttachmentCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        return getAttachmentByCriteria(criteria, null).size();
    }

    protected List<Attachment> getAttachmentByCriteria(AttachmentCriteria criteria, Pageable page) {
        StringBuilder specification = new StringBuilder("SELECT * FROM ATTACHMENT ");
        if (criteria != null) {
            if (criteria.getPostId() != null) {
                specification.append(" LEFT JOIN POST ON ATTACHMENT.post_id = POST.id ");
                specification.append(" AND ATTACHMENT.post_id ="+ criteria.getPostId());
            }else{
                specification.append(" WHERE 1=1");
            }

            if (criteria.getId() != null) {
                specification.append(" AND "+ Attachment_.id +"=" + criteria.getId());
            }
            if (criteria.getFileName() != null) {
                specification.append(" AND "+ Attachment_.fileName +"=" + criteria.getFileName());
            }
            if(page != null){
                specification.append(" limit="+ page.getSize());
                specification.append(" offset="+ (page.getNumber() - 1) * page.getSize());
            }
        }
        return entityManager.createNativeQuery(specification.toString(), Attachment.class).getResultList();
    }
}
