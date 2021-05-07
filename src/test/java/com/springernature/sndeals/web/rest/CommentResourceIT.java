package com.springernature.sndeals.web.rest;


import com.springernature.sndeals.domain.Comment;
import com.springernature.sndeals.domain.Post;
import com.springernature.sndeals.repository.CommentRepository;

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

import com.springernature.sndeals.service.dto.CommentDTO;
import com.springernature.sndeals.service.mapper.CommentMapper;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Integration tests for the {@Link CommentResource} REST controller.
 */
@MicronautTest(transactional = false)
@Property(name = "micronaut.security.enabled", value = "false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentResourceIT {

    private static final String DEFAULT_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_COMMENT = "BBBBBBBBBB";

    @Inject
    private CommentMapper commentMapper;
    @Inject
    private CommentRepository commentRepository;

    @Inject
    private EntityManager em;

    @Inject
    SynchronousTransactionManager<Connection> transactionManager;

    @Inject @Client("/")
    RxHttpClient client;

    private Comment comment;

    @BeforeEach
    public void initTest() {
        comment = createEntity(transactionManager, em);
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
    public static Comment createEntity(TransactionOperations<Connection> transactionManager, EntityManager em) {
        Comment comment = new Comment()
            .comment(DEFAULT_COMMENT);
        // Add required entity
        Post post;
        if (TestUtil.findAll(transactionManager, em, Post.class).isEmpty()) {
            post = PostResourceIT.createEntity(transactionManager, em);
            transactionManager.executeWrite(status -> {
                em.persist(post);
                em.flush();
                return post;
            });
        } else {
            post = TestUtil.findAll(transactionManager, em, Post.class).get(0);
        }
        comment.setPost(post);
        return comment;
    }

    /**
     * Delete all comment entities.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static void deleteAll(TransactionOperations<Connection> transactionManager, EntityManager em) {
        TestUtil.removeAll(transactionManager, em, Comment.class);
        // Delete required entities
        PostResourceIT.deleteAll(transactionManager, em);
    }


    @Test
    public void createComment() throws Exception {
        int databaseSizeBeforeCreate = commentRepository.findAll().size();

        CommentDTO commentDTO = commentMapper.toDto(comment);

        // Create the Comment
        HttpResponse<CommentDTO> response = client.exchange(HttpRequest.POST("/api/comments", commentDTO), CommentDTO.class).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.CREATED.getCode());

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll();
        assertThat(commentList).hasSize(databaseSizeBeforeCreate + 1);
        Comment testComment = commentList.get(commentList.size() - 1);

        assertThat(testComment.getComment()).isEqualTo(DEFAULT_COMMENT);
    }

    @Test
    public void createCommentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = commentRepository.findAll().size();

        // Create the Comment with an existing ID
        comment.setId(1L);
        CommentDTO commentDTO = commentMapper.toDto(comment);

        // An entity with an existing ID cannot be created, so this API call must fail
        @SuppressWarnings("unchecked")
        HttpResponse<CommentDTO> response = client.exchange(HttpRequest.POST("/api/comments", commentDTO), CommentDTO.class)
            .onErrorReturn(t -> (HttpResponse<CommentDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll();
        assertThat(commentList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    public void checkCommentIsRequired() throws Exception {
        int databaseSizeBeforeTest = commentRepository.findAll().size();
        // set the field null
        comment.setComment(null);

        // Create the Comment, which fails.
        CommentDTO commentDTO = commentMapper.toDto(comment);

        @SuppressWarnings("unchecked")
        HttpResponse<CommentDTO> response = client.exchange(HttpRequest.POST("/api/comments", commentDTO), CommentDTO.class)
            .onErrorReturn(t -> (HttpResponse<CommentDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        List<Comment> commentList = commentRepository.findAll();
        assertThat(commentList).hasSize(databaseSizeBeforeTest);
    }

    //TODO Check below test
    //@Test
    public void getAllComments() throws Exception {
        // Initialize the database
        commentRepository.saveAndFlush(comment);

        // Get the commentList w/ all the comments
        List<CommentDTO> comments = client.retrieve(HttpRequest.GET("/api/comments?eagerload=true"), Argument.listOf(CommentDTO.class)).blockingFirst();
        CommentDTO testComment = comments.get(0);


        assertThat(testComment.getComment()).isEqualTo(DEFAULT_COMMENT);
    }

    @Test
    public void getComment() throws Exception {
        // Initialize the database
        commentRepository.saveAndFlush(comment);

        // Get the comment
        CommentDTO testComment = client.retrieve(HttpRequest.GET("/api/comments/" + comment.getId()), CommentDTO.class).blockingFirst();


        assertThat(testComment.getComment()).isEqualTo(DEFAULT_COMMENT);
    }

    @Test
    public void getNonExistingComment() throws Exception {
        // Get the comment
        @SuppressWarnings("unchecked")
        HttpResponse<CommentDTO> response = client.exchange(HttpRequest.GET("/api/comments/"+ Long.MAX_VALUE), CommentDTO.class)
            .onErrorReturn(t -> (HttpResponse<CommentDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.NOT_FOUND.getCode());
    }

    @Test
    public void updateComment() throws Exception {
        // Initialize the database
        commentRepository.saveAndFlush(comment);

        int databaseSizeBeforeUpdate = commentRepository.findAll().size();

        // Update the comment
        Comment updatedComment = commentRepository.findById(comment.getId()).get();

        updatedComment
            .comment(UPDATED_COMMENT);
        CommentDTO updatedCommentDTO = commentMapper.toDto(updatedComment);

        @SuppressWarnings("unchecked")
        HttpResponse<CommentDTO> response = client.exchange(HttpRequest.PUT("/api/comments", updatedCommentDTO), CommentDTO.class)
            .onErrorReturn(t -> (HttpResponse<CommentDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.OK.getCode());

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll();
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate);
        Comment testComment = commentList.get(commentList.size() - 1);

        assertThat(testComment.getComment()).isEqualTo(UPDATED_COMMENT);
    }

    @Test
    public void updateNonExistingComment() throws Exception {
        int databaseSizeBeforeUpdate = commentRepository.findAll().size();

        // Create the Comment
        CommentDTO commentDTO = commentMapper.toDto(comment);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        @SuppressWarnings("unchecked")
        HttpResponse<CommentDTO> response = client.exchange(HttpRequest.PUT("/api/comments", commentDTO), CommentDTO.class)
            .onErrorReturn(t -> (HttpResponse<CommentDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());

        // Validate the Comment in the database
        List<Comment> commentList = commentRepository.findAll();
        assertThat(commentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteComment() throws Exception {
        // Initialize the database with one entity
        commentRepository.saveAndFlush(comment);

        int databaseSizeBeforeDelete = commentRepository.findAll().size();

        // Delete the comment
        @SuppressWarnings("unchecked")
        HttpResponse<CommentDTO> response = client.exchange(HttpRequest.DELETE("/api/comments/"+ comment.getId()), CommentDTO.class)
            .onErrorReturn(t -> (HttpResponse<CommentDTO>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.NO_CONTENT.getCode());

            // Validate the database is now empty
        List<Comment> commentList = commentRepository.findAll();
        assertThat(commentList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Comment.class);
        Comment comment1 = new Comment();
        comment1.setId(1L);
        Comment comment2 = new Comment();
        comment2.setId(comment1.getId());
        assertThat(comment1).isEqualTo(comment2);
        comment2.setId(2L);
        assertThat(comment1).isNotEqualTo(comment2);
        comment1.setId(null);
        assertThat(comment1).isNotEqualTo(comment2);
    }
}
