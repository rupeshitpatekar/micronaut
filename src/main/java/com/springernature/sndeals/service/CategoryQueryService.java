package com.springernature.sndeals.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.transaction.annotation.ReadOnly;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import com.springernature.sndeals.domain.Category;
import com.springernature.sndeals.domain.*; // for static metamodels
import com.springernature.sndeals.repository.CategoryRepository;
import com.springernature.sndeals.service.dto.CategoryCriteria;
import com.springernature.sndeals.service.dto.CategoryDTO;
import com.springernature.sndeals.service.mapper.CategoryMapper;

@Singleton
@ReadOnly
@Transactional
public class CategoryQueryService{

    private final Logger log = LoggerFactory.getLogger(CategoryQueryService.class);

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final EntityManager entityManager;

    public CategoryQueryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper, EntityManager entityManager) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.entityManager = entityManager;
    }

    /**
     * Return a {@link List} of {@link CategoryDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @ReadOnly
    @Transactional
    public List<CategoryDTO> findByCriteria(CategoryCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        return categoryMapper.toDto(getCategoryByCriteria(criteria, null));
    }

    /**
     * Return a {@link Page} of {@link CategoryDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @ReadOnly
    @Transactional
    public Page<CategoryDTO> findByCriteria(CategoryCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        List<Category> categories = getCategoryByCriteria(criteria, page);
        return Page.of(categories.stream().map(categoryMapper::toDto).collect(Collectors.toList()), page, categories.size());
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @ReadOnly
    @Transactional
    public long countByCriteria(CategoryCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        return getCategoryByCriteria(criteria, null).size();
    }

    protected List<Category> getCategoryByCriteria(CategoryCriteria criteria, Pageable page) {
        StringBuilder specification = new StringBuilder("SELECT * FROM CATEGORY WHERE 1=1");
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification.append(" AND "+ Category_.ID +"=" + criteria.getId());
            }
            if (criteria.getInternalId() != null) {
                specification.append(" AND "+ Category_.internalId +"=" + criteria.getInternalId());
            }
            if (criteria.getDisplayName() != null) {
                specification.append(" AND "+ Category_.displayName +"=" + criteria.getDisplayName());
            }
            if(page != null){
                specification.append(" limit="+ page.getSize());
                specification.append(" offset="+ (page.getNumber() - 1) * page.getSize());
            }
        }
        return entityManager.createNativeQuery(specification.toString(), Category.class).getResultList();
    }
}
