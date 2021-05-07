package com.springernature.sndeals.web.rest;


import com.springernature.sndeals.domain.Post;
import com.springernature.sndeals.domain.Category;
import com.springernature.sndeals.repository.PostRepository;

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

import com.springernature.sndeals.service.dto.PostDTO;
import com.springernature.sndeals.service.mapper.PostMapper;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Integration tests for the {@Link PostResource} REST controller.
 */
@MicronautTest(transactional = false)
@Property(name = "micronaut.security.enabled", value = "false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_LOCATION = "AAAAAAAAAA";
    private static final String UPDATED_LOCATION = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    @Inject
    private PostMapper postMapper;
    @Inject
    private PostRepository postRepository;

    @Inject
    private EntityManager em;

    @Inject
    SynchronousTransactionManager<Connection> transactionManager;

    @Inject @Client("/")
    RxHttpClient client;

    private Post post;

    @BeforeEach
    public void initTest() {
        post = createEntity(transactionManager, em);
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
    public static Post createEntity(TransactionOperations<Connection> transactionManager, EntityManager em) {
        Post post = new Post()
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION)
            .location(DEFAULT_LOCATION)
            .status(DEFAULT_STATUS);
        // Add required entity
        Category category;
        if (TestUtil.findAll(transactionManager, em, Category.class).isEmpty()) {
            category = CategoryResourceIT.createEntity(transactionManager, em);
            transactionManager.executeWrite(status -> {
                em.persist(category);
                em.flush();
                return category;
            });
        } else {
            category = TestUtil.findAll(transactionManager, em, Category.class).get(0);
        }
        post.setCategory(category);
        return post;
    }

    /**
     * Delete all post entities.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static void deleteAll(TransactionOperations<Connection> transactionManager, EntityManager em) {
        TestUtil.removeAll(transactionManager, em, Post.class);
        // Delete required entities
        CategoryResourceIT.deleteAll(transactionManager, em);
    }


    @Test
    public void createPost() throws Exception {
        int databaseSizeBeforeCreate = postRepository.findAll().size();

        PostDTO postDTO = postMapper.toDto(post);

        // Create the Post
        HttpResponse<PostDTO> response = client.exchange(HttpRequest.POST("/api/posts", postDTO), PostDTO.class).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.CREATED.getCode());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeCreate + 1);
        Post testPost = postList.get(postList.size() - 1);

        assertThat(testPost.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPost.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testPost.getLocation()).isEqualTo(DEFAULT_LOCATION);
        assertThat(testPost.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    public void createPostWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = postRepository.findAll().size();

        // Create the Post with an existing ID
        post.setId(1L);
        PostDTO postDTO = postMapper.toDto(post);

        // An entity with an existing ID cannot be created, so this API call must fail
        @SuppressWarnings("unchecked")
        HttpResponse<PostDTO> response = client.exchange(HttpRequest.POST("/api/posts", postDTO), PostDTO.class)
            .onErrorReturn(t -> (HttpResponse<PostDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = postRepository.findAll().size();
        // set the field null
        post.setTitle(null);

        // Create the Post, which fails.
        PostDTO postDTO = postMapper.toDto(post);

        @SuppressWarnings("unchecked")
        HttpResponse<PostDTO> response = client.exchange(HttpRequest.POST("/api/posts", postDTO), PostDTO.class)
            .onErrorReturn(t -> (HttpResponse<PostDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkLocationIsRequired() throws Exception {
        int databaseSizeBeforeTest = postRepository.findAll().size();
        // set the field null
        post.setLocation(null);

        // Create the Post, which fails.
        PostDTO postDTO = postMapper.toDto(post);

        @SuppressWarnings("unchecked")
        HttpResponse<PostDTO> response = client.exchange(HttpRequest.POST("/api/posts", postDTO), PostDTO.class)
            .onErrorReturn(t -> (HttpResponse<PostDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = postRepository.findAll().size();
        // set the field null
        post.setStatus(null);

        // Create the Post, which fails.
        PostDTO postDTO = postMapper.toDto(post);

        @SuppressWarnings("unchecked")
        HttpResponse<PostDTO> response = client.exchange(HttpRequest.POST("/api/posts", postDTO), PostDTO.class)
            .onErrorReturn(t -> (HttpResponse<PostDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeTest);
    }

    //TODO Check below test
    //@Test
    public void getAllPosts() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get the postList w/ all the posts
        List<PostDTO> posts = client.retrieve(HttpRequest.GET("/api/posts?eagerload=true"), Argument.listOf(PostDTO.class)).blockingFirst();
        PostDTO testPost = posts.get(0);


        assertThat(testPost.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPost.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testPost.getLocation()).isEqualTo(DEFAULT_LOCATION);
        assertThat(testPost.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    public void getPost() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get the post
        PostDTO testPost = client.retrieve(HttpRequest.GET("/api/posts/" + post.getId()), PostDTO.class).blockingFirst();


        assertThat(testPost.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPost.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testPost.getLocation()).isEqualTo(DEFAULT_LOCATION);
        assertThat(testPost.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    public void getNonExistingPost() throws Exception {
        // Get the post
        @SuppressWarnings("unchecked")
        HttpResponse<PostDTO> response = client.exchange(HttpRequest.GET("/api/posts/"+ Long.MAX_VALUE), PostDTO.class)
            .onErrorReturn(t -> (HttpResponse<PostDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    public void updatePost() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        int databaseSizeBeforeUpdate = postRepository.findAll().size();

        // Update the post
        Post updatedPost = postRepository.findById(post.getId()).get();

        updatedPost
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .location(UPDATED_LOCATION)
            .status(UPDATED_STATUS);
        PostDTO updatedPostDTO = postMapper.toDto(updatedPost);

        @SuppressWarnings("unchecked")
        HttpResponse<PostDTO> response = client.exchange(HttpRequest.PUT("/api/posts", updatedPostDTO), PostDTO.class)
            .onErrorReturn(t -> (HttpResponse<PostDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.OK.getCode());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
        Post testPost = postList.get(postList.size() - 1);

        assertThat(testPost.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPost.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testPost.getLocation()).isEqualTo(UPDATED_LOCATION);
        assertThat(testPost.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    public void updateNonExistingPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().size();

        // Create the Post
        PostDTO postDTO = postMapper.toDto(post);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        @SuppressWarnings("unchecked")
        HttpResponse<PostDTO> response = client.exchange(HttpRequest.PUT("/api/posts", postDTO), PostDTO.class)
            .onErrorReturn(t -> (HttpResponse<PostDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deletePost() throws Exception {
        // Initialize the database with one entity
        postRepository.saveAndFlush(post);

        int databaseSizeBeforeDelete = postRepository.findAll().size();

        // Delete the post
        @SuppressWarnings("unchecked")
        HttpResponse<PostDTO> response = client.exchange(HttpRequest.DELETE("/api/posts/"+ post.getId()), PostDTO.class)
            .onErrorReturn(t -> (HttpResponse<PostDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.NO_CONTENT.getCode());

            // Validate the database is now empty
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Post.class);
        Post post1 = new Post();
        post1.setId(1L);
        Post post2 = new Post();
        post2.setId(post1.getId());
        assertThat(post1).isEqualTo(post2);
        post2.setId(2L);
        assertThat(post1).isNotEqualTo(post2);
        post1.setId(null);
        assertThat(post1).isNotEqualTo(post2);
    }
}
