package com.springernature.sndeals.web.rest;


import com.springernature.sndeals.domain.Category;
import com.springernature.sndeals.repository.CategoryRepository;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.SynchronousTransactionManager;
import io.micronaut.transaction.TransactionOperations;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import java.sql.Connection;
import java.util.List;

import com.springernature.sndeals.service.dto.CategoryDTO;
import com.springernature.sndeals.service.mapper.CategoryMapper;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Integration tests for the {@Link CategoryResource} REST controller.
 */
@MicronautTest(transactional = false)
@Property(name = "micronaut.security.enabled", value = "false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CategoryResourceIT {

    private static final String DEFAULT_INTERNAL_ID = "AAAAAAAAAA";
    private static final String UPDATED_INTERNAL_ID = "BBBBBBBBBB";

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DISPLAY_NAME = "BBBBBBBBBB";

    @Inject
    private CategoryMapper categoryMapper;
    @Inject
    private CategoryRepository categoryRepository;

    @Inject
    private EntityManager em;

    @Inject
    SynchronousTransactionManager<Connection> transactionManager;

    @Inject @Client("/")
    RxHttpClient client;

    private Category category;

    @BeforeEach
    public void initTest() {
        category = createEntity(transactionManager, em);
    }

    @AfterEach
    public void cleanUpTest() {
        deleteAll(transactionManager, em);
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Category createEntity(TransactionOperations<Connection> transactionManager, EntityManager em) {
        Category category = new Category()
            .internalId(DEFAULT_INTERNAL_ID)
            .displayName(DEFAULT_DISPLAY_NAME);
        return category;
    }

    /**
     * Delete all category entities.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static void deleteAll(TransactionOperations<Connection> transactionManager, EntityManager em) {
        TestUtil.removeAll(transactionManager, em, Category.class);
    }


    @Test
    public void createCategory() throws Exception {
        int databaseSizeBeforeCreate = categoryRepository.findAll().size();

        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // Create the Category
        HttpResponse<CategoryDTO> response = client.exchange(HttpRequest.POST("/api/categories", categoryDTO), CategoryDTO.class).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.CREATED.getCode());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeCreate + 1);
        Category testCategory = categoryList.get(categoryList.size() - 1);

        assertThat(testCategory.getInternalId()).isEqualTo(DEFAULT_INTERNAL_ID);
        assertThat(testCategory.getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
    }

    @Test
    public void createCategoryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = categoryRepository.findAll().size();

        // Create the Category with an existing ID
        category.setId(1L);
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // An entity with an existing ID cannot be created, so this API call must fail
        @SuppressWarnings("unchecked")
        HttpResponse<CategoryDTO> response = client.exchange(HttpRequest.POST("/api/categories", categoryDTO), CategoryDTO.class)
            .onErrorReturn(t -> (HttpResponse<CategoryDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void checkInternalIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = categoryRepository.findAll().size();
        // set the field null
        category.setInternalId(null);

        // Create the Category, which fails.
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        @SuppressWarnings("unchecked")
        HttpResponse<CategoryDTO> response = client.exchange(HttpRequest.POST("/api/categories", categoryDTO), CategoryDTO.class)
            .onErrorReturn(t -> (HttpResponse<CategoryDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkDisplayNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = categoryRepository.findAll().size();
        // set the field null
        category.setDisplayName(null);

        // Create the Category, which fails.
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        @SuppressWarnings("unchecked")
        HttpResponse<CategoryDTO> response = client.exchange(HttpRequest.POST("/api/categories", categoryDTO), CategoryDTO.class)
            .onErrorReturn(t -> (HttpResponse<CategoryDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeTest);
    }

    //TODO Check below test
    //@Test
    public void getAllCategories() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get the categoryList w/ all the categories
        List<CategoryDTO> categories = client.retrieve(HttpRequest.GET("/api/categories?eagerload=true"), Argument.listOf(CategoryDTO.class)).blockingFirst();
        CategoryDTO testCategory = categories.get(0);


        assertThat(testCategory.getInternalId()).isEqualTo(DEFAULT_INTERNAL_ID);
        assertThat(testCategory.getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
    }

    @Test
    public void getCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get the category
        CategoryDTO testCategory = client.retrieve(HttpRequest.GET("/api/categories/" + category.getId()), CategoryDTO.class).blockingFirst();


        assertThat(testCategory.getInternalId()).isEqualTo(DEFAULT_INTERNAL_ID);
        assertThat(testCategory.getDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
    }

    @Test
    public void getNonExistingCategory() throws Exception {
        // Get the category
        @SuppressWarnings("unchecked")
        HttpResponse<CategoryDTO> response = client.exchange(HttpRequest.GET("/api/categories/"+ Long.MAX_VALUE), CategoryDTO.class)
            .onErrorReturn(t -> (HttpResponse<CategoryDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    public void updateCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();

        // Update the category
        Category updatedCategory = categoryRepository.findById(category.getId()).get();

        updatedCategory
            .internalId(UPDATED_INTERNAL_ID)
            .displayName(UPDATED_DISPLAY_NAME);
        CategoryDTO updatedCategoryDTO = categoryMapper.toDto(updatedCategory);

        @SuppressWarnings("unchecked")
        HttpResponse<CategoryDTO> response = client.exchange(HttpRequest.PUT("/api/categories", updatedCategoryDTO), CategoryDTO.class)
            .onErrorReturn(t -> (HttpResponse<CategoryDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.OK.getCode());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
        Category testCategory = categoryList.get(categoryList.size() - 1);

        assertThat(testCategory.getInternalId()).isEqualTo(UPDATED_INTERNAL_ID);
        assertThat(testCategory.getDisplayName()).isEqualTo(UPDATED_DISPLAY_NAME);
    }

    @Test
    public void updateNonExistingCategory() throws Exception {
        int databaseSizeBeforeUpdate = categoryRepository.findAll().size();

        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.toDto(category);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        @SuppressWarnings("unchecked")
        HttpResponse<CategoryDTO> response = client.exchange(HttpRequest.PUT("/api/categories", categoryDTO), CategoryDTO.class)
            .onErrorReturn(t -> (HttpResponse<CategoryDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        // Validate the Category in the database
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteCategory() throws Exception {
        // Initialize the database with one entity
        categoryRepository.saveAndFlush(category);

        int databaseSizeBeforeDelete = categoryRepository.findAll().size();

        // Delete the category
        @SuppressWarnings("unchecked")
        HttpResponse<CategoryDTO> response = client.exchange(HttpRequest.DELETE("/api/categories/"+ category.getId()), CategoryDTO.class)
            .onErrorReturn(t -> (HttpResponse<CategoryDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.NO_CONTENT.getCode());

            // Validate the database is now empty
        List<Category> categoryList = categoryRepository.findAll();
        assertThat(categoryList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Category.class);
        Category category1 = new Category();
        category1.setId(1L);
        Category category2 = new Category();
        category2.setId(category1.getId());
        assertThat(category1).isEqualTo(category2);
        category2.setId(2L);
        assertThat(category1).isNotEqualTo(category2);
        category1.setId(null);
        assertThat(category1).isNotEqualTo(category2);
    }
}
